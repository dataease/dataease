#!/bin/sh
mvn clean package

cp view-racebar-backend/target/view-racebar-backend-1.18.13.jar .

zip -r racebar.zip  ./view-racebar-backend-1.18.13.jar ./plugin.json

rm -f ./view-racebar-backend-1.18.13.jar
