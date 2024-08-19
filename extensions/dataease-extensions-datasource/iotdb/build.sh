#!/bin/sh
mvn clean package -U -Dmaven.test.skip=true

cp iotdb-backend/target/iotdb-backend-1.18.24.jar .

zip -r iotdb.zip  ./iotdb-backend-1.18.24.jar ./iotdbDriver   ./plugin.json
