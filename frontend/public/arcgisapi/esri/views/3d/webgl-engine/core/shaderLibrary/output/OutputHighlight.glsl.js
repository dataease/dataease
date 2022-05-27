// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(d,f){function b(a){a.fragment.uniforms.add("depthTex","sampler2D");a.fragment.uniforms.add("highlightViewportPixelSz","vec4");a.fragment.code.add(f.glsl`
    void outputHighlight() {
      vec4 fragCoord = gl_FragCoord;

      float sceneDepth = texture2D(depthTex, (fragCoord.xy - highlightViewportPixelSz.xy) * highlightViewportPixelSz.zw).r;
      if (fragCoord.z > sceneDepth + 5e-7) {
        gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);
      }
      else {
        gl_FragColor = vec4(1.0, 0.0, 1.0, 1.0);
      }
    }
  `)}(function(a){a.bindOutputHighlight=function(g,e,c){g.bindTexture(c.highlightDepthTexture,5);e.setUniform1i("depthTex",5);e.setUniform4f("highlightViewportPixelSz",0,0,c.inverseViewport[0],c.inverseViewport[1])}})(b||(b={}));d.OutputHighlight=b;Object.defineProperty(d,"__esModule",{value:!0})});