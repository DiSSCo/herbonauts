

# Build

Récupérer le jar oracle et l'installer dans le repository maven

$ mvn install:install-file -Dfile=/Users/jgs/Missions/Herbonautes/workspace_v2.1/herbonautes_v2/lib/ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.4 -Dpackaging=jar
