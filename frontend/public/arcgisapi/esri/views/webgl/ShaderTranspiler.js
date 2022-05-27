// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../chunks/_commonjsHelpers","./reservedWordsGLSL3"],function(B,y,J){function K(){function c(n){n.length&&r.push({type:L[g],data:n,position:t,line:C,column:z})}function d(){e=e.length?[]:e;if("/"===h&&"*"===a)return t=p+b-1,g=0,h=a,b+1;if("/"===h&&"/"===a)return t=p+b-1,g=1,h=a,b+1;if("#"===a)return g=2,t=p+b,b;if(/\s/.test(a))return g=9,t=p+b,b;D=/\d/.test(a);E=/[^\w_]/.test(a);t=p+b;g=D?4:E?3:9999;return b}function k(){if(("\r"===a||"\n"===a)&&"\\"!==h)return c(e.join("")),
g=999,b;e.push(a);h=a;return b+1}function f(){if("."===h&&/\d/.test(a))return g=5,b;if("/"===h&&"*"===a)return g=0,b;if("/"===h&&"/"===a)return g=1,b;if("."===a&&e.length){for(;m(e););g=5;return b}if(";"===a||")"===a||"("===a){if(e.length)for(;m(e););c(a);g=999;return b+1}var n=2===e.length&&"\x3d"!==a;if(/[\w_\d\s]/.test(a)||n){for(;m(e););g=999;return b}e.push(a);h=a;return b+1}function m(n){var v=0;do{var G=F.indexOf(n.slice(0,n.length+v).join(""));var x=F[G];if(-1===G){if(0<v--+n.length)continue;
x=n.slice(0,1).join("")}c(x);t+=x.length;e=e.slice(x.length);return e.length}while(1)}function q(){if("."===a||/[eE]/.test(a))return e.push(a),g=5,h=a,b+1;if("x"===a&&1===e.length&&"0"===e[0])return g=11,e.push(a),h=a,b+1;if(/[^\d]/.test(a))return c(e.join("")),g=999,b;e.push(a);h=a;return b+1}function l(){"f"===a&&(e.push(a),h=a,b+=1);if(/[eE]/.test(a)||"-"===a&&/[eE]/.test(h))return e.push(a),h=a,b+1;if(/[^\d]/.test(a))return c(e.join("")),g=999,b;e.push(a);h=a;return b+1}var b=0,p=0,g=999,a,h,
e=[],r=[],C=1,z=0,t=0,D=!1,E=!1,u="",H;return function(n){r=[];if(null!==n){n=n.replace?n.replace(/\r\n/g,"\n"):n;b=0;u+=n;for(H=u.length;a=u[b],b<H;){n=b;switch(g){case 0:"/"===a&&"*"===h?(e.push(a),c(e.join("")),g=999):(e.push(a),h=a);b+=1;break;case 1:b=k();break;case 2:b=k();break;case 3:b=f();break;case 4:b=q();break;case 11:/[^a-fA-F0-9]/.test(a)?(c(e.join("")),g=999):(e.push(a),h=a,b+=1);break;case 5:b=l();break;case 9999:if(/[^\d\w_]/.test(a)){var v=e.join("");g=-1<M.indexOf(v)?8:-1<N.indexOf(v)?
7:6;c(e.join(""));g=999}else e.push(a),h=a,b+=1;break;case 9:/[^\s]/g.test(a)?(c(e.join("")),g=999):(e.push(a),h=a,b+=1);break;case 999:b=d()}if(n!==b)switch(u[n]){case "\n":z=0;++C;break;default:++z}}p+=b;u=u.slice(b);return r}e.length&&c(e.join(""));g=10;c("(eof)");return r}}function O(c){return c.map(d=>"eof"!==d.type?d.data:"").join("")}function P(c,d="100",k="300 es"){const f=/^\s*#version\s+([0-9]+(\s+[a-zA-Z]+)?)\s*/;for(const m of c)if("preprocessor"===m.type){const q=f.exec(m.data);if(q){c=
q[1].replace(/\s\s+/g," ");if(c===k)return c;if(c===d)return m.data="#version "+k,d;throw Error("unknown glsl version: "+c);}}c.splice(0,0,{type:"preprocessor",data:"#version "+k},{type:"whitespace",data:"\n"});return null}function w(c,d,k,f){f=f||k;for(const m of c)if("ident"===m.type&&m.data===k)return f in d?d[f]++:d[f]=0,w(c,d,f+"_"+d[f],f);return k}function I(c,d,k="afterVersion"){const f={data:"\n",type:"whitespace"},m=l=>l<c.length?/[^\r\n]$/.test(c[l].data):!1;let q=function(l){let b=-1,p=
0,g=-1;for(let h=0;h<l.length;h++){var a=l[h];"preprocessor"===a.type&&(a.data.match(/#(if|ifdef|ifndef)\s+.+/)?++p:a.data.match(/#endif\s*.*/)&&--p);("afterVersion"===k||"afterPrecision"===k)&&"preprocessor"===a.type&&/^#version/.test(a.data)&&(g=Math.max(g,h));if("afterPrecision"===k&&"keyword"===a.type&&"precision"===a.data){a:{for(a=h;a<l.length;a++){const e=l[a];if("operator"===e.type&&";"===e.data)break a}a=null}if(null===a)throw Error("precision statement not followed by any semicolons!");
g=Math.max(g,a)}b<g&&0===p&&(b=h)}return b+1}(c);m(q-1)&&c.splice(q++,0,f);for(const l of d)c.splice(q++,0,l);m(q-1)&&m(q)&&c.splice(q,0,f)}function Q(c,d,k,f="lowp"){I(c,[{type:"keyword",data:"out"},{type:"whitespace",data:" "},{type:"keyword",data:f},{type:"whitespace",data:" "},{type:"keyword",data:k},{type:"whitespace",data:" "},{type:"ident",data:d},{type:"operator",data:";"}],"afterPrecision")}function R(c,d,k,f,m="lowp"){I(c,[{type:"keyword",data:"layout"},{type:"operator",data:"("},{type:"keyword",
data:"location"},{type:"whitespace",data:" "},{type:"operator",data:"\x3d"},{type:"whitespace",data:" "},{type:"integer",data:f.toString()},{type:"operator",data:")"},{type:"whitespace",data:" "},{type:"keyword",data:"out"},{type:"whitespace",data:" "},{type:"keyword",data:m},{type:"whitespace",data:" "},{type:"keyword",data:k},{type:"whitespace",data:" "},{type:"ident",data:d},{type:"operator",data:";"}],"afterPrecision")}var M=y.createCommonjsModule(function(c){(function(d){d=d();void 0!==d&&(c.exports=
d)})(function(){return"precision highp mediump lowp attribute const uniform varying break continue do for while if else in out inout float int void bool true false discard return mat2 mat3 mat4 vec2 vec3 vec4 ivec2 ivec3 ivec4 bvec2 bvec3 bvec4 sampler1D sampler2D sampler3D samplerCube sampler1DShadow sampler2DShadow struct asm class union enum typedef template this packed goto switch default inline noinline volatile public static extern external interface long short double half fixed unsigned input output hvec2 hvec3 hvec4 dvec2 dvec3 dvec4 fvec2 fvec3 fvec4 sampler2DRect sampler3DRect sampler2DRectShadow sizeof cast namespace using".split(" ")})}),
F=y.createCommonjsModule(function(c){(function(d){d=d();void 0!==d&&(c.exports=d)})(function(){"use strict;";return"\x3c\x3c\x3d \x3e\x3e\x3d ++ -- \x3c\x3c \x3e\x3e \x3c\x3d \x3e\x3d \x3d\x3d !\x3d \x26\x26 || +\x3d -\x3d *\x3d /\x3d %\x3d \x26\x3d ^^ ^\x3d |\x3d ( ) [ ] . ! ~ * / % + - \x3c \x3e \x26 ^ | ? : \x3d , ; { }".split(" ")})}),N=y.createCommonjsModule(function(c){(function(d){d=d();void 0!==d&&(c.exports=d)})(function(){return"abs acos all any asin atan ceil clamp cos cross dFdx dFdy degrees distance dot equal exp exp2 faceforward floor fract gl_BackColor gl_BackLightModelProduct gl_BackLightProduct gl_BackMaterial gl_BackSecondaryColor gl_ClipPlane gl_ClipVertex gl_Color gl_DepthRange gl_DepthRangeParameters gl_EyePlaneQ gl_EyePlaneR gl_EyePlaneS gl_EyePlaneT gl_Fog gl_FogCoord gl_FogFragCoord gl_FogParameters gl_FragColor gl_FragCoord gl_FragData gl_FragDepth gl_FragDepthEXT gl_FrontColor gl_FrontFacing gl_FrontLightModelProduct gl_FrontLightProduct gl_FrontMaterial gl_FrontSecondaryColor gl_LightModel gl_LightModelParameters gl_LightModelProducts gl_LightProducts gl_LightSource gl_LightSourceParameters gl_MaterialParameters gl_MaxClipPlanes gl_MaxCombinedTextureImageUnits gl_MaxDrawBuffers gl_MaxFragmentUniformComponents gl_MaxLights gl_MaxTextureCoords gl_MaxTextureImageUnits gl_MaxTextureUnits gl_MaxVaryingFloats gl_MaxVertexAttribs gl_MaxVertexTextureImageUnits gl_MaxVertexUniformComponents gl_ModelViewMatrix gl_ModelViewMatrixInverse gl_ModelViewMatrixInverseTranspose gl_ModelViewMatrixTranspose gl_ModelViewProjectionMatrix gl_ModelViewProjectionMatrixInverse gl_ModelViewProjectionMatrixInverseTranspose gl_ModelViewProjectionMatrixTranspose gl_MultiTexCoord0 gl_MultiTexCoord1 gl_MultiTexCoord2 gl_MultiTexCoord3 gl_MultiTexCoord4 gl_MultiTexCoord5 gl_MultiTexCoord6 gl_MultiTexCoord7 gl_Normal gl_NormalMatrix gl_NormalScale gl_ObjectPlaneQ gl_ObjectPlaneR gl_ObjectPlaneS gl_ObjectPlaneT gl_Point gl_PointCoord gl_PointParameters gl_PointSize gl_Position gl_ProjectionMatrix gl_ProjectionMatrixInverse gl_ProjectionMatrixInverseTranspose gl_ProjectionMatrixTranspose gl_SecondaryColor gl_TexCoord gl_TextureEnvColor gl_TextureMatrix gl_TextureMatrixInverse gl_TextureMatrixInverseTranspose gl_TextureMatrixTranspose gl_Vertex greaterThan greaterThanEqual inversesqrt length lessThan lessThanEqual log log2 matrixCompMult max min mix mod normalize not notEqual pow radians reflect refract sign sin smoothstep sqrt step tan texture2D texture2DLod texture2DProj texture2DProjLod textureCube textureCubeLod texture2DLodEXT texture2DProjLodEXT textureCubeLodEXT texture2DGradEXT texture2DProjGradEXT textureCubeGradEXT".split(" ")})}),
L="block-comment line-comment preprocessor operator integer float ident builtin keyword whitespace eof integer".split(" ");const A=["GL_OES_standard_derivatives","GL_EXT_frag_depth","GL_EXT_draw_buffers","GL_EXT_shader_texture_lod"];B.transpileShader=function(c,d){var k=K(),f=[];f=f.concat(k(c));c=f=f.concat(k(null));if("300 es"===P(c,"100","300 es"))throw Error("shader is already glsl 300 es");f=k=null;const m={},q={};for(let g=0;g<c.length;++g){const a=c[g];switch(a.type){case "keyword":"vertex"===
d&&"attribute"===a.data?a.data="in":"varying"===a.data&&(a.data="vertex"===d?"out":"in");break;case "builtin":/^texture(2D|Cube)(Proj)?(Lod|Grad)?(EXT)?$/.test(a.data.trim())&&(a.data=a.data.replace(/(2D|Cube|EXT)/g,""));"fragment"===d&&"gl_FragColor"===a.data&&(k||(k=w(c,m,"fragColor"),Q(c,k,"vec4")),a.data=k);if("fragment"===d&&"gl_FragData"===a.data){{var l=void 0;let h=void 0;var b=c;var p=g+1;let e=-1;for(;p<b.length;p++){const r=b[p];if("operator"===r.type&&("["===r.data&&(h=p),"]"===r.data)){l=
p;break}"integer"===r.type&&(e=parseInt(r.data,10))}h&&l&&b.splice(h,l-h+1);b=e}l=w(c,m,"fragData");R(c,l,"vec4",b,"mediump");a.data=l}else"fragment"===d&&"gl_FragDepthEXT"===a.data&&(f||(f=w(c,m,"gl_FragDepth")),a.data=f);break;case "ident":if(0<=J.indexOf(a.data)){if(b="vertex"===d)a:{for(b=g-1;0<=b;b--)if(l=c[b],"whitespace"!==l.type&&"block-comment"!==l.type)if("keyword"===l.type){if("attribute"===l.data||"in"===l.data){b=!0;break a}}else break;b=!1}if(b)throw Error("attribute in vertex shader uses a name that is a reserved word in glsl 300 es");
a.data in q||(q[a.data]=w(c,m,a.data));a.data=q[a.data]}}}for(d=c.length-1;0<=d;--d)k=c[d],"preprocessor"===k.type&&((f=k.data.match(/#extension\s+(.*):/))&&f[1]&&0<=A.indexOf(f[1].trim())&&(f=c[d+1],c.splice(d,f&&"whitespace"===f.type?2:1)),(f=k.data.match(/#ifdef\s+(.*)/))&&f[1]&&0<=A.indexOf(f[1].trim())&&(k.data="#if 1"),(f=k.data.match(/#ifndef\s+(.*)/))&&f[1]&&0<=A.indexOf(f[1].trim())&&(k.data="#if 0"));return O(c)};Object.defineProperty(B,"__esModule",{value:!0})});