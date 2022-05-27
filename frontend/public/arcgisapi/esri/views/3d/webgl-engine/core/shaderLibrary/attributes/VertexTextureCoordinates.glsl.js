// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces","./TextureCoordinateAttribute.glsl","../util/TextureAtlasLookup.glsl"],function(d,c,e,f){d.VertexTextureCoordinates=function(a,b){a.include(e.TextureCoordinateAttribute,b);a.fragment.code.add(c.glsl`
  struct TextureLookupParameter {
    vec2 uv;
    ${b.supportsTextureAtlas?"vec2 size;":""}
  } vtc;
  `);1===b.attributeTextureCoordinates&&a.fragment.code.add(c.glsl`
      vec4 textureLookup(sampler2D tex, TextureLookupParameter params) {
        return texture2D(tex, params.uv);
      }
    `);2===b.attributeTextureCoordinates&&(a.include(f.TextureAtlasLookup),a.fragment.code.add(c.glsl`
    vec4 textureLookup(sampler2D tex, TextureLookupParameter params) {
        return textureAtlasLookup(tex, params.size, params.uv, vuvRegion);
      }
    `))};Object.defineProperty(d,"__esModule",{value:!0})});