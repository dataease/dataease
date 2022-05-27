// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../views/3d/webgl-engine/core/shaderModules/interfaces ../views/3d/webgl-engine/core/shaderModules/ShaderBuilder ../views/3d/webgl-engine/core/shaderLibrary/util/ColorConversion.glsl ../views/3d/webgl-engine/core/shaderLibrary/raster/BasicGrid.glsl ../views/3d/webgl-engine/core/shaderLibrary/raster/Colormap.glsl ../views/3d/webgl-engine/core/shaderLibrary/raster/Common.glsl".split(" "),function(d,c,f,g,h,k,l){function e(b){const a=new f.ShaderBuilder;a.include(h.BasicGrid);a.include(l.Common);
a.include(k.Colormap);0===b.output?(b=b.applyColormap,a.fragment.uniforms.add("u_bandCount","int"),a.fragment.uniforms.add("u_minCutOff","float",3),a.fragment.uniforms.add("u_maxCutOff","float",3),a.fragment.uniforms.add("u_factor","float",3),a.fragment.uniforms.add("u_minOutput","float"),a.fragment.uniforms.add("u_maxOutput","float"),a.fragment.uniforms.add("u_useGamma","bool"),a.fragment.uniforms.add("u_gamma","float",3),a.fragment.uniforms.add("u_gammaCorrection","float",3),a.fragment.code.add(c.glsl`
      float stretchOneValue(float val, float minCutOff, float maxCutOff, float minOutput, float maxOutput, float factor, bool useGamma, float gamma, float gammaCorrection) {
        // clamp values
        if (val >= maxCutOff) {
          return maxOutput;
        } else if (val <= minCutOff) {
          return minOutput;
        }

        // stretch a single value based on whether to use gamma
        float stretchedVal;
        if (useGamma) {
          float tempf = 1.0;
          float outRange = maxOutput - minOutput;
          float relativeVal = (val - minCutOff) / (maxCutOff - minCutOff);
          if (gamma > 1.0) {
            tempf -= pow(1.0 / outRange, relativeVal * gammaCorrection);
          }
          stretchedVal = (tempf * outRange * pow(relativeVal, 1.0 / gamma) + minOutput) / 255.0;
        } else {
          stretchedVal = minOutput + (val - minCutOff) * factor;
        }
        return stretchedVal;
      }
    `),b=b?c.glsl`gl_FragColor = colormap(vec4(grayVal, grayVal, grayVal, currentPixel.a), !u_useGamma);`:c.glsl`gl_FragColor = vec4(grayVal, grayVal, grayVal, 1.0) * currentPixel.a * u_opacity;`,a.fragment.code.add(c.glsl`
      void main() {
        vec2 pixelLocation = getPixelLocation(v_texcoord);
        if (isOutside(pixelLocation)) {
          gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
          return;
        }

        vec4 currentPixel = getPixel(pixelLocation);
        if (currentPixel.a == 0.0) {
          gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
          return;
        }

        if (u_bandCount == 1) {
          float grayVal = stretchOneValue(currentPixel.r, u_minCutOff[0], u_maxCutOff[0], u_minOutput, u_maxOutput, u_factor[0], u_useGamma, u_gamma[0], u_gammaCorrection[0]);
          ${b}
        } else {
          float redVal = stretchOneValue(currentPixel.r, u_minCutOff[0], u_maxCutOff[0], u_minOutput, u_maxOutput, u_factor[0], u_useGamma, u_gamma[0], u_gammaCorrection[0]);
          float greenVal = stretchOneValue(currentPixel.g, u_minCutOff[1], u_maxCutOff[1], u_minOutput, u_maxOutput, u_factor[1], u_useGamma, u_gamma[1], u_gammaCorrection[1]);
          float blueVal = stretchOneValue(currentPixel.b, u_minCutOff[2], u_maxCutOff[2], u_minOutput, u_maxOutput, u_factor[2], u_useGamma, u_gamma[2], u_gammaCorrection[2]);
          gl_FragColor = vec4(redVal, greenVal, blueVal, 1.0) * currentPixel.a * u_opacity;
        }
      }
    `)):1===b.output?a.fragment.code.add(c.glsl`
      void main() {
        // get pixel location
        vec2 pixelLocation = getPixelLocation(v_texcoord);
        if (isOutside(pixelLocation)) {
          gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
          return;
        }

        vec4 currentPixel = getPixel(pixelLocation);
        // apply colormap we use float texture here
        gl_FragColor = colormap(currentPixel, true);
      }
    `):2===b.output&&(b=b.applyColormap,a.fragment.uniforms.add("u_hillshadeType","int"),a.fragment.uniforms.add("u_sinZcosAs","float",6),a.fragment.uniforms.add("u_sinZsinAs","float",6),a.fragment.uniforms.add("u_cosZs","float",6),a.fragment.uniforms.add("u_weights","float",6),a.fragment.uniforms.add("u_factor","vec2"),a.fragment.uniforms.add("u_applyColormap","bool"),a.fragment.uniforms.add("u_minValue","float"),a.fragment.uniforms.add("u_maxValue","float"),a.fragment.uniforms.add("u_srcImageSize",
"vec2"),a.fragment.include(g.ColorConversion),a.fragment.code.add(c.glsl`
  vec4 overlay(float val, float minValue, float maxValue, float hillshade, float alpha) {
    val = clamp((val - minValue) / (maxValue - minValue), 0.0, 1.0);
    vec4 rgb = colormap(vec4(val, val, val, 1.0), false);
    vec3 hsv = rgb2hsv(rgb.xyz);
    hsv.z = hillshade;
    return vec4(hsv2rgb(hsv) * alpha, alpha);
  }
`),a.fragment.code.add(c.glsl`
    float getNeighborHoodAlpha(float a, float b, float c, float d, float e, float f, float g, float h, float i){
      if (a == 0.0 || a == 0.0 || a==0.0 || a == 0.0 || a == 0.0 || a==0.0 || a == 0.0 || a == 0.0 || a==0.0) {
        return 0.0;
      }  else {
        return e;
      }
    }
  `),b=b?c.glsl`gl_FragColor = overlay(ve.r, u_minValue, u_maxValue, hillshade, alpha);`:c.glsl`
       hillshade *= alpha;
       gl_FragColor = vec4(hillshade, hillshade, hillshade, alpha);
       `,a.fragment.code.add(c.glsl`
    void main() {
      vec2 pixelLocation = getPixelLocation(v_texcoord);
      if (isOutside(pixelLocation)) {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
        return;
      }

      vec4 currentPixel = getPixel(pixelLocation);
      if (currentPixel.a == 0.0) {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
        return;
      }

      //mirror edge pixels
      vec2 axy = vec2(-1.0, -1.0);
      vec2 bxy = vec2(0.0, -1.0);
      vec2 cxy = vec2(1.0, -1.0);
      vec2 dxy = vec2(-1.0, 0.0);
      vec2 fxy = vec2(1.0, 0.0);
      vec2 gxy = vec2(-1.0, 1.0);
      vec2 hxy = vec2(0.0, 1.0);
      vec2 ixy = vec2(1.0, 1.0);
      vec2 onePixel = 1.0 / u_srcImageSize;
      if (pixelLocation.s < onePixel.s) {
        axy[0] = 1.0;
        dxy[0] = 1.0;
        gxy[0] = 1.0;
      }
      if (pixelLocation.t < onePixel.t) {
        axy[1] = 1.0;
        bxy[1] = 1.0;
        cxy[1] = 1.0;
      }
      if (pixelLocation.s > 1.0 - onePixel.s) {
        cxy[0] = -1.0;
        fxy[0] = -1.0;
        ixy[0] = -1.0;
      }
      if (pixelLocation.t > 1.0 - onePixel.t) {
        gxy[1] = -1.0;
        hxy[1] = -1.0;
        ixy[1] = -1.0;
      }

      // calculate hillshade
      vec4 va = texture2D(u_image, pixelLocation + onePixel * axy);
      vec4 vb = texture2D(u_image, pixelLocation + onePixel * bxy);
      vec4 vc = texture2D(u_image, pixelLocation + onePixel * cxy);
      vec4 vd = texture2D(u_image, pixelLocation + onePixel * dxy);
      vec4 ve = texture2D(u_image, pixelLocation);
      vec4 vf = texture2D(u_image, pixelLocation + onePixel * fxy);
      vec4 vg = texture2D(u_image, pixelLocation + onePixel * gxy);
      vec4 vh = texture2D(u_image, pixelLocation + onePixel * hxy);
      vec4 vi = texture2D(u_image, pixelLocation + onePixel * ixy);

      // calculate the rate of z change along the x, y, and diagonal direction
      float dzx = (vc + 2.0 * vf + vi - va - 2.0 * vd - vg).r * u_factor.s;
      float dzy = (vg + 2.0 * vh + vi - va - 2.0 * vb - vc).r * u_factor.t;
      float dzd = sqrt(1.0 + dzx * dzx + dzy * dzy);
      float hillshade = 0.0;

      // traditional single light source
      if (u_hillshadeType == 0){
        float cosDelta = u_sinZsinAs[0] * dzy - u_sinZcosAs[0] * dzx;
        float z = (u_cosZs[0] + cosDelta) / dzd;
        if (z < 0.0)  z = 0.0;
        hillshade = z;
      } else {
        // multi-directional with 6 light sources
        for (int k = 0; k < 6; k++) {
        float cosDelta = u_sinZsinAs[k] * dzy - u_sinZcosAs[k] * dzx;
        float z = (u_cosZs[k] + cosDelta) / dzd;
        if (z < 0.0) z = 0.0;
        hillshade = hillshade + z * u_weights[k];
        if (k == 5) break;
        }
      }

      // set color
      float alpha = getNeighborHoodAlpha(va.a, vb.a, vc.a, vd.a, ve.a, vf.a, vg.a, vh.a, vi.a);
      alpha *= u_opacity;
      ${b}
    }
  `));return a}var m=Object.freeze({__proto__:null,build:e});d.RasterColorizerShader=m;d.build=e});