// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["require","exports"],function(b,a){a.parseWhereClause=async function(c,d){const {WhereClause:e}=await new Promise(function(f,g){b(["./sql/WhereClause"],f,g)});return e.create(c,d)};Object.defineProperty(a,"__esModule",{value:!0})});