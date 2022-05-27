// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces","../util/TextureAtlasLookup.glsl","../attributes/VertexTextureCoordinates.glsl"],function(e,b,f,g){e.ReadBaseColorTexture=function(c,d){const a=c.fragment;d.baseColorTexture?(c.include(g.VertexTextureCoordinates,d),a.uniforms.add("uBaseColorTexture","sampler2D"),a.uniforms.add("uBaseColorTextureSize","vec2"),2===d.attributeTextureCoordinates?(c.include(f.TextureAtlasLookup),a.code.add(b.glsl`
        vec4 readBaseColorTexture() {
          return textureAtlasLookup(
            uBaseColorTexture,
            uBaseColorTextureSize,
            vuv0,
            vuvRegion
          );
        }
      `)):a.code.add(b.glsl`
        vec4 readBaseColorTexture() {
          return texture2D(uBaseColorTexture, vuv0);
        }
      `)):a.code.add(b.glsl`
      vec4 readBaseColorTexture() { return vec4(1.0); }
    `)};Object.defineProperty(e,"__esModule",{value:!0})});