# Continous Deployment with GitHub, Travis and Artifactory

## Continous Integration mit Travis

* Erstellen `.travis.yml` für CI mit Travis, z. B.:

```yaml
language: java

jdk:
  - openjdk8

sudo: false

cache:
  directories:
    - $HOME/.m2
    - $HOME/.gradle

deploy:
  provider: script
  script: "mvn deploy --settings .travis.settings.xml"
  skip_cleanup: true
  on:
    branch: master
```

## Deployment mit Maven von Travis nach Artifactory

* Hinzufügen Artifactory-URL in pom.xml:

```xml
<project>
    <!-- ... -->
    <distributionManagement>
        <repository>
            <id>central</id>
            <name>artifactory-releases</name>
            <url>http://artifactory-ls6.informatik.uni-wuerzburg.de/artifactory/libs-snapshot</url>
        </repository>
        <snapshotRepository>
            <id>snapshots</id>
            <name>artifactory-snapshots</name>
            <url>http://artifactory-ls6.informatik.uni-wuerzburg.de/artifactory/libs-snapshot</url>
        </snapshotRepository>
    </distributionManagement>
    <!-- ... -->
</project>
```

* Hinzufügen Datei mit Deploy-Einstellungen für Maven `.travis.settings.xml` (siehe oben, deploy):

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>central</id>
            <username>travis-ci</username>
            <password>${env.ARTIFACTORY_ACCESS_TOKEN}</password>
        </server>
        <server>
            <id>snapshots</id>
            <username>travis-ci</username>
            <password>${env.ARTIFACTORY_ACCESS_TOKEN}</password>
        </server>
    </servers>
</settings>
```

* Hinzufügen von Umgebungsvariable ARTIFACTORY_ACCESS_TOKEN zu Travis-Build-Umgebung