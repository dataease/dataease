#!/bin/sh
mvn clean package -U -Dmaven.test.skip=true

cp iotdb-backend/target/iotdb-backend-1.18.24.jar .

rm -f iotdb.zip

zip -r iotdb.zip  ./iotdb-backend-1.18.24.jar ./iotdbDriver   ./plugin.json
