// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["require","exports"],function(d,e){e.createOrReuseTileRenderer=async function(f,g){if(!f)return null;switch(f.type){case "class-breaks":case "simple":case "unique-value":case "dot-density":case "dictionary":return new (await new Promise(function(a,b){d(["./tileRenderers/SymbolTileRenderer"],function(c){a(Object.freeze({__proto__:null,"default":c}))},b)})).default(g);case "heatmap":return new (await new Promise(function(a,b){d(["./tileRenderers/HeatmapTileRenderer"],function(c){a(Object.freeze({__proto__:null,
"default":c}))},b)})).default(g)}};Object.defineProperty(e,"__esModule",{value:!0})});