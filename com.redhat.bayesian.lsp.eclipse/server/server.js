/* --------------------------------------------------------------------------------------------
 * Copyright (c) Pavel Odvody 2016
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
'use strict';
Object.defineProperty(exports, "__esModule", { value: true });
const path = require("path");
const fs = require("fs");
const vscode_languageserver_1 = require("vscode-languageserver");
const utils_1 = require("./utils");
const collector_1 = require("./collector");
const consumers_1 = require("./consumers");
const url = require('url');
const https = require('https');
const request = require('request');
const winston = require('winston');
winston.level = 'debug';
winston.add(winston.transports.File, { filename: 'bayesian.log' });
winston.remove(winston.transports.Console);
winston.info('Starting Bayesian');
/*
let log_file = fs.openSync('file_log.log', 'w');
let _LOG = (data) => {
    fs.writeFileSync('file_log.log', data + '\n');
}
*/
var EventStream;
(function (EventStream) {
    EventStream[EventStream["Invalid"] = 0] = "Invalid";
    EventStream[EventStream["Diagnostics"] = 1] = "Diagnostics";
    EventStream[EventStream["CodeLens"] = 2] = "CodeLens";
})(EventStream || (EventStream = {}));
;
let connection = null;
/* use stdio for transfer if applicable */
if (process.argv.indexOf('--stdio') == -1)
    connection = vscode_languageserver_1.createConnection(new vscode_languageserver_1.IPCMessageReader(process), new vscode_languageserver_1.IPCMessageWriter(process));
else
    connection = vscode_languageserver_1.createConnection();
