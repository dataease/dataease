// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../views/3d/webgl-engine/core/shaderModules/interfaces ../views/3d/webgl-engine/core/shaderLibrary/Transform.glsl ../views/3d/webgl-engine/core/shaderModules/ShaderBuilder ../views/3d/webgl-engine/core/shaderLibrary/util/ColorConversion.glsl ../views/3d/webgl-engine/core/shaderLibrary/Slice.glsl ../views/3d/webgl-engine/core/shaderLibrary/output/OutputHighlight.glsl ../views/3d/webgl-engine/core/shaderLibrary/util/AlphaDiscard.glsl ../views/3d/webgl-engine/core/shaderLibrary/attributes/VertexColor.glsl ../views/3d/webgl-engine/core/shaderLibrary/output/OutputDepth.glsl".split(" "),
function(e,c,g,h,k,l,m,n,p,q){function f(b){const a=new h.ShaderBuilder,d=1===b.output;a.include(g.Transform,{linearDepth:d});a.include(p.VertexColor,b);a.vertex.uniforms.add("proj","mat4").add("view","mat4");a.attributes.add("position","vec3");a.varyings.add("vpos","vec3");d&&(a.include(q.OutputDepth,b),a.vertex.uniforms.add("nearFar","vec2"),a.varyings.add("linearDepth","float"));a.vertex.code.add(c.glsl`
    void main(void) {
      vpos = position;
      forwardNormalizedVertexColor();
      gl_Position = ${d?c.glsl`transformPositionWithDepth(proj, view, vpos, nearFar, linearDepth);`:c.glsl`transformPosition(proj, view, vpos);`}
    }
  `);a.include(l.Slice,b);a.fragment.include(k.ColorConversion);a.fragment.uniforms.add("eColor","vec4");4===b.output&&a.include(m.OutputHighlight);a.fragment.code.add(c.glsl`
  void main() {
    discardBySlice(vpos);
    vec4 color = ${b.attributeColor?"vColor * eColor;":"eColor;"}

    if (color.a < ${c.glsl.float(n.symbolAlphaCutoff)}) {
      discard;
    }

    ${7===b.output?c.glsl`gl_FragColor = vec4(color.a);`:""}

    ${0===b.output?c.glsl`gl_FragColor = highlightSlice(color, vpos); ${b.OITEnabled?"gl_FragColor \x3d premultiplyAlpha(gl_FragColor);":""}`:""}
    ${4===b.output?c.glsl`outputHighlight();`:""};
    ${1===b.output?c.glsl`outputDepth(linearDepth);`:""};
  }
  `);return a}var r=Object.freeze({__proto__:null,build:f});e.ColorMaterialShader=r;e.build=f});