// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(b,a){b.DiscardOrAdjustAlpha=function(d,e){const c=d.fragment;switch(e.alphaDiscardMode){case 0:c.code.add(a.glsl`
        #define discardOrAdjustAlpha(color) { if (color.a < ${a.glsl.float(.001)}) { discard; } }
      `);break;case 1:c.code.add(a.glsl`
        void discardOrAdjustAlpha(inout vec4 color) {
          color.a = 1.0;
        }
      `);break;case 2:c.uniforms.add("textureAlphaCutoff","float");c.code.add(a.glsl`
        #define discardOrAdjustAlpha(color) { if (color.a < textureAlphaCutoff) { discard; } else { color.a = 1.0; } }
      `);break;case 3:d.fragment.uniforms.add("textureAlphaCutoff","float"),d.fragment.code.add(a.glsl`
        #define discardOrAdjustAlpha(color) { if (color.a < textureAlphaCutoff) { discard; } }
      `)}};b.defaultMaskAlphaCutoff=.1;b.symbolAlphaCutoff=.001;Object.defineProperty(b,"__esModule",{value:!0})});