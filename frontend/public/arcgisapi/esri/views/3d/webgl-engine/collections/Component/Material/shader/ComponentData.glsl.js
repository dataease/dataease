// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../core/shaderModules/interfaces","./DecodeSymbolColor.glsl"],function(c,b,e){c.ComponentData=function(a,d){1===d.componentData&&(a.vertex.uniforms.add("uComponentColorTex","sampler2D"),a.vertex.uniforms.add("uComponentColorTexInvDim","vec2"),a.attributes.add("componentIndex","float"),a.varyings.add("vExternalColorMixMode","mediump float"),a.varyings.add("vExternalColor","vec4"),a.include(e.DecodeSymbolColor),a.vertex.code.add(b.glsl`
      vec4 _readComponentColor() {
        float normalizedIndex = (componentIndex + 0.5) * uComponentColorTexInvDim.x;
        vec2 indexCoord = vec2(
          mod(normalizedIndex, 1.0),
          (floor(normalizedIndex) + 0.5) * uComponentColorTexInvDim.y
        );
        return texture2D(uComponentColorTex, indexCoord);
       }

      vec4 forwardExternalColor(out bool castShadows) {
        vec4 componentColor = _readComponentColor() * 255.0;

        float shadowFlag = mod(componentColor.b * 255.0, 2.0);
        componentColor.b -= shadowFlag;
        castShadows = shadowFlag >= 1.0;

        int decodedColorMixMode;
        vExternalColor = decodeSymbolColor(componentColor, decodedColorMixMode) * 0.003921568627451; // = 1/255;
        vExternalColorMixMode = float(decodedColorMixMode) + 0.5; // add 0.5 to avoid interpolation artifacts

        return vExternalColor;
      }
    `),a.fragment.code.add(b.glsl`
      void readExternalColor(out vec4 externalColor, out int externalColorMixMode) {
        externalColor = vExternalColor;
        externalColorMixMode = int(vExternalColorMixMode);
      }
    `));0===d.componentData&&(a.vertex.uniforms.add("uExternalColor","vec4"),a.fragment.uniforms.add("uExternalColorMixMode","int"),a.varyings.add("vExternalColor","vec4"),a.vertex.code.add(b.glsl`
      vec4 forwardExternalColor(out bool castShadows) {
        vExternalColor = uExternalColor;
        castShadows = true;
        return uExternalColor;
      }
    `),a.fragment.code.add(b.glsl`
      void readExternalColor(out vec4 externalColor, out int externalColorMixMode) {
        externalColor = vExternalColor;
        externalColorMixMode = uExternalColorMixMode;
      }
    `))};Object.defineProperty(c,"__esModule",{value:!0})});