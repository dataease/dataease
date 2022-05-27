// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./isWebGL2Context"],function(b,c){b.load=function(a,d){return c(a)?{createVertexArray:a.createVertexArray.bind(a),deleteVertexArray:a.deleteVertexArray.bind(a),bindVertexArray:a.bindVertexArray.bind(a)}:d.vao?null:(a=a.getExtension("OES_vertex_array_object")||a.getExtension("MOZ_OES_vertex_array_object")||a.getExtension("WEBKIT_OES_vertex_array_object"))?{createVertexArray:a.createVertexArrayOES.bind(a),deleteVertexArray:a.deleteVertexArrayOES.bind(a),bindVertexArray:a.bindVertexArrayOES.bind(a)}:
null};Object.defineProperty(b,"__esModule",{value:!0})});