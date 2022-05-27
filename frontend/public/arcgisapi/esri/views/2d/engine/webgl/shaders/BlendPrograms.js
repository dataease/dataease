// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./sources/resolver"],function(a,b){a.blend={name:"blend",attributes:{a_pos:0,a_tex:1},shaders:c=>({vertexShader:`#define ${c.replace("-","_").toUpperCase()}\n`+b.resolveIncludes("blend/blend.vert"),fragmentShader:`#define ${c.replace("-","_").toUpperCase()}\n`+b.resolveIncludes("blend/blend.frag")})};Object.defineProperty(a,"__esModule",{value:!0})});