// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(d,b){d.TerrainTexture=function(a,c){a.varyings.add("vtc","vec2");a.vertex.uniforms.add("texOffsetAndScale","vec4");a.fragment.uniforms.add("tex","sampler2D");c.textureFadingEnabled&&(a.vertex.uniforms.add("nextTexOffsetAndScale","vec4"),a.varyings.add("nvtc","vec2"),a.fragment.uniforms.add("texNext","sampler2D"),a.fragment.uniforms.add("textureFadeFactor","float"));a.vertex.code.add(b.glsl`
  void forwardTextureCoordinates(in vec2 uv) {
    vtc = uv * texOffsetAndScale.zw + texOffsetAndScale.xy;
    ${c.textureFadingEnabled?b.glsl`nvtc = uv * nextTexOffsetAndScale.zw + nextTexOffsetAndScale.xy;`:b.glsl``}
  }
  `);a.fragment.code.add(b.glsl`
  vec4 getTileColor() {
    ${c.textureFadingEnabled?b.glsl`return textureFadeFactor < 1.0 ? mix(texture2D(texNext, nvtc), texture2D(tex, vtc), textureFadeFactor) : texture2D(tex, vtc);`:b.glsl`return texture2D(tex, vtc);`}
  }
  `)};Object.defineProperty(d,"__esModule",{value:!0})});