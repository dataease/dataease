// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../../core/arrayUtils"],function(d,f){let g=function(){function e(){this._uniforms={proj:[],shadowMapDistance:[],viewportPixelSz:[],lightingMainDirection:[]}}var b=e.prototype;b.dispose=function(){this._uniforms=null};b.getPrograms=function(a){return this._uniforms[a]||[]};b.subscribeProgram=function(a){for(const c in this._uniforms)a.hasUniform(c)&&this._uniforms[c].push(a)};b.unsubscribeProgram=function(a){for(const c in this._uniforms)f.removeUnordered(this._uniforms[c],
a)};return e}();d.CommonUniformStore=g;Object.defineProperty(d,"__esModule",{value:!0})});