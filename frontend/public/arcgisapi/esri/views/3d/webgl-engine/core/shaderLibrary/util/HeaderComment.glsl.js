// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../../../core/has","../../shaderModules/interfaces"],function(b,d,c){b.HeaderComment=function(e,a){c.glsl`
  /*
  *  ${a.name}
  *  ${0===a.output?"RenderOutput: Color":1===a.output?"RenderOutput: Depth":3===a.output?"RenderOutput: Shadow":2===a.output?"RenderOutput: Normal":4===a.output?"RenderOutput: Highlight":""}
  */
  `};Object.defineProperty(b,"__esModule",{value:!0})});