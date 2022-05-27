// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../../../chunks/vec3f64","../../shaderModules/interfaces","../../../lib/doublePrecisionUtils","../util/DoublePrecision.glsl"],function(f,g,c,l,m){function e(a,b){b.instanced&&b.instancedDoublePrecision&&(a.attributes.add("modelOriginHi","vec3"),a.attributes.add("modelOriginLo","vec3"),a.attributes.add("model","mat3"),a.attributes.add("modelNormal","mat3"));b.instancedDoublePrecision&&(a.vertex.include(m.DoublePrecision,b),a.vertex.uniforms.add("viewOriginHi","vec3"),a.vertex.uniforms.add("viewOriginLo",
"vec3"));const d=[c.glsl`
    vec3 calculateVPos() {
      ${b.instancedDoublePrecision?"return model * localPosition().xyz;":"return localPosition().xyz;"}
    }
    `,c.glsl`
    vec3 subtractOrigin(vec3 _pos) {
      ${b.instancedDoublePrecision?c.glsl`
          vec3 originDelta = dpAdd(viewOriginHi, viewOriginLo, -modelOriginHi, -modelOriginLo);
          return _pos - originDelta;`:"return vpos;"}
    }
    `,c.glsl`
    vec3 dpNormal(vec4 _normal) {
      ${b.instancedDoublePrecision?"return normalize(modelNormal * _normal.xyz);":"return normalize(_normal.xyz);"}
    }
    `,c.glsl`
    vec3 dpNormalView(vec4 _normal) {
      ${b.instancedDoublePrecision?"return normalize((viewNormal * vec4(modelNormal * _normal.xyz, 1.0)).xyz);":"return normalize((viewNormal * _normal).xyz);"}
    }
    `,b.vertexTangets?c.glsl`
    vec4 dpTransformVertexTangent(vec4 _tangent) {
      ${b.instancedDoublePrecision?"return vec4(modelNormal * _tangent.xyz, _tangent.w);":"return _tangent;"}

    }
    `:c.glsl``];a.vertex.code.add(d[0]);a.vertex.code.add(d[1]);a.vertex.code.add(d[2]);2===b.output&&a.vertex.code.add(d[3]);a.vertex.code.add(d[4])}(function(a){a.Uniforms=function(){};a.bindCustomOrigin=function(b,d){l.encodeDoubleArraySplit(d,h,k,3);b.setUniform3fv("viewOriginHi",h);b.setUniform3fv("viewOriginLo",k)}})(e||(e={}));const h=g.create(),k=g.create();f.InstancedDoublePrecision=e;Object.defineProperty(f,"__esModule",{value:!0})});