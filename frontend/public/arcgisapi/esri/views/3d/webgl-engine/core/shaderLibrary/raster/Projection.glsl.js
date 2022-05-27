// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(b,c){b.Projection=function(a){a.fragment.uniforms.add("u_transformGrid","sampler2D");a.fragment.uniforms.add("u_transformSpacing","vec2");a.fragment.uniforms.add("u_transformGridSize","vec2");a.fragment.uniforms.add("u_targetImageSize","vec2");a.fragment.code.add(c.glsl`
    vec2 projectPixelLocation(vec2 coords) {
      // pixel index in row/column, corresponds to upperleft corner, e.g. [100, 20]
      vec2 index_image = floor(coords * u_targetImageSize);

      // pixel's corresponding cell index in transform grid
      // each transform cell corresponds to 4 pixels: 6 coefficients from lowerleft triangle followed by 6 coefficients from upperright triangle
      vec2 oneTransformPixel = vec2(0.25 / u_transformGridSize.s, 1.0 / u_transformGridSize.t);
      vec2 index_transform = floor(index_image / u_transformSpacing) / u_transformGridSize;

      // correspoding position in transform grid cell, cell center coordinates
      vec2 pos = fract((index_image + vec2(0.5, 0.5)) / u_transformSpacing);
      vec2 srcLocation;
      // pixel's corresponding transform cell location, center cell coordinates
      vec2 transform_location = index_transform + oneTransformPixel * 0.5;

      // use lower triangle or upper triangle
      if (pos.s <= pos.t) {
        vec4 ll_abc = texture2D(u_transformGrid, vec2(transform_location.s, transform_location.t));
        vec4 ll_def = texture2D(u_transformGrid, vec2(transform_location.s + oneTransformPixel.s, transform_location.t));
        srcLocation.s = dot(ll_abc.rgb, vec3(pos, 1.0));
        srcLocation.t = dot(ll_def.rgb, vec3(pos, 1.0));
      } else {
        vec4 ur_abc = texture2D(u_transformGrid, vec2(transform_location.s + 2.0 * oneTransformPixel.s, transform_location.t));
        vec4 ur_def = texture2D(u_transformGrid, vec2(transform_location.s + 3.0 * oneTransformPixel.s, transform_location.t));
        srcLocation.s = dot(ur_abc.rgb, vec3(pos, 1.0));
        srcLocation.t = dot(ur_def.rgb, vec3(pos, 1.0));
      }
      return srcLocation;;
    }
  `)};Object.defineProperty(b,"__esModule",{value:!0})});