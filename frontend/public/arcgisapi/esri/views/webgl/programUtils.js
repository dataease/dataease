// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./Program"],function(e,g){e.createProgram=function(d,c,b,a){b=b||{};a=a||"";b="function"===typeof c.shaders?c.shaders(b):c.shaders;return new g(d,a+b.vertexShader,a+b.fragmentShader,c.attributes)};e.glslifyDefineMap=function(d){let c="";for(const b in d){const a=d[b];if("boolean"===typeof a)a&&(c+=`#define ${b}\n`);else if("number"===typeof a)c+=`#define ${b} ${a.toFixed()}\n`;else if("object"===typeof a){const f=a.options;let h=0;for(const k in f)c+=`#define ${f[k]} ${(h++).toFixed()}\n`;
c+=`#define ${b} ${f[a.value]}\n`}}return c};Object.defineProperty(e,"__esModule",{value:!0})});