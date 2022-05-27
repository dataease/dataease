// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./Collection"],function(c,d){c.castForReferenceSetter=function(a){return a};c.referenceSetter=function(a,b,e=d){b||(b=new e);if(b===a)return b;b.removeAll();a&&(Array.isArray(a)||"items"in a&&Array.isArray(a.items))?b.addMany(a):a&&b.add(a);return b};Object.defineProperty(c,"__esModule",{value:!0})});