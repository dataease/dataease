// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../views/3d/webgl-engine/core/shaderModules/interfaces","../views/3d/webgl-engine/core/shaderModules/ShaderBuilder","../views/3d/webgl-engine/core/shaderLibrary/ScreenSpacePass"],function(d,b,f,g){function e(c){const a=new f.ShaderBuilder;a.include(g.ScreenSpacePass);a.fragment.uniforms.add("tex","sampler2D");0===c.function&&(c.hasOpacityFactor?(a.fragment.uniforms.add("opacity","float"),a.fragment.code.add(b.glsl`
      void main() {
        gl_FragColor = texture2D(tex, uv) * opacity;
      }`)):a.fragment.code.add(b.glsl`
      void main() {
        gl_FragColor = texture2D(tex, uv);
      }`));2===c.function&&a.fragment.code.add(b.glsl`
    void main() {
      gl_FragColor = vec4(1.0 - texture2D(tex, uv).a);
    }`);3===c.function&&(a.fragment.uniforms.add("colorTexture","sampler2D"),a.fragment.uniforms.add("alphaTexture","sampler2D"),a.fragment.uniforms.add("frontFaceTexture","sampler2D"),a.fragment.code.add(b.glsl`
    void main() {
      vec4 srcColor = texture2D(colorTexture, uv);
      float srcAlpha = texture2D(alphaTexture, uv).r;
      vec4 frontFace = texture2D(frontFaceTexture, uv);

      gl_FragColor = vec4(mix(srcColor.rgb/srcColor.a, frontFace.rgb, frontFace.a), 1.0 - srcAlpha);
    }`));return a}var h=Object.freeze({__proto__:null,build:e});d.CompositingShader=h;d.build=e});