let documents = new vscode_languageserver_1.TextDocuments();
documents.listen(connection);
let workspaceRoot;
connection.onInitialize((params) => {
    workspaceRoot = params.rootPath;
    return {
        capabilities: {
            textDocumentSync: documents.syncKind,
            codeActionProvider: true
        }
    };
});
;
;
;
class AnalysisFileHandler {
    constructor(matcher, stream, callback) {
        this.stream = stream;
        this.callback = callback;
        this.matcher = new RegExp(matcher);
    }
}
;
class AnalysisFiles {
    constructor() {
        this.handlers = [];
        this.file_data = new Map();
    }
    on(stream, matcher, cb) {
        this.handlers.push(new AnalysisFileHandler(matcher, stream, cb));
        return this;
    }
    run(stream, uri, file, contents) {
        for (let handler of this.handlers) {
            if (handler.stream == stream && handler.matcher.test(file)) {
                return handler.callback(uri, file, contents);
            }
        }
    }
}
;
;
class AnalysisLSPServer {
    constructor(connection, files) {
        this.connection = connection;
        this.files = files;
    }
    handle_file_event(uri, contents) {
        let path_name = url.parse(uri).pathname;
        let file_name = path.basename(path_name);
        this.files.file_data[uri] = contents;
        this.files.run(EventStream.Diagnostics, uri, file_name, contents);
    }
    handle_code_lens_event(uri) {
        let path_name = url.parse(uri).pathname;
        let file_name = path.basename(path_name);
        let lenses = [];
        let contents = this.files.file_data[uri];
        return this.files.run(EventStream.CodeLens, uri, file_name, contents);
    }
}
;
;
class Aggregator {
    constructor(items, callback) {
        this.callback = callback;
        this.mapping = new Map();
        for (let item of items) {
            this.mapping.set(item, false);
        }
    }
    is_ready() {
        let val = true;
        for (let m of this.mapping.entries()) {
            val = val && m[1];
        }
        return val;
    }
    aggregate(dep) {
        this.mapping.set(dep, true);
        if (this.is_ready()) {
            this.callback();
        }
    }
}
;
class AnalysisConfig {
    constructor() {
        // TODO: this needs to be configurable
        this.server_url = 'https://recommender.api.openshift.io/api/v1';
        this.api_token = process.env.RECOMMENDER_API_TOKEN || "token-not-available-in-lsp";
        this.forbidden_licenses = [];
        this.no_crypto = false;
        this.home_dir = process.env[(process.platform == 'win32') ? 'USERPROFILE' : 'HOME'];
    }
}
;
let config = new AnalysisConfig();
let files = new AnalysisFiles();
let server = new AnalysisLSPServer(connection, files);
let rc_file = path.join(config.home_dir, '.analysis_rc');
if (fs.existsSync(rc_file)) {
    let rc = JSON.parse(fs.readFileSync(rc_file, 'utf8'));
    if ('server' in rc) {
        config.server_url = `${rc.server}/api/v1`;
    }
}
let DiagnosticsEngines = [consumers_1.SecurityEngine];
// TODO: in-memory caching only, this needs to be more robust
let metadataCache = new Map();
let get_metadata = (ecosystem, name, version, cb) => {
    let cacheKey = ecosystem + " " + name + " " + version;
    let metadata = metadataCache[cacheKey];
    if (metadata != null) {
        winston.info('cache hit for ' + cacheKey);
        cb(metadata);
        return;
    }
    let part = [ecosystem, name, version].join('/');
    const options = url.parse(config.server_url);
    options['path'] += `/component-analyses/${part}/`;
    options['headers'] = { 'Authorization': 'Bearer ' + config.api_token };
    winston.debug('get ' + options['host'] + options['path']);
    https.get(options, function (res) {
        let body = '';
        res.on('data', function (chunk) { body += chunk; });
        res.on('end', function () {
            winston.info('status ' + this.statusCode);
            if (this.statusCode == 200 || this.statusCode == 202) {
                let response = JSON.parse(body);
                winston.debug('response ' + response);
                metadataCache[cacheKey] = response;
                cb(response);
            }
            else {
                cb(null);
            }
        });
    });
};
files.on(EventStream.Diagnostics, "^package\\.json$", (uri, name, contents) => {
    /* Convert from readable stream into string */
    let stream = utils_1.stream_from_string(contents);
    let collector = new collector_1.DependencyCollector(null);
    collector.collect(stream).then((deps) => {
        let diagnostics = [];
        /* Aggregate asynchronous requests and send the diagnostics at once */
        let aggregator = new Aggregator(deps, () => {
            connection.sendDiagnostics({ uri: uri, diagnostics: diagnostics });
        });
        for (let dependency of deps) {
            get_metadata('npm', dependency.name.value, dependency.version.value, (response) => {
                if (response != null) {
                    let pipeline = new consumers_1.DiagnosticsPipeline(DiagnosticsEngines, dependency, config, diagnostics);
                    pipeline.run(response);
                }
                aggregator.aggregate(dependency);
            });
        }
    });
});
files.on(EventStream.Diagnostics, "^pom\\.xml$", (uri, name, contents) => {
    /* Convert from readable stream into string */
    let stream = utils_1.stream_from_string(contents);
    let collector = new collector_1.PomXmlDependencyCollector();
    collector.collect(stream).then((deps) => {
        let diagnostics = [];
        /* Aggregate asynchronous requests and send the diagnostics at once */
        let aggregator = new Aggregator(deps, () => {
            connection.sendDiagnostics({ uri: uri, diagnostics: diagnostics });
        });
        for (let dependency of deps) {
            get_metadata('maven', dependency.name.value, dependency.version.value, (response) => {
                if (response != null) {
                    let pipeline = new consumers_1.DiagnosticsPipeline(DiagnosticsEngines, dependency, config, diagnostics);
                    pipeline.run(response);
                }
                aggregator.aggregate(dependency);
            });
        }
    });
});
let checkDelay;
connection.onDidSaveTextDocument((params) => {
    clearTimeout(checkDelay);
    server.handle_file_event(params.textDocument.uri, server.files.file_data[params.textDocument.uri]);
});
connection.onDidChangeTextDocument((params) => {
    /* Update internal state for code lenses */
    server.files.file_data[params.textDocument.uri] = params.contentChanges[0].text;
    clearTimeout(checkDelay);
    checkDelay = setTimeout(() => {
        server.handle_file_event(params.textDocument.uri, server.files.file_data[params.textDocument.uri]);
    }, 500);
});
connection.onDidOpenTextDocument((params) => {
    server.handle_file_event(params.textDocument.uri, params.textDocument.text);
});
connection.onCodeAction((params, token) => {
    clearTimeout(checkDelay);
    let commands = [];
    for (let diagnostic of params.context.diagnostics) {
        let command = consumers_1.codeActionsMap[diagnostic.message];
        if (command != null) {
            commands.push(command);
        }
    }
    return commands;
});
connection.onDidCloseTextDocument((params) => {
    clearTimeout(checkDelay);
});
connection.listen();
//# sourceMappingURL=server.js.map