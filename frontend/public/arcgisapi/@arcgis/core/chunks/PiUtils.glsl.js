/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{L as e,i as t}from"./Logger.js";import{l as i,h as r,d as s,c as a,k as n}from"./mathUtils2.js";import{c as o,Z as c}from"./vec3f64.js";import{f as l,a as f,s as d,p as u,e as v,n as m,c as h}from"./vec3.js";import{t as p}from"./mat4.js";import{b as g,h as _,a as b,e as x}from"./BufferView.js";import{c as S,s as P,g as O}from"./aaBoundingBox.js";import{s as y,c as F,a as A}from"./isWebGL2Context.js";import{c as z}from"./mat4f32.js";import{I as C}from"./geometryUtils.js";import{a as M,V as I}from"./Util.js";import{i as L,I as w}from"./Object3D.js";function D(e,...t){let i="";for(let r=0;r<t.length;r++)i+=e[r]+t[r];return i+=e[e.length-1],i}function T(e,t){t.linearDepth?e.vertex.code.add(D`
    vec4 transformPositionWithDepth(mat4 proj, mat4 view, vec3 pos, vec2 nearFar, out float depth) {
      vec4 eye = view * vec4(pos, 1.0);
      depth = (-eye.z - nearFar[0]) / (nearFar[1] - nearFar[0]) ;
      return proj * eye;
    }
    `):e.vertex.code.add(D`
    vec4 transformPosition(mat4 proj, mat4 view, vec3 pos) {
      // Make sure the order of operations is the same as in transformPositionWithDepth.
      return proj * (view * vec4(pos, 1.0));
    }
    `)}!function(e){e.int=function(e){return Math.round(e).toString()},e.float=function(e){return e.toPrecision(8)}}(D||(D={}));const B=e.getLogger("esri.views.3d.webgl-engine.core.shaderModules.shaderBuilder");class V{constructor(){this.includedModules=new Map}include(e,t){this.includedModules.has(e)?this.includedModules.get(e)!==t&&B.error("Trying to include shader module multiple times with different sets of options."):(this.includedModules.set(e,t),e(this.builder,t))}}class U extends V{constructor(){super(...arguments),this.vertex=new H,this.fragment=new H,this.attributes=new E,this.varyings=new G,this.extensions=new k,this.defines=new j}get builder(){return this}generateSource(e){const t=this.extensions.generateSource(e),i=this.attributes.generateSource(e),r=this.varyings.generateSource(),s="vertex"===e?this.vertex:this.fragment,a=s.uniforms.generateSource(),n=s.code.generateSource(),o="vertex"===e?W:$,c=this.defines.generateSource().concat(s.defines.generateSource());return`\n${t.join("\n")}\n\n${c.join("\n")}\n\n${o}\n\n${a.join("\n")}\n\n${i.join("\n")}\n\n${r.join("\n")}\n\n${n.join("\n")}`}}class R{constructor(){this._entries=new Array,this._set=new Set}add(e,t,i){const r=`${e}_${t}_${i}`;return this._set.has(r)||(this._entries.push([e,t,i]),this._set.add(r)),this}generateSource(){return this._entries.map((e=>{return`uniform ${e[1]} ${e[0]}${t=e[2],t?`[${t}]`:""};`;var t}))}}class N{constructor(){this._entries=new Array}add(e){this._entries.push(e)}generateSource(){return this._entries}}class H extends V{constructor(){super(...arguments),this.uniforms=new R,this.code=new N,this.defines=new j}get builder(){return this}}class E{constructor(){this._entries=new Array}add(e,t){this._entries.push([e,t])}generateSource(e){return"fragment"===e?[]:this._entries.map((e=>`attribute ${e[1]} ${e[0]};`))}}class G{constructor(){this._entries=new Array}add(e,t){this._entries.push([e,t])}generateSource(){return this._entries.map((e=>`varying ${e[1]} ${e[0]};`))}}class k{constructor(){this._entries=new Set}add(e){this._entries.add(e)}generateSource(e){const t="vertex"===e?k.ALLOWLIST_VERTEX:k.ALLOWLIST_FRAGMENT;return Array.from(this._entries).filter((e=>t.includes(e))).map((e=>`#extension ${e} : enable`))}}k.ALLOWLIST_FRAGMENT=["GL_EXT_shader_texture_lod","GL_OES_standard_derivatives"],k.ALLOWLIST_VERTEX=[];class j{constructor(){this._entries=new Map}addInt(e,t){const i=t%1==0?t.toFixed(0):t.toString();this._entries.set(e,i)}addFloat(e,t){const i=t%1==0?t.toFixed(1):t.toString();this._entries.set(e,i)}generateSource(){return Array.from(this._entries,(([e,t])=>`#define ${e} ${t}`))}}const $="#ifdef GL_FRAGMENT_PRECISION_HIGH\n  precision highp float;\n  precision highp sampler2D;\n#else\n  precision mediump float;\n  precision mediump sampler2D;\n#endif",W="precision highp float;\nprecision highp sampler2D;";class q{constructor(e,t){this._module=e,this._loadModule=t}get(){return this._module}async reload(){return this._module=await this._loadModule(),this._module}}class Y{constructor(e,t){t&&(this._config=t.snapshot()),this._program=this.initializeProgram(e),e.commonUniformStore&&(this._commonUniformStore=e.commonUniformStore,this._commonUniformStore.subscribeProgram(this._program)),this._pipeline=this.initializePipeline(e)}dispose(){this._program&&(this._commonUniformStore&&this._commonUniformStore.unsubscribeProgram(this._program),this._program.dispose(),this._program=null)}reload(e){this._program&&(this._commonUniformStore&&this._commonUniformStore.unsubscribeProgram(this._program),this._program.dispose()),this._program=this.initializeProgram(e),this._commonUniformStore&&this._commonUniformStore.subscribeProgram(this._program)}get program(){return this._program}get pipeline(){return this._pipeline}get key(){return this._config.key}get configuration(){return this._config}bindPass(e,t,i){}bindMaterial(e,t,i){}bindDraw(e,t,i){}bindPipelineState(e){e.setPipelineState(this.pipeline)}ensureAttributeLocations(e){this.program.assertCompatibleVertexAttributeLocations(e)}get primitiveType(){return 4}}class X{constructor(){this._key="",this._keyDirty=!1,this._parameterBits=this._parameterBits.map((()=>0))}get key(){return this._keyDirty&&(this._keyDirty=!1,this._key=String.fromCharCode.apply(String,this._parameterBits)),this._key}snapshot(){const e=this._parameterNames,t={key:this.key};for(const i of e)t[i]=this[i];return t}}function K(e={}){return(t,r)=>{var s,a;t._parameterNames=null!=(s=t._parameterNames)?s:[],t._parameterNames.push(r);const n=t._parameterNames.length-1,o=e.count||2,c=Math.ceil(i(o)),l=null!=(a=t._parameterBits)?a:[0];let f=0;for(;l[f]+c>16;)f++,f>=l.length&&l.push(0);t._parameterBits=l;const d=l[f],u=(1<<c)-1<<d;l[f]+=c,Object.defineProperty(t,r,{get(){return this[n]},set(e){if(this[n]!==e&&(this[n]=e,this._keyDirty=!0,this._parameterBits[f]=this._parameterBits[f]&~u|+e<<d&u,"number"!=typeof e&&"boolean"!=typeof e))throw"Configuration values must be booleans or numbers!"}})}}const Z={position:0,normal:1,uv0:2,color:3,size:4,tangent:4,uvMapSpace:4,auxpos1:5,symbolColor:5,auxpos2:6,featureAttribute:6,instanceFeatureAttribute:6,instanceColor:7,bound1:5,bound2:6,bound3:7,model:8,modelNormal:12,modelOriginHi:11,modelOriginLo:15};var J;!function(e){function t(e,t,i){p(Q,i,t),e.setUniform3fv("localOrigin",t),e.setUniformMatrix4fv("view",Q)}e.bindCamPosition=function(e,t,i){e.setUniform3f("camPos",i[3]-t[0],i[7]-t[1],i[11]-t[2])},e.bindProjectionMatrix=function(e,t){e.setUniformMatrix4fv("proj",t)},e.bindNearFar=function(e,t){e.setUniform2fv("nearFar",t)},e.bindViewCustomOrigin=t,e.bindView=function(e,i){t(e,i.origin,i.camera.viewMatrix)},e.bindViewport=function(e,t){e.setUniform4fv("viewport",t.camera.fullViewport)}}(J||(J={}));const Q=z();let ee=1,te=null;const ie=new Uint32Array([0]);function re(e){if(1===e)return ie;if(e>ee||null==te){for(;e>ee;)ee*=2;te=new Uint32Array(ee);for(let e=0;e<ee;e++)te[e]=e}return new Uint32Array(te.buffer,0,e)}function se(e,t,i){if(!e)return!1;const{strideIdx:r,offsetIdx:s,data:a}=e;l(i,0,0,0),l(ue,0,0,0);let n=0,o=0;for(let e=0;e<t.length-2;e+=3){const c=t[e+0]*r+s,u=t[e+1]*r+s,v=t[e+2]*r+s;l(le,a[c+0],a[c+1],a[c+2]),l(fe,a[u+0],a[u+1],a[u+2]),l(de,a[v+0],a[v+1],a[v+2]);const m=C(le,fe,de);m?(f(le,le,fe),f(le,le,de),d(le,le,1/3*m),f(i,i,le),n+=m):(f(ue,ue,le),f(ue,ue,fe),f(ue,ue,de),o+=3)}return(0!==o||0!==n)&&(0!==n?(d(i,i,1/n),!0):0!==o&&(d(i,ue,1/o),!0))}function ae(e,t,i){if(!e||!t)return!1;const{strideIdx:r,offsetIdx:s,data:a}=e;l(i,0,0,0);let n=-1,o=0;for(let e=0;e<t.length;e++){const c=t[e]*r+s;n!==c&&(i[0]+=a[c+0],i[1]+=a[c+1],i[2]+=a[c+2],o++),n=c}return o>1&&d(i,i,1/o),o>0}function ne(e,t,i,r){if(!e)return!1;const{strideIdx:s,offsetIdx:a,data:n}=e;l(r,0,0,0),l(ue,0,0,0);let o=0,c=0;const v=t?t.length-1:n.length/s-1,m=v+(i?2:0);for(let e=0;e<m;e+=2){const i=e<v?e:v,l=e<v?e+1:0,m=(t?t[i]:i)*s+a,h=(t?t[l]:l)*s+a;le[0]=n[m+0],le[1]=n[m+1],le[2]=n[m+2],fe[0]=n[h+0],fe[1]=n[h+1],fe[2]=n[h+2],d(le,f(le,le,fe),.5);const p=u(le,fe);p>0?(f(r,r,d(le,le,p)),o+=p):(f(ue,ue,le),c++)}return 0!==o?(d(r,r,1/o),!0):0!==c&&(d(r,ue,1/c),!0)}let oe=0;function ce(){return oe++}const le=o(),fe=o(),de=o(),ue=o();function ve(e){e.code.add(D`
    vec4 premultiplyAlpha(vec4 v) {
      return vec4(v.rgb * v.a, v.a);
    }

    // Note: the min in the last line has been added to fix an instability in chrome.
    // See https://devtopia.esri.com/WebGIS/arcgis-js-api/issues/23911
    // With proper floating point handling, the value could never be >1.
    vec3 rgb2hsv(vec3 c) {
      vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
      vec4 p = c.g < c.b ? vec4(c.bg, K.wz) : vec4(c.gb, K.xy);
      vec4 q = c.r < p.x ? vec4(p.xyw, c.r) : vec4(c.r, p.yzx);

      float d = q.x - min(q.w, q.y);
      float e = 1.0e-10;
      return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), min(d / (q.x + e), 1.0), q.x);
    }

    vec3 hsv2rgb(vec3 c) {
      vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
      vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
      return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
    }

    float rgb2v(vec3 c) {
      return max(c.x, max(c.y, c.z));
    }
  `)}function me(e,t){return new Se(e,Pe,t)}function he(e,t){const{curvatureDependent:i,scaleStart:r,scaleFallOffRange:s}=Pe;return new Se(e,{curvatureDependent:{min:{curvature:i.min.curvature,tiltAngle:i.min.tiltAngle,scaleFallOffFactor:Oe.curvatureDependent.min.scaleFallOffFactor},max:{curvature:i.max.curvature,tiltAngle:i.max.tiltAngle,scaleFallOffFactor:Oe.curvatureDependent.max.scaleFallOffFactor}},scaleStart:r,scaleFallOffRange:s,minPixelSize:Oe.minPixelSize},t)}function pe(e,t,i){const r=i.parameters,s=i.paddingPixelsOverride;return ye.scale=Math.min(r.divisor/(t-r.offset),1),ye.factor=function(e){return Math.abs(e*e*e)}(e),ye.minPixelSize=r.minPixelSize,ye.paddingPixels=s,ye}function ge(e,t){return 0===e?t.minPixelSize:t.minPixelSize*(1+2*t.paddingPixels/e)}function _e(e,t){return Math.max(r(e*t.scale,e,t.factor),ge(e,t))}function be(e,t,i,r){r.scale=function(e,t,i){const r=pe(e,t,i);return r.minPixelSize=0,r.paddingPixels=0,_e(1,r)}(e,t,i),r.factor=0,r.minPixelSize=i.parameters.minPixelSize,r.paddingPixels=i.paddingPixelsOverride}function xe(e,t,i=[0,0]){const r=Math.min(Math.max(t.scale,ge(e[1],t)/e[1]),1);return i[0]=e[0]*r,i[1]=e[1]*r,i}class Se{constructor(e,t,i,r={camera:{distance:0,fovY:0},divisor:0,offset:0,minPixelSize:0,paddingPixels:0},s){this.viewingMode=e,this.description=t,this.ellipsoidRadius=i,this.parameters=r,this._paddingPixelsOverride=s,2===this.viewingMode?(this.coverageCompensation=this.surfaceCoverageCompensationLocal,this.calculateCurvatureDependentParameters=this.calculateCurvatureDependentParametersLocal):(this.coverageCompensation=this.surfaceCoverageCompensationGlobal,this.calculateCurvatureDependentParameters=this.calculateCurvatureDependentParametersGlobal)}get paddingPixelsOverride(){return this._paddingPixelsOverride||this.parameters.paddingPixels}update(e){return(!this.parameters||this.parameters.camera.fovY!==e.fovY||this.parameters.camera.distance!==e.distance)&&(this.calculateParameters(e,this.ellipsoidRadius,this.parameters),!0)}overridePadding(e){return e!==this.paddingPixelsOverride?new Se(this.viewingMode,this.description,this.ellipsoidRadius,this.parameters,e):this}calculateParameters(e,t,i){const{scaleStart:r,scaleFallOffRange:s,minPixelSize:a}=this.description,{fovY:n,distance:o}=e,c=this.calculateCurvatureDependentParameters(e,t),l=this.coverageCompensation(e,t,c),{tiltAngle:f,scaleFallOffFactor:d}=c,u=Math.sin(f)*o,v=.5*Math.PI-f-n*(.5-r*l),m=u/Math.cos(v),h=v+n*s*l,p=(m-d*(u/Math.cos(h)))/(1-d);return i.camera.fovY=e.fovY,i.camera.distance=e.distance,i.offset=p,i.divisor=m-p,i.minPixelSize=a,i}calculateCurvatureDependentParametersLocal(e,t,i=Fe){return i.tiltAngle=this.description.curvatureDependent.min.tiltAngle,i.scaleFallOffFactor=this.description.curvatureDependent.min.scaleFallOffFactor,i}calculateCurvatureDependentParametersGlobal(e,t,i=Fe){const s=this.description.curvatureDependent,n=1+e.distance/t,o=Math.sqrt(n*n-1),[c,l]=[s.min.curvature,s.max.curvature],f=a((o-c)/(l-c),0,1),[d,u]=[s.min,s.max];return i.tiltAngle=r(d.tiltAngle,u.tiltAngle,f),i.scaleFallOffFactor=r(d.scaleFallOffFactor,u.scaleFallOffFactor,f),i}surfaceCoverageCompensationLocal(e,t,i){return(e.fovY-i.tiltAngle)/e.fovY}surfaceCoverageCompensationGlobal(e,t,i){const r=t*t,s=i.tiltAngle+.5*Math.PI,{fovY:a,distance:n}=e,o=n*n+r-2*Math.cos(s)*n*t,c=Math.sqrt(o),l=Math.sqrt(o-r);return(Math.acos(l/c)-Math.asin(t/(c/Math.sin(s)))+.5*a)/a}}const Pe={curvatureDependent:{min:{curvature:s(10),tiltAngle:s(12),scaleFallOffFactor:.5},max:{curvature:s(70),tiltAngle:s(40),scaleFallOffFactor:.8}},scaleStart:.3,scaleFallOffRange:.65,minPixelSize:0},Oe={curvatureDependent:{min:{scaleFallOffFactor:.7},max:{scaleFallOffFactor:.95}},minPixelSize:14};const ye={scale:0,factor:0,minPixelSize:0,paddingPixels:0},Fe={tiltAngle:0,scaleFallOffFactor:0},Ae=e=>class extends e{constructor(){super(...arguments),this._isDisposed=!1}dispose(){for(const t of null!=(e=this._managedDisposables)?e:[]){var e;const i=this[t];this[t]=null,i&&"function"==typeof i.dispose&&i.dispose()}this._isDisposed=!0}get isDisposed(){return this._isDisposed}};class ze extends(Ae(class{})){}function Ce(){return(e,t)=>{var i,r;e.hasOwnProperty("_managedDisposables")||(e._managedDisposables=null!=(i=null==(r=e._managedDisposables)?void 0:r.slice())?i:[]);e._managedDisposables.unshift(t)}}class Me extends ze{constructor(e){super(),this.material=e.material,this.techniqueRep=e.techniqueRep,this.output=e.output}getTechnique(){return this.technique}getPipelineState(e,t){return this.getTechnique().pipeline}ensureResources(e){return 2}ensureParameters(e){}}const Ie=S(),Le=I;function we(e,t,i,r,s,a,n){const o=L(t),c=i.tolerance;if(!o)if(e.boundingInfo)M("triangle"===e.data.primitiveType),Te(e.boundingInfo,r,s,c,a,n);else{const t=e.getIndices(Le.POSITION),i=e.getAttribute(Le.POSITION);Ue(r,s,0,t.length/3,t,i,void 0,a,n)}}const De=o();function Te(e,i,r,s,a,n){const o=Ee(i,r,De);if(P(Ie,e.getBBMin()),O(Ie,e.getBBMax()),t(a)&&a.applyToAabb(Ie),Ge(Ie,i,o,s)){const t=e.getPrimitiveIndices(),o=e.getIndices(),c=e.getPosition(),l=t?t.length:o.length/3;if(l>Ze){const t=e.getChildren();if(void 0!==t){for(let e=0;e<8;++e)void 0!==t[e]&&Te(t[e],i,r,s,a,n);return}}Ue(i,r,0,l,o,c,t,a,n)}}const Be=2**-52,Ve=o();function Ue(e,i,r,s,a,n,o,c,l){if(o)return function(e,i,r,s,a,n,o,c,l){const{data:f,offsetIdx:d,strideIdx:u}=n,v=e[0],m=e[1],h=e[2],p=i[0],g=i[1],_=i[2],b=p-v,x=g-m,S=_-h;for(let e=r;e<s;++e){const i=o[e];let r=3*i,s=d+u*a[r++],n=f[s++],p=f[s++],g=f[s];s=d+u*a[r++];let _=f[s++],P=f[s++],O=f[s];s=d+u*a[r];let y=f[s++],F=f[s++],A=f[s];t(c)&&([n,p,g]=c.applyToVertex(n,p,g,e),[_,P,O]=c.applyToVertex(_,P,O,e),[y,F,A]=c.applyToVertex(y,F,A,e));const z=_-n,C=P-p,M=O-g,I=y-n,L=F-p,w=A-g,D=x*w-L*S,T=S*I-w*b,B=b*L-I*x,V=z*D+C*T+M*B;if(Math.abs(V)<=Be)continue;const U=v-n,R=m-p,N=h-g,H=U*D+R*T+N*B;if(V>0){if(H<0||H>V)continue}else if(H>0||H<V)continue;const E=R*M-C*N,G=N*z-M*U,k=U*C-z*R,j=b*E+x*G+S*k;if(V>0){if(j<0||H+j>V)continue}else if(j>0||H+j<V)continue;const $=(I*E+L*G+w*k)/V;if($>=0){l($,He(z,C,M,I,L,w,Ve),i)}}}(e,i,r,s,a,n,o,c,l);const{data:f,offsetIdx:d,strideIdx:u}=n,v=e[0],m=e[1],h=e[2],p=i[0]-v,g=i[1]-m,_=i[2]-h;for(let e=r,i=3*r;e<s;++e){let r=d+u*a[i++],s=f[r++],n=f[r++],o=f[r];r=d+u*a[i++];let b=f[r++],x=f[r++],S=f[r];r=d+u*a[i++];let P=f[r++],O=f[r++],y=f[r];t(c)&&([s,n,o]=c.applyToVertex(s,n,o,e),[b,x,S]=c.applyToVertex(b,x,S,e),[P,O,y]=c.applyToVertex(P,O,y,e));const F=b-s,A=x-n,z=S-o,C=P-s,M=O-n,I=y-o,L=g*I-M*_,w=_*C-I*p,D=p*M-C*g,T=F*L+A*w+z*D;if(Math.abs(T)<=Be)continue;const B=v-s,V=m-n,U=h-o,R=B*L+V*w+U*D;if(T>0){if(R<0||R>T)continue}else if(R>0||R<T)continue;const N=V*z-A*U,H=U*F-z*B,E=B*A-F*V,G=p*N+g*H+_*E;if(T>0){if(G<0||R+G>T)continue}else if(G>0||R+G<T)continue;const k=(C*N+M*H+I*E)/T;if(k>=0){l(k,He(F,A,z,C,M,I,Ve),e)}}}const Re=o(),Ne=o();function He(e,t,i,r,s,a,n){return l(Re,e,t,i),l(Ne,r,s,a),v(n,Re,Ne),m(n,n),n}function Ee(e,t,i){return l(i,1/(t[0]-e[0]),1/(t[1]-e[1]),1/(t[2]-e[2]))}function Ge(e,t,i,r){return ke(e,t,i,r,1/0)}function ke(e,t,i,r,s){const a=(e[0]-r-t[0])*i[0],n=(e[3]+r-t[0])*i[0];let o=Math.min(a,n),c=Math.max(a,n);const l=(e[1]-r-t[1])*i[1],f=(e[4]+r-t[1])*i[1];if(c=Math.min(c,Math.max(l,f)),c<0)return!1;if(o=Math.max(o,Math.min(l,f)),o>c)return!1;const d=(e[2]-r-t[2])*i[2],u=(e[5]+r-t[2])*i[2];return c=Math.min(c,Math.max(d,u)),!(c<0)&&(o=Math.max(o,Math.min(d,u)),!(o>c)&&o<s)}function je(e,t,i,r,s){let a=(i.screenLength||0)*e.pixelRatio;s&&(a=function(e,t,i,r){return _e(e,pe(t,i,r))}(a,r,t,s));const o=a*Math.tan(.5*e.fovY)/(.5*e.fullHeight);return n(o*t,i.minWorldLength||0,null!=i.maxWorldLength?i.maxWorldLength:1/0)}function $e(e,t,i){if(!e)return;const r=e.parameters,s=e.paddingPixelsOverride;t.setUniform4f(i,r.divisor,r.offset,r.minPixelSize,s)}function We(e,t){const i=t?We(t):{};for(const t in e){let r=e[t];r&&r.forEach&&(r=Xe(r)),null==r&&t in i||(i[t]=r)}return i}function qe(e,t){let i=!1;for(const r in t){const s=t[r];void 0!==s&&(i=!0,Array.isArray(s)?e[r]=s.slice():e[r]=s)}return i}function Ye(e,t,i,r,s){if(!t.options.selectionMode)return;const n=e.getAttribute(Le.POSITION).data,o=e.getAttribute(Le.SIZE),c=o&&o.data[0],l=i[0],f=i[1],d=((c+r)/2+4)*e.screenToWorldRatio;let u=Number.MAX_VALUE;for(let e=0;e<n.length-5;e+=3){const t=n[e],i=n[e+1],r=l-t,s=f-i,o=n[e+3]-t,c=n[e+4]-i,d=a((o*r+c*s)/(o*o+c*c),0,1),v=o*d-r,m=c*d-s,h=v*v+m*m;h<u&&(u=h)}u<d*d&&s()}function Xe(e){const t=[];return e.forEach((e=>t.push(e))),t}const Ke={multiply:1,ignore:2,replace:3,tint:4},Ze=1e3;class Je{constructor(e,t,i){this.supportsEdges=!1,this._visible=!0,this._renderPriority=0,this._insertOrder=0,this._vertexAttributeLocations=Z,this.id=Je._idGen.gen(e),this._params=We(t,i),this.validateParameterValues(this._params)}dispose(){}get params(){return this._params}update(){return!1}setParameterValues(e){qe(this._params,e)&&(this.validateParameterValues(this._params),this.parametersChanged())}validateParameterValues(){}get visible(){return this._visible}set visible(e){e!==this._visible&&(this._visible=e,this.parametersChanged())}isVisibleInPass(e){return!0}get renderOccluded(){return this.params.renderOccluded}get renderPriority(){return this._renderPriority}set renderPriority(e){e!==this._renderPriority&&(this._renderPriority=e,this.parametersChanged())}get insertOrder(){return this._insertOrder}set insertOrder(e){e!==this._insertOrder&&(this._insertOrder=e,this.parametersChanged())}get vertexAttributeLocations(){return this._vertexAttributeLocations}isVisible(){return this._visible}parametersChanged(){t(this.materialRepository)&&this.materialRepository.materialChanged(this)}}function Qe(e,t){return e.isVisible()&&e.isVisibleInPass(t.pass)&&0!=(e.renderOccluded&t.renderOccludedMask)}Je._idGen=new w;const et={renderOccluded:1};function tt(e,t,i,r,s){const a=i.typedBuffer,n=i.typedBufferStride,o=e.length;if(r*=n,null==s||1===s)for(let i=0;i<o;++i){const s=2*e[i];a[r]=t[s],a[r+1]=t[s+1],r+=n}else for(let i=0;i<o;++i){const o=2*e[i];for(let e=0;e<s;++e)a[r]=t[o],a[r+1]=t[o+1],r+=n}}function it(e,t,i,r,s){const a=i.typedBuffer,n=i.typedBufferStride,o=e.length;if(r*=n,null==s||1===s)for(let i=0;i<o;++i){const s=3*e[i];a[r]=t[s],a[r+1]=t[s+1],a[r+2]=t[s+2],r+=n}else for(let i=0;i<o;++i){const o=3*e[i];for(let e=0;e<s;++e)a[r]=t[o],a[r+1]=t[o+1],a[r+2]=t[o+2],r+=n}}function rt(e,t,i,r,s){const a=i.typedBuffer,n=i.typedBufferStride,o=e.length;if(r*=n,null==s||1===s)for(let i=0;i<o;++i){const s=4*e[i];a[r]=t[s],a[r+1]=t[s+1],a[r+2]=t[s+2],a[r+3]=t[s+3],r+=n}else for(let i=0;i<o;++i){const o=4*e[i];for(let e=0;e<s;++e)a[r]=t[o],a[r+1]=t[o+1],a[r+2]=t[o+2],a[r+3]=t[o+3],r+=n}}function st(e,t,i,r,s,a){if(i){const n=i,o=r.typedBuffer,c=r.typedBufferStride,l=e.length;if(s*=c,null==a||1===a)for(let i=0;i<l;++i){const r=3*e[i],a=t[r],l=t[r+1],f=t[r+2];o[s]=n[0]*a+n[4]*l+n[8]*f+n[12],o[s+1]=n[1]*a+n[5]*l+n[9]*f+n[13],o[s+2]=n[2]*a+n[6]*l+n[10]*f+n[14],s+=c}else for(let i=0;i<l;++i){const r=3*e[i],l=t[r],f=t[r+1],d=t[r+2],u=n[0]*l+n[4]*f+n[8]*d+n[12],v=n[1]*l+n[5]*f+n[9]*d+n[13],m=n[2]*l+n[6]*f+n[10]*d+n[14];for(let e=0;e<a;++e)o[s]=u,o[s+1]=v,o[s+2]=m,s+=c}}else it(e,t,r,s,a)}function at(e,t,i,r,s,a){if(i){const n=i,o=r.typedBuffer,c=r.typedBufferStride,l=e.length,f=n[0],d=n[1],u=n[2],v=n[4],m=n[5],h=n[6],p=n[8],g=n[9],_=n[10],b=Math.abs(1-f*f+v*v+p*p)>1e-5||Math.abs(1-d*d+m*m+g*g)>1e-5||Math.abs(1-u*u+h*h+_*_)>1e-5;if(s*=c,null==a||1===a)for(let i=0;i<l;++i){const r=3*e[i],a=t[r],n=t[r+1],l=t[r+2];let x=f*a+v*n+p*l,S=d*a+m*n+g*l,P=u*a+h*n+_*l;if(b){const e=x*x+S*S+P*P;if(e<.999999&&e>1e-6){const t=Math.sqrt(e);x/=t,S/=t,P/=t}}o[s+0]=x,o[s+1]=S,o[s+2]=P,s+=c}else for(let i=0;i<l;++i){const r=3*e[i],n=t[r],l=t[r+1],x=t[r+2];let S=f*n+v*l+p*x,P=d*n+m*l+g*x,O=u*n+h*l+_*x;if(b){const e=S*S+P*P+O*O;if(e<.999999&&e>1e-6){const t=Math.sqrt(e);S/=t,P/=t,O/=t}}for(let e=0;e<a;++e)o[s+0]=S,o[s+1]=P,o[s+2]=O,s+=c}}else it(e,t,r,s,a)}function nt(e,t,i,r,s,a){const n=r.typedBuffer,o=r.typedBufferStride,c=e.length;if(s*=o,null==a||1===a){if(4===i)for(let i=0;i<c;++i){const r=4*e[i];n[s]=t[r],n[s+1]=t[r+1],n[s+2]=t[r+2],n[s+3]=t[r+3],s+=o}else if(3===i)for(let i=0;i<c;++i){const r=3*e[i];n[s]=t[r],n[s+1]=t[r+1],n[s+2]=t[r+2],n[s+3]=255,s+=o}}else if(4===i)for(let i=0;i<c;++i){const r=4*e[i];for(let e=0;e<a;++e)n[s]=t[r],n[s+1]=t[r+1],n[s+2]=t[r+2],n[s+3]=t[r+3],s+=o}else if(3===i)for(let i=0;i<c;++i){const r=3*e[i];for(let e=0;e<a;++e)n[s]=t[r],n[s+1]=t[r+1],n[s+2]=t[r+2],n[s+3]=255,s+=o}}function ot(e,t,i,r,s,a){for(const n of t.fieldNames){const t=e.vertexAttr[n],o=e.indices[n];if(t&&o)switch(n){case I.POSITION:{M(3===t.size);const e=s.getField(n,b);e&&st(o,t.data,i,e,a);break}case I.NORMAL:{M(3===t.size);const e=s.getField(n,b);e&&at(o,t.data,r,e,a);break}case I.UV0:{M(2===t.size);const e=s.getField(n,x);e&&tt(o,t.data,e,a);break}case I.UVMAPSPACE:{M(4===t.size);const e=s.getField(n,g);e&&rt(o,t.data,e,a);break}case I.MEANVERTEXPOSITION:{M(3===t.size);const e=s.getField(n,b);e&&st(o,t.data,i,e,a);break}case I.BOUND1:case I.BOUND2:case I.BOUND3:{M(3===t.size);const e=s.getField(n,b);e&&st(o,t.data,i,e,a);break}case I.COLOR:{M(3===t.size||4===t.size);const e=s.getField(n,_);e&&nt(o,t.data,t.size,e,a);break}case I.SYMBOLCOLOR:{M(3===t.size||4===t.size);const e=s.getField(n,_);e&&nt(o,t.data,t.size,e,a);break}case I.TANGENT:{M(4===t.size);const e=s.getField(n,g);e&&rt(o,t.data,e,a);break}}}}function ct(e,t){if(t.slicePlaneEnabled){e.extensions.add("GL_OES_standard_derivatives"),t.sliceEnabledForVertexPrograms&&(e.vertex.uniforms.add("slicePlaneOrigin","vec3"),e.vertex.uniforms.add("slicePlaneBasis1","vec3"),e.vertex.uniforms.add("slicePlaneBasis2","vec3")),e.fragment.uniforms.add("slicePlaneOrigin","vec3"),e.fragment.uniforms.add("slicePlaneBasis1","vec3"),e.fragment.uniforms.add("slicePlaneBasis2","vec3");const i=D`
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
    `,r=D`
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
    `,s=t.sliceHighlightDisabled?D`#define highlightSlice(_color_, _pos_) (_color_)`:D`
        ${r}
        #define highlightSlice(_color_, _pos_) (sliceEnabled() ? applySliceHighlight(_color_, _pos_) : (_color_))
      `;t.sliceEnabledForVertexPrograms&&e.vertex.code.add(i),e.fragment.code.add(i),e.fragment.code.add(s)}else{const i=D`
      #define rejectBySlice(_pos_) false
      #define discardBySlice(_pos_) {}
      #define highlightSlice(_color_, _pos_) (_color_)
    `;t.sliceEnabledForVertexPrograms&&e.vertex.code.add(i),e.fragment.code.add(i)}}!function(e){e.bindUniformsWithOrigin=function(t,i,r){e.bindUniforms(t,i,r.slicePlane,r.origin)},e.bindUniforms=function(e,i,r,s){i.slicePlaneEnabled&&(t(r)?(s?(h(lt,r.origin,s),e.setUniform3fv("slicePlaneOrigin",lt)):e.setUniform3fv("slicePlaneOrigin",r.origin),e.setUniform3fv("slicePlaneBasis1",r.basis1),e.setUniform3fv("slicePlaneBasis2",r.basis2)):(e.setUniform3fv("slicePlaneBasis1",c),e.setUniform3fv("slicePlaneBasis2",c),e.setUniform3fv("slicePlaneOrigin",c)))}}(ct||(ct={}));const lt=o();function ft(e){e.fragment.uniforms.add("depthTex","sampler2D"),e.fragment.uniforms.add("highlightViewportPixelSz","vec4"),e.fragment.code.add(D`
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
  `)}function dt(e,t){t.vvInstancingEnabled&&(t.vvSize||t.vvColor)&&e.attributes.add("instanceFeatureAttribute","vec4"),t.vvSize?(e.vertex.uniforms.add("vvSizeMinSize","vec3"),e.vertex.uniforms.add("vvSizeMaxSize","vec3"),e.vertex.uniforms.add("vvSizeOffset","vec3"),e.vertex.uniforms.add("vvSizeFactor","vec3"),e.vertex.uniforms.add("vvSymbolRotationMatrix","mat3"),e.vertex.uniforms.add("vvSymbolAnchor","vec3"),e.vertex.code.add(D`
      vec3 vvScale(vec4 _featureAttribute) {
        return clamp(vvSizeOffset + _featureAttribute.x * vvSizeFactor, vvSizeMinSize, vvSizeMaxSize);
      }

      vec4 vvTransformPosition(vec3 position, vec4 _featureAttribute) {
        return vec4(vvSymbolRotationMatrix * ( vvScale(_featureAttribute) * (position + vvSymbolAnchor)), 1.0);
      }
    `),e.vertex.code.add(D`
      const float eps = 1.192092896e-07;
      vec4 vvTransformNormal(vec3 _normal, vec4 _featureAttribute) {
        vec3 vvScale = clamp(vvSizeOffset + _featureAttribute.x * vvSizeFactor, vvSizeMinSize + eps, vvSizeMaxSize);
        return vec4(vvSymbolRotationMatrix * _normal / vvScale, 1.0);
      }

      ${t.vvInstancingEnabled?D`
      vec4 vvLocalNormal(vec3 _normal) {
        return vvTransformNormal(_normal, instanceFeatureAttribute);
      }

      vec4 localPosition() {
        return vvTransformPosition(position, instanceFeatureAttribute);
      }`:""}
    `)):e.vertex.code.add(D`
      vec4 localPosition() { return vec4(position, 1.0); }

      vec4 vvLocalNormal(vec3 _normal) { return vec4(_normal, 1.0); }
    `),t.vvColor?(e.vertex.defines.addInt("VV_COLOR_N",8),e.vertex.code.add(D`
      uniform float vvColorValues[VV_COLOR_N];
      uniform vec4 vvColorColors[VV_COLOR_N];

      vec4 vvGetColor(vec4 featureAttribute, float values[VV_COLOR_N], vec4 colors[VV_COLOR_N]) {
        float value = featureAttribute.y;
        if (value <= values[0]) {
          return colors[0];
        }

        for (int i = 1; i < VV_COLOR_N; ++i) {
          if (values[i] >= value) {
            float f = (value - values[i-1]) / (values[i] - values[i-1]);
            return mix(colors[i-1], colors[i], f);
          }
        }
        return colors[VV_COLOR_N - 1];
      }

      ${t.vvInstancingEnabled?D`
      vec4 vvColor() {
        return vvGetColor(instanceFeatureAttribute, vvColorValues, vvColorColors);
      }`:""}
    `)):e.vertex.code.add(D`
      vec4 vvColor() { return vec4(1.0); }
    `)}!function(e){e.bindOutputHighlight=function(e,t,i){e.bindTexture(i.highlightDepthTexture,5),t.setUniform1i("depthTex",5),t.setUniform4f("highlightViewportPixelSz",0,0,i.inverseViewport[0],i.inverseViewport[1])}}(ft||(ft={})),function(e){function t(e,t){t.vvSizeEnabled&&(e.setUniform3fv("vvSizeMinSize",t.vvSizeMinSize),e.setUniform3fv("vvSizeMaxSize",t.vvSizeMaxSize),e.setUniform3fv("vvSizeOffset",t.vvSizeOffset),e.setUniform3fv("vvSizeFactor",t.vvSizeFactor)),t.vvColorEnabled&&(e.setUniform1fv("vvColorValues",t.vvColorValues),e.setUniform4fv("vvColorColors",t.vvColorColors))}e.bindUniforms=t,e.bindUniformsWithOpacity=function(e,i){t(e,i),i.vvOpacityEnabled&&(e.setUniform1fv("vvOpacityValues",i.vvOpacityValues),e.setUniform1fv("vvOpacityOpacities",i.vvOpacityOpacities))},e.bindUniformsForSymbols=function(e,i){t(e,i),i.vvSizeEnabled&&(e.setUniform3fv("vvSymbolAnchor",i.vvSymbolAnchor),e.setUniformMatrix3fv("vvSymbolRotationMatrix",i.vvSymbolRotationMatrix))}}(dt||(dt={}));const ut=.1,vt=.001;function mt(e,t){const i=e.fragment;switch(t.alphaDiscardMode){case 0:i.code.add(D`
        #define discardOrAdjustAlpha(color) { if (color.a < ${D.float(.001)}) { discard; } }
      `);break;case 1:i.code.add(D`
        void discardOrAdjustAlpha(inout vec4 color) {
          color.a = 1.0;
        }
      `);break;case 2:i.uniforms.add("textureAlphaCutoff","float"),i.code.add(D`
        #define discardOrAdjustAlpha(color) { if (color.a < textureAlphaCutoff) { discard; } else { color.a = 1.0; } }
      `);break;case 3:e.fragment.uniforms.add("textureAlphaCutoff","float"),e.fragment.code.add(D`
        #define discardOrAdjustAlpha(color) { if (color.a < textureAlphaCutoff) { discard; } }
      `)}}function ht(e){e.code.add(D`
    // This is the maximum float value representable as 32bit fixed point,
    // it is rgba2float(vec4(1)) inlined.
    const float MAX_RGBA_FLOAT =
      255.0 / 256.0 +
      255.0 / 256.0 / 256.0 +
      255.0 / 256.0 / 256.0 / 256.0 +
      255.0 / 256.0 / 256.0 / 256.0 / 256.0;

    // Factors to convert to fixed point, i.e. factors (256^0, 256^1, 256^2, 256^3)
    const vec4 FIXED_POINT_FACTORS = vec4(1.0, 256.0, 256.0 * 256.0, 256.0 * 256.0 * 256.0);

    vec4 float2rgba(const float value) {
      // Make sure value is in the domain we can represent
      float valueInValidDomain = clamp(value, 0.0, MAX_RGBA_FLOAT);

      // Decompose value in 32bit fixed point parts represented as
      // uint8 rgba components. Decomposition uses the fractional part after multiplying
      // by a power of 256 (this removes the bits that are represented in the previous
      // component) and then converts the fractional part to 8bits.
      vec4 fixedPointU8 = floor(fract(valueInValidDomain * FIXED_POINT_FACTORS) * 256.0);

      // Convert uint8 values (from 0 to 255) to floating point representation for
      // the shader
      const float toU8AsFloat = 1.0 / 255.0;

      return fixedPointU8 * toU8AsFloat;
    }

    // Factors to convert rgba back to float
    const vec4 RGBA_2_FLOAT_FACTORS = vec4(
      255.0 / (256.0),
      255.0 / (256.0 * 256.0),
      255.0 / (256.0 * 256.0 * 256.0),
      255.0 / (256.0 * 256.0 * 256.0 * 256.0)
    );

    float rgba2float(vec4 rgba) {
      // Convert components from 0->1 back to 0->255 and then
      // add the components together with their corresponding
      // fixed point factors, i.e. (256^1, 256^2, 256^3, 256^4)
      return dot(rgba, RGBA_2_FLOAT_FACTORS);
    }
  `)}const pt=y(770,1,771,771),gt=F(1,1),_t=F(0,771);function bt(e){return 2===e?null:1===e?_t:gt}function xt(e){return 2===e?A:null}const St=5e5,Pt={factor:-1,units:-2};function Ot(e){return e?Pt:null}function yt(e){return 3===e||2===e?513:515}const Ft={func:513},At={func:519},zt={mask:255},Ct={mask:0},Mt=e=>({function:{func:517,ref:e,mask:e},operation:{fail:7680,zFail:7680,zPass:7680}}),It=e=>({function:{func:519,ref:e,mask:e},operation:{fail:7680,zFail:7680,zPass:7681}}),Lt={function:{func:519,ref:2,mask:2},operation:{fail:7680,zFail:7680,zPass:0}},wt={function:{func:519,ref:2,mask:2},operation:{fail:7680,zFail:7680,zPass:7681}},Dt={function:{func:514,ref:2,mask:2},operation:{fail:7680,zFail:7680,zPass:7680}},Tt={function:{func:517,ref:2,mask:2},operation:{fail:7680,zFail:7680,zPass:7680}};function Bt(e,t){t.attributeColor?(e.attributes.add("color","vec4"),e.varyings.add("vColor","vec4"),e.vertex.code.add(D`
      void forwardVertexColor() { vColor = color; }
    `),e.vertex.code.add(D`
      void forwardNormalizedVertexColor() { vColor = color * 0.003921568627451; }
    `)):e.vertex.code.add(D`
      void forwardVertexColor() {}
      void forwardNormalizedVertexColor() {}
    `)}function Vt(e,t){e.fragment.include(ht),3===t.output?(e.extensions.add("GL_OES_standard_derivatives"),e.fragment.code.add(D`
      float _calculateFragDepth(const in float depth) {
        // calc polygon offset
        const float SLOPE_SCALE = 2.0;
        const float BIAS = 2.0 * .000015259;    // 1 / (2^16 - 1)
        float m = max(abs(dFdx(depth)), abs(dFdy(depth)));
        float result = depth + SLOPE_SCALE * m + BIAS;
        return clamp(result, .0, .999999);
      }

      void outputDepth(float _linearDepth) {
        gl_FragColor = float2rgba(_calculateFragDepth(_linearDepth));
      }
    `)):1===t.output&&e.fragment.code.add(D`
      void outputDepth(float _linearDepth) {
        gl_FragColor = float2rgba(_linearDepth);
      }
    `)}function Ut(e){e.vertex.code.add(D`
    const float PI = 3.141592653589793;
  `),e.fragment.code.add(D`
    const float PI = 3.141592653589793;
    const float LIGHT_NORMALIZATION = 1.0 / PI;
    const float INV_PI = 0.3183098861837907;
    const float HALF_PI = 1.570796326794897;
    `)}export{Ae as $,Ue as A,Ge as B,ve as C,Z as D,ne as E,ot as F,Me as G,me as H,he as I,He as J,Mt as K,ke as L,Je as M,ze as N,Vt as O,Ce as P,re as Q,q as R,U as S,T,Ee as U,J as V,mt as W,It as X,ut as Y,ht as Z,Qe as _,X as a,Ut as a0,dt as a1,At as a2,Tt as a3,Ct as a4,Dt as a5,Ft as a6,je as a7,_e as a8,ae as a9,be as aa,nt as ab,ce as ac,se as ad,Ye as ae,We as af,qe as ag,Y as b,xe as c,Bt as d,ct as e,ft as f,D as g,pt as h,bt as i,yt as j,xt as k,zt as l,wt as m,Lt as n,Ot as o,K as p,St as q,we as r,vt as s,et as t,$e as u,at as v,st as w,rt as x,Ke as y,Pt as z};
