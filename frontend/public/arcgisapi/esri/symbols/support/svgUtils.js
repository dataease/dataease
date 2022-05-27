// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../core/has ../../Color ../../widgets/support/widgetUtils ../../chunks/index ../../chunks/mat2d ../../chunks/mat2df32".split(" "),function(t,D,A,B,n,p,E){function F(a){return a.map(b=>`${b.command} ${b.values.join(" ")}`).join(" ").trim()}function G(a,b,c,d){if(a){if("circle"===a.type)return n.jsx("circle",{fill:b,"fill-rule":"evenodd",stroke:c.color,"stroke-width":c.width,"stroke-linecap":c.cap,"stroke-linejoin":c.join,"stroke-dasharray":c.dashArray,"stroke-miterlimit":"4",cx:a.cx,
cy:a.cy,r:a.r});if("ellipse"===a.type)return n.jsx("ellipse",{fill:b,"fill-rule":"evenodd",stroke:c.color,"stroke-width":c.width,"stroke-linecap":c.cap,"stroke-linejoin":c.join,"stroke-dasharray":c.dashArray,"stroke-miterlimit":"4",cx:a.cx,cy:a.cy,rx:a.rx,ry:a.ry});if("rect"===a.type)return n.jsx("rect",{fill:b,"fill-rule":"evenodd",stroke:c.color,"stroke-width":c.width,"stroke-linecap":c.cap,"stroke-linejoin":c.join,"stroke-dasharray":c.dashArray,"stroke-miterlimit":"4",x:a.x,y:a.y,width:a.width,
height:a.height});if("image"===a.type)return n.jsx("image",{href:a.src,x:a.x,y:a.y,width:a.width,height:a.height,preserveAspectRatio:"none"});if("path"===a.type)return a="string"!==typeof a.path?F(a.path):a.path,n.jsx("path",{fill:b,"fill-rule":"evenodd",stroke:c.color,"stroke-width":c.width,"stroke-linecap":c.cap,"stroke-linejoin":c.join,"stroke-dasharray":c.dashArray,"stroke-miterlimit":"4",d:a});if("text"===a.type)return n.jsx("text",{fill:b,"fill-rule":"evenodd",stroke:c.color,"stroke-width":c.width,
"stroke-linecap":c.cap,"stroke-linejoin":c.join,"stroke-dasharray":c.dashArray,"stroke-miterlimit":"4","text-anchor":d.align,"text-decoration":d.decoration,kerning:d.kerning,rotate:d.rotate,"text-rendering":U,"font-style":d.font.style,"font-variant":d.font.variant,"font-weight":d.font.weight,"font-size":d.font.size,"font-family":d.font.family,x:a.x,y:a.y},a.text)}return null}function H(a){const b={fill:"none",pattern:null,linearGradient:null};if(a)if("type"in a&&"pattern"===a.type){var c=`patternId-${++V}`;
b.fill=`url(#${c})`;b.pattern={id:c,x:a.x,y:a.y,width:a.width,height:a.height,image:{x:0,y:0,width:a.width,height:a.height,href:a.src}}}else"type"in a&&"linear"===a.type?(c=`linearGradientId-${++W}`,b.fill=`url(#${c})`,b.linearGradient={id:c,x1:a.x1,y1:a.y1,x2:a.x2,y2:a.y2,stops:a.colors.map(d=>({offset:d.offset,color:d.color&&(new A(d.color)).toString()}))}):a&&(a=new A(a),b.fill=a.toString());return b}function I(a){const b={color:"none",width:1,cap:"butt",join:"4",dashArray:"none"};if(a&&(null!=
a.width&&(b.width=a.width),a.cap&&(b.cap=a.cap),a.join&&(b.join=a.join.toString()),a.color&&(b.color=(new A(a.color)).toString()),a.style)){let d=null;a.style in J&&(d=J[a.style]);if(Array.isArray(d)){d=d.slice(0);for(var c=0;c<d.length;++c)d[c]*=a.width;if("butt"!==a.cap){for(c=0;c<d.length;c+=2)d[c]-=a.width,1>d[c]&&(d[c]=1);for(c=1;c<d.length;c+=2)d[c]+=a.width}d=d.join(",")}b.dashArray=d}return b}function K(a,b){const c={align:null,decoration:null,kerning:null,rotate:null,font:{style:null,variant:null,
weight:null,size:null,family:null}};a&&(c.align=a.align,c.decoration=a.decoration,c.kerning=a.kerning?"auto":"0",c.rotate=a.rotated?"90":"0",c.font.style=b.style||"normal",c.font.variant=b.variant||"normal",c.font.weight=b.weight||"normal",c.font.size=b.size&&b.size.toString()||"10pt",c.font.family=b.family||"serif");return c}function L(a){const {pattern:b,linearGradient:c}=a;return b?n.jsx("pattern",{id:b.id,patternUnits:"userSpaceOnUse",x:b.x,y:b.y,width:b.width,height:b.height},n.jsx("image",{x:b.image.x,
y:b.image.y,width:b.image.width,height:b.image.height,href:b.image.href})):c?(a=c.stops.map((d,f)=>n.jsx("stop",{key:`${f}-stop`,offset:d.offset,"stop-color":d.color})),n.jsx("linearGradient",{id:c.id,gradientUnits:"userSpaceOnUse",x1:c.x1,y1:c.y1,x2:c.x2,y2:c.y2},a)):null}function M(a,b,c){return p.translate(a,p.identity(a),[b,c])}function N(a,b,c,d,f){p.scale(a,p.identity(a),[b,c]);a[4]=a[4]*b-d*b+d;a[5]=a[5]*c-f*c+f;return a}function X(a,b,c,d){var f=b%360*Math.PI/180;p.rotate(a,p.identity(a),
f);b=Math.cos(f);f=Math.sin(f);const g=a[4],l=a[5];a[4]=g*b-l*f+d*f-c*b+c;a[5]=l*b+g*f-c*f-d*b+d;return a}function m(a,b){k&&"left"in k?(k.left>a&&(k.left=a),k.right<a&&(k.right=a),k.top>b&&(k.top=b),k.bottom<b&&(k.bottom=b)):k={left:a,bottom:b,right:a,top:b}}function O(a){const b=a.args,c=b.length;switch(a.action){case "M":case "L":case "C":case "S":case "Q":case "T":for(a=0;a<c;a+=2)m(b[a],b[a+1]);e.x=b[c-2];e.y=b[c-1];break;case "H":for(a=0;a<c;++a)m(b[a],e.y);e.x=b[c-1];break;case "V":for(a=0;a<
c;++a)m(e.x,b[a]);e.y=b[c-1];break;case "m":a=0;"x"in e||(m(e.x=b[0],e.y=b[1]),a=2);for(;a<c;a+=2)m(e.x+=b[a],e.y+=b[a+1]);break;case "l":case "t":for(a=0;a<c;a+=2)m(e.x+=b[a],e.y+=b[a+1]);break;case "h":for(a=0;a<c;++a)m(e.x+=b[a],e.y);break;case "v":for(a=0;a<c;++a)m(e.x,e.y+=b[a]);break;case "c":for(a=0;a<c;a+=6)m(e.x+b[a],e.y+b[a+1]),m(e.x+b[a+2],e.y+b[a+3]),m(e.x+=b[a+4],e.y+=b[a+5]);break;case "s":case "q":for(a=0;a<c;a+=4)m(e.x+b[a],e.y+b[a+1]),m(e.x+=b[a+2],e.y+=b[a+3]);break;case "A":for(a=
0;a<c;a+=7)m(b[a+5],b[a+6]);e.x=b[c-2];e.y=b[c-1];break;case "a":for(a=0;a<c;a+=7)m(e.x+=b[a+5],e.y+=b[a+6])}}function P(a,b,c){const d=Y[a.toLowerCase()];"number"===typeof d&&(d?b.length>=d&&(a={action:a,args:b.slice(0,b.length-b.length%d)},c.push(a),O(a)):(a={action:a,args:[]},c.push(a),O(a)))}function Q(a){const b={x:0,y:0,width:0,height:0};if("circle"===a.type)b.x=a.cx-a.r,b.y=a.cy-a.r,b.width=2*a.r,b.height=2*a.r;else if("ellipse"===a.type)b.x=a.cx-a.rx,b.y=a.cy-a.ry,b.width=2*a.rx,b.height=
2*a.ry;else if("image"===a.type||"rect"===a.type)b.x=a.x,b.y=a.y,b.width=a.width,b.height=a.height;else if("path"===a.type){{const f=("string"!==typeof a.path?F(a.path):a.path).match(Z),g=[];k={};e={};if(f){a="";var c=[],d=f.length;for(let l=0;l<d;++l){const u=f[l],w=parseFloat(u);isNaN(w)?(a&&P(a,c,g),c=[],a=u):c.push(w)}P(a,c,g);a={x:0,y:0,width:0,height:0};k&&"left"in k&&(a.x=k.left,a.y=k.top,a.width=k.right-k.left,a.height=k.bottom-k.top)}else a=null}b.x=a.x;b.y=a.y;b.width=a.width;b.height=a.height}return b}
function R(a){const b={x:0,y:0,width:0,height:0};let c=null,d=Number.NEGATIVE_INFINITY,f=Number.NEGATIVE_INFINITY;for(const g of a)c?(c.x=Math.min(c.x,g.x),c.y=Math.min(c.y,g.y),d=Math.max(d,g.x+g.width),f=Math.max(f,g.y+g.height)):(c=b,c.x=g.x,c.y=g.y,d=g.x+g.width,f=g.y+g.height);c&&(c.width=d-c.x,c.height=f-c.y);return c}function S(a,b,c,d,f,g,l){var u=b/2;const w=c/2,q=a.width+d,r=a.height+d,x=E.create(),h=E.create();var v=!1;if(f&&0!==q&&0!==r){f=q/r;v=b>c?b:c;let y=1,z=1;isNaN(v)||(1<f?(y=v/
q,z=v/f/r):(z=v/r,y=v*f/q));p.multiply(h,h,N(x,y,z,u,w));v=!0}f=a.x+(q-d)/2;a=a.y+(r-d)/2;p.multiply(h,h,M(x,u-f,w-a));!v&&(q>b||r>c)&&(u=q/b>r/c,b=(u?b:c)/(u?q:r),p.multiply(h,h,N(x,b,b,f,a)));g&&p.multiply(h,h,X(x,g,f,a));l&&p.multiply(h,h,M(x,l[0],l[1]));return`matrix(${h[0]},${h[1]},${h[2]},${h[3]},${h[4]},${h[5]})`}let V=0,W=0;B=D("android");const U=D("chrome")||B&&4<=B?"auto":"optimizeLegibility",Y={m:2,l:2,h:1,v:1,c:6,s:4,q:4,t:2,a:7,z:0},Z=/([A-DF-Za-df-z])|([-+]?\d*[.]?\d+(?:[eE][-+]?\d+)?)/g;
let k={},e={};const J={solid:"none",shortdash:[4,1],shortdot:[1,1],shortdashdot:[4,1,1,1],shortdashdotdot:[4,1,1,1,1,1],dot:[1,3],dash:[4,3],longdash:[8,3],dashdot:[4,3,1,3],longdashdot:[8,3,1,3],longdashdotdot:[8,3,1,3,1,3]};t.computeBBox=R;t.generateFillAttributes=H;t.generateStrokeAttributes=I;t.generateTextAttributes=K;t.getBoundingBox=Q;t.getTransformMatrix=S;t.renderDef=L;t.renderSVG=function(a,b,c,d){const f=[],g=[];for(const u of a){a=[];var l=[];let w=0,q=0,r=0;for(const x of u){const {shape:h,
fill:v,stroke:y,font:z,offset:C}=x;w+=y&&y.width||0;const T=H(v),aa=I(y),ba="text"===h.type?K(h,z):null;f.push(L(T));a.push(G(h,T.fill,aa,ba));l.push(Q(h));C&&(q+=C[0],r+=C[1])}l=S(R(l),b,c,w,d&&d.scale,d&&d.rotation,[q,r]);g.push(n.jsx("g",{transform:l},a))}return n.jsx("svg",{xmlns:"http://www.w3.org/2000/svg",width:b,height:c},n.jsx("defs",null,f),g)};t.renderShape=G;Object.defineProperty(t,"__esModule",{value:!0})});