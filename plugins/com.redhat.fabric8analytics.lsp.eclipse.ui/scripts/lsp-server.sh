rm -rf ca-lsp-server*

wget https://github.com/invincibleJai/sampleBootstrapTemplate/releases/download/0.0.6/ca-lsp-server.tar

archive=ca-lsp-server-0.0.6-SNAPSHOT.tar
mkdir ${archive%.tar*} 
tar --extract --file=${archive} --strip-components=1 --directory=${archive%.tar*}

chmod +x ${archive%.tar*}/server.js