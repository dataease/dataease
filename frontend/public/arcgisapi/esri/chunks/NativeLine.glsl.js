// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../views/3d/webgl-engine/core/shaderModules/interfaces ../views/3d/webgl-engine/core/shaderLibrary/Transform.glsl ../views/3d/webgl-engine/core/shaderModules/ShaderBuilder ../views/3d/webgl-engine/core/shaderLibrary/Slice.glsl ../views/3d/webgl-engine/core/shaderLibrary/output/OutputHighlight.glsl ../views/3d/webgl-engine/core/shaderLibrary/util/AlphaDiscard.glsl ../views/3d/webgl-engine/core/shaderLibrary/attributes/VertexColor.glsl ../views/3d/webgl-engine/core/shaderLibrary/shading/LineStipple.glsl".split(" "),
function(d,c,f,g,h,k,l,m,n){function e(b){const a=new g.ShaderBuilder;a.include(f.Transform,{linearDepth:!1});a.include(m.VertexColor,b);a.include(n.LineStipple,b);a.vertex.uniforms.add("proj","mat4").add("view","mat4");a.attributes.add("position","vec3");a.varyings.add("vpos","vec3");a.vertex.code.add(c.glsl`
    void main(void) {
      vpos = position;
      forwardNormalizedVertexColor();
      gl_Position = transformPosition(proj, view, vpos);
  `);b.stippleEnabled&&(a.attributes.add("auxpos1","vec3"),a.vertex.uniforms.add("ndcToPixel","vec2"),a.vertex.code.add(c.glsl`
    vec4 vpos2 = transformPosition(proj, view, auxpos1);
    float lineSegmentPixelSize = length((vpos2.xy / vpos2.w - gl_Position.xy / gl_Position.w) * ndcToPixel);

    stipplePatternUv = lineSegmentPixelSize * stipplePatternPixelSizeInv;
    ${b.stippleIntegerRepeatsEnabled?"stipplePatternUv \x3d floor(stipplePatternUv + 0.5);":""}

    // Cancel out perspective correct interpolation because we want this length the really represent
    // the screen distance
    stipplePatternUv *= gl_Position.w;
    `));a.vertex.code.add(c.glsl`
  }
  `);4===b.output&&a.include(k.OutputHighlight);a.include(h.Slice,b);a.fragment.uniforms.add("constantColor","vec4").add("alphaCoverage","float");a.fragment.code.add(c.glsl`
  void main() {
    discardBySlice(vpos);

    vec4 color = ${b.attributeColor?"vColor":"constantColor"};

    float stippleAlpha = getStippleAlpha();
    discardByStippleAlpha(stippleAlpha, STIPPLE_ALPHA_COLOR_DISCARD);

    vec4 finalColor = blendStipple(vec4(color.rgb, color.a * alphaCoverage), stippleAlpha);

    if (finalColor.a < ${c.glsl.float(l.symbolAlphaCutoff)}) {
      discard;
    }

    ${0===b.output?c.glsl`gl_FragColor = highlightSlice(finalColor, vpos);`:""}
    ${4===b.output?c.glsl`outputHighlight();`:""}
  }
  `);return a}var p=Object.freeze({__proto__:null,build:e});d.NativeLineShader=p;d.build=e});