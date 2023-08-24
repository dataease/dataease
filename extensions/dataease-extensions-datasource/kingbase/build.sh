#!/bin/sh
mvn clean package -U -Dmaven.test.skip=true

cp kingbase-backend/target/kingbase-backend-1.18.0.jar .

zip -r kingbase.zip  ./kingbase-backend-1.18.0.jar ./kingbaseDriver   ./plugin.json
