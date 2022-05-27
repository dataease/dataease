// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(a){let f=function(){function b(d,e){this._module=d;this._loadModule=e}var c=b.prototype;c.get=function(){return this._module};c.reload=async function(){return this._module=await this._loadModule()};return b}();a.ReloadableShaderModule=f;Object.defineProperty(a,"__esModule",{value:!0})});