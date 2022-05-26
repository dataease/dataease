// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/core/accessorSupport/decorators/reader",["require","exports","../../object","./property"],function(m,d,h,k){Object.defineProperty(d,"__esModule",{value:!0});d.reader=function(b,c,e){var f,g;void 0===c||Array.isArray(c)?(g=b,e=c,f=[void 0]):(g=c,f=Array.isArray(b)?b:[b]);return function(b,c,d){var l=b.constructor.prototype;f.forEach(function(a){a=k.propertyJSONMeta(b,a,g);a.read&&"object"!==typeof a.read&&(a.read={});h.setDeepValue("read.reader",l[c],a);e&&(a.read.source=
(a.read.source||[]).concat(e))})}}});