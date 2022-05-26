// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/rasterLib/function/pixelShaders",["dojo/_base/declare","dojo/_base/lang","./pixelShaderScripts"],function(c,d,e){c={shaderType:"fragment",getShader:function(a,c){var b;b=a.createShader(a.FRAGMENT_SHADER);a.shaderSource(b,c);a.compileShader(b);a.getShaderParameter(b,a.COMPILE_STATUS)||(b=null);return b}};d.mixin(c,e);return c});