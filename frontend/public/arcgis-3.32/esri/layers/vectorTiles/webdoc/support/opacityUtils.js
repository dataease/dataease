// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/webdoc/support/opacityUtils",["require","exports","../../core/accessorSupport/ensureType"],function(d,b,c){Object.defineProperty(b,"__esModule",{value:!0});b.opacityToTransparency=function(a){a=c.ensureInteger(100*(1-a));return Math.max(0,Math.min(a,100))};b.transparencyToOpacity=function(a){return Math.max(0,Math.min(1-a/100,1))}});