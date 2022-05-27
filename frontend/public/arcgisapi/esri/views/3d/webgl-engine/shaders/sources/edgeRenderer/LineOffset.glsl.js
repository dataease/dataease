// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../core/shaderModules/interfaces","../../../core/shaderLibrary/util/RgbaFloatEncoding.glsl","./EdgeUtil.glsl","./UnpackAttributes.glsl"],function(f,a,g,h,k){f.LineOffset=function(c,e){const b=c.vertex;c.include(k.UnpackAttributes,e);const d=c.fragment;h.EdgeUtil.usesSketchLogic(e)&&(b.uniforms.add("uStrokesTextureScale","vec2"),b.uniforms.add("uStrokesLog2Resolution","float"),b.uniforms.add("uStrokeVariants","float"),c.varyings.add("vStrokeUV","vec2"),d.uniforms.add("uStrokesTexture",
"sampler2D"),d.uniforms.add("uStrokesNormalizationScale","float"),b.code.add(a.glsl`
      void calculateStyleOutputsSketch(float lineLength, UnpackedAttributes unpackedAttributes) {
        vec2 sidenessNorm = unpackedAttributes.sidenessNorm;

        float lineIndex = clamp(ceil(log2(lineLength)), 0.0, uStrokesLog2Resolution);

        vStrokeUV = vec2(exp2(lineIndex) * sidenessNorm.y, lineIndex * uStrokeVariants + variantStroke + 0.5) * uStrokesTextureScale;
        vStrokeUV.x += variantOffset;
      }
    `),c.fragment.include(g.RgbaFloatEncoding),d.code.add(a.glsl`
      float calculateLineOffsetSketch() {
        float offsetNorm = rgba2float(texture2D(uStrokesTexture, vStrokeUV));
        return (offsetNorm - 0.5) * uStrokesNormalizationScale;
      }

      float calculateLinePressureSketch() {
        return rgba2float(texture2D(uStrokesTexture, vStrokeUV + vec2(0.0, 0.5)));
      }
    `));switch(e.mode){case 0:b.code.add(a.glsl`
        void calculateStyleOutputs(UnpackedAttributes unpackedAttributes) {}
      `);d.code.add(a.glsl`
        float calculateLineOffset() {
          return 0.0;
        }

        float calculateLinePressure() {
          return 1.0;
        }
      `);break;case 1:b.code.add(a.glsl`
        void calculateStyleOutputs(UnpackedAttributes unpackedAttributes)
        {
          calculateStyleOutputsSketch(vLineLengthPixels, unpackedAttributes);
        }
      `);d.code.add(a.glsl`
        float calculateLineOffset() {
          return calculateLineOffsetSketch();
        }

        float calculateLinePressure() {
          return calculateLinePressureSketch();
        }
      `);break;case 2:c.varyings.add("vType","float"),b.code.add(a.glsl`
        void calculateStyleOutputs(UnpackedAttributes unpackedAttributes)
        {
          vType = unpackedAttributes.type;

          if (unpackedAttributes.type <= 0.0) {
            calculateStyleOutputsSketch(vLineLengthPixels, unpackedAttributes);
          }
        }
      `),d.code.add(a.glsl`
        float calculateLineOffset() {
          if (vType <= 0.0) {
            return calculateLineOffsetSketch();
          }
          else {
            return 0.0;
          }
        }

        float calculateLinePressure() {
          if (vType <= 0.0) {
            return calculateLinePressureSketch();
          }
          else {
            return 1.0;
          }
        }
      `)}};Object.defineProperty(f,"__esModule",{value:!0})});