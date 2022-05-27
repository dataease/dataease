// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces","../../../collections/Component/Material/shader/DecodeSymbolColor.glsl"],function(b,c,e){b.SymbolColor=function(a,d){d.symbolColor?(a.include(e.DecodeSymbolColor),a.attributes.add("symbolColor","vec4"),a.varyings.add("colorMixMode","mediump float")):a.fragment.uniforms.add("colorMixMode","int");d.symbolColor?a.vertex.code.add(c.glsl`
    int symbolColorMixMode;

    vec4 getSymbolColor() {
      return decodeSymbolColor(symbolColor, symbolColorMixMode) * 0.003921568627451;
    }

    void forwardColorMixMode() {
      colorMixMode = float(symbolColorMixMode) + 0.5;
    }
  `):a.vertex.code.add(c.glsl`
    vec4 getSymbolColor() { return vec4(1.0); }
    void forwardColorMixMode() {}
    `)};Object.defineProperty(b,"__esModule",{value:!0})});