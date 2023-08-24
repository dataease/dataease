#!/bin/sh
 mvn clean package -U -Dmaven.test.skip=true

cp kylin-backend/target/kylin-backend-1.18.0.jar .

zip -r kylin.zip  ./kylin-backend-1.18.0.jar ./kylinDriver   ./plugin.json
