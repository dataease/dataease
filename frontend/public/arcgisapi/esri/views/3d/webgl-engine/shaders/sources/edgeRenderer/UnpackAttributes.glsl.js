// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../core/shaderModules/interfaces","./EdgeUtil.glsl"],function(e,a,f){e.UnpackAttributes=function(c,d){const b=c.vertex;c.include(f.EdgeUtil,d);c.attributes.add("sideness","vec2");2===d.mode?b.code.add(a.glsl`
      struct UnpackedAttributes {
        vec2 sideness;
        vec2 sidenessNorm;
        float lineWidthPixels;
        float extensionLengthPixels;
        float type;
      };
    `):b.code.add(a.glsl`
      struct UnpackedAttributes {
        vec2 sideness;
        vec2 sidenessNorm;
        float lineWidthPixels;
        float extensionLengthPixels;
      };
  `);switch(d.mode){case 2:b.code.add(a.glsl`
        UnpackedAttributes unpackAttributes(ComponentData component) {
          vec2 sidenessNorm = sideness;
          vec2 sideness = sidenessNorm * 2.0 - 1.0;
          float fType = component.type;
          float extensionLengthPixels = component.extensionLength;
          float lineWidth = component.lineWidth;

          if (fType <= 0.0) {
            extensionLengthPixels *= variantExtension * 2.0 - 1.0;
          }

          return UnpackedAttributes(sideness, sidenessNorm, lineWidth, extensionLengthPixels, fType);
        }
      `);break;case 1:b.code.add(a.glsl`
        UnpackedAttributes unpackAttributes(ComponentData component) {
          vec2 sidenessNorm = sideness;
          vec2 sideness = sidenessNorm * 2.0 - 1.0;
          float extensionLengthPixels = component.extensionLength;
          extensionLengthPixels *= variantExtension * 2.0 - 1.0;
          float lineWidth = component.lineWidth;

          return UnpackedAttributes(sideness, sidenessNorm, lineWidth, extensionLengthPixels);
        }
      `);break;case 0:b.code.add(a.glsl`
        UnpackedAttributes unpackAttributes(ComponentData component) {
          vec2 sidenessNorm = sideness;
          vec2 sideness = sidenessNorm * 2.0 - 1.0;
          float extensionLengthPixels = component.extensionLength;
          float lineWidth = component.lineWidth;

          return UnpackedAttributes(sideness, sidenessNorm, lineWidth, extensionLengthPixels);
        }
      `)}};Object.defineProperty(e,"__esModule",{value:!0})});