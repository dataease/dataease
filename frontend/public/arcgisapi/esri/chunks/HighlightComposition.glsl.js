// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../views/3d/webgl-engine/core/shaderModules/interfaces","../views/3d/webgl-engine/core/shaderModules/ShaderBuilder"],function(f,b,h){function g(c){const a=new h.ShaderBuilder,d=a.vertex.code,e=a.fragment.code;a.attributes.add("position","vec2");2===c.highlightStage&&(d.add(b.glsl`
    void main() {
      gl_Position = vec4(vec2(1.0) - position * 2.0, 0.0, 1.0);
    }`),a.fragment.uniforms.add("tex","sampler2D"),a.fragment.uniforms.add("invFramebufferDim","vec2"),e.add(b.glsl`
      void main() {
        vec2 coord = gl_FragCoord.xy * invFramebufferDim;
        vec4 value = texture2D(tex, coord);
        float mx = floor(max(value.g, value.b));
        gl_FragColor = vec4(ceil(value.r), mx, mx, 1.0);
      }`));0===c.highlightStage&&(a.attributes.add("uv0","vec2"),c.gridOptimization?(a.vertex.uniforms.add("coverageTex","sampler2D"),a.fragment.uniforms.add("blurSize","vec2"),a.varyings.add("blurCoordinate","vec3")):(a.vertex.uniforms.add("blurSize","vec2"),a.varyings.add("blurCoordinates[5]","vec2")),d.add(b.glsl`
    void main() {
      gl_Position = vec4(position, 0.0, 1.0);
      ${c.gridOptimization?b.glsl`
      vec4 cov = texture2D(coverageTex, uv0);
      if (cov.r == 0.0) {
        gl_Position = vec4(0.0);
      }
      blurCoordinate = vec3(gl_Position.xy * 0.5 + vec2(0.5), max(cov.g, cov.b));
      `:b.glsl`
      vec2 uv = position.xy * 0.5 + vec2(0.5);
      blurCoordinates[0] = uv;
      blurCoordinates[1] = uv + blurSize * 1.407333;
      blurCoordinates[2] = uv - blurSize * 1.407333;
      blurCoordinates[3] = uv + blurSize * 3.294215;
      blurCoordinates[4] = uv - blurSize * 3.294215;
          `}
    }`),a.fragment.uniforms.add("tex","sampler2D"),e.add(b.glsl`
    void main() {
      ${c.gridOptimization?b.glsl`
          vec2 uv = blurCoordinate.xy;
          vec4 center = texture2D(tex, uv);

          // do not blur if no pixel or all pixels in neighborhood are set
          if (blurCoordinate.z == 1.0) {
            gl_FragColor = center;
          } else {
            vec4 sum = vec4(0.0);
            sum += center * 0.204164;
            sum += texture2D(tex, uv + blurSize * 1.407333) * 0.304005;
            sum += texture2D(tex, uv - blurSize * 1.407333) * 0.304005;
            sum += texture2D(tex, uv + blurSize * 3.294215) * 0.093913;
            sum += texture2D(tex, uv - blurSize * 3.294215) * 0.093913;
            gl_FragColor = sum;
          }`:b.glsl`
          vec4 sum = vec4(0.0);
          sum += texture2D(tex, blurCoordinates[0]) * 0.204164;
          sum += texture2D(tex, blurCoordinates[1]) * 0.304005;
          sum += texture2D(tex, blurCoordinates[2]) * 0.304005;
          sum += texture2D(tex, blurCoordinates[3]) * 0.093913;
          sum += texture2D(tex, blurCoordinates[4]) * 0.093913;
          gl_FragColor = sum;`}
    }`));1===c.highlightStage&&(a.varyings.add("uv","vec2"),c.gridOptimization&&(a.attributes.add("uv0","vec2"),a.vertex.uniforms.add("coverageTex","sampler2D")),d.add(b.glsl`
      void main() {
        ${c.gridOptimization?b.glsl`
            vec4 cov = texture2D(coverageTex, uv0);
            // if no highlight pixel set in this block, hide block
            if (cov.r == 0.0) {
              gl_Position = vec4(0.0);
              return;
            }`:""}
        gl_Position = vec4(position, 0.0, 1.0);
        uv = position.xy * 0.5 + vec2(0.5);
      }`),a.fragment.uniforms.add("tex","sampler2D"),a.fragment.uniforms.add("origin","sampler2D"),a.fragment.uniforms.add("color","vec4"),a.fragment.uniforms.add("haloColor","vec4"),a.fragment.uniforms.add("outlineSize","float"),a.fragment.uniforms.add("blurSize","float"),a.fragment.uniforms.add("opacities","vec4"),e.add(b.glsl`
      void main() {
        // Read the highlight intensity from the blurred highlight image
        vec4 blurredHighlightValue = texture2D(tex, uv);
        float highlightIntensity = blurredHighlightValue.a;

        // Discard all pixels which are not affected by highlight
        if (highlightIntensity == 0.0) {
          discard;
        }

        vec4 origin_color = texture2D(origin, uv);

        float outlineIntensity;
        float fillIntensity;

        // if occluded
        if (blurredHighlightValue.g > blurredHighlightValue.b) {
          outlineIntensity = haloColor.w * opacities[1];
          fillIntensity = color.w * opacities[3];
        }
        // if unoccluded
        else {
          outlineIntensity = haloColor.w * opacities[0];
          fillIntensity = color.w * opacities[2];
        }

        float inner = 1.0 - outlineSize / 9.0;
        float outer = 1.0 - (outlineSize + blurSize) / 9.0;

        float outlineFactor = smoothstep(outer, inner, highlightIntensity);
        //float fillFactor = smoothstep(0.6, 0.72, highlightIntensity);
        float fillFactor = any(notEqual(origin_color, vec4(0.0, 0.0, 0.0, 0.0))) ? 1.0 : 0.0;
        float intensity = outlineIntensity * outlineFactor * (1.0 - fillFactor) + fillIntensity * fillFactor;

        // Blending equation: gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
        // I.e., color should not be premultiplied with alpha
        gl_FragColor = vec4(mix(haloColor.rgb, color.rgb, fillFactor), intensity);
      }`));return a}var k=Object.freeze({__proto__:null,build:g});f.HighlightCompositionShader=k;f.build=g});