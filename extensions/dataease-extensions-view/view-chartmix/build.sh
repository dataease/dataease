#!/bin/sh
mvn clean package

cp view-chartmix-backend/target/view-chartmix-backend-1.18.14.jar .

zip -r chartmix.zip  ./view-chartmix-backend-1.18.14.jar ./plugin.json

rm -f ./view-chartmix-backend-1.18.14.jar
