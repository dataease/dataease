#!/bin/sh
mvn clean package

cp maxcompute-backend/target/maxcompute-backend-1.18.0.jar .

zip -r maxcompute.zip  ./maxcompute-backend-1.18.0.jar ./maxcomputeDriver   ./plugin.json
