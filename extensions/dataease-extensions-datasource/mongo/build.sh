#!/bin/sh
mvn clean package -U -Dmaven.test.skip=true

cp mongo-backend/target/mongo-backend-1.18.0.jar .

zip -r mongo.zip  ./mongo-backend-1.18.0.jar ./mongobiDriver   ./plugin.json
