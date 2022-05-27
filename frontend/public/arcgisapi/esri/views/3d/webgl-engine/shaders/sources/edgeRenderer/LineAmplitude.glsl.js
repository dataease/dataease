// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../core/shaderModules/interfaces","./EdgeUtil.glsl","./UnpackAttributes.glsl"],function(d,b,f,g){d.LineAmplitude=function(e,c){const a=e.vertex;e.include(g.UnpackAttributes,c);f.EdgeUtil.usesSketchLogic(c)&&a.uniforms.add("uStrokesAmplitude","float");switch(c.mode){case 0:a.code.add(b.glsl`
        float calculateLineAmplitude(UnpackedAttributes unpackedAttributes) {
          return 0.0;
        }
      `);break;case 1:a.code.add(b.glsl`
        float calculateLineAmplitude(UnpackedAttributes unpackedAttributes) {
          return uStrokesAmplitude;
        }
      `);break;case 2:a.code.add(b.glsl`
        float calculateLineAmplitude(UnpackedAttributes unpackedAttributes) {
          float type = unpackedAttributes.type;

          if (type <= 0.0) {
            return uStrokesAmplitude;
          }
          else {
            return 0.0;
          }
        }
      `)}};Object.defineProperty(d,"__esModule",{value:!0})});