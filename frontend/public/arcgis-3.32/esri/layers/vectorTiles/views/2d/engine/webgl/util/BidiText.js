// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/views/2d/engine/webgl/util/BidiText",["require","exports","dojox/string/BidiEngine"],function(e,a,d){Object.defineProperty(a,"__esModule",{value:!0});var c=new d;a.bidiText=function(b){if(!c.hasBidiChar(b))return[b,!1];var a;a="rtl"===c.checkContextual(b)?"IDNNN":"ICNNN";return[c.bidiTransform(b,a,"VLYSN"),!0]}});