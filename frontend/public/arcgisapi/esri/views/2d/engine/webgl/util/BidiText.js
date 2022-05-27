// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../../core/BidiEngine"],function(c,e){const b=new e;c.bidiText=function(a){if(!b.hasBidiChar(a))return[a,!1];let d;d="rtl"===b.checkContextual(a)?"IDNNN":"ICNNN";return[b.bidiTransform(a,d,"VLYSN"),!0]};Object.defineProperty(c,"__esModule",{value:!0})});