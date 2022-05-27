// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(a){const b=[{output:0,name:"color"},{output:1,name:"depth"},{output:2,name:"normal"},{output:3,name:"depthShadowMap"},{output:4,name:"highlight"},{output:5,name:"draped"},{output:6,name:"occlusion"},{output:7,name:"alpha"}];a.ShaderOutputTypes=b;a.getShaderOutputID=function(c,d){return c+"_"+b.find(e=>e.output===d).name};Object.defineProperty(a,"__esModule",{value:!0})});