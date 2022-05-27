// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../CIMCursor","../CIMOperators"],function(b,f,g){let k=function(){function c(){}c.getPlacement=function(a,d,h){const e=g.getPlacementOperator(d);if(!e)return null;a=f.cloneAndDecodeGeometry(a);return e.execute(a,d,h)};return c}();b.CIMMarkerPlacementHelper=k;Object.defineProperty(b,"__esModule",{value:!0})});