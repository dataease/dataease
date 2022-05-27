// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../support/validationUtilsAjv","../../chunks/ajv.bundle","../../portal/schemas/webScene"],function(h,m,n,f){function c(d){return d?`${d}_schema.json`:"webScene_schema.json"}const a=new n.Ajv({allErrors:!0,extendRefs:!0});a.addSchema(f.json,c());h.validate=function(d,e){{const k=c(e);if(!a.getSchema(k)){{var b=f.json.definitions[c(e)];if(!b)throw Error(`invalid schema name to validate against '${e}'`);const g={};for(const l in b)g[l]=b[l];g.definitions=f.json.definitions;b=g}a.addSchema(b,
k)}}return a.validate(c(e),d)?[]:m.convertAjvErrors(a.errors)};Object.defineProperty(h,"__esModule",{value:!0})});