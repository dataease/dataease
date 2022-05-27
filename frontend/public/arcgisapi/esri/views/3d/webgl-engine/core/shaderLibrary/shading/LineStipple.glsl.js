// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(d,c){d.LineStipple=function(a,b){a.defines.addFloat("STIPPLE_ALPHA_COLOR_DISCARD",.001);a.defines.addFloat("STIPPLE_ALPHA_HIGHLIGHT_DISCARD",.5);b.stippleEnabled?(a.vertex.uniforms.add("stipplePatternPixelSizeInv","float"),b.stippleUVMaxEnabled&&a.varyings.add("stipplePatternUvMax","float"),a.varyings.add("stipplePatternUv","float"),a.fragment.uniforms.add("stipplePatternTexture","sampler2D"),b.stippleOffColorEnabled&&a.fragment.uniforms.add("stippleOffColor",
"vec4"),a.fragment.code.add(c.glsl`
  float getStippleAlpha() {
    float stipplePatternUvClamped = stipplePatternUv * gl_FragCoord.w;
    ${b.stippleUVMaxEnabled?"stipplePatternUvClamped \x3d clamp(stipplePatternUvClamped, 0.0, stipplePatternUvMax);":""}

    return texture2D(stipplePatternTexture, vec2(mod(stipplePatternUvClamped, 1.0), 0.5)).a;
  }`),b.stippleOffColorEnabled?a.fragment.code.add(c.glsl`
    #define discardByStippleAlpha(stippleAlpha, threshold) {}
    #define blendStipple(color, stippleAlpha) mix(color, stippleOffColor, stippleAlpha)
    `):a.fragment.code.add(c.glsl`
    #define discardByStippleAlpha(stippleAlpha, threshold) if (stippleAlpha < threshold) { discard; }
    #define blendStipple(color, stippleAlpha) vec4(color.rgb, color.a * stippleAlpha)
    `)):a.fragment.code.add(c.glsl`
  float getStippleAlpha() { return 1.0; }

  #define discardByStippleAlpha(_stippleAlpha_, _threshold_) {}
  #define blendStipple(color, _stippleAlpha_) color
  `)};Object.defineProperty(d,"__esModule",{value:!0})});