// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./Pattern3D","./StylePattern3D"],function(b,c,e){function f(a,d,g){if(!a)return a;switch(a.type){case "style":return d=new e,d.read(a,g),d}}c={types:{key:"type",base:c,typeMap:{style:e}},json:{read:f,write:!0}};b.read=f;b.symbol3dPatternProperty=c;Object.defineProperty(b,"__esModule",{value:!0})});