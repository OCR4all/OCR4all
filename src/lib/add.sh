#!/bin/bash

FILE=$1
GROUPID=$2
ARTIFACTID=$3
VERSION=$4

mvn install:install-file -Dfile=$FILE -DgroupId=$GROUPID -DartifactId=$ARTIFACTID -Dversion=$VERSION -Dpackaging=jar -DlocalRepositoryPath=repository/
