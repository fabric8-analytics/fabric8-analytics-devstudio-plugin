/* --------------------------------------------------------------------------------------------
 * Copyright (c) Pavel Odvody 2016
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
'use strict';
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
const json_1 = require("./json");
const Xml2Object = require("xml2object");
/* By default the collector is going to process these dependency keys */
const DefaultClasses = ["dependencies", "devDependencies", "optionalDependencies"];
/* Dependency class that can be created from `IKeyValueEntry` */
class Dependency {
    constructor(dependency) {
        this.name = {
            value: dependency.key,
            position: dependency.key_position
        };
        this.version = {
            value: dependency.value.object,
            position: dependency.value_position
        };
    }
}
/* Process entries found in the JSON files and collect all dependency
 * related information */
class DependencyCollector {
    constructor(classes) {
        this.classes = classes;
        this.classes = classes || DefaultClasses;
    }
    collect(file) {
        return __awaiter(this, void 0, void 0, function* () {
            let parser = new json_1.StreamingParser(file);
            let dependencies = [];
            let tree = yield parser.parse();
            let top_level = tree.children[0];
            /* Iterate over all keys, select those in which we're interested as defined
            by `classes`, and map each item to a new `Dependency` object */
            for (const p of top_level.properties) {
                if (this.classes.indexOf(p.key) > -1) {
                    for (const dependency of p.value.object) {
                        dependencies.push(new Dependency(dependency));
                    }
                }
            }
            return dependencies;
        });
    }
}
exports.DependencyCollector = DependencyCollector;
class NaivePomXmlSaxParser {
    constructor(stream) {
        this.dependencies = [];
        this.isDependency = false;
        this.versionStartLine = 0;
        this.versionStartColumn = 0;
        this.stream = stream;
        this.parser = this.createParser();
    }
    createParser() {
        let parser = new Xml2Object(["dependency"], { strict: true, trackPosition: true });
        let deps = this.dependencies;
        let versionLine = this.versionStartLine;
        let versionColumn = this.versionStartColumn;
        parser.on("object", function (name, obj) {
            if (obj.hasOwnProperty("groupId") && obj.hasOwnProperty("artifactId") && obj.hasOwnProperty("version")) {
                let ga = `${obj["groupId"]}:${obj["artifactId"]}`;
                let entry = new json_1.KeyValueEntry(ga, { line: 0, column: 0 });
                entry.value = new json_1.Variant(json_1.ValueType.String, obj["version"]);
                entry.value_position = { line: versionLine, column: versionColumn };
                let dep = new Dependency(entry);
                deps.push(dep);
            }
        });
        parser.saxStream.on("opentag", function (node) {
            if (node.name == "dependency") {
                this.isDependency = true;
            }
            if (this.isDependency && node.name == "version") {
                versionLine = parser.saxStream._parser.line + 1;
                versionColumn = parser.saxStream._parser.column + 1;
            }
        });
        parser.saxStream.on("closetag", function (nodeName) {
            // TODO: nested deps!
            if (nodeName == "dependency") {
                this.isDependency = false;
            }
        });
        parser.on("error", function (e) {
            // the XML document doesn't have to be well-formed, that's fine
            parser.error = null;
        });
        return parser;
    }
    parse() {
        try {
            this.stream.pipe(this.parser.saxStream);
        }
        catch (e) {
            console.error(e.message);
        }
        return this.dependencies;
    }
}
class PomXmlDependencyCollector {
    constructor(classes = ["dependencies"]) {
        this.classes = classes;
    }
    collect(file) {
        return __awaiter(this, void 0, void 0, function* () {
            let parser = new NaivePomXmlSaxParser(file);
            let dependencies = parser.parse();
            return dependencies;
        });
    }
}
exports.PomXmlDependencyCollector = PomXmlDependencyCollector;
//# sourceMappingURL=collector.js.map