// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../../core/maybe","../../../../../chunks/vec3f64","../../../../../chunks/vec3","../shaderModules/interfaces"],function(h,m,f,n,e){function g(a,b){if(b.slicePlaneEnabled){a.extensions.add("GL_OES_standard_derivatives");b.sliceEnabledForVertexPrograms&&(a.vertex.uniforms.add("slicePlaneOrigin","vec3"),a.vertex.uniforms.add("slicePlaneBasis1","vec3"),a.vertex.uniforms.add("slicePlaneBasis2","vec3"));a.fragment.uniforms.add("slicePlaneOrigin","vec3");a.fragment.uniforms.add("slicePlaneBasis1",
"vec3");a.fragment.uniforms.add("slicePlaneBasis2","vec3");var d=e.glsl`
      struct SliceFactors {
        float front;
        float side0;
        float side1;
        float side2;
        float side3;
      };

      SliceFactors calculateSliceFactors(vec3 pos) {
        vec3 rel = pos - slicePlaneOrigin;

        vec3 slicePlaneNormal = -cross(slicePlaneBasis1, slicePlaneBasis2);
        float slicePlaneW = -dot(slicePlaneNormal, slicePlaneOrigin);

        float basis1Len2 = dot(slicePlaneBasis1, slicePlaneBasis1);
        float basis2Len2 = dot(slicePlaneBasis2, slicePlaneBasis2);

        float basis1Dot = dot(slicePlaneBasis1, rel);
        float basis2Dot = dot(slicePlaneBasis2, rel);

        return SliceFactors(
          dot(slicePlaneNormal, pos) + slicePlaneW,
          -basis1Dot - basis1Len2,
          basis1Dot - basis1Len2,
          -basis2Dot - basis2Len2,
          basis2Dot - basis2Len2
        );
      }

      bool sliceByFactors(SliceFactors factors) {
        return factors.front < 0.0
          && factors.side0 < 0.0
          && factors.side1 < 0.0
          && factors.side2 < 0.0
          && factors.side3 < 0.0;
      }

      bool sliceEnabled() {
        // a slicePlaneBasis1 vector of zero length is used to disable slicing in the shader during draped rendering.
        return dot(slicePlaneBasis1, slicePlaneBasis1) != 0.0;
      }

      bool sliceByPlane(vec3 pos) {
        return sliceEnabled() && sliceByFactors(calculateSliceFactors(pos));
      }

      #define rejectBySlice(_pos_) sliceByPlane(_pos_)
      #define discardBySlice(_pos_) { if (sliceByPlane(_pos_)) discard; }
    `,c=e.glsl`
      vec4 applySliceHighlight(vec4 color, vec3 pos) {
        SliceFactors factors = calculateSliceFactors(pos);

        if (sliceByFactors(factors)) {
          return color;
        }

        const float HIGHLIGHT_WIDTH = 1.0;
        const vec4 HIGHLIGHT_COLOR = vec4(0.0, 0.0, 0.0, 0.3);

        factors.front /= (2.0 * HIGHLIGHT_WIDTH) * fwidth(factors.front);
        factors.side0 /= (2.0 * HIGHLIGHT_WIDTH) * fwidth(factors.side0);
        factors.side1 /= (2.0 * HIGHLIGHT_WIDTH) * fwidth(factors.side1);
        factors.side2 /= (2.0 * HIGHLIGHT_WIDTH) * fwidth(factors.side2);
        factors.side3 /= (2.0 * HIGHLIGHT_WIDTH) * fwidth(factors.side3);

        float highlightFactor = (1.0 - step(0.5, factors.front))
          * (1.0 - step(0.5, factors.side0))
          * (1.0 - step(0.5, factors.side1))
          * (1.0 - step(0.5, factors.side2))
          * (1.0 - step(0.5, factors.side3));

        return mix(color, vec4(HIGHLIGHT_COLOR.rgb, color.a), highlightFactor * HIGHLIGHT_COLOR.a);
      }
    `;c=b.sliceHighlightDisabled?e.glsl`#define highlightSlice(_color_, _pos_) (_color_)`:e.glsl`
        ${c}
        #define highlightSlice(_color_, _pos_) (sliceEnabled() ? applySliceHighlight(_color_, _pos_) : (_color_))
      `;b.sliceEnabledForVertexPrograms&&a.vertex.code.add(d);a.fragment.code.add(d);a.fragment.code.add(c)}else d=e.glsl`
      #define rejectBySlice(_pos_) false
      #define discardBySlice(_pos_) {}
      #define highlightSlice(_color_, _pos_) (_color_)
    `,b.sliceEnabledForVertexPrograms&&a.vertex.code.add(d),a.fragment.code.add(d)}(function(a){a.bindUniformsWithOrigin=function(b,d,c){a.bindUniforms(b,d,c.slicePlane,c.origin)};a.bindUniforms=function(b,d,c,k){d.slicePlaneEnabled&&(m.isSome(c)?(k?(n.subtract(l,c.origin,k),b.setUniform3fv("slicePlaneOrigin",l)):b.setUniform3fv("slicePlaneOrigin",c.origin),b.setUniform3fv("slicePlaneBasis1",c.basis1),b.setUniform3fv("slicePlaneBasis2",c.basis2)):(b.setUniform3fv("slicePlaneBasis1",f.ZEROS),b.setUniform3fv("slicePlaneBasis2",
f.ZEROS),b.setUniform3fv("slicePlaneOrigin",f.ZEROS)))}})(g||(g={}));const l=f.create();h.Slice=g;Object.defineProperty(h,"__esModule",{value:!0})});