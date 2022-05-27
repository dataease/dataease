/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as e}from"./tslib.es6.js";import{O as t}from"./ArrayPool.js";import"./object.js";import{clone as i,b as r,c as n,t as s}from"../core/lang.js";import{b as a,i as o,u as l,c,L as p}from"./Logger.js";import{P as d}from"../core/scheduling.js";import{E as h}from"./Evented.js";import{i as u,c as f}from"./mathUtils2.js";import{c as m,b as g,O as v,f as y,a as O}from"./vec3f64.js";import{c as b,e as S,n as x,s as A,a as P,d as _,f as C,g as I,i as D,b as T,l as w,k as E}from"./vec3.js";import{b as R,a as z,r as j}from"../geometry/Polygon.js";import{fromJSON as N}from"../geometry/support/jsonUtils.js";import{d as L}from"./screenUtils.js";import M from"../Graphic.js";import{d as U,e as F,f as V}from"./mat4.js";import{c as q}from"./aaBoundingRect.js";import{projectPoint as B}from"../geometry/projection.js";import{b as W,O as G}from"./vec4f64.js";import{c as H,a as Z}from"./quatf64.js";import{c as k}from"./vec2.js";import{s as $}from"./vec4.js";import{i as J,A as X}from"./aaBoundingBox.js";import{m as Y}from"./dehydratedFeatures.js";import{f as Q}from"./vec2f64.js";import{F as K,g as ee,S as te,a0 as ie,O as re,e as ne,C as se,s as ae,p as oe,a as le,b as ce,V as pe,f as de,a1 as he,h as ue,i as fe,j as me,k as ge,l as ve,m as ye,n as Oe,z as be,a2 as Se,a3 as xe,a4 as Ae,a5 as Pe,a6 as _e,R as Ce,t as Ie,M as De,E as Te,G as we,T as Ee,d as Re,D as ze,o as je,q as Ne,r as Le}from"./PiUtils.glsl.js";import{P as Me}from"./Program.js";import{m as Ue,a as Fe,d as Ve}from"./isWebGL2Context.js";import{n as qe}from"./InterleavedLayout.js";import{f as Be,c as We,a as Ge}from"./vec3f32.js";import{p as He,r as Ze,s as ke,w as $e,u as Je}from"./geometryUtils.js";import{V as Xe,a as Ye,c as Qe,d as Ke}from"./Util.js";import{a as et}from"./Geometry.js";import{I as tt,i as it}from"./Object3D.js";var rt;!function(e){e.length=function(e,t){const i=e[t],r=e[t+1],n=e[t+2];return Math.sqrt(i*i+r*r+n*n)},e.normalize=function(e,t){const i=e[t],r=e[t+1],n=e[t+2],s=1/Math.sqrt(i*i+r*r+n*n);e[t]*=s,e[t+1]*=s,e[t+2]*=s},e.scale=function(e,t,i){e[t]*=i,e[t+1]*=i,e[t+2]*=i},e.add=function(e,t,i,r,n,s=t){(n=n||e)[s]=e[t]+i[r],n[s+1]=e[t+1]+i[r+1],n[s+2]=e[t+2]+i[r+2]},e.subtract=function(e,t,i,r,n,s=t){(n=n||e)[s]=e[t]-i[r],n[s+1]=e[t+1]-i[r+1],n[s+2]=e[t+2]-i[r+2]}}(rt||(rt={}));const nt=rt;var st,at,ot,lt;!function(e){const t=.5,i=[[-t,-t,t],[t,-t,t],[t,t,t],[-t,t,t],[-t,-t,-t],[t,-t,-t],[t,t,-t],[-t,t,-t]],r=[0,0,1,-1,0,0,1,0,0,0,-1,0,0,1,0,0,0,-1],n=[0,0,1,0,1,1,0,1],s=[0,1,2,2,3,0,4,0,3,3,7,4,1,5,6,6,2,1,1,0,4,4,5,1,3,2,6,6,7,3,5,4,7,7,6,5],a=new Array(36);for(let e=0;e<6;e++)for(let t=0;t<6;t++)a[6*e+t]=e;const o=new Array(36);for(let e=0;e<6;e++)o[6*e+0]=0,o[6*e+1]=1,o[6*e+2]=2,o[6*e+3]=2,o[6*e+4]=3,o[6*e+5]=0;e.createGeometry=function(e){Array.isArray(e)||(e=[e,e,e]);const t=new Float32Array(24);for(let r=0;r<8;r++)t[3*r]=i[r][0]*e[0],t[3*r+1]=i[r][1]*e[1],t[3*r+2]=i[r][2]*e[2];const l={};l[Xe.POSITION]=new Uint32Array(s),l[Xe.NORMAL]=new Uint32Array(a),l[Xe.UV0]=new Uint32Array(o);const c={};return c[Xe.POSITION]={size:3,data:t},c[Xe.NORMAL]={size:3,data:new Float32Array(r)},c[Xe.UV0]={size:2,data:new Float32Array(n)},new et(c,l)}}(st||(st={})),function(e){const t=.5,i=[[-t,0,-t],[t,0,-t],[t,0,t],[-t,0,t],[0,-t,0],[0,t,0]],r=[0,1,-1,1,1,0,0,1,1,-1,1,0,0,-1,-1,1,-1,0,0,-1,1,-1,-1,0],n=[5,1,0,5,2,1,5,3,2,5,0,3,4,0,1,4,1,2,4,2,3,4,3,0],s=[0,0,0,1,1,1,2,2,2,3,3,3,4,4,4,5,5,5,6,6,6,7,7,7];e.createGeometry=function(e){Array.isArray(e)||(e=[e,e,e]);const t=new Float32Array(18);for(let r=0;r<6;r++)t[3*r]=i[r][0]*e[0],t[3*r+1]=i[r][1]*e[1],t[3*r+2]=i[r][2]*e[2];const a={};a[Xe.POSITION]=new Uint32Array(n),a[Xe.NORMAL]=new Uint32Array(s);const o={};return o[Xe.POSITION]={size:3,data:t},o[Xe.NORMAL]={size:3,data:new Float32Array(r)},new et(o,a)}}(at||(at={})),function(e){const t=.5,i=Be(-t,0,-t),r=Be(t,0,-t),n=Be(0,0,t),s=Be(0,.5,0),a=We(),o=We(),l=We(),c=We(),p=We();b(a,i,s),b(o,i,r),S(l,a,o),x(l,l),b(a,r,s),b(o,r,n),S(c,a,o),x(c,c),b(a,n,s),b(o,n,i),S(p,a,o),x(p,p);const d=[i,r,n,s],h=[0,-1,0,l[0],l[1],l[2],c[0],c[1],c[2],p[0],p[1],p[2]],u=[0,1,2,3,1,0,3,2,1,3,0,2],f=[0,0,0,1,1,1,2,2,2,3,3,3];e.createGeometry=function(e){Array.isArray(e)||(e=[e,e,e]);const t=new Float32Array(12);for(let i=0;i<4;i++)t[3*i]=d[i][0]*e[0],t[3*i+1]=d[i][1]*e[1],t[3*i+2]=d[i][2]*e[2];const i={};i[Xe.POSITION]=new Uint32Array(u),i[Xe.NORMAL]=new Uint32Array(f);const r={};return r[Xe.POSITION]={size:3,data:t},r[Xe.NORMAL]={size:3,data:new Float32Array(h)},new et(r,i)}}(ot||(ot={})),function(e){e.createBoxGeometry=st.createGeometry,e.createDiamondGeometry=at.createGeometry,e.createTetrahedronGeometry=ot.createGeometry,e.createSphereGeometry=function(e,t,i,r){const{phiStart:n=-Math.PI,phiLength:s=2*Math.PI,thetaStart:a=-Math.PI/2,thetaLength:o=Math.PI}=r||{},{position:l=Xe.POSITION,normal:c=Xe.NORMAL,uv:p=Xe.UV0}=(null==r?void 0:r.attributes)||{},d=Math.max(3,Math.floor(t)),h=Math.max(2,Math.floor(i)),u=(d+1)*(h+1),f=new Float32Array(3*u),m=new Float32Array(3*u),g=new Float32Array(2*u),v=[];let y=0;for(let t=0;t<=h;t++){const i=[],r=t/h,l=a+r*o,c=Math.cos(l);for(let t=0;t<=d;t++){const a=t/d,o=n+a*s,p=Math.cos(o)*c,h=Math.sin(l),u=-Math.sin(o)*c;f[3*y]=p*e,f[3*y+1]=h*e,f[3*y+2]=u*e,m[3*y]=p,m[3*y+1]=h,m[3*y+2]=u,g[2*y]=a,g[2*y+1]=r,i.push(y),++y}v.push(i)}const O=new Uint32Array(2*d*(h-1)*3);y=0;for(let e=0;e<h;e++)for(let t=0;t<d;t++){const i=v[e][t],r=v[e][t+1],n=v[e+1][t+1],s=v[e+1][t];0===e?(O[y++]=i,O[y++]=n,O[y++]=s):e===h-1?(O[y++]=i,O[y++]=r,O[y++]=n):(O[y++]=i,O[y++]=r,O[y++]=n,O[y++]=n,O[y++]=s,O[y++]=i)}const b={};b[l]=O,b[c]=O;const S={};return S[l]={size:3,data:f},S[c]={size:3,data:m},p&&(S[p]={size:2,data:g},b[p]=O),new et(S,b)},e.createPolySphereGeometry=function(e,t,i){const r=e;let n,s;if(i)n=[0,-1,0,1,0,0,0,0,1,-1,0,0,0,0,-1,0,1,0],s=new Uint32Array([0,1,2,0,2,3,0,3,4,0,4,1,1,5,2,2,5,3,3,5,4,4,5,1]);else{const e=r*(1+Math.sqrt(5))/2;n=[-r,e,0,r,e,0,-r,-e,0,r,-e,0,0,-r,e,0,r,e,0,-r,-e,0,r,-e,e,0,-r,e,0,r,-e,0,-r,-e,0,r],s=new Uint32Array([0,11,5,0,5,1,0,1,7,0,7,10,0,10,11,1,5,9,5,11,4,11,10,2,10,7,6,7,1,8,3,9,4,3,4,2,3,2,6,3,6,8,3,8,9,4,9,5,2,4,11,6,2,10,8,6,7,9,8,1])}for(let t=0;t<n.length;t+=3)nt.scale(n,t,e/nt.length(n,t));let a={};function o(t,i){t>i&&([t,i]=[i,t]);const r=t.toString()+"."+i.toString();if(a[r])return a[r];let s=n.length;return n.length+=3,nt.add(n,3*t,n,3*i,n,s),nt.scale(n,s,e/nt.length(n,s)),s/=3,a[r]=s,s}for(let e=0;e<t;e++){const e=s.length,t=new Uint32Array(4*e);for(let i=0;i<e;i+=3){const e=s[i],r=s[i+1],n=s[i+2],a=o(e,r),l=o(r,n),c=o(n,e),p=4*i;t[p]=e,t[p+1]=a,t[p+2]=c,t[p+3]=r,t[p+4]=l,t[p+5]=a,t[p+6]=n,t[p+7]=c,t[p+8]=l,t[p+9]=a,t[p+10]=l,t[p+11]=c}s=t,a={}}const l=new Float32Array(n);for(let e=0;e<l.length;e+=3)nt.normalize(l,e);const c={};c[Xe.POSITION]=s,c[Xe.NORMAL]=s;const p={};return p[Xe.POSITION]={size:3,data:new Float32Array(n)},p[Xe.NORMAL]={size:3,data:l},new et(p,c)},e.createPointGeometry=function(e,t,i,r,n,s,a,o){const l=t?new Float64Array([t[0],t[1],t[2]]):new Float32Array([0,0,0]),c=e?new Float32Array([e[0],e[1],e[2]]):new Float32Array([0,0,1]),p=s?new Float32Array(s):new Float32Array([0,0]),d=i?new Uint8Array([255*i[0],255*i[1],255*i[2],i.length>3?255*i[3]:255]):new Uint8Array([255,255,255,255]),h=null!=r&&2===r.length?new Float32Array(r):new Float32Array([1,1]),u={};if(u[Xe.POSITION]={size:3,data:l},u[Xe.NORMAL]={size:3,data:c},u[Xe.UV0]={size:p.length,data:p},u[Xe.COLOR]={size:4,data:d},u[Xe.SIZE]={size:2,data:h},null!=n){const e=new Float32Array([n[0],n[1],n[2],n[3]]);u[Xe.AUXPOS1]={size:4,data:e}}if(null!=a){const e=new Float32Array([a[0],a[1],a[2],a[3]]);u[Xe.AUXPOS2]={size:4,data:e}}return null!=o?(o.initialize(u,et.DefaultIndices,"point"),o):new et(u,et.DefaultIndices,"point")},e.updatePointGeometry=function(e,t,i,r,n,s,a,o){const l=o.vertexAttributes;if(null!=e){const{data:t}=l[Xe.NORMAL];t[0]=e[0],t[1]=e[1],t[2]=e[2]}if(null!=t){const{data:e}=l[Xe.POSITION];e[0]=t[0],e[1]=t[1],e[2]=t[2]}if(null!=i){const{data:e}=l[Xe.COLOR];e[0]=i[0],e[1]=i[1],e[2]=i[2],e[3]=i[3]}if(null!=r){const{data:e}=l[Xe.SIZE];e[0]=r[0],e[1]=r[1]}if(null!=n){const{data:e}=l[Xe.AUXPOS1];e[0]=n[0],e[1]=n[1],e[2]=n[2],e[3]=n[3]}if(null!=s){const{data:e}=l[Xe.UV0];e[0]=s[0],e[1]=s[1]}if(null!=a){const{data:e}=l[Xe.AUXPOS2];e[0]=a[0],e[1]=a[1],e[2]=a[2],e[3]=a[3]}return o},e.createPointArrayGeometry=function(e,t){const i=new Float32Array(3*e.length),r=new Float32Array(t?3*e.length:3),n=new Uint32Array(e.length),s=new Uint32Array(e.length);for(let a=0;a<e.length;a++)i[3*a]=e[a][0],i[3*a+1]=e[a][1],i[3*a+2]=e[a][2],t&&(r[3*a]=t[a][0],r[3*a+1]=t[a][1],r[3*a+2]=t[a][2]),n[a]=a,s[a]=0;t||(r[0]=0,r[1]=1,r[2]=0);const a=new Float32Array(2);a[0]=0,a[1]=0;const o={};o[Xe.POSITION]=n,o[Xe.NORMAL]=t?n:s,o[Xe.UV0]=s;const l={};return l[Xe.POSITION]={size:3,data:i},l[Xe.NORMAL]={size:3,data:r},l[Xe.UV0]={size:2,data:a},new et(l,o,"point")},e.createTriangleGeometry=function(){const e=new Float32Array([0,0,0,0,0,100,100,0,0]),t=new Uint32Array([0,1,2]),i=new Float32Array([0,1,0]),r=new Uint32Array([0,0,0]),n=new Float32Array([0,0]),s=new Uint32Array([0,0,0]),a={};a[Xe.POSITION]=t,a[Xe.NORMAL]=r,a[Xe.UV0]=s;const o={};return o[Xe.POSITION]={size:3,data:e},o[Xe.NORMAL]={size:3,data:i},o[Xe.UV0]={size:2,data:n},new et(o,a)};const t=[[-1,-1,0],[1,-1,0],[1,1,0],[-1,1,0]];function i(e,t,i,r,n){return!(Math.abs(_(t,e))>n)&&(S(i,e,t),x(i,i),S(r,i,e),x(r,r),!0)}function r(e,t,r,n,s,a,o){return i(e,t,s,a,o)||i(e,r,s,a,o)||i(e,n,s,a,o)}e.createSquareGeometry=function(e=t){const i=new Float64Array(12);for(let t=0;t<4;t++)for(let r=0;r<3;r++)i[3*t+r]=e[t][r];const r=new Uint32Array([0,1,2,2,3,0]),n=new Float32Array([0,0,1]),s=new Uint32Array([0,0,0,0,0,0]),a=new Float32Array([0,0,1,0,1,1,0,1]),o=new Uint8Array([255,255,255,255]),l={[Xe.POSITION]:r,[Xe.NORMAL]:s,[Xe.UV0]:r,[Xe.COLOR]:s},c={[Xe.POSITION]:{size:3,data:i},[Xe.NORMAL]:{size:3,data:n},[Xe.UV0]:{size:2,data:a},[Xe.COLOR]:{size:4,data:o}};return new et(c,l)},e.createConeGeometry=function(e,t,i,r,n=!0,s=!0){let a=0;const o=t,l=e;let c=Be(0,a,0),p=Be(0,a+l,0),d=Be(0,-1,0),h=Be(0,1,0);r&&(a=l,p=Be(0,0,0),c=Be(0,a,0),d=Be(0,1,0),h=Be(0,-1,0));const u=[p,c],f=[d,h],m=i+2,g=Math.sqrt(l*l+o*o);if(r)for(let e=i-1;e>=0;e--){const t=e*(2*Math.PI/i),r=Be(Math.cos(t)*o,a,Math.sin(t)*o);u.push(r);const n=Be(l*Math.cos(t)/g,-o/g,l*Math.sin(t)/g);f.push(n)}else for(let e=0;e<i;e++){const t=e*(2*Math.PI/i),r=Be(Math.cos(t)*o,a,Math.sin(t)*o);u.push(r);const n=Be(l*Math.cos(t)/g,o/g,l*Math.sin(t)/g);f.push(n)}const v=new Uint32Array(2*(i+2)*3),y=new Uint32Array(2*(i+2)*3);let O=0,b=0;if(n){for(let e=3;e<u.length;e++)v[O++]=1,v[O++]=e-1,v[O++]=e,y[b++]=0,y[b++]=0,y[b++]=0;v[O++]=u.length-1,v[O++]=2,v[O++]=1,y[b++]=0,y[b++]=0,y[b++]=0}if(s){for(let e=3;e<u.length;e++)v[O++]=e,v[O++]=e-1,v[O++]=0,y[b++]=e,y[b++]=e-1,y[b++]=1;v[O++]=0,v[O++]=2,v[O++]=u.length-1,y[b++]=1,y[b++]=2,y[b++]=f.length-1}const S=new Float32Array(3*m);for(let e=0;e<m;e++)S[3*e]=u[e][0],S[3*e+1]=u[e][1],S[3*e+2]=u[e][2];const x=new Float32Array(3*m);for(let e=0;e<m;e++)x[3*e]=f[e][0],x[3*e+1]=f[e][1],x[3*e+2]=f[e][2];const A={};A[Xe.POSITION]=v,A[Xe.NORMAL]=y;const P={};return P[Xe.POSITION]={size:3,data:S},P[Xe.NORMAL]={size:3,data:x},new et(P,A)},e.createCylinderGeometry=function(e,t,i,r,n,s){const a=r?Ge(r):Be(1,0,0),o=n?Ge(n):Be(0,0,0),l=void 0===s||s,c=We();x(c,a);const p=We();A(p,c,Math.abs(e));const d=We();A(d,p,-.5),P(d,d,o);const h=Be(0,1,0);Math.abs(1-_(c,h))<.2&&C(h,0,0,1);const u=We();S(u,c,h),x(u,u),S(h,u,c);const f=2*i+(l?2:0),m=i+(l?2:0),g=new Float32Array(3*f),v=new Float32Array(3*m),y=new Float32Array(2*f),O=new Uint32Array(3*i*(l?4:2)),b=new Uint32Array(3*i*(l?4:2));l&&(g[3*(f-2)+0]=d[0],g[3*(f-2)+1]=d[1],g[3*(f-2)+2]=d[2],y[2*(f-2)]=0,y[2*(f-2)+1]=0,g[3*(f-1)+0]=g[3*(f-2)+0]+p[0],g[3*(f-1)+1]=g[3*(f-2)+1]+p[1],g[3*(f-1)+2]=g[3*(f-2)+2]+p[2],y[2*(f-1)]=1,y[2*(f-1)+1]=1,v[3*(m-2)+0]=-c[0],v[3*(m-2)+1]=-c[1],v[3*(m-2)+2]=-c[2],v[3*(m-1)+0]=c[0],v[3*(m-1)+1]=c[1],v[3*(m-1)+2]=c[2]);const I=function(e,t,i){O[e]=t,b[e]=i};let D=0;const T=We(),w=We();for(let e=0;e<i;e++){const r=e*(2*Math.PI/i);A(T,h,Math.sin(r)),A(w,u,Math.cos(r)),P(T,T,w),v[3*e+0]=T[0],v[3*e+1]=T[1],v[3*e+2]=T[2],A(T,T,t),P(T,T,d),g[3*e+0]=T[0],g[3*e+1]=T[1],g[3*e+2]=T[2],y[2*e+0]=e/i,y[2*e+1]=0,g[3*(e+i)+0]=g[3*e+0]+p[0],g[3*(e+i)+1]=g[3*e+1]+p[1],g[3*(e+i)+2]=g[3*e+2]+p[2],y[2*(e+i)+0]=e/i,y[2*e+1]=1;const n=(e+1)%i;I(D++,e,e),I(D++,e+i,e),I(D++,n,n),I(D++,n,n),I(D++,e+i,e),I(D++,n+i,n)}if(l){for(let e=0;e<i;e++){const t=(e+1)%i;I(D++,f-2,m-2),I(D++,e,m-2),I(D++,t,m-2)}for(let e=0;e<i;e++){const t=(e+1)%i;I(D++,e+i,m-1),I(D++,f-1,m-1),I(D++,t+i,m-1)}}const E={};E[Xe.POSITION]=O,E[Xe.NORMAL]=b,E[Xe.UV0]=O;const R={};return R[Xe.POSITION]={size:3,data:g},R[Xe.NORMAL]={size:3,data:v},R[Xe.UV0]={size:2,data:y},new et(R,E)},e.createTubeGeometry=function(t,i,r,n,s){r=r||10,n=null==n||n,Ye(t.length>1);const a=[],o=[];for(let e=0;e<r;e++){a.push([0,-e-1,-(e+1)%r-1]);const t=e/r*2*Math.PI;o.push([Math.cos(t)*i,Math.sin(t)*i])}return e.createPathExtrusionGeometry(o,t,[[0,0,0]],a,n,s)},e.createPathExtrusionGeometry=function(e,t,i,n,s,a=Be(0,0,0)){const o=e.length,l=new Float32Array(t.length*o*3+(6*i.length||0)),c=new Float32Array(t.length*o+(2*i.length||0)),p=new Float32Array(t.length*o*3+(i?6:0)),d=(t.length-1)*o*6+3*n.length*2,h=new Uint32Array(d),u=new Uint32Array(d);let f=0,g=0,v=0,y=0,O=0;const D=We(),T=We(),w=We(),E=We(),R=We(),z=We(),j=We(),N=m(),L=We(),M=We(),U=We(),F=We(),V=We(),q=He.create();C(L,0,1,0),b(T,t[1],t[0]),x(T,T),s?(P(N,t[0],a),x(w,N)):C(w,0,0,1),r(T,w,L,L,R,w,ct),I(E,w),I(F,R);for(let e=0;e<i.length;e++)A(z,R,i[e][0]),A(N,w,i[e][2]),P(z,z,N),P(z,z,t[0]),l[f++]=z[0],l[f++]=z[1],l[f++]=z[2],c[v++]=0;p[g++]=-T[0],p[g++]=-T[1],p[g++]=-T[2];for(let e=0;e<n.length;e++)h[y++]=n[e][0]>0?n[e][0]:-n[e][0]-1+i.length,h[y++]=n[e][1]>0?n[e][1]:-n[e][1]-1+i.length,h[y++]=n[e][2]>0?n[e][2]:-n[e][2]-1+i.length,u[O++]=0,u[O++]=0,u[O++]=0;let B=i.length;const W=i.length-1;for(let i=0;i<t.length;i++){let n=!1;if(i>0){I(D,T),i<t.length-1?(b(T,t[i+1],t[i]),x(T,T)):n=!0,P(M,D,T),x(M,M),P(U,t[i-1],E),He.fromPositionAndNormal(t[i],M,q);He.intersectRay(q,Ze.wrap(U,D),N)?(b(N,N,t[i]),x(w,N),S(R,M,w),x(R,R)):r(M,E,F,L,R,w,ct),I(E,w),I(F,R)}s&&(P(N,t[i],a),x(V,N));for(let r=0;r<o;r++)if(A(z,R,e[r][0]),A(N,w,e[r][1]),P(z,z,N),x(j,z),p[g++]=j[0],p[g++]=j[1],p[g++]=j[2],c[v++]=s?_(z,V):z[2],P(z,z,t[i]),l[f++]=z[0],l[f++]=z[1],l[f++]=z[2],!n){const e=(r+1)%o;h[y++]=B+r,h[y++]=B+o+r,h[y++]=B+e,h[y++]=B+e,h[y++]=B+o+r,h[y++]=B+o+e;for(let e=0;e<6;e++)u[O++]=h[y-6+e]-W}B+=o}const G=t[t.length-1];for(let e=0;e<i.length;e++)A(z,R,i[e][0]),A(N,w,i[e][1]),P(z,z,N),P(z,z,G),l[f++]=z[0],l[f++]=z[1],l[f++]=z[2],c[v++]=0;const H=g/3;p[g++]=T[0],p[g++]=T[1],p[g++]=T[2];const Z=B-o;for(let e=0;e<n.length;e++)h[y++]=n[e][0]>=0?B+n[e][0]:-n[e][0]-1+Z,h[y++]=n[e][2]>=0?B+n[e][2]:-n[e][2]-1+Z,h[y++]=n[e][1]>=0?B+n[e][1]:-n[e][1]-1+Z,u[O++]=H,u[O++]=H,u[O++]=H;const k={};k[Xe.POSITION]=h,k[Xe.NORMAL]=u;const $={};return $[Xe.POSITION]={size:3,data:l},$.zOffset={size:1,data:c},$[Xe.NORMAL]={size:3,data:p},new et($,k)},e.createPolylineGeometry=function(e,t){Ye(e.length>1,"createPolylineGeometry(): polyline needs at least 2 points"),Ye(3===e[0].length,"createPolylineGeometry(): malformed vertex"),Ye(void 0===t||t.length===e.length,"createPolylineGeometry: need same number of points and normals"),Ye(void 0===t||3===t[0].length,"createPolylineGeometry(): malformed normal");const i=new Float64Array(3*e.length),r=new Uint32Array(2*(e.length-1));let n=0,s=0;for(let t=0;t<e.length;t++){for(let r=0;r<3;r++)i[n++]=e[t][r];t>0&&(r[s++]=t-1,r[s++]=t)}const a={},o={};if(a[Xe.POSITION]=r,o[Xe.POSITION]={size:3,data:i},t){const i=new Float32Array(3*t.length);let n=0;for(let r=0;r<e.length;r++)for(let e=0;e<3;e++)i[n++]=t[r][e];a[Xe.NORMAL]=r,o[Xe.NORMAL]={size:3,data:i}}return new et(o,a,"line")},e.createExtrudedTriangle=function(e,t,i,r){const n=new Float32Array(18),s=[[-t,0,r/2],[i,0,r/2],[0,e,r/2],[-t,0,-r/2],[i,0,-r/2],[0,e,-r/2]];for(let e=0;e<6;e++)n[3*e]=s[e][0],n[3*e+1]=s[e][1],n[3*e+2]=s[e][2];const a={[Xe.POSITION]:new Uint32Array([0,1,2,3,0,2,2,5,3,1,4,5,5,2,1,1,0,3,3,4,1,4,3,5])},o={[Xe.POSITION]:{size:3,data:n}};return new et(o,a)},e.transformInPlace=function(e,t){const i=e.vertexAttributes[Xe.POSITION].data;for(let e=0;e<i.length;e+=3){const r=i[e],n=i[e+1],s=i[e+2];C(pt,r,n,s),D(pt,pt,t),i[e]=pt[0],i[e+1]=pt[1],i[e+2]=pt[2]}},e.cgToGIS=function(e,t=e){const i=e.getVertexAttr(),r=i.position.data,n=i.normal.data,s=t.getVertexAttr(),a=s.position.data,o=s.normal.data;if(n)for(let e=0;e<n.length;e+=3){const t=n[e+1];o[e+1]=-n[e+2],o[e+2]=t}if(r)for(let e=0;e<r.length;e+=3){const t=r[e+1];a[e+1]=-r[e+2],a[e+2]=t}return t},e.makeOrthoBasisDirUp=i,e.makeOrthoBasisDirUpFallback=r}(lt||(lt={}));const ct=.99619469809,pt=We();var dt=lt;function ht(e){return"declaredClass"in e}function ut(e){return"declaredClass"in e}function ft(e,t){if(!e)return null;if(function(e){return"declaredClass"in e}(e))return e;const r=new M({layer:t,sourceLayer:t});return r.visible=e.visible,r.symbol=i(e.symbol),r.attributes=i(e.attributes),r.geometry=mt(e.geometry),r}function mt(e){return a(e)?null:ht(e)?e:N(function(e){const t=e.spatialReference.toJSON();switch(e.type){case"point":{const{x:i,y:r,z:n,m:s}=e;return{x:i,y:r,z:n,m:s,spatialReference:t}}case"polygon":{const{rings:i,hasZ:r,hasM:n}=e;return{rings:gt(i),hasZ:r,hasM:n,spatialReference:t}}case"polyline":{const{paths:i,hasZ:r,hasM:n}=e;return{paths:gt(i),hasZ:r,hasM:n,spatialReference:t}}case"extent":{const{xmin:i,xmax:r,ymin:n,ymax:s,zmin:a,zmax:o,mmin:l,mmax:c,hasZ:p,hasM:d}=e;return{xmin:i,xmax:r,ymin:n,ymax:s,zmin:a,zmax:o,mmin:l,mmax:c,hasZ:p,hasM:d,spatialReference:t}}case"multipoint":{const{points:i,hasZ:r,hasM:n}=e;return{points:yt(i)?vt(i):i,hasZ:r,hasM:n,spatialReference:t}}default:return}}(e))}function gt(e){return function(e){for(const t of e)if(0!==t.length)return yt(t);return!1}(e)?e.map((e=>vt(e))):e}function vt(e){return e.map((e=>s(e)))}function yt(e){return e.length&&(r(e[0])||n(e[0]))}function Ot(e,t){if(!e)return null;let i;if(ut(e)){if(null==t)return e.clone();if(ut(t))return t.copy(e)}return null!=t?(i=t,i.x=e.x,i.y=e.y,i.spatialReference=e.spatialReference,e.hasZ?(i.z=e.z,i.hasZ=e.hasZ):(i.z=null,i.hasZ=!1),e.hasM?(i.m=e.m,i.hasM=!0):(i.m=null,i.hasM=!1)):(i=Y(e.x,e.y,e.z,e.spatialReference),e.hasM&&(i.m=e.m,i.hasM=!0)),i}function bt(e,t){if("point"===e.type)return xt(e,t,!1);if(ht(e))switch(e.type){case"extent":return xt(e.center,t,!1);case"polygon":return xt(e.centroid,t,!1);case"polyline":return xt(St(e),t,!0);case"mesh":return xt(e.extent.center,t,!1)}else switch(e.type){case"extent":return xt(function(e){const t=u(e.zmin);return Y(.5*(e.xmax+e.xmin),.5*(e.ymax+e.ymin),t?.5*(e.zmax+e.zmin):void 0,e.spatialReference)}(e),t,!0);case"polygon":return xt(function(e){const t=e.rings[0];if(!t||0===t.length)return null;const i=j(e.rings,e.hasZ);return Y(i[0],i[1],i[2],e.spatialReference)}(e),t,!0);case"polyline":return xt(St(e),t,!0)}}function St(e){const t=e.paths[0];if(!t||0===t.length)return null;const i=R(t,z(t)/2);return Y(i[0],i[1],i[2],e.spatialReference)}function xt(e,t,i){const r=i?e:Ot(e);return t&&e?B(e,r,t)?r:null:r}function At(e,t,i,r=0){if(e){t||(t=q());const n=e;let s=.5*n.width*(i-1),a=.5*n.height*(i-1);return n.width<1e-7*n.height?s+=a/20:n.height<1e-7*n.width&&(a+=s/20),$(t,n.xmin-s-r,n.ymin-a-r,n.xmax+s+r,n.ymax+a+r),t}return null}function Pt(e,t){for(let i=0;i<e.geometries.length;++i){const r=e.geometries[i].data.vertexAttributes.auxpos1;r&&r.data[3]!==t&&(r.data[3]=t,e.geometryVertexAttrsUpdated(i))}}function _t(e,t){const i=W(G);return o(e)&&(i[0]=e[0],i[1]=e[1],i[2]=e[2]),o(t)?i[3]=t:o(e)&&e.length>3&&(i[3]=e[3]),i}function Ct(e,t,i,r,n,s=[0,0,0,0]){for(let t=0;t<3;++t)o(e)&&null!=e[t]?s[t]=e[t]:o(i)&&null!=i[t]?s[t]=i[t]:s[t]=n[t];return o(t)?s[3]=t:o(r)?s[3]=r:s[3]=n[3],s}function It(e=v,t,i,r=1){const n=new Array(3);if(a(t)||a(i))n[0]=1,n[1]=1,n[2]=1;else{let r,s=0;for(let a=2;a>=0;a--){const o=e[a];let l;const c=null!=o,p=0===a&&!r&&!c,d=i[a];"symbol-value"===o||p?l=0!==d?t[a]/d:1:c&&"proportional"!==o&&isFinite(o)&&(l=0!==d?o/d:1),null!=l&&(n[a]=l,r=l,s=Math.max(s,Math.abs(l)))}for(let e=2;e>=0;e--)null==n[e]?n[e]=r:0===n[e]&&(n[e]=.001*s)}for(let e=2;e>=0;e--)n[e]/=r;return g(n)}function Dt(e){return null!=e.isPrimitive&&(e=[e.width,e.depth,e.height]),Tt(e)?null:"Symbol sizes may not be negative values"}function Tt(e){if(Array.isArray(e)){for(const t of e)if(!Tt(t))return!1;return!0}return null==e||e>=0}function wt(e,t,i,r=H()){const n=e||0,s=t||0,a=i||0;return 0!==n&&U(r,r,-n/180*Math.PI),0!==s&&F(r,r,s/180*Math.PI),0!==a&&V(r,r,a/180*Math.PI),r}function Et(e,t){return null!=t.minDemResolution?t.minDemResolution:J(e)?t.minDemResolutionForPoints:.01*X(e)}const Rt={"bottom-left":Q(0,0),bottom:Q(.5,0),"bottom-right":Q(1,0),left:Q(0,.5),center:Q(.5,.5),right:Q(1,.5),"top-left":Q(0,1),top:Q(.5,1),"top-right":Q(1,1)},zt=qe().vec3f(Xe.POSITION),jt=qe().vec3f(Xe.POSITION).vec2f(Xe.UV0),Nt=qe().vec3f(Xe.POSITION).vec4u8(Xe.COLOR),Lt=(qe().vec3f(Xe.POSITION).vec4u8(Xe.COLOR).vec4f(Xe.UVMAPSPACE).vec3f(Xe.MEANVERTEXPOSITION),qe().vec3f(Xe.POSITION).vec4u8(Xe.COLOR).vec4f(Xe.UVMAPSPACE).vec3f(Xe.BOUND1).vec3f(Xe.BOUND2).vec3f(Xe.BOUND3));class Mt{constructor(e){this.vertexBufferLayout=e}allocate(e){return this.vertexBufferLayout.createBuffer(e)}elementCount(e){return e.indices[Xe.POSITION].length}write(e,t,i,r){K(t,this.vertexBufferLayout,e.transformation,e.invTranspTransformation,i,r)}}class Ut{constructor(e,t){this._objectToBoundingSphere=e,this._maximumObjectsPerNode=10,this._maximumDepth=20,this._degenerateObjects=new Set,this._objectCount=0,t&&(void 0!==t.maximumObjectsPerNode&&(this._maximumObjectsPerNode=t.maximumObjectsPerNode),void 0!==t.maximumDepth&&(this._maximumDepth=t.maximumDepth)),this._root=new Ft(null,y(0,0,0),0)}get center(){return this._root.center}get size(){return 2*this._root.halfSize}get root(){return this._root.node}get maximumObjectsPerNode(){return this._maximumObjectsPerNode}get maximumDepth(){return this._maximumDepth}get objectCount(){return this._objectCount}destroy(){this._degenerateObjects.clear(),this._root=null,Ft.clearPool(),Jt[0]=null,ti.prune(),oi.prune(),li.prune()}add(e,t){const i=Xt(e);t=null==t?i.length:t,this._objectCount+=t,this._grow(i,t);const r=Ft.acquire();for(let e=0;e<t;e++){const t=i[e];this._isDegenerate(t)?this._degenerateObjects.add(t):(r.init(this._root),this._add(t,r))}Ft.release(r)}remove(e,t){const i=Xt(e);this._objectCount-=i.length;const r=Ft.acquire();for(const e of i){const i=t||this._boundingSphereFromObject(e,ii);this._isValidRadius(i.radius)?(r.init(this._root),this._remove(e,i,r)):this._degenerateObjects.delete(e)}Ft.release(r),this._shrink()}update(e,t){!this._isValidRadius(t.radius)&&this._isDegenerate(e)||(this.remove(e,t),this.add(e))}forEachAlongRay(e,t,i){const r=Ze.wrap(e,t);this._forEachNode(this._root,(e=>{if(!this._intersectsNode(r,e))return!1;const t=e.node;return t.terminals.forAll((e=>{this._intersectsObject(r,e)&&i(e)})),null!==t.residents&&t.residents.forAll((e=>{this._intersectsObject(r,e)&&i(e)})),!0}))}forEachAlongRayWithVerticalOffset(e,t,i,r){const n=Ze.wrap(e,t);this._forEachNode(this._root,(e=>{if(!this._intersectsNodeWithOffset(n,e,r))return!1;const t=e.node;return t.terminals.forAll((e=>{this._intersectsObjectWithOffset(n,e,r)&&i(e)})),null!==t.residents&&t.residents.forAll((e=>{this._intersectsObjectWithOffset(n,e,r)&&i(e)})),!0}))}forEach(e){this._forEachNode(this._root,(t=>{const i=t.node;return i.terminals.forAll(e),null!==i.residents&&i.residents.forAll(e),!0})),this._degenerateObjects.forEach(e)}forEachDegenerateObject(e){this._degenerateObjects.forEach(e)}findClosest(e,t,i,r,n){return this._findClosest(e,"front-to-back"===t?1:-1,i,r,n)}forEachInDepthRange(e,t,i,r,n,s,a,o){this._forEachInDepthRange(e,t,"front-to-back"===i?1:-1,r,n,s,a,o)}forEachNode(e){this._forEachNode(this._root,(t=>e(t.node,t.center,2*t.halfSize)))}_intersectsNode(e,t){return Bt(t.center,2*-t.halfSize,Kt),Bt(t.center,2*t.halfSize,ei),Qe(e.origin,e.direction,Kt,ei)}_intersectsNodeWithOffset(e,t,i){return Bt(t.center,2*-t.halfSize,Kt),Bt(t.center,2*t.halfSize,ei),i.applyToMinMax(Kt,ei),Qe(e.origin,e.direction,Kt,ei)}_intersectsObject(e,t){const i=this._objectToBoundingSphere.getRadius(t);return!(i>0)||ke.intersectsRay(ke.wrap(i,this._objectToBoundingSphere.getCenter(t)),e)}_intersectsObjectWithOffset(e,t,i){const r=this._objectToBoundingSphere.getRadius(t);return!(r>0)||ke.intersectsRay(i.applyToBoundingSphere(r,this._objectToBoundingSphere.getCenter(t)),e)}_forEachNode(e,t){let i=Ft.acquire().init(e);const r=[i];for(;0!==r.length;){if(i=r.pop(),t(i)&&!i.isLeaf())for(let e=0;e<i.node.children.length;e++){i.node.children[e]&&r.push(Ft.acquire().init(i).advance(e))}Ft.release(i)}}_forEachNodeDepthOrdered(e,t,i,r=1){let n=Ft.acquire().init(e);const s=[n];for(!function(e,t,i){if(!oi.length)for(let e=0;e<8;++e)oi.push({index:0,distance:0});for(let i=0;i<8;++i){const r=Zt[i];oi.data[i].index=i,oi.data[i].distance=Ht(e,t,r)}oi.sort(((e,t)=>e.distance-t.distance)),i.clear();for(let e=0;e<8;++e)i.push(oi.data[e].index)}(i,r,li);0!==s.length;){if(n=s.pop(),t(n)&&!n.isLeaf())for(let e=7;e>=0;--e){const t=li.data[e];if(t>=n.node.children.length)continue;n.node.children[t]&&s.push(Ft.acquire().init(n).advance(t))}Ft.release(n)}}_findClosest(e,t,i,r,n){let s=1/0,a=1/0,o=null;const l=Gt(e,t);let c=0;const p=n=>{if(++c,r&&!r(n))return;const l=this._objectToBoundingSphere.getCenter(n),p=this._objectToBoundingSphere.getRadius(n);if(i&&Wt(l,p,i))return;const d=Ht(e,t,l),h=d-p;h<s&&(s=h,a=d+p,o=n)};return this._forEachNodeDepthOrdered(this._root,(r=>{if(null!=n&&c>=n)return!1;if(i&&Wt(r.center,r.halfSize*$t,i))return!1;A(Qt,l,r.halfSize),P(Qt,Qt,r.center);if(Ht(e,t,Qt)>a)return!1;const s=r.node;return s.terminals.forAll((e=>{p(e)})),null!==s.residents&&s.residents.forAll((e=>{p(e)})),!0}),e,t),o}_forEachInDepthRange(e,t,i,r,n,s,a,o){let l=-1/0,c=1/0;const p={setRange:e=>{1===i?(l=Math.max(l,e.near),c=Math.min(c,e.far)):(l=Math.max(l,-e.far),c=Math.min(c,-e.near))}};p.setRange(r);const d=Ht(t,i,e),h=Gt(t,i),u=Gt(t,-1*i);let f=0;const m=e=>{if(++f,a&&!a(e))return;const r=this._objectToBoundingSphere.getCenter(e),o=this._objectToBoundingSphere.getRadius(e),h=Ht(t,i,r)-d;h-o>c||h+o<l||s&&Wt(r,o,s)||n(e,p)};this._forEachNodeDepthOrdered(this._root,(e=>{if(null!=o&&f>=o)return!1;if(s&&Wt(e.center,e.halfSize*$t,s))return!1;A(Qt,h,e.halfSize),P(Qt,Qt,e.center);if(Ht(t,i,Qt)-d>c)return!1;A(Qt,u,e.halfSize),P(Qt,Qt,e.center);if(Ht(t,i,Qt)-d<l)return!1;const r=e.node;return r.terminals.forAll((e=>{m(e)})),null!==r.residents&&r.residents.forAll((e=>{m(e)})),!0}),t,i)}_remove(e,t,i){ti.clear();const r=i.advanceTo(t,((e,t)=>{ti.push(e.node),ti.push(t)}))?i.node.terminals:i.node.residents;if(r.removeUnordered(e),0===r.length)for(let e=ti.length-2;e>=0;e-=2){const t=ti.data[e],i=ti.data[e+1];if(!this._purge(t,i))break}}_nodeIsEmpty(e){if(0!==e.terminals.length)return!1;if(null!==e.residents)return 0===e.residents.length;for(let t=0;t<e.children.length;t++)if(e.children[t])return!1;return!0}_purge(e,t){return t>=0&&(e.children[t]=null),!!this._nodeIsEmpty(e)&&(null===e.residents&&(e.residents=new d({shrink:!0})),!0)}_add(e,t){t.advanceTo(this._boundingSphereFromObject(e,ii))?t.node.terminals.push(e):(t.node.residents.push(e),t.node.residents.length>this._maximumObjectsPerNode&&t.depth<this._maximumDepth&&this._split(t))}_split(e){const t=e.node.residents;e.node.residents=null;for(let i=0;i<t.length;i++){const r=Ft.acquire().init(e);this._add(t.data[i],r),Ft.release(r)}}_grow(e,t){if(0===t)return;const i=this._boundingSphereFromObjects(e,t,((e,t)=>this._boundingSphereFromObject(e,t)),ri);if(this._isValidRadius(i.radius)&&!this._fitsInsideTree(i))if(this._nodeIsEmpty(this._root.node))I(this._root.center,i.center),this._root.halfSize=1.25*i.radius;else{const e=Ft.acquire();this._rootBoundsForRootAsSubNode(i,e),this._placingRootViolatesMaxDepth(e)?this._rebuildTree(i,e):this._growRootAsSubNode(e),Ft.release(e)}}_rebuildTree(e,t){I(ni.center,t.center),ni.radius=t.halfSize;const i=this._boundingSphereFromObjects([e,ni],2,(e=>e),si),r=Ft.acquire().init(this._root);this._root.initFrom(null,i.center,1.25*i.radius),this._forEachNode(r,(e=>(this.add(e.node.terminals.data,e.node.terminals.length),null!==e.node.residents&&this.add(e.node.residents.data,e.node.residents.length),!0))),Ft.release(r)}_placingRootViolatesMaxDepth(e){let t=0;this._forEachNode(this._root,(e=>(t=Math.max(t,e.depth),!0)));return t+Math.log(e.halfSize/this._root.halfSize)*Math.LOG2E>this._maximumDepth}_rootBoundsForRootAsSubNode(e,t){const i=e.radius,r=e.center;let n=-1/0;const s=this._root.center,a=this._root.halfSize;for(let e=0;e<3;e++){const t=s[e]-a-(r[e]-i),o=r[e]+i-(s[e]+a),l=Math.max(0,Math.ceil(t/(2*a))),c=Math.max(0,Math.ceil(o/(2*a)))+1,p=Math.pow(2,Math.ceil(Math.log(l+c)*Math.LOG2E));n=Math.max(n,p),ai[e].min=l,ai[e].max=c}for(let e=0;e<3;e++){let t=ai[e].min,i=ai[e].max;const r=(n-(t+i))/2;t+=Math.ceil(r),i+=Math.floor(r);const o=s[e]-a-t*a*2;Yt[e]=o+(i+t)*a}return t.initFrom(null,Yt,n*a,0)}_growRootAsSubNode(e){const t=this._root.node;I(ri.center,this._root.center),ri.radius=this._root.halfSize,this._root.init(e),e.advanceTo(ri,null,!0),e.node.children=t.children,e.node.residents=t.residents,e.node.terminals=t.terminals}_shrink(){for(;;){const e=this._findShrinkIndex();if(-1===e)break;this._root.advance(e),this._root.depth=0}}_findShrinkIndex(){if(0!==this._root.node.terminals.length||this._root.isLeaf())return-1;let e=null;const t=this._root.node.children;let i=0,r=0;for(;r<t.length&&null==e;)i=r++,e=t[i];for(;r<t.length;)if(t[r++])return-1;return i}_isDegenerate(e){const t=this._objectToBoundingSphere.getRadius(e);return!this._isValidRadius(t)}_isValidRadius(e){return!isNaN(e)&&e!==-1/0&&e!==1/0&&e>0}_fitsInsideTree(e){const t=this._root.center,i=this._root.halfSize,r=e.center;return e.radius<=i&&r[0]>=t[0]-i&&r[0]<=t[0]+i&&r[1]>=t[1]-i&&r[1]<=t[1]+i&&r[2]>=t[2]-i&&r[2]<=t[2]+i}_boundingSphereFromObject(e,t){return I(t.center,this._objectToBoundingSphere.getCenter(e)),t.radius=this._objectToBoundingSphere.getRadius(e),t}_boundingSphereFromObjects(e,t,i,r){if(1===t){const t=i(e[0],ri);I(r.center,t.center),r.radius=t.radius}else{Kt[0]=1/0,Kt[1]=1/0,Kt[2]=1/0,ei[0]=-1/0,ei[1]=-1/0,ei[2]=-1/0;for(let r=0;r<t;r++){const t=i(e[r],ri);this._isValidRadius(t.radius)&&(Vt(Kt,t.center,t.radius),qt(ei,t.center,t.radius))}T(r.center,Kt,ei,.5),r.radius=Math.max(ei[0]-Kt[0],ei[1]-Kt[1],ei[2]-Kt[2])/2}return r}}class Ft{constructor(e,t,i=0){this.center=m(),this.initFrom(e,t,i,0)}init(e){return this.initFrom(e.node,e.center,e.halfSize,e.depth)}initFrom(e=null,t,i=this.halfSize,r=this.depth){return this.node=e||Ft.createEmptyNode(),t&&I(this.center,t),this.halfSize=i,this.depth=r,this}advance(e){let t=this.node.children[e];t||(t=Ft.createEmptyNode(),this.node.children[e]=t),this.node=t,this.halfSize/=2,this.depth++;const i=Zt[e];return this.center[0]+=i[0]*this.halfSize,this.center[1]+=i[1]*this.halfSize,this.center[2]+=i[2]*this.halfSize,this}advanceTo(e,t,i=!1){for(;;){if(this.isTerminalFor(e))return t&&t(this,-1),!0;if(this.isLeaf()&&!i)return t&&t(this,-1),!1;this.isLeaf()&&(this.node.residents=null);const r=this._childIndex(e);t&&t(this,r),this.advance(r)}}isLeaf(){return null!=this.node.residents}isTerminalFor(e){return e.radius>this.halfSize/2}_childIndex(e){const t=e.center,i=this.center;let r=0;for(let e=0;e<3;e++)i[e]<t[e]&&(r|=1<<e);return r}static createEmptyNode(){return{children:[null,null,null,null,null,null,null,null],terminals:new d({shrink:!0}),residents:new d({shrink:!0})}}static acquire(){return Ft._pool.acquire()}static release(e){Ft._pool.release(e)}static clearPool(){Ft._pool.prune()}}function Vt(e,t,i){e[0]=Math.min(e[0],t[0]-i),e[1]=Math.min(e[1],t[1]-i),e[2]=Math.min(e[2],t[2]-i)}function qt(e,t,i){e[0]=Math.max(e[0],t[0]+i),e[1]=Math.max(e[1],t[1]+i),e[2]=Math.max(e[2],t[2]+i)}function Bt(e,t,i){return(i=i||e)[0]=e[0]+t,i[1]=e[1]+t,i[2]=e[2]+t,i}function Wt(e,t,i){return!$e.intersectsSphere(i.planes,ke.wrap(t,e))}function Gt(e,t){let i=1/0,r=null;for(let n=0;n<8;++n){const s=Ht(e,t,kt[n]);s<i&&(i=s,r=kt[n])}return r}function Ht(e,t,i){return t*(e[0]*i[0]+e[1]*i[1]+e[2]*i[2])}Ft._pool=new t(Ft);const Zt=[y(-1,-1,-1),y(1,-1,-1),y(-1,1,-1),y(1,1,-1),y(-1,-1,1),y(1,-1,1),y(-1,1,1),y(1,1,1)],kt=[y(-1,-1,-1),y(-1,-1,1),y(-1,1,-1),y(-1,1,1),y(1,-1,-1),y(1,-1,1),y(1,1,-1),y(1,1,1)],$t=Math.sqrt(3),Jt=[null];function Xt(e){return Array.isArray(e)?e:(Jt[0]=e,Jt)}const Yt=m(),Qt=m(),Kt=m(),ei=m(),ti=new d,ii={center:m(),radius:0},ri={center:m(),radius:0},ni={center:m(),radius:0},si={center:m(),radius:0},ai=[{min:0,max:0},{min:0,max:0},{min:0,max:0}],oi=new d,li=new d;class ci extends h{constructor(e,t,i){super(),this._parentStages=new Map,this._objects=new Set,this.id=ci._idGen.gen(e),this.apiLayerUid=i,this.name=e,t=t||{},this.group=t.group||"",this.isVisible=null==t.isVisible||t.isVisible,this.isPickable=null==t.isPickable||t.isPickable,this.isSliceable=!1,this.translation=t.translation?O(t.translation):m()}addParentStage(e){if(!this._parentStages.has(e)){const t=this.on("dirty",(t=>{e.notifyDirty(t.origin,t.dirtyType,t.subObject)}));this._parentStages.set(e,t)}}removeParentStage(e){const t=this._parentStages.get(e);t&&(t.remove(),this._parentStages.delete(e)),this.invalidateSpatialQueryAccelerator()}getName(){return this.name}getGroup(){return this.group}getTranslation(){return this.translation}getObjectIds(){return Array.from(this._objects,(e=>e.id))}getObjects(){return[...this._objects]}addObject(e){this._objects.add(e),e.parentLayer=this,this.notifyDirty("layerObjectAdded",e),this._octree&&this._octree.add(e)}hasObject(e){return this._objects.has(e)}removeObject(e){return!!this._objects.delete(e)&&(e.parentLayer=null,this.notifyDirty("layerObjectRemoved",e),this._octree&&this._octree.remove(e),!0)}notifyObjectBBChanged(e,t){this._octree&&this._octree.update(e,t)}getSpatialQueryAccelerator(){return!this._octree&&this._objects.size>50&&this._createOctree(),this._octree}shaderTransformationChanged(){this.notifyDirty("shaderTransformationChanged",null)}invalidateSpatialQueryAccelerator(){this._octree&&(this._octree.destroy(),this._octree=null)}notifyDirty(e,t,i,r){const n={origin:r||this,dirtyType:e,subObject:t};this.emit("dirty",n)}_createOctree(){this._octree=new Ut({getRadius:e=>e.getBSRadius(),getCenter:e=>e.getCenter()}),this._octree.add([...this._objects])}}var pi;ci._idGen=new tt,function(e){e.Default={vvSizeEnabled:!1,vvSizeMinSize:Be(1,1,1),vvSizeMaxSize:Be(100,100,100),vvSizeOffset:Be(0,0,0),vvSizeFactor:Be(1,1,1),vvSizeValue:Be(1,1,1),vvColorEnabled:!1,vvColorValues:[0,0,0,0,0,0,0,0],vvColorColors:[1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0],vvOpacityEnabled:!1,vvOpacityValues:[0,0,0,0,0,0,0,0],vvOpacityOpacities:[1,1,1,1,1,1,1,1],vvSymbolAnchor:[0,0,0],vvSymbolRotationMatrix:Z()}}(pi||(pi={}));var di=pi;function hi(e,t){e.vertex.uniforms.add("intrinsicWidth","float"),t.vvSize?(e.attributes.add("sizeFeatureAttribute","float"),e.vertex.uniforms.add("vvSizeMinSize","vec3"),e.vertex.uniforms.add("vvSizeMaxSize","vec3"),e.vertex.uniforms.add("vvSizeOffset","vec3"),e.vertex.uniforms.add("vvSizeFactor","vec3"),e.vertex.code.add(ee`
    float getSize() {
      return intrinsicWidth * clamp(vvSizeOffset + sizeFeatureAttribute * vvSizeFactor, vvSizeMinSize, vvSizeMaxSize).x;
    }
    `)):(e.attributes.add("size","float"),e.vertex.code.add(ee`
    float getSize(){
      return intrinsicWidth * size;
    }
    `)),t.vvOpacity?(e.attributes.add("opacityFeatureAttribute","float"),e.vertex.defines.addInt("VV_OPACITY_N",8),e.vertex.code.add(ee`
    uniform float vvOpacityValues[VV_OPACITY_N];
    uniform float vvOpacityOpacities[VV_OPACITY_N];

    float interpolateOpacity( float value ){
      if (value <= vvOpacityValues[0]) {
        return vvOpacityOpacities[0];
      }

      for (int i = 1; i < VV_OPACITY_N; ++i) {
        if (vvOpacityValues[i] >= value) {
          float f = (value - vvOpacityValues[i-1]) / (vvOpacityValues[i] - vvOpacityValues[i-1]);
          return mix(vvOpacityOpacities[i-1], vvOpacityOpacities[i], f);
        }
      }

      return vvOpacityOpacities[VV_OPACITY_N - 1];
    }

    vec4 applyOpacity( vec4 color ){
      return vec4(color.xyz, interpolateOpacity(opacityFeatureAttribute));
    }
    `)):e.vertex.code.add(ee`
    vec4 applyOpacity( vec4 color ){
      return color;
    }
    `),t.vvColor?(e.attributes.add("colorFeatureAttribute","float"),e.vertex.defines.addInt("VV_COLOR_N",8),e.vertex.code.add(ee`
    uniform float vvColorValues[VV_COLOR_N];
    uniform vec4 vvColorColors[VV_COLOR_N];

    vec4 interpolateColor( float value ) {
      if (value <= vvColorValues[0]) {
        return vvColorColors[0];
      }

      for (int i = 1; i < VV_COLOR_N; ++i) {
        if (vvColorValues[i] >= value) {
          float f = (value - vvColorValues[i-1]) / (vvColorValues[i] - vvColorValues[i-1]);
          return mix(vvColorColors[i-1], vvColorColors[i], f);
        }
      }

      return vvColorColors[VV_COLOR_N - 1];
    }

    vec4 getColor(){
      return applyOpacity(interpolateColor(colorFeatureAttribute));
    }
    `)):(e.attributes.add("color","vec4"),e.vertex.code.add(ee`
    vec4 getColor(){
      return applyOpacity(color);
    }
    `))}function ui(e,t){e.defines.addFloat("STIPPLE_ALPHA_COLOR_DISCARD",.001),e.defines.addFloat("STIPPLE_ALPHA_HIGHLIGHT_DISCARD",.5),t.stippleEnabled?function(e,t){e.vertex.uniforms.add("stipplePatternPixelSizeInv","float"),t.stippleUVMaxEnabled&&e.varyings.add("stipplePatternUvMax","float");e.varyings.add("stipplePatternUv","float"),e.fragment.uniforms.add("stipplePatternTexture","sampler2D"),t.stippleOffColorEnabled&&e.fragment.uniforms.add("stippleOffColor","vec4");e.fragment.code.add(ee`
  float getStippleAlpha() {
    float stipplePatternUvClamped = stipplePatternUv * gl_FragCoord.w;
    ${t.stippleUVMaxEnabled?"stipplePatternUvClamped = clamp(stipplePatternUvClamped, 0.0, stipplePatternUvMax);":""}

    return texture2D(stipplePatternTexture, vec2(mod(stipplePatternUvClamped, 1.0), 0.5)).a;
  }`),t.stippleOffColorEnabled?e.fragment.code.add(ee`
    #define discardByStippleAlpha(stippleAlpha, threshold) {}
    #define blendStipple(color, stippleAlpha) mix(color, stippleOffColor, stippleAlpha)
    `):e.fragment.code.add(ee`
    #define discardByStippleAlpha(stippleAlpha, threshold) if (stippleAlpha < threshold) { discard; }
    #define blendStipple(color, stippleAlpha) vec4(color.rgb, color.a * stippleAlpha)
    `)}(e,t):function(e){e.fragment.code.add(ee`
  float getStippleAlpha() { return 1.0; }

  #define discardByStippleAlpha(_stippleAlpha_, _threshold_) {}
  #define blendStipple(color, _stippleAlpha_) color
  `)}(e)}var fi=Object.freeze({__proto__:null,build:function(e){const t=new te;t.extensions.add("GL_OES_standard_derivatives"),t.include(ie),t.include(hi,e),t.include(ui,e),1===e.output&&t.include(re,e),t.vertex.uniforms.add("proj","mat4").add("view","mat4").add("nearFar","vec2").add("pixelRatio","float").add("miterLimit","float").add("screenSize","vec2"),t.attributes.add("position","vec3"),t.attributes.add("subdivisionFactor","float"),t.attributes.add("uv0","vec2"),t.attributes.add("auxpos1","vec3"),t.attributes.add("auxpos2","vec3"),t.varyings.add("vColor","vec4"),t.varyings.add("vpos","vec3"),t.varyings.add("linearDepth","float");const i=e.falloffEnabled,r=e.innerColorEnabled;return r&&t.varyings.add("vLineDistance","float"),i&&t.varyings.add("vLineDistanceNorm","float"),e.falloffEnabled&&t.fragment.uniforms.add("falloff","float"),e.innerColorEnabled&&(t.fragment.uniforms.add("innerColor","vec4"),t.fragment.uniforms.add("innerWidth","float")),t.vertex.code.add(ee`
		#define PERPENDICULAR(v) vec2(v.y, -v.x);
		#define ISOUTSIDE (left.x * right.y - left.y * right.x)*uv0.y > 0.0

		float interp(float ncp, vec4 a, vec4 b) {
			return (-ncp - a.z) / (b.z - a.z);
		}

		vec2 rotate(vec2 v, float a) {
			float s = sin(a);
			float c = cos(a);
			mat2 m = mat2(c, -s, s, c);
			return m * v;
		}
`),t.vertex.code.add(ee`
    vec4 projectAndScale(vec4 pos) {
      vec4 posNdc = proj * pos;

      // Note that posNdc is in -1:1, scaling by screenSize converts this to a coordinate system
      // that is twice scaled (going from -size:size).
      posNdc.xy *= screenSize / posNdc.w;
      return posNdc;
    }
`),t.vertex.code.add(ee`
    void clipAndTransform(inout vec4 pos, inout vec4 prev, inout vec4 next, in bool isStartVertex) {
      float vnp = nearFar[0] * 0.99;

      //current pos behind ncp --> we need to clip
      if(pos.z > -nearFar[0]) {
        if (!isStartVertex) {
          //previous in front of ncp
          if(prev.z < -nearFar[0]) {
            pos = mix(prev, pos, interp(vnp, prev, pos));
            next = pos;
          } else {
            pos = vec4(0.0, 0.0, 0.0, 1.0);
          }
        }
        //next in front of ncp
        if(isStartVertex) {
          if(next.z < -nearFar[0]) {
            pos = mix(pos, next, interp(vnp, pos, next));
            prev = pos;
          } else {
            pos = vec4(0.0, 0.0, 0.0, 1.0);
          }
        }
      } else {
        //current position visible
        //previous behind ncp
        if (prev.z > -nearFar[0]) {
          prev = mix(pos, prev, interp(vnp, pos, prev));
        }
        //next behind ncp
        if (next.z > -nearFar[0]) {
          next = mix(next, pos, interp(vnp, next, pos));
        }
      }

      linearDepth = (-pos.z - nearFar[0]) / (nearFar[1] - nearFar[0]);

      pos = projectAndScale(pos);
      next = projectAndScale(next);
      prev = projectAndScale(prev);
    }
`),t.vertex.code.add(ee`
  void main(void) {
    float coverage = 1.0;
    vpos = position;

    // Check for special value of uv0.y which is used by the Renderer when graphics
    // are removed before the VBO is recompacted. If this is the case, then we just
    // project outside of clip space.
    if (uv0.y == 0.0) {
      // Project out of clip space
      gl_Position = vec4(1e038, 1e038, 1e038, 1.0);
    }
    else {
      bool isStartVertex = abs(abs(uv0.y)-3.0) == 1.0;
      bool isJoin = abs(uv0.y)-3.0 < 0.0;

      float lineWidth = getSize() * pixelRatio;

      // convert sub-pixel coverage to alpha
      if (lineWidth < 1.0) {
        coverage = lineWidth;
        lineWidth = 1.0;
      }else{
        // Ribbon lines cannot properly render non-integer sizes. Round width to integer size if
        // larger than one for better quality. Note that we do render < 1 pixels more or less correctly
        // so we only really care to round anything larger than 1.
        lineWidth = floor(lineWidth + 0.5);
      }

      vec4 pos  = view * vec4(position.xyz, 1.0);
      vec4 prev = view * vec4(auxpos1.xyz, 1.0);
      vec4 next = view * vec4(auxpos2.xyz, 1.0);

      clipAndTransform(pos, prev, next, isStartVertex);

      vec2 left = (pos.xy - prev.xy);
      vec2 right = (next.xy - pos.xy);

      float leftLen = length(left);
      float rightLen = length(right);
  `),e.stippleEnabled&&t.vertex.code.add(ee`
      // uv0.x is either 0 or 1, depending on whether this is considered the start of a line segment
      // or the end. If start, then use pos->next, otherwise use prev->pos to define the line segment
      // vector
      vec4 stippleSegmentInfo = mix(vec4(pos.xy, right), vec4(prev.xy, left), uv0.x);
      vec2 stippleSegmentOrigin = stippleSegmentInfo.xy;

      // Scale s.t. it's in units of stipple pattern size.
      vec2 stippleSegmentDirection = stippleSegmentInfo.zw;
    `),t.vertex.code.add(ee`
    left = (leftLen > 0.001) ? left/leftLen : vec2(0.0, 0.0);
    right = (rightLen > 0.001) ? right/rightLen : vec2(0.0, 0.0);

    vec2 capDisplacementDir = vec2(0, 0);
    vec2 joinDisplacementDir = vec2(0, 0);
    float displacementLen = lineWidth;

    if (isJoin) {

      // JOIN handling ---------------------------------------------------
      // determine if vertex is on the "outside or "inside" of the join
      bool isOutside = ISOUTSIDE;

      // compute miter join position first
      joinDisplacementDir = normalize(left + right);
      joinDisplacementDir = PERPENDICULAR(joinDisplacementDir);

      // compute miter stretch
      if (leftLen > 0.001 && rightLen > 0.001) {
        float nDotSeg = dot(joinDisplacementDir, left);
        displacementLen /= length(nDotSeg * left - joinDisplacementDir);

        // limit displacement of inner vertices
        if (!isOutside) {
          displacementLen = min(displacementLen, min(leftLen, rightLen)/abs(nDotSeg));
        }
      }

      if (isOutside && (displacementLen > miterLimit * lineWidth)) {
    `),e.roundJoins?t.vertex.code.add(ee`
        vec2 startDir;
        vec2 endDir;

        if (leftLen < 0.001) {
          startDir = right;
        }
        else{
          startDir = left;
        }
        startDir = normalize(startDir);
        startDir = PERPENDICULAR(startDir);

        if (rightLen < 0.001) {
          endDir = left;
        }
        else{
          endDir = right;
        }
        endDir = normalize(endDir);
        endDir = PERPENDICULAR(endDir);

        float rotationAngle = acos(clamp(dot(startDir, endDir), -1.0, 1.0));
        joinDisplacementDir = rotate(startDir, -sign(uv0.y) * subdivisionFactor * rotationAngle);
      `):t.vertex.code.add(ee`
        // convert to bevel join if miterLimit is exceeded
        if (leftLen < 0.001) {
          joinDisplacementDir = right;
        }
        else if (rightLen < 0.001) {
          joinDisplacementDir = left;
        }
        else {
          joinDisplacementDir = isStartVertex ? right : left;
        }
        joinDisplacementDir = normalize(joinDisplacementDir);
        joinDisplacementDir = PERPENDICULAR(joinDisplacementDir);
  `),t.vertex.code.add(ee`
        displacementLen = lineWidth;
      }
    } else {
    // CAP handling ---------------------------------------------------
    if (leftLen < 0.001) {
      joinDisplacementDir = right;
    }
    else if (rightLen < 0.001) {
      joinDisplacementDir = left;
    }
    else {
      joinDisplacementDir = isStartVertex ? right : left;
    }
    joinDisplacementDir = normalize(joinDisplacementDir);
    joinDisplacementDir = PERPENDICULAR(joinDisplacementDir);
    displacementLen = lineWidth;

    capDisplacementDir = isStartVertex ? -right : left;
  `),e.roundCaps?t.vertex.code.add(ee`
    float angle = subdivisionFactor*PI*0.5;
    joinDisplacementDir *= cos(angle);
    capDisplacementDir *= sin(angle);
    `):t.vertex.code.add(ee`
    capDisplacementDir *= subdivisionFactor;
    `),t.vertex.code.add(ee`
  }

  // Displacement (in pixels) caused by join/or cap
  vec2 dpos = joinDisplacementDir * sign(uv0.y) * displacementLen + capDisplacementDir * displacementLen;

  ${i||r?ee`float lineDist = lineWidth * sign(uv0.y) * pos.w;`:""}

  ${r?ee`vLineDistance = lineDist;`:""}
  ${i?ee`vLineDistanceNorm = lineDist / lineWidth;`:""}

  pos.xy += dpos;
  `),e.stippleEnabled&&(t.vertex.code.add(ee`
    {
      // Compute the stipple pattern UV coordinate from the actual position, based on the origin
      // and direction of the line segment on which the stipple pattern is based.

      // Project the vector from the origin of the segment to the vertex onto the line segment.
      // Note the 0.5 factor due to projected positions being at twice the screen size scale (see projectAndScale)
      vec2 posVec = pos.xy - stippleSegmentOrigin;

      float stippleSegmentDirectionLength = length(stippleSegmentDirection);
    `),e.stippleIntegerRepeatsEnabled&&t.vertex.code.add(ee`
      float numberOfPatternRepeats = stippleSegmentDirectionLength * 0.5 * stipplePatternPixelSizeInv;
      float roundedNumberOfPatternRepeats = floor(numberOfPatternRepeats);
      stipplePatternUvMax = roundedNumberOfPatternRepeats;
      `),t.vertex.code.add(ee`
      if (stippleSegmentDirectionLength >= 0.001) {
        // Project the vertex position onto the line segment.
        float projectedLength = dot(stippleSegmentDirection, posVec) / stippleSegmentDirectionLength * 0.5;
     ${e.stippleIntegerRepeatsEnabled?"float wholeNumberOfRepeatsScale = roundedNumberOfPatternRepeats / numberOfPatternRepeats;":"float wholeNumberOfRepeatsScale = 1.0;"}
        stipplePatternUv = projectedLength * wholeNumberOfRepeatsScale * stipplePatternPixelSizeInv * pos.w;
        } else {
          stipplePatternUv = 1.0;
        }
      }
    `)),t.vertex.code.add(ee`
      // Convert back into NDC
      pos.xy = pos.xy / screenSize * pos.w;

      vColor = getColor();
      vColor.a *= coverage;

      gl_Position = pos;
    }
  }
  `),t.include(ne,e),t.fragment.uniforms.add("intrinsicColor","vec4"),t.fragment.include(se),t.fragment.code.add(ee`
  void main() {
    discardBySlice(vpos);
    float stippleAlpha = getStippleAlpha();
    discardByStippleAlpha(stippleAlpha, STIPPLE_ALPHA_COLOR_DISCARD);

    vec4 color = intrinsicColor * vColor;
  `),e.innerColorEnabled&&(t.fragment.uniforms.add("pixelRatio","float"),t.fragment.code.add(ee`
    float distToInner = abs(vLineDistance * gl_FragCoord.w) - innerWidth;
    float innerAA = clamp(0.5 - distToInner, 0.0, 1.0);
    float innerAlpha = innerColor.a + color.a * (1.0 - innerColor.a);
    color = mix(color, vec4(innerColor.rgb, innerAlpha), innerAA);
    `)),t.fragment.code.add(ee`
    vec4 finalColor = blendStipple(color, stippleAlpha);
  `),e.falloffEnabled&&t.fragment.code.add(ee`
    finalColor.a *= pow(max(0.0, 1.0 - abs(vLineDistanceNorm * gl_FragCoord.w)), falloff);
    `),t.fragment.code.add(ee`
    if (finalColor.a < ${ee.float(ae)}) {
      discard;
    }

    ${7===e.output?ee`gl_FragColor = vec4(finalColor.a);`:""}
    ${0===e.output?ee`gl_FragColor = highlightSlice(finalColor, vpos);`:""}
    ${0===e.output&&e.OITEnabled?"gl_FragColor = premultiplyAlpha(gl_FragColor);":""}
    ${4===e.output?ee`gl_FragColor = vec4(1.0);`:""}
    ${1===e.output?ee`outputDepth(linearDepth);`:""}
  }
  `),t}});const mi={POSITION:"position",SUBDIVISIONFACTOR:"subdivisionFactor",UV0:"uv0",AUXPOS1:"auxpos1",AUXPOS2:"auxpos2",SUBDIVISIONS:"subdivisions",COLOR:"color",COLORFEATUREATTRIBUTE:"colorFeatureAttribute",SIZE:"size",SIZEFEATUREATTRIBUTE:"sizeFeatureAttribute",OPACITYFEATUREATTRIBUTE:"opacityFeatureAttribute"},gi={position:0,subdivisionFactor:1,uv0:2,auxpos1:3,auxpos2:4,size:6,sizeFeatureAttribute:6,color:5,colorFeatureAttribute:5,opacityFeatureAttribute:7};class vi extends ce{constructor(e,t){super(e,t),this.stipplePattern=null,this.stippleTextureBind=null,this.stippleTextureRepository=e.stippleTextureRepository}initializeProgram(e){const t=vi.shader.get(),i=this.configuration,r=t.build({OITEnabled:0===i.transparencyPassType,output:i.output,slicePlaneEnabled:i.slicePlaneEnabled,sliceHighlightDisabled:i.sliceHighlightDisabled,sliceEnabledForVertexPrograms:!1,stippleEnabled:i.stippleEnabled,stippleOffColorEnabled:i.stippleOffColorEnabled,stippleUVMaxEnabled:i.stippleIntegerRepeatsEnabled,stippleIntegerRepeatsEnabled:i.stippleIntegerRepeatsEnabled,roundCaps:i.roundCaps,roundJoins:i.roundJoins,vvColor:i.vvColor,vvSize:i.vvSize,vvInstancingEnabled:!0,vvOpacity:i.vvOpacity,falloffEnabled:i.falloffEnabled,innerColorEnabled:i.innerColorEnabled});return new Me(e.rctx,r.generateSource("vertex"),r.generateSource("fragment"),gi)}dispose(){super.dispose(),this.stippleTextureRepository.release(this.stipplePattern),this.stipplePattern=null,this.stippleTextureBind=null}bindPass(e,t,i){if(pe.bindProjectionMatrix(this.program,i.camera.projectionMatrix),4===this.configuration.output&&de.bindOutputHighlight(e,this.program,i),this.program.setUniform1f("intrinsicWidth",t.width),this.program.setUniform4fv("intrinsicColor",t.color),this.program.setUniform1f("miterLimit","miter"!==t.join?0:t.miterLimit),this.program.setUniform2fv("nearFar",i.camera.nearFar),this.program.setUniform1f("pixelRatio",i.camera.pixelRatio),this.program.setUniform2f("screenSize",i.camera.fullViewport[2],i.camera.fullViewport[3]),he.bindUniformsWithOpacity(this.program,t),this.stipplePattern!==t.stipplePattern){const e=t.stipplePattern;this.stippleTextureBind=this.stippleTextureRepository.swap(this.stipplePattern,e),this.stipplePattern=e}if(this.configuration.stippleEnabled){const r=o(this.stippleTextureBind)?this.stippleTextureBind(e,0)*i.camera.pixelRatio:1;if(this.program.setUniform1i("stipplePatternTexture",0),this.program.setUniform1f("stipplePatternPixelSizeInv",1/r),this.configuration.stippleOffColorEnabled){const e=l(t.stippleOffColor);this.program.setUniform4f("stippleOffColor",e[0],e[1],e[2],e.length>3?e[3]:1)}}this.configuration.falloffEnabled&&this.program.setUniform1f("falloff",t.falloff),this.configuration.innerColorEnabled&&(this.program.setUniform4fv("innerColor",c(t.innerColor,t.color)),this.program.setUniform1f("innerWidth",t.innerWidth*i.camera.pixelRatio))}bindDraw(e){pe.bindView(this.program,e),ne.bindUniformsWithOrigin(this.program,this.configuration,e)}setPipelineState(e,t){const i=this.configuration,r=3===e,n=2===e;return Ue({blending:0===i.output||7===i.output?r?ue:fe(e):null,depthTest:{func:me(e)},depthWrite:r?!i.transparent&&i.writeDepth&&Fe:ge(e),colorWrite:Ve,stencilWrite:i.sceneHasOcludees?ve:null,stencilTest:i.sceneHasOcludees?t?ye:Oe:null,polygonOffset:r||n?i.polygonOffset&&yi:be})}initializePipeline(){const e=this.configuration,t=e.polygonOffset&&yi;return e.occluder&&(this._occluderPipelineTransparent=Ue({blending:ue,polygonOffset:t,depthTest:Se,depthWrite:null,colorWrite:Ve,stencilWrite:null,stencilTest:xe}),this._occluderPipelineOpaque=Ue({blending:ue,polygonOffset:t,depthTest:Se,depthWrite:null,colorWrite:Ve,stencilWrite:Ae,stencilTest:Pe}),this._occluderPipelineMaskWrite=Ue({blending:null,polygonOffset:t,depthTest:_e,depthWrite:null,colorWrite:null,stencilWrite:ve,stencilTest:ye})),this._occludeePipelineState=this.setPipelineState(this.configuration.transparencyPassType,!0),this.setPipelineState(this.configuration.transparencyPassType,!1)}get primitiveType(){return 5}getPipelineState(e,t){return t?this._occludeePipelineState:this.configuration.occluder?11===e?this._occluderPipelineTransparent:10===e?this._occluderPipelineOpaque:this._occluderPipelineMaskWrite:this.pipeline}}vi.shader=new Ce(fi,(()=>Promise.resolve().then((function(){return fi}))));const yi={factor:0,units:-4};class Oi extends le{constructor(){super(...arguments),this.output=0,this.occluder=!1,this.slicePlaneEnabled=!1,this.sliceHighlightDisabled=!1,this.vertexColors=!1,this.transparent=!1,this.polygonOffset=!1,this.writeDepth=!1,this.stippleEnabled=!1,this.stippleOffColorEnabled=!1,this.stippleIntegerRepeatsEnabled=!1,this.roundCaps=!1,this.roundJoins=!1,this.vvSize=!1,this.vvColor=!1,this.vvOpacity=!1,this.falloffEnabled=!1,this.innerColorEnabled=!1,this.sceneHasOcludees=!1,this.transparencyPassType=3}}e([oe({count:8})],Oi.prototype,"output",void 0),e([oe()],Oi.prototype,"occluder",void 0),e([oe()],Oi.prototype,"slicePlaneEnabled",void 0),e([oe()],Oi.prototype,"sliceHighlightDisabled",void 0),e([oe()],Oi.prototype,"vertexColors",void 0),e([oe()],Oi.prototype,"transparent",void 0),e([oe()],Oi.prototype,"polygonOffset",void 0),e([oe()],Oi.prototype,"writeDepth",void 0),e([oe()],Oi.prototype,"stippleEnabled",void 0),e([oe()],Oi.prototype,"stippleOffColorEnabled",void 0),e([oe()],Oi.prototype,"stippleIntegerRepeatsEnabled",void 0),e([oe()],Oi.prototype,"roundCaps",void 0),e([oe()],Oi.prototype,"roundJoins",void 0),e([oe()],Oi.prototype,"vvSize",void 0),e([oe()],Oi.prototype,"vvColor",void 0),e([oe()],Oi.prototype,"vvOpacity",void 0),e([oe()],Oi.prototype,"falloffEnabled",void 0),e([oe()],Oi.prototype,"innerColorEnabled",void 0),e([oe()],Oi.prototype,"sceneHasOcludees",void 0),e([oe({count:4})],Oi.prototype,"transparencyPassType",void 0);const bi=p.getLogger("esri.views.3d.webgl-engine.materials.RibbonLineMaterial");class Si extends De{constructor(e,t){super(t,e,Ai),this._vertexAttributeLocations=gi,this.techniqueConfig=new Oi,this.layout=this.createLayout()}isClosed(e,t){return Ii(this.params,e,t)}dispose(){}getPassParameters(){return this.params}getTechniqueConfig(e,t){this.techniqueConfig.output=e;const i=o(this.params.stipplePattern);return this.techniqueConfig.stippleEnabled=i,this.techniqueConfig.stippleIntegerRepeatsEnabled=i&&this.params.stippleIntegerRepeats,this.techniqueConfig.stippleOffColorEnabled=i&&o(this.params.stippleOffColor),this.techniqueConfig.slicePlaneEnabled=this.params.slicePlaneEnabled,this.techniqueConfig.sceneHasOcludees=this.params.sceneHasOcludees,this.techniqueConfig.roundJoins="round"===this.params.join,this.techniqueConfig.roundCaps="round"===this.params.cap,this.techniqueConfig.transparent=this.params.transparent,this.techniqueConfig.polygonOffset=this.params.polygonOffset,this.techniqueConfig.writeDepth=this.params.writeDepth,this.techniqueConfig.vvColor=this.params.vvColorEnabled,this.techniqueConfig.vvOpacity=this.params.vvOpacityEnabled,this.techniqueConfig.vvSize=this.params.vvSizeEnabled,this.techniqueConfig.innerColorEnabled=this.params.innerWidth>0&&o(this.params.innerColor),this.techniqueConfig.falloffEnabled=this.params.falloff>0,this.techniqueConfig.occluder=8===this.params.renderOccluded,this.techniqueConfig.transparencyPassType=t?t.transparencyPassType:3,this.techniqueConfig}intersect(e,t,i,r,n,s,a,o,l){l?this.intersectDrapedLineGeometry(e,r,s,a):this.intersectLineGeometry(e,t,i,r,this.params.width,a)}intersectDrapedLineGeometry(e,t,i,r){if(!t.options.selectionMode)return;const n=e.getAttribute(mi.POSITION).data,s=e.getAttribute(mi.SIZE);let a=this.params.width;if(this.params.vvSizeEnabled){const t=e.getAttribute(mi.SIZEFEATUREATTRIBUTE).data[0];a*=f(this.params.vvSizeOffset[0]+t*this.params.vvSizeFactor[0],this.params.vvSizeMinSize[0],this.params.vvSizeMaxSize[0])}else s&&(a*=s.data[0]);const o=i[0],l=i[1],c=(a/2+4)*e.screenToWorldRatio;let p=Number.MAX_VALUE;for(let e=0;e<n.length-5;e+=3){const t=n[e],i=n[e+1],r=o-t,s=l-i,a=n[e+3]-t,c=n[e+4]-i,d=f((a*r+c*s)/(a*a+c*c),0,1),h=a*d-r,u=c*d-s,m=h*h+u*u;m<p&&(p=m)}p<c*c&&r()}intersectLineGeometry(e,t,i,r,n,s){if(!r.options.selectionMode||it(t))return;if(!Ke(i))return void bi.error("intersection assumes a translation-only matrix");const a=e.data,o=a.getVertexAttr(),l=o[mi.POSITION].data;let c=n;if(this.params.vvSizeEnabled){const e=o[mi.SIZEFEATUREATTRIBUTE].data[0];c*=f(this.params.vvSizeOffset[0]+e*this.params.vvSizeFactor[0],this.params.vvSizeMinSize[0],this.params.vvSizeMaxSize[0])}else o[mi.SIZE]&&(c*=o[mi.SIZE].data[0]);const p=r.camera,d=ji;k(d,r.point);const h=c*p.pixelRatio/2+4*p.pixelRatio;C(Gi[0],d[0]-h,d[1]+h,0),C(Gi[1],d[0]+h,d[1]+h,0),C(Gi[2],d[0]+h,d[1]-h,0),C(Gi[3],d[0]-h,d[1]-h,0);for(let e=0;e<4;e++)if(!p.unprojectFromRenderScreen(Gi[e],Hi[e]))return;He.fromPoints(p.eye,Hi[0],Hi[1],Zi),He.fromPoints(p.eye,Hi[1],Hi[2],ki),He.fromPoints(p.eye,Hi[2],Hi[3],$i),He.fromPoints(p.eye,Hi[3],Hi[0],Ji);let u=Number.MAX_VALUE;const m=Ci(this.params,o,a.indices)?l.length-2:l.length-5;for(let e=0;e<m;e+=3){wi[0]=l[e]+i[12],wi[1]=l[e+1]+i[13],wi[2]=l[e+2]+i[14];const t=(e+3)%l.length;if(Ei[0]=l[t]+i[12],Ei[1]=l[t+1]+i[13],Ei[2]=l[t+2]+i[14],He.signedDistance(Zi,wi)<0&&He.signedDistance(Zi,Ei)<0||He.signedDistance(ki,wi)<0&&He.signedDistance(ki,Ei)<0||He.signedDistance($i,wi)<0&&He.signedDistance($i,Ei)<0||He.signedDistance(Ji,wi)<0&&He.signedDistance(Ji,Ei)<0)continue;if(p.projectToRenderScreen(wi,Ni),p.projectToRenderScreen(Ei,Li),Ni[2]<0&&Li[2]>0){b(Ri,wi,Ei);const e=p.frustum,t=-He.signedDistance(e.planes[4],wi)/_(Ri,He.normal(e.planes[4]));A(Ri,Ri,t),P(wi,wi,Ri),p.projectToRenderScreen(wi,Ni)}else if(Ni[2]>0&&Li[2]<0){b(Ri,Ei,wi);const e=p.frustum,t=-He.signedDistance(e.planes[4],Ei)/_(Ri,He.normal(e.planes[4]));A(Ri,Ri,t),P(Ei,Ei,Ri),p.projectToRenderScreen(Ei,Li)}else if(Ni[2]<0&&Li[2]<0)continue;Ni[2]=0,Li[2]=0;const r=Je.distance2(Je.fromPoints(Ni,Li,Fi),d);r<u&&(u=r,I(Mi,wi),I(Ui,Ei))}const g=r.rayBeginPoint,v=r.rayEndPoint;if(u<h*h){let e=Number.MAX_VALUE;if(Je.closestLineSegmentPoint(Je.fromPoints(Mi,Ui,Fi),Je.fromPoints(g,v,Vi),zi)){b(zi,zi,g);const t=w(zi);A(zi,zi,1/t),e=t/E(g,v)}s(e,zi)}}computeAttachmentOrigin(e,t){const i=e.data,r="getVertexAttr"in i?i.getVertexAttr():"vertexAttr"in i?i.vertexAttr:null;if(!r)return null;const n=mi.POSITION,s="getVertexAttr"in i?i.indices:null,a=r[n];return Te(a,s?s[n]:null,s&&Ci(this.params,r,s),t)}createLayout(){const e=qe().vec3f(mi.POSITION).f32(mi.SUBDIVISIONFACTOR).vec2f(mi.UV0).vec3f(mi.AUXPOS1).vec3f(mi.AUXPOS2);return this.params.vvSizeEnabled?e.f32(mi.SIZEFEATUREATTRIBUTE):e.f32(mi.SIZE),this.params.vvColorEnabled?e.f32(mi.COLORFEATUREATTRIBUTE):e.vec4f(mi.COLOR),this.params.vvOpacityEnabled&&e.f32(mi.OPACITYFEATUREATTRIBUTE),e}createBufferWriter(){return new Pi(this.layout,this.params)}getGLMaterial(e){return 0===e.output||7===e.output||4===e.output||1===e.output?new xi(e):void 0}validateParameterValues(e){"miter"!==e.join&&(e.miterLimit=0),this.requiresTransparent(e)&&(e.transparent=!0)}requiresTransparent(e){return!!((e.color&&e.color[3])<1||e.innerWidth>0&&this.colorRequiresTransparent(e.innerColor)||e.stipplePattern&&this.colorRequiresTransparent(e.stippleOffColor)||e.falloff>0)}colorRequiresTransparent(e){return o(e)&&e[3]<1&&e[3]>0}}class xi extends we{constructor(e){super(e),this.updateParameters()}updateParameters(e){this.technique=this.techniqueRep.acquireAndReleaseExisting(vi,this.material.getTechniqueConfig(this.output,e),this.technique)}beginSlot(e){return this.technique.configuration.occluder?3===e||10===e||11===e:0===this.output||7===this.output?(this.targetSlot=this.technique.configuration.writeDepth?5:8,e===this.targetSlot):3===e}_updateOccludeeState(e){e.hasOccludees!==this.material.params.sceneHasOcludees&&this.material.setParameterValues({sceneHasOcludees:e.hasOccludees})}ensureParameters(e){0!==this.output&&7!==this.output||this._updateOccludeeState(e),this.updateParameters(e)}bind(e,t){e.bindProgram(this.technique.program),this.technique.bindPass(e,this.material.getPassParameters(),t)}getPipelineState(e,t){return this.technique.getPipelineState(e,t)}}const Ai={width:0,color:[1,1,1,1],join:"miter",cap:"butt",miterLimit:5,writeDepth:!0,polygonOffset:!1,stipplePattern:null,stippleIntegerRepeats:!1,stippleOffColor:null,slicePlaneEnabled:!1,vvFastUpdate:!1,transparent:!1,isClosed:!1,falloff:0,innerWidth:0,innerColor:null,sceneHasOcludees:!1,...Ie,...di.Default};class Pi{constructor(e,t){switch(this.params=t,this.numJoinSubdivisions=0,this.vertexBufferLayout=e,this.params.join){case"miter":case"bevel":this.numJoinSubdivisions=t.stipplePattern?1:0;break;case"round":this.numJoinSubdivisions=Ti}}isClosed(e){return Ci(this.params,e.vertexAttr,e.indices)}numCapSubdivisions(e){if(this.isClosed(e))return 0;switch(this.params.cap){case"butt":return 0;case"square":return 1;case"round":return Di}}allocate(e){return this.vertexBufferLayout.createBuffer(e)}elementCount(e){const t=2*this.numCapSubdivisions(e)+2,i=e.indices[mi.POSITION].length/2+1,r=this.isClosed(e);let n=r?2:2*t;const s=r?0:1,a=r?i:i-1;if(e.vertexAttr[mi.SUBDIVISIONS]){const t=e.vertexAttr[mi.SUBDIVISIONS].data;for(let e=s;e<a;++e){n+=4+2*t[e]}}else{n+=(a-s)*(2*this.numJoinSubdivisions+4)}return n+=2,n}write(e,t,i,r){const n=qi,s=Bi,a=Wi,o=t.vertexAttr[mi.POSITION].data,l=t.indices&&t.indices[mi.POSITION],c=this.numCapSubdivisions(t);l&&l.length!==2*(o.length/3-1)&&console.warn("RibbonLineMaterial does not support indices");let p=null;t.vertexAttr[mi.SUBDIVISIONS]&&(p=t.vertexAttr[mi.SUBDIVISIONS].data);let d=1,h=0;this.params.vvSizeEnabled?h=t.vertexAttr[mi.SIZEFEATUREATTRIBUTE].data[0]:t.vertexAttr[mi.SIZE]&&(d=t.vertexAttr[mi.SIZE].data[0]);let u=[1,1,1,1],f=0;this.params.vvColorEnabled?f=t.vertexAttr[mi.COLORFEATUREATTRIBUTE].data[0]:t.vertexAttr[mi.COLOR]&&(u=t.vertexAttr[mi.COLOR].data);let m=0;this.params.vvOpacityEnabled&&(m=t.vertexAttr[mi.OPACITYFEATUREATTRIBUTE].data[0]);const g=o.length/3,v=e.transformation,y=new Float32Array(i.buffer),O=this.vertexBufferLayout.stride/4;let b=r*O;const S=b,x=(e,t,i,r,n,s,a)=>{if(y[b++]=t[0],y[b++]=t[1],y[b++]=t[2],y[b++]=r,y[b++]=n,y[b++]=s,y[b++]=e[0],y[b++]=e[1],y[b++]=e[2],y[b++]=i[0],y[b++]=i[1],y[b++]=i[2],this.params.vvSizeEnabled?y[b++]=h:y[b++]=d,this.params.vvColorEnabled)y[b++]=f;else{const e=Math.min(4*a,u.length-4);y[b++]=u[e+0],y[b++]=u[e+1],y[b++]=u[e+2],y[b++]=u[e+3]}this.params.vvOpacityEnabled&&(y[b++]=m)};b+=O,C(s,o[0],o[1],o[2]),v&&D(s,s,v);const A=this.isClosed(t);if(A){const e=o.length-3;C(n,o[e],o[e+1],o[e+2]),v&&D(n,n,v)}else{I(n,s),C(a,o[3],o[4],o[5]),v&&D(a,a,v);for(let e=0;e<c;++e){const t=1-e/c;x(n,s,a,t,1,-4,0),x(n,s,a,t,1,4,0)}x(n,s,a,0,0,-4,0),x(n,s,a,0,0,4,0),I(n,s),I(s,a)}const P=A?g:g-1;for(let e=A?0:1;e<P;e++){const t=(e+1)%g*3;C(a,o[t+0],o[t+1],o[t+2]),v&&D(a,a,v),x(n,s,a,0,1,-1,e),x(n,s,a,0,1,1,e);const i=p?p[e]:this.numJoinSubdivisions;for(let t=0;t<i;++t){const r=(t+1)/(i+1);x(n,s,a,r,1,-2,e),x(n,s,a,r,1,2,e)}x(n,s,a,1,0,-2,e),x(n,s,a,1,0,2,e),I(n,s),I(s,a)}if(A){b=_i(y,S+O,y,b,2*O)}else{x(n,s,a,0,1,-5,P),x(n,s,a,0,1,5,P);for(let e=0;e<c;++e){const t=(e+1)/c;x(n,s,a,t,1,-5,P),x(n,s,a,t,1,5,P)}}_i(y,S+O,y,S,O);b=_i(y,b-O,y,b,O)}}function _i(e,t,i,r,n){for(let s=0;s<n;s++)i[r++]=e[t++];return r}function Ci(e,t,i){return Ii(e,t[mi.POSITION].data,i?i[mi.POSITION]:null)}function Ii(e,t,i){return!!e.isClosed&&(i?i.length>2:t.length>6)}const Di=3,Ti=1,wi=m(),Ei=m(),Ri=m(),zi=m(),ji=m(),Ni=L(),Li=L(),Mi=m(),Ui=m(),Fi=Je.create(),Vi=Je.create(),qi=m(),Bi=m(),Wi=m(),Gi=[L(),L(),L(),L()],Hi=[m(),m(),m(),m()],Zi=He.create(),ki=He.create(),$i=He.create(),Ji=He.create();var Xi=Object.freeze({__proto__:null,build:function(e){const t=new te,i=1===e.output;return t.include(Ee,{linearDepth:i}),t.include(Re,e),t.vertex.uniforms.add("proj","mat4").add("view","mat4"),t.attributes.add("position","vec3"),t.varyings.add("vpos","vec3"),i&&(t.include(re,e),t.vertex.uniforms.add("nearFar","vec2"),t.varyings.add("linearDepth","float")),t.vertex.code.add(ee`
    void main(void) {
      vpos = position;
      forwardNormalizedVertexColor();
      gl_Position = ${i?ee`transformPositionWithDepth(proj, view, vpos, nearFar, linearDepth);`:ee`transformPosition(proj, view, vpos);`}
    }
  `),t.include(ne,e),t.fragment.include(se),t.fragment.uniforms.add("eColor","vec4"),4===e.output&&t.include(de),t.fragment.code.add(ee`
  void main() {
    discardBySlice(vpos);
    vec4 color = ${e.attributeColor?"vColor * eColor;":"eColor;"}

    if (color.a < ${ee.float(ae)}) {
      discard;
    }

    ${7===e.output?ee`gl_FragColor = vec4(color.a);`:""}

    ${0===e.output?ee`gl_FragColor = highlightSlice(color, vpos); ${e.OITEnabled?"gl_FragColor = premultiplyAlpha(gl_FragColor);":""}`:""}
    ${4===e.output?ee`outputHighlight();`:""};
    ${1===e.output?ee`outputDepth(linearDepth);`:""};
  }
  `),t}});class Yi extends ce{initializeProgram(e){const t=Yi.shader.get(),i=this.configuration,r=t.build({output:i.output,OITEnabled:0===i.transparencyPassType,attributeColor:i.vertexColors,slicePlaneEnabled:i.slicePlaneEnabled,sliceHighlightDisabled:i.sliceHighlightDisabled,sliceEnabledForVertexPrograms:!1});return new Me(e.rctx,r.generateSource("vertex"),r.generateSource("fragment"),ze)}bindPass(e,t,i){pe.bindProjectionMatrix(this.program,i.camera.projectionMatrix),this.program.setUniform4fv("eColor",t.color),4===this.configuration.output&&de.bindOutputHighlight(e,this.program,i),1===this.configuration.output&&this.program.setUniform2fv("nearFar",i.camera.nearFar)}bindDraw(e){pe.bindView(this.program,e),ne.bindUniformsWithOrigin(this.program,this.configuration,e)}setPipelineState(e,t){const i=this.configuration,r=3===e,n=2===e;return Ue({blending:0!==i.output&&7!==i.output||!i.transparent?null:r?ue:fe(e),culling:(s=i.cullFace,0!==s&&{face:1===s?1028:1029,mode:2305}),depthTest:{func:me(e)},depthWrite:r||n?i.writeDepth&&Fe:null,colorWrite:Ve,stencilWrite:i.sceneHasOcludees?ve:null,stencilTest:i.sceneHasOcludees?t?ye:Oe:null,polygonOffset:r||n?i.polygonOffset&&Qi:je(i.enableOffset)});var s}initializePipeline(){return this._occludeePipelineState=this.setPipelineState(this.configuration.transparencyPassType,!0),this.setPipelineState(this.configuration.transparencyPassType,!1)}getPipelineState(e){return e?this._occludeePipelineState:this.pipeline}}Yi.shader=new Ce(Xi,(()=>Promise.resolve().then((function(){return Xi}))));const Qi={factor:1,units:1};class Ki extends le{constructor(){super(...arguments),this.output=0,this.cullFace=0,this.slicePlaneEnabled=!1,this.sliceHighlightDisabled=!1,this.vertexColors=!1,this.transparent=!1,this.polygonOffset=!1,this.enableOffset=!0,this.writeDepth=!0,this.sceneHasOcludees=!1,this.transparencyPassType=3}}e([oe({count:8})],Ki.prototype,"output",void 0),e([oe({count:3})],Ki.prototype,"cullFace",void 0),e([oe()],Ki.prototype,"slicePlaneEnabled",void 0),e([oe()],Ki.prototype,"sliceHighlightDisabled",void 0),e([oe()],Ki.prototype,"vertexColors",void 0),e([oe()],Ki.prototype,"transparent",void 0),e([oe()],Ki.prototype,"polygonOffset",void 0),e([oe()],Ki.prototype,"enableOffset",void 0),e([oe()],Ki.prototype,"writeDepth",void 0),e([oe()],Ki.prototype,"sceneHasOcludees",void 0),e([oe({count:4})],Ki.prototype,"transparencyPassType",void 0);class er extends De{constructor(e,t){super(t,e,ir),this.supportsEdges=!0,this.techniqueConfig=new Ki}getTechniqueConfig(e,t){return this.techniqueConfig.output=e,this.techniqueConfig.cullFace=this.params.cullFace,this.techniqueConfig.vertexColors=this.params.vertexColors,this.techniqueConfig.slicePlaneEnabled=this.params.slicePlaneEnabled,this.techniqueConfig.transparent=this.params.transparent,this.techniqueConfig.polygonOffset=this.params.polygonOffset,this.techniqueConfig.writeDepth=this.params.writeDepth,this.techniqueConfig.sceneHasOcludees=this.params.sceneHasOcludees,this.techniqueConfig.transparencyPassType=t?t.transparencyPassType:3,this.techniqueConfig.enableOffset=!t||t.camera.relativeElevation<Ne,this.techniqueConfig}getPassParameters(){return this.params}intersect(e,t,i,r,n,s,a){Le(e,t,r,n,s,void 0,a)}getGLMaterial(e){return 0===e.output||7===e.output||4===e.output||1===e.output&&this.params.writeLinearDepth?new tr(e):void 0}createBufferWriter(){return new Mt(Nt)}}class tr extends we{constructor(e){super(e),this.updateParameters()}updateParameters(e){this.technique=this.techniqueRep.acquireAndReleaseExisting(Yi,this.material.getTechniqueConfig(this.output,e),this.technique)}beginSlot(e){if(4===this.output)return 3===e;return e===(this.technique.configuration.transparent?this.technique.configuration.writeDepth?5:8:3)}_updateOccludeeState(e){e.hasOccludees!==this.material.params.sceneHasOcludees&&this.material.setParameterValues({sceneHasOcludees:e.hasOccludees})}ensureParameters(e){0!==this.output&&7!==this.output||this._updateOccludeeState(e),this.updateParameters(e)}bind(e,t){e.bindProgram(this.technique.program),this.technique.bindPass(e,this.material.getPassParameters(),t)}getPipelineState(e,t){return this.technique.getPipelineState(t)}}const ir={color:[1,1,1,1],transparent:!1,writeDepth:!0,writeLinearDepth:!1,vertexColors:!1,polygonOffset:!1,slicePlaneEnabled:!1,cullFace:0,sceneHasOcludees:!1,...Ie};export{er as C,Mt as D,dt as G,ci as L,Ut as O,Lt as P,Si as R,di as V,It as a,wt as b,bt as c,Et as d,jt as e,mi as f,mt as g,ft as h,Tt as i,ui as j,Nt as k,zt as l,_t as m,Rt as n,Ot as o,At as p,Ct as q,Pt as u,Dt as v};
