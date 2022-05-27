// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(d,b){d.TextureCoordinateAttribute=function(a,c){1===c.attributeTextureCoordinates&&(a.attributes.add("uv0","vec2"),a.varyings.add("vuv0","vec2"),a.vertex.code.add(b.glsl`
      void forwardTextureCoordinates() {
        vuv0 = uv0;
      }
    `));2===c.attributeTextureCoordinates&&(a.attributes.add("uv0","vec2"),a.varyings.add("vuv0","vec2"),a.attributes.add("uvRegion","vec4"),a.varyings.add("vuvRegion","vec4"),a.vertex.code.add(b.glsl`
      void forwardTextureCoordinates() {
        vuv0 = uv0;
        vuvRegion = uvRegion;
      }
    `));0===c.attributeTextureCoordinates&&a.vertex.code.add(b.glsl`
      void forwardTextureCoordinates() {}
    `)};Object.defineProperty(d,"__esModule",{value:!0})});