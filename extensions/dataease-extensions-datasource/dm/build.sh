#!/bin/sh
mvn clean package -U -Dmaven.test.skip=true

cp dm-backend/target/dm-backend-1.18.0.jar .

zip -r dm.zip  ./dm-backend-1.18.0.jar ./dmDriver   ./plugin.json
