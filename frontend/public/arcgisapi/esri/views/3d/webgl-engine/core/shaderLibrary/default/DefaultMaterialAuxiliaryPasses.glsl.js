// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../shaderModules/interfaces ../Transform.glsl ../Slice.glsl ../output/OutputHighlight.glsl ../shading/VisualVariables.glsl ../util/AlphaDiscard.glsl ../output/OutputDepth.glsl ../attributes/TextureCoordinateAttribute.glsl ../attributes/NormalAttribute.glsl ../attributes/VertexNormal.glsl".split(" "),function(m,c,d,e,n,f,g,p,h,q,r){m.DefaultMaterialAuxiliaryPasses=function(a,b){const k=a.vertex.code,l=a.fragment.code;if(1===b.output||3===b.output)a.include(d.Transform,{linearDepth:!0}),
a.include(h.TextureCoordinateAttribute,b),a.include(f.VisualVariables,b),a.include(p.OutputDepth,b),a.include(e.Slice,b),a.vertex.uniforms.add("nearFar","vec2"),a.varyings.add("depth","float"),b.hasColorTexture&&a.fragment.uniforms.add("tex","sampler2D"),k.add(c.glsl`
      void main(void) {
        vpos = calculateVPos();
        vpos = subtractOrigin(vpos);
        vpos = addVerticalOffset(vpos, localOrigin);
        gl_Position = transformPositionWithDepth(proj, view, vpos, nearFar, depth);
        forwardTextureCoordinates();
      }
    `),a.include(g.DiscardOrAdjustAlpha,b),l.add(c.glsl`
      void main(void) {
        discardBySlice(vpos);
        ${b.hasColorTexture?c.glsl`
        vec4 texColor = texture2D(tex, vuv0);
        discardOrAdjustAlpha(texColor);`:""}
        outputDepth(depth);
      }
    `);2===b.output&&(a.include(d.Transform,{linearDepth:!1}),a.include(q.NormalAttribute,b),a.include(r.VertexNormal,b),a.include(h.TextureCoordinateAttribute,b),a.include(f.VisualVariables,b),b.hasColorTexture&&a.fragment.uniforms.add("tex","sampler2D"),a.vertex.uniforms.add("viewNormal","mat4"),a.varyings.add("vPositionView","vec3"),k.add(c.glsl`
      void main(void) {
        vpos = calculateVPos();
        vpos = subtractOrigin(vpos);
        ${0===b.normalType?c.glsl`
        vNormalWorld = dpNormalView(vvLocalNormal(normalModel()));`:""}
        vpos = addVerticalOffset(vpos, localOrigin);
        gl_Position = transformPosition(proj, view, vpos);
        forwardTextureCoordinates();
      }
    `),a.include(e.Slice,b),a.include(g.DiscardOrAdjustAlpha,b),l.add(c.glsl`
      void main() {
        discardBySlice(vpos);
        ${b.hasColorTexture?c.glsl`
        vec4 texColor = texture2D(tex, vuv0);
        discardOrAdjustAlpha(texColor);`:""}

        ${3===b.normalType?c.glsl`
            vec3 normal = screenDerivativeNormal(vPositionView);`:c.glsl`
            vec3 normal = normalize(vNormalWorld);
            if (gl_FrontFacing == false) normal = -normal;`}
        gl_FragColor = vec4(vec3(0.5) + 0.5 * normal, 1.0);
      }
    `));4===b.output&&(a.include(d.Transform,{linearDepth:!1}),a.include(h.TextureCoordinateAttribute,b),a.include(f.VisualVariables,b),b.hasColorTexture&&a.fragment.uniforms.add("tex","sampler2D"),k.add(c.glsl`
      void main(void) {
        vpos = calculateVPos();
        vpos = subtractOrigin(vpos);
        vpos = addVerticalOffset(vpos, localOrigin);
        gl_Position = transformPosition(proj, view, vpos);
        forwardTextureCoordinates();
      }
    `),a.include(e.Slice,b),a.include(g.DiscardOrAdjustAlpha,b),a.include(n.OutputHighlight),l.add(c.glsl`
      void main() {
        discardBySlice(vpos);
        ${b.hasColorTexture?c.glsl`
        vec4 texColor = texture2D(tex, vuv0);
        discardOrAdjustAlpha(texColor);`:""}
        outputHighlight();
      }
    `))};Object.defineProperty(m,"__esModule",{value:!0})});