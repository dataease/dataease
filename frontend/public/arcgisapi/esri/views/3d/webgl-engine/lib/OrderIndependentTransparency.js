// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../webgl/renderState"],function(a,c){const g=c.separateBlendingParams(770,1,771,771),d=c.simpleBlendingParams(1,1),e=c.simpleBlendingParams(0,771),f={factor:-1,units:-2};a.OITBlending=function(b){return 2===b?null:1===b?e:d};a.OITDepthTest=function(b){return 3===b||2===b?513:515};a.OITDepthWrite=function(b){return 2===b?c.defaultDepthWriteParams:null};a.OITPolygonOffset=f;a.OITPolygonOffsetLimit=5E5;a.blendingAlpha=e;a.blendingColor=d;a.blendingDefault=g;a.getOITPolygonOffset=
function(b){return b?f:null};Object.defineProperty(a,"__esModule",{value:!0})});