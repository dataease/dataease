// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../core/Error"],function(b,c){b.createCopyError=function(){return new c("webscene:failed-to-copy-embedded-resources","Copying of embedded resources is currently not supported")};b.createSchemaValidationError=function(a){return new c("webscene:schema-validation","Failed to save webscene due to schema validation errors. See 'details.errors' for more detailed information",{errors:a})};b.isCopyError=function(a){return a&&"webscene:failed-to-copy-embedded-resources"===a.name};b.isSchemaValidationError=
function(a){return a&&"webscene:schema-validation"===a.name};Object.defineProperty(b,"__esModule",{value:!0})});