// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(a,c){a.PositionAttribute=function(b){b.attributes.add("position","vec3");b.vertex.code.add(c.glsl`
    vec3 positionModel() { return position; }
  `)};Object.defineProperty(a,"__esModule",{value:!0})});