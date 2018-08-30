curl -XDELETE 'http://localhost:9200/herbonautes_prod'
curl -XPUT 'http://localhost:9200/herbonautes_prod' -H "Content-type:application/json" --data @mapping.json
java -cp "lib/*" -Dlog4j.configurationFile=bin/log4j2.xml org.xbib.tools.Runner org.xbib.tools.JDBCImporter init_prod.json