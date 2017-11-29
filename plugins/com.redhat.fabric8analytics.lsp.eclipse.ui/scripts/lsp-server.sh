rm -rf ca-lsp-server*

wget http://download.jboss.org/jbosstools/oxygen/snapshots/builds/jbosstools-fabric8analytics-lsp-server_master/latest/ca-lsp-server-0.0.6-SNAPSHOT.tar

archive=ca-lsp-server-0.0.6-SNAPSHOT.tar
mkdir ${archive%.tar*} 
tar --extract --file=${archive} --strip-components=1 --directory=${archive%.tar*}