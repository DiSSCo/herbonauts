Instalation du batch de calcul des tuiles

1) Check out du projet ‡ partir de svn

2) configuration de conf.properties

dans /src/main/resources







Alternative : utiliser maven :

Installer le jar Oracle dans le repository local :

mvn install:install-file -DgroupId=oracle -DartifactId=ojdbc -Dversion=5 -Dfile=lib/ojdbc5.jar -Dpackaging=jar

Cr√©er le jar executable :

mvn package

Lancer le batch (exemple de configuration dans les sources) :

java -jar target/herbonautes-tiles-1.0-SNAPSHOT-jar-with-dependencies.jar src/main/resources/conf.properties 