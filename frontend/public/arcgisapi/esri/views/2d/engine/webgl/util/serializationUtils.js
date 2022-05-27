// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(d){d.deserializeList=function(a,b,e){var c=a.readInt32();c=Array(c);for(let f=0;f<c.length;f++)c[f]=b.deserialize(a,e);return c};d.serializeList=function(a,b){if(null===b)a.push(0);else{a.push(b.length);for(const e of b)e.serialize(a);return a}};Object.defineProperty(d,"__esModule",{value:!0})});