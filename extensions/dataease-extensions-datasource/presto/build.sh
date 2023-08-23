#!/bin/sh
mvn clean package

cp presto-backend/target/presto-backend-1.18.0.jar .

zip -r presto.zip  ./presto-backend-1.18.0.jar ./prestoDriver   ./plugin.json
