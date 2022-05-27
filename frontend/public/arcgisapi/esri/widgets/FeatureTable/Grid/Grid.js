// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
/*

  Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
  This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
  The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
  The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
  Code distributed by Google as part of the polymer project is also
  subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
 Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 This code may only be used under the BSD style license found at
 http://polymer.github.io/LICENSE.txt The complete set of authors may be found
 at http://polymer.github.io/AUTHORS.txt The complete set of contributors may
 be found at http://polymer.github.io/CONTRIBUTORS.txt Code distributed by
 Google as part of the polymer project is also subject to an additional IP
 rights grant found at http://polymer.github.io/PATENTS.txt

  Copyright (c) 2019 The Polymer Project Authors. All rights reserved.
  This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
  The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
  The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
  Code distributed by Google as part of the polymer project is also
  subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt

  Copyright (c) 2015 The Polymer Project Authors. All rights reserved.
  This code may only be used under the BSD style license found at
  http://polymer.github.io/LICENSE.txt The complete set of authors may be found at
  http://polymer.github.io/AUTHORS.txt The complete set of contributors may be
  found at http://polymer.github.io/CONTRIBUTORS.txt Code distributed by Google as
  part of the polymer project is also subject to an additional IP rights grant
  found at http://polymer.github.io/PATENTS.txt

  Copyright (c) 2016 The Polymer Project Authors. All rights reserved.
  This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
  The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
  The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
  Code distributed by Google as part of the polymer project is also
  subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt

  Copyright (c) 2017 Vaadin Ltd.
  This program is available under Apache License Version 2.0, available at https://vaadin.com/license/

  Copyright (c) 2019 Vaadin Ltd.
  This program is available under Apache License Version 2.0, available at https://vaadin.com/license/

  Copyright (c) 2020 Vaadin Ltd.
  This program is available under Apache License Version 2.0, available at https://vaadin.com/license/

  Copyright (c) 2016 The Polymer Project Authors. All rights reserved.
  This code may only be used under the BSD style license found at
  http://polymer.github.io/LICENSE.txt The complete set of authors may be found at
  http://polymer.github.io/AUTHORS.txt The complete set of contributors may be
  found at http://polymer.github.io/CONTRIBUTORS.txt Code distributed by Google as
  part of the polymer project is also subject to an additional IP rights grant
  found at http://polymer.github.io/PATENTS.txt

  Copyright (c) 2018 Vaadin Ltd.
  This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
*/
define("../../../chunks/_rollupPluginBabelHelpers ../../../chunks/tslib.es6 ../../../core/has ../../../core/maybe ../../../core/Logger ../../../core/accessorSupport/decorators/property ../../../core/accessorSupport/decorators/aliasOf ../../../core/accessorSupport/decorators/cast ../../../core/jsonMap ../../../core/accessorSupport/decorators/subclass ../../../core/urlUtils ../../../core/uuid ../../../portal/support/resourceExtension ../../../core/events ../../../core/Collection ../../../core/watchUtils ../../../core/HandleOwner ../../support/widgetUtils ../../support/decorators/messageBundle ../../support/decorators/renderable ../../../chunks/index ../../Widget ./GridViewModel".split(" "),
function(Xd,x,og,Yd,pg,W,J,Zd,qg,$d,rg,sg,tg,Xb,ae,Yb,be,ce,de,Ga,ka,ee,fe){function Zb(b){requestAnimationFrame(function(){$b?$b(b):(eb||(eb=new Promise(a=>{fb=a}),"complete"===document.readyState?fb():document.addEventListener("readystatechange",()=>{"complete"===document.readyState&&fb()})),eb.then(function(){b&&b()}))})}function gb(b,a){for(let c in a)null===c?b.style.removeProperty(c):b.style.setProperty(c,a[c])}function ac(b,a){return(b=window.getComputedStyle(b).getPropertyValue(a))?b.trim():
""}function bc(b){hb=b&&b.shimcssproperties?!1:Ha||!(navigator.userAgent.match(/AppleWebKit\/601|Edge\/15/)||!window.CSS||!CSS.supports||!CSS.supports("box-shadow","0 0 0 var(--foo)"))}function la(b,a){if(b&&ge.test(b)||"//"===b)return b;if(void 0===Ia){Ia=!1;try{const c=new URL("b","http://a");c.pathname="c%20d";Ia="http://a/c%20d"===c.href}catch(c){}}a||(a=document.baseURI||window.location.href);if(Ia)try{return(new URL(b,a)).href}catch(c){return b}C||(C=document.implementation.createHTMLDocument("temp"),
C.base=C.createElement("base"),C.head.appendChild(C.base),C.anchor=C.createElement("a"),C.body.appendChild(C.anchor));C.base.href=a;C.anchor.href=b;return C.anchor.href||b}function ib(b,a){return b.replace(he,function(c,d,e,f){return d+"'"+la(e.replace(/["']/g,""),a)+"'"+f})}function jb(b){return b.substring(0,b.lastIndexOf("/")+1)}function cc(b){b=ib((b.body?b.body:b).textContent,b.baseURI);const a=document.createElement("style");a.textContent=b;return a}function ie(b){b=b.trim().split(/\s+/);const a=
[];for(let c=0;c<b.length;c++)a.push(...je(b[c]));return a}function je(b){const a=O.import(b);if(!a)return console.warn("Could not find style data in module named",b),[];if(void 0===a._styles){b=[];b.push(...kb(a));const c=a.querySelector("template");c&&b.push(...lb(c,a.assetpath));a._styles=b}return a._styles}function lb(b,a){if(!b._styles){const c=[],d=b.content.querySelectorAll("style");for(let e=0;e<d.length;e++){let f=d[e],g=f.getAttribute("include");g&&c.push(...ie(g).filter(function(h,k,l){return l.indexOf(h)===
k}));a&&(f.textContent=ib(f.textContent,a));c.push(f)}b._styles=c}return b._styles}function ke(b){return(b=O.import(b))?kb(b):[]}function kb(b){const a=[];b=b.querySelectorAll("link[rel\x3dimport][type~\x3dcss]");for(let d=0;d<b.length;d++){var c=b[d];if(c.import){const e=c.import;if((c=c.hasAttribute("shady-unscoped"))&&!e._unscopedStyle){const f=cc(e);f.setAttribute("shady-unscoped","");e._unscopedStyle=f}else e._style||(e._style=cc(e));a.push(c?e._unscopedStyle:e._style)}}return a}function K(b){let a=
b.indexOf(".");return-1===a?b:b.slice(0,a)}function ma(b){if(Array.isArray(b)){let a=[];for(let c=0;c<b.length;c++){let d=b[c].toString().split(".");for(let e=0;e<d.length;e++)a.push(d[e])}return a.join(".")}return b}function dc(b){return Array.isArray(b)?ma(b).split("."):b.toString().split(".")}function A(b,a,c){a=dc(a);for(let d=0;d<a.length;d++){if(!b)return;b=b[a[d]]}c&&(c.path=a.join("."));return b}function ec(b,a,c){let d=dc(a),e=d[d.length-1];if(1<d.length){for(a=0;a<d.length-1;a++)if(b=b[d[a]],
!b)return;b[e]=c}else b[a]=c;return d.join(".")}function fc(b){return Ja[b]||(Ja[b]=0>b.indexOf("-")?b:b.replace(le,a=>a[1].toUpperCase()))}function Ka(b){return Ja[b]||(Ja[b]=b.replace(me,"-$1").toLowerCase())}function gc(b,a){let c=a.parentInfo&&gc(b,a.parentInfo);if(c)for(let d=c.firstChild,e=0;d;d=d.nextSibling){if(a.parentIndex===e++)return d}else return b}function ne(b,a,c){b=b._methodHost||b;return function(d){if(b[c])b[c](d,d.detail);else console.warn("listener method `"+c+"` not defined")}}
function mb(b,a,c){let d=b[a];if(!d)d=b[a]={};else if(!b.hasOwnProperty(a)&&(d=b[a]=Object.create(b[a]),c))for(let e in d)for(b=d[e],a=d[e]=Array(b.length),c=0;c<b.length;c++)a[c]=b[c];return d}function na(b,a,c,d,e,f){if(a){let h=!1;const k=oa++;for(let l in c){var g=e?K(l):l;if(g=a[g])for(let m=0,n=g.length,p;m<n&&(p=g[m]);m++)p.info&&p.info.lastRun===k||e&&!nb(l,p.trigger)||(p.info&&(p.info.lastRun=k),p.fn(b,l,c,d,p.info,e,f),h=!0)}return h}return!1}function nb(b,a){if(a){let c=a.name;return c==
b||!(!a.structured||0!==c.indexOf(b+"."))||!(!a.wildcard||0!==b.indexOf(c+"."))}return!0}function hc(b,a,c,d,e){a="string"===typeof e.method?b[e.method]:e.method;c=e.property;a?a.call(b,b.__data[c],d[c]):e.dynamicFn||console.warn("observer method `"+e.method+"` not defined")}function ic(b,a,c,d){c={value:c,queueProperty:!0};d&&(c.path=d);q(b).dispatchEvent(new CustomEvent(a,{detail:c}))}function oe(b,a,c,d,e,f){f=(d=(f?K(a):a)!=a?a:null)?A(b,d):b.__data[a];d&&void 0===f&&(f=c[a]);ic(b,e.eventName,
f,d)}function pe(b,a,c,d,e){c=b.__data[a];La&&(c=La(c,e.attrName,"attribute",b));b._propertyToAttribute(a,e.attrName,c)}function qe(b){let a=b.constructor.__orderedComputedDeps;if(!a){a=new Map;const d=b[t.COMPUTE];let {counts:e,ready:f,total:g}=re(b);for(var c;c=f.shift();)a.set(c,a.size),(c=d[c])&&c.forEach(h=>{h=h.info.methodInfo;--g;0===--e[h]&&f.push(h)});0!==g&&console.warn(`Computed graph for ${b.localName} incomplete; circular?`);b.constructor.__orderedComputedDeps=a}return a}function re(b){const a=
b.__computeInfo,c={};b=b[t.COMPUTE];const d=[];let e=0;for(let f in a){const g=a[f];e+=c[f]=g.args.filter(h=>!h.literal).length+(g.dynamicFn?1:0)}for(let f in b)a[f]||d.push(f);return{counts:c,ready:d,total:e}}function jc(b,a,c,d,e){a=ob(b,a,c,d,e);if(a===pa)return!1;e=e.methodInfo;if(b.__dataHasAccessor&&b.__dataHasAccessor[e])return b._setPendingProperty(e,a,!0);b[e]=a;return!1}function pb(b,a,c,d,e,f,g){c.bindings=c.bindings||[];d={kind:d,target:e,parts:f,literal:g,isCompound:1!==f.length};c.bindings.push(d);
if(d.target&&"attribute"!=d.kind&&"text"!=d.kind&&!d.isCompound&&"{"===d.parts[0].mode){let {event:n,negate:p}=d.parts[0];d.listenerEvent=n||Ka(e)+"-changed";d.listenerNegate=p}c=a.nodeInfoList.length;for(g=0;g<d.parts.length;g++){var h=d.parts[g];h.compoundIndex=g;e=b;f=a;var k=d,l=h,m=c;if(!l.literal)if("attribute"===k.kind&&"-"===k.target[0])console.warn("Cannot set attribute "+k.target+' because "-" is not a valid attribute starting character');else for(h=l.dependencies,k={index:m,binding:k,part:l,
evaluator:e},l=0;l<h.length;l++)m=h[l],"string"==typeof m&&(m=kc(m),m.wildcard=!0),e._addTemplatePropertyEffect(f,m.rootProperty,{fn:se,info:k,trigger:m})}}function se(b,a,c,d,e,f,g){g=g[e.index];var h=e.binding;let k=e.part;f&&k.source&&a.length>k.source.length&&"property"==h.kind&&!h.isCompound&&g.__isPropertyEffectsClient&&g.__dataHasAccessor&&g.__dataHasAccessor[h.target]?(c=c[a],a=h.target+a.slice(k.source.length),g._setPendingPropertyOrPath(a,c,!1,!0)&&b._enqueueClient(g)):(a=e.evaluator._evaluateBinding(b,
k,a,c,d,f),a!==pa&&(h.isCompound&&(c=g.__dataCompoundStorage[h.target],c[k.compoundIndex]=a,a=c.join("")),"attribute"===h.kind||"textContent"!==h.target&&("value"!==h.target||"input"!==g.localName&&"textarea"!==g.localName)||(a=void 0==a?"":a),La&&(a=La(a,h.target,h.kind,g)),"attribute"==h.kind?b._valueToNodeAttribute(g,a,h.target):(h=h.target,g.__isPropertyEffectsClient&&g.__dataHasAccessor&&g.__dataHasAccessor[h]?g[t.READ_ONLY]&&g[t.READ_ONLY][h]||g._setPendingProperty(h,a)&&b._enqueueClient(g):
b._setUnmanagedPropertyToNode(g,h,a))))}function te(b,a,c){if(c.listenerEvent){let d=c.parts[0];b.addEventListener(c.listenerEvent,function(e){{var f=c.target,g=d.source,h=d.negate;let k=e.detail,l=k&&k.path;l?(g+=l.slice(f.length),e=k&&k.value):e=e.currentTarget[f];a[t.READ_ONLY]&&a[t.READ_ONLY][g]||!a._setPendingPropertyOrPath(g,h?!e:e,!0,!!l)||k&&k.queueProperty||a._invalidateProperties()}})}}function lc(b,a,c,d,e,f){f=a.static||f&&("object"!==typeof f||f[a.methodName]);e={methodName:a.methodName,
args:a.args,methodInfo:e,dynamicFn:f};for(let g=0,h;g<a.args.length&&(h=a.args[g]);g++)h.literal||b._addPropertyEffect(h.rootProperty,c,{fn:d,info:e,trigger:h});f&&b._addPropertyEffect(a.methodName,c,{fn:d,info:e});return e}function ob(b,a,c,d,e){d=b._methodHost||b;let f=d[e.methodName];if(f)return b=b._marshalArgs(e.args,a,c),b===pa?pa:f.apply(d,b);e.dynamicFn||console.warn("method `"+e.methodName+"` not defined")}function mc(b){let a="";for(let c=0;c<b.length;c++)a+=b[c].literal||"";return a}function qb(b){var a=
b.match(/([^\s]+?)\(([\s\S]*)\)/);return a?(b={methodName:a[1],static:!0,args:ue},a[2].trim()?(a=a[2].replace(/\\,/g,"\x26comma;").split(","),ve(a,b)):b):null}function ve(b,a){a.args=b.map(function(c){c=kc(c);c.literal||(a.static=!1);return c},this);return a}function kc(b){b=b.trim().replace(/&comma;/g,",").replace(/\\(.)/g,"$1");let a={name:b,value:"",literal:!1},c=b[0];"-"===c&&(c=b[1]);"0"<=c&&"9">=c&&(c="#");switch(c){case "'":case '"':a.value=b.slice(1,-1);a.literal=!0;break;case "#":a.value=
Number(b),a.literal=!0}a.literal||(a.rootProperty=K(b),a.structured=0<=b.indexOf("."),a.structured&&(a.wildcard=".*"==b.slice(-2),a.wildcard&&(a.name=b.slice(0,-2))));return a}function nc(b,a,c){b=A(b,c);void 0===b&&(b=a[c]);return b}function oc(b,a,c,d){d={indexSplices:d};rb&&!b._overrideLegacyUndefined&&(a.splices=d);b.notifyPath(c+".splices",d);b.notifyPath(c+".length",a.length);rb&&!b._overrideLegacyUndefined&&(d.indexSplices=[])}function qa(b,a,c,d,e,f){oc(b,a,c,[{index:d,addedCount:e,removed:f,
object:a,type:"splice"}])}function we(b){return b[0].toUpperCase()+b.substring(1)}function pc(b){if(!(-1<Ma.indexOf(b))&&"touchend"!==b&&sb&&tb&&xe)return{passive:!0}}function qc(b){let a=rc?["click"]:Ma;for(let c=0,d;c<a.length;c++)d=a[c],b?(ub.length=0,document.addEventListener(d,sc,!0)):document.removeEventListener(d,sc,!0)}function X(b){var a=b.type;return-1<Ma.indexOf(a)?"mousemove"===a?(a=void 0===b.buttons?1:b.buttons,b instanceof window.MouseEvent&&!ye&&(a=ze[b.which]||0),!!(a&1)):0===(void 0===
b.button?0:b.button):!1}function Ae(b){if("click"===b.type){if(0===b.detail)return!0;var a=P(b);if(!a.nodeType||a.nodeType!==Node.ELEMENT_NODE)return!0;a=a.getBoundingClientRect();let c=b.pageX;b=b.pageY;return!(c>=a.left&&c<=a.right&&b>=a.top&&b<=a.bottom)}return!1}function tc(b,a,c){b.movefn=a;b.upfn=c;document.addEventListener("mousemove",a);document.addEventListener("mouseup",c)}function aa(b){document.removeEventListener("mousemove",b.movefn);document.removeEventListener("mouseup",b.upfn);b.movefn=
null;b.upfn=null}function P(b){const a=Na(b);return 0<a.length?a[0]:b.target}function uc(b){let a=b.type;var c=b.currentTarget.__polymerGestures;if(c&&(c=c[a])){if(!b.__polymerGesturesHandled&&(b.__polymerGesturesHandled={},"touch"===a.slice(0,5))){var d=b.changedTouches[0];"touchstart"===a&&1===b.touches.length&&(y.touch.id=d.identifier);if(y.touch.id!==d.identifier)return;if(!sb&&("touchstart"===a||"touchmove"===a)){d=b.changedTouches[0];var e=b.type;if("touchstart"===e)y.touch.x=d.clientX,y.touch.y=
d.clientY,y.touch.scrollDecided=!1;else if("touchmove"===e&&!y.touch.scrollDecided){y.touch.scrollDecided=!0;e="auto";var f=Na(b);for(let h=0,k;h<f.length;h++)if(k=f[h],k.__polymerGesturesTouchAction){e=k.__polymerGesturesTouchAction;break}f=!1;var g=Math.abs(y.touch.x-d.clientX);d=Math.abs(y.touch.y-d.clientY);b.cancelable&&("none"===e?f=!0:"pan-x"===e?f=d>g:"pan-y"===e&&(f=g>d));f?b.preventDefault():Oa("track")}}}d=b.__polymerGesturesHandled;if(!d.skip){for(let h=0,k;h<Y.length;h++)k=Y[h],c[k.name]&&
!d[k.name]&&k.flow&&-1<k.flow.start.indexOf(b.type)&&k.reset&&k.reset();for(let h=0,k;h<Y.length;h++)k=Y[h],c[k.name]&&!d[k.name]&&(d[k.name]=!0,k[a](b))}}}function vb(b,a,c){if(ra[a]){{let d=ra[a],e=d.deps,f=d.name,g=b.__polymerGestures;g||(b.__polymerGestures=g={});for(let h=0,k,l;h<e.length;h++)k=e[h],rc&&-1<Ma.indexOf(k)&&"click"!==k||((l=g[k])||(g[k]=l={_count:0}),0===l._count&&b.addEventListener(k,uc,pc(k)),l[f]=(l[f]||0)+1,l._count=(l._count||0)+1);b.addEventListener(a,c);d.touchAction&&vc(b,
d.touchAction)}return!0}return!1}function wb(b){Y.push(b);for(let a=0;a<b.emits.length;a++)ra[b.emits[a]]=b}function vc(b,a){sb&&b instanceof HTMLElement&&G.run(()=>{b.style.touchAction=a});b.__polymerGesturesTouchAction=a}function xb(b,a,c){a=new Event(a,{bubbles:!0,cancelable:!0,composed:!0});a.detail=c;q(b).dispatchEvent(a);a.defaultPrevented&&(b=c.preventer||c.sourceEvent)&&b.preventDefault&&b.preventDefault()}function Oa(b){a:{for(let a=0,c;a<Y.length;a++){c=Y[a];for(let d=0,e;d<c.emits.length;d++)if(e=
c.emits[d],e===b){b=c;break a}}b=null}b.info&&(b.info.prevent=!0)}function sa(b,a,c,d){a&&xb(a,b,{x:c.clientX,y:c.clientY,sourceEvent:c,preventer:d,prevent:function(e){return Oa(e)}})}function wc(b,a,c){if(b.prevent)return!1;if(b.started)return!0;c=Math.abs(b.y-c);return 5<=Math.abs(b.x-a)||5<=c}function yb(b,a,c){if(a){var d=b.moves[b.moves.length-2],e=b.moves[b.moves.length-1],f=e.x-b.x,g=e.y-b.y,h=0;if(d){var k=e.x-d.x;h=e.y-d.y}xb(a,"track",{state:b.state,x:c.clientX,y:c.clientY,dx:f,dy:g,ddx:k,
ddy:h,sourceEvent:c,hover:function(){{var l=c.clientX;var m=c.clientY;let n=document.elementFromPoint(l,m),p=n;for(;p&&p.shadowRoot&&!window.ShadyDOM;){let r=p;p=p.shadowRoot.elementFromPoint(l,m);if(r===p)break;p&&(n=p)}l=n}return l}})}}function xc(b,a,c){let d=Math.abs(a.clientX-b.x),e=Math.abs(a.clientY-b.y),f=P(c||a);!f||Be[f.localName]&&f.hasAttribute("disabled")||!(isNaN(d)||isNaN(e)||25>=d&&25>=e||Ae(a))||b.prevent||xb(f,"tap",{x:a.clientX,y:a.clientY,sourceEvent:a,preventer:c})}function Ce(){return yc(function(){return!0})}
function De(){return Pa&&0<Object.keys(Pa).map(b=>Pa[b]).filter(b=>b.productionMode).length?!0:!1}function yc(b,a){if("function"===typeof b){var c=Ee.exec(b.toString());if(c)try{b=new Function(c[1])}catch(d){console.log("vaadin-development-mode-detector: uncommentAndRun() failed",d)}return b(a)}}function Fe(){}function zc(b){b=b.replace(L.comments,"").replace(L.port,"");{var a=b;let c=new Ac;c.start=0;c.end=a.length;let d=c;for(let e=0,f=a.length;e<f;e++)if("{"===a[e]){d.rules||(d.rules=[]);let g=
d,h=g.rules[g.rules.length-1]||null;d=new Ac;d.start=e+1;d.parent=g;d.previous=h;g.rules.push(d)}else"}"===a[e]&&(d.end=e+1,d=d.parent||c);a=c}return Bc(a,b)}function Bc(b,a){var c=a.substring(b.start,b.end-1);b.parsedCssText=b.cssText=c.trim();b.parent&&(c=a.substring(b.previous?b.previous.end:b.parent.start,b.start-1),c=Ge(c),c=c.replace(L.multipleSpaces," "),c=c.substring(c.lastIndexOf(";")+1),c=b.parsedSelector=b.selector=c.trim(),b.atRule=0===c.indexOf("@"),b.atRule?0===c.indexOf("@media")?b.type=
Q.MEDIA_RULE:c.match(L.keyframesRule)&&(b.type=Q.KEYFRAMES_RULE,b.keyframesName=b.selector.split(L.multipleSpaces).pop()):0===c.indexOf("--")?b.type=Q.MIXIN_RULE:b.type=Q.STYLE_RULE);if(c=b.rules)for(let d=0,e=c.length,f;d<e&&(f=c[d]);d++)Bc(f,a);return b}function Ge(b){return b.replace(/\\([0-9a-f]{1,6})\s/gi,function(a,c){a=c;for(c=6-a.length;c--;)a="0"+a;return"\\"+a})}function Cc(b,a,c=""){let d="";if(b.cssText||b.rules){let f=b.rules;var e;if(e=f)e=f[0],e=!!e&&!!e.selector&&0===e.selector.indexOf("--"),
e=!e;if(e)for(let g=0,h=f.length,k;g<h&&(k=f[g]);g++)d=Cc(k,a,d);else a?a=b.cssText:(a=b.cssText,a=a.replace(L.customProp,"").replace(L.mixinProp,""),a=a.replace(L.mixinApply,"").replace(L.varApply,"")),(d=a.trim())&&(d="  "+d+"\n")}d&&(b.selector&&(c+=b.selector+" {\n"),c+=d,b.selector&&(c+="}\n\n"));return c}function zb(b,a){if(!b)return"";"string"===typeof b&&(b=zc(b));a&&ta(b,a);return Cc(b,Ab)}function Dc(b){!b.__cssRules&&b.textContent&&(b.__cssRules=zc(b.textContent));return b.__cssRules||
null}function ta(b,a,c,d){if(b){var e=!1,f=b.type;if(d&&f===Q.MEDIA_RULE){let g=b.selector.match(He);g&&(window.matchMedia(g[1]).matches||(e=!0))}f===Q.STYLE_RULE?a(b):c&&f===Q.KEYFRAMES_RULE?c(b):f===Q.MIXIN_RULE&&(e=!0);if((b=b.rules)&&!e)for(let g=0,h=b.length,k;g<h&&(k=b[g]);g++)ta(k,a,c,d)}}function Ec(b,a){var c=b.indexOf("var(");if(-1===c)return a(b,"","","");a:{var d=0;for(let g=c+3,h=b.length;g<h;g++)if("("===b[g])d++;else if(")"===b[g]&&0===--d){d=g;break a}d=-1}var e=d;d=b.substring(c+
4,e);c=b.substring(0,c);b=Ec(b.substring(e+1),a);let f=d.indexOf(",");if(-1===f)return a(c,d.trim(),"",b);e=d.substring(0,f).trim();d=d.substring(f+1).trim();return a(c,e,d,b)}function Fc(b){if(void 0!==ua)return ua;if(void 0===b.__cssBuild){var a=b.getAttribute("css-build");if(a)b.__cssBuild=a;else{a:{a="template"===b.localName?b.content.firstChild:b.firstChild;if(a instanceof Comment&&(a=a.textContent.trim().split(":"),"css-build"===a[0])){a=a[1];break a}a=""}if(""!==a){{const c="template"===b.localName?
b.content.firstChild:b.firstChild;c.parentNode.removeChild(c)}}b.__cssBuild=a}}return b.__cssBuild||""}function Ie(b){if(b=Bb[b])b._applyShimCurrentVersion=b._applyShimCurrentVersion||0,b._applyShimValidatingVersion=b._applyShimValidatingVersion||0,b._applyShimNextVersion=(b._applyShimNextVersion||0)+1}function Gc(b){return b._applyShimCurrentVersion===b._applyShimNextVersion}function Je(b){b._applyShimValidatingVersion=b._applyShimNextVersion;b._validating||(b._validating=!0,Ke.then(function(){b._applyShimCurrentVersion=
b._applyShimNextVersion;b._validating=!1}))}function Hc(){va=document.documentElement.getAttribute("dir");va=document.documentElement.getAttribute("dir");for(let a=0;a<wa.length;a++){var b=wa[a];b.__autoDirOptOut||b.setAttribute("dir",va)}}function Ic(){Qa=!0;requestAnimationFrame(function(){Qa=!1;Le(Jc);setTimeout(function(){var b=Kc;for(let a=0,c=b.length;a<c;a++)Lc(b.shift())})})}function Le(b){for(;b.length;)Lc(b.shift())}function Lc(b){const a=b[0],c=b[1];b=b[2];try{c.apply(a,b)}catch(d){setTimeout(()=>
{throw d;})}}function Me(b,a,c){Qa||Ic();Jc.push([b,a,c])}function Mc(b,a,c){Qa||Ic();Kc.push([b,a,c])}function Nc(){document.body.removeAttribute("unresolved")}function xa(b,a,c){return{index:b,removed:a,addedCount:c}}function Oc(b,a,c,d,e,f){var g=0,h=0,k=Math.min(c-a,f-e);if(0==a&&0==e)a:{for(g=0;g<k;g++)if(b[g]!==d[g])break a;g=k}if(c==b.length&&f==d.length){h=k-g;k=b.length;for(var l=d.length,m=0;m<h&&Ne(b[--k],d[--l]);)m++;h=m}a+=g;e+=g;c-=h;f-=h;if(0==c-a&&0==f-e)return[];if(a==c){for(b=xa(a,
[],0);e<f;)b.removed.push(d[e++]);return[b]}if(e==f)return[xa(a,[],c-a)];g=a;h=e;f=f-h+1;c=c-g+1;k=Array(f);for(l=0;l<f;l++)k[l]=Array(c),k[l][0]=l;for(l=0;l<c;l++)k[0][l]=l;for(l=1;l<f;l++)for(m=1;m<c;m++)if(b[g+m-1]===d[h+l-1])k[l][m]=k[l-1][m-1];else{var n=k[l-1][m]+1;let p=k[l][m-1]+1;k[l][m]=n<p?n:p}b=k;c=b.length-1;f=b[0].length-1;g=b[c][f];for(h=[];0<c||0<f;)0==c?(h.push(2),f--):0==f?(h.push(3),c--):(k=b[c-1][f-1],l=b[c-1][f],m=b[c][f-1],n=l<m?l<k?l:k:m<k?m:k,n==k?(k==g?h.push(0):(h.push(1),
g=k),c--,f--):n==l?(h.push(3),c--,g=l):(h.push(2),f--,g=m));h.reverse();c=h;b=void 0;f=[];for(g=0;g<c.length;g++)switch(c[g]){case 0:b&&(f.push(b),b=void 0);a++;e++;break;case 1:b||(b=xa(a,[],0));b.addedCount++;a++;b.removed.push(d[e]);e++;break;case 2:b||(b=xa(a,[],0));b.addedCount++;a++;break;case 3:b||(b=xa(a,[],0)),b.removed.push(d[e]),e++}b&&f.push(b);return f}function Ne(b,a){return b===a}function ba(b){return"slot"===b.localName}function Oe(b,a){for(let c=0;c<a.length;c++){let d=a[c];b[d]=
function(){return this.node[d].apply(this.node,arguments)}}}function Pc(b,a){for(let c=0;c<a.length;c++){let d=a[c];Object.defineProperty(b,d,{get:function(){return this.node[d]},configurable:!0})}}function Pe(b,a){for(let c=0;c<a.length;c++){let d=a[c];Object.defineProperty(b,d,{get:function(){return this.node[d]},set:function(e){this.node[d]=e},configurable:!0})}}function Qe(b,a=!1){if(!Cb||!Qc||!Cb.handlesDynamicScoping)return null;const c=Qc.ScopingShim;if(!c)return null;const d=c.scopeForNode(b),
e=q(b).getRootNode(),f=g=>{if(q(g).getRootNode()===e){var h=Array.from(Cb.nativeMethods.querySelectorAll.call(g,"*"));h.push(g);for(g=0;g<h.length;g++){const l=h[g];var k=e;q(l).getRootNode()===k&&(k=c.currentScopeForNode(l),k!==d&&(""!==k&&c.unscopeNode(l,k),c.scopeNode(l,d)))}}};f(b);return a?(a=new MutationObserver(g=>{for(let h=0;h<g.length;h++){const k=g[h];for(let l=0;l<k.addedNodes.length;l++){const m=k.addedNodes[l];m.nodeType===Node.ELEMENT_NODE&&f(m)}}}),a.observe(b,{childList:!0,subtree:!0}),
a):null}function Rc(b,a,c,d){{const e=a._noAccessors,f=Object.getOwnPropertyNames(a);for(let g=0;g<f.length;g++){let h=f[g];if(!(h in d))if(e)b[h]=a[h];else{let k=Object.getOwnPropertyDescriptor(a,h);k&&(k.configurable=!0,Object.defineProperty(b,h,k))}}}for(let e in Re)a[e]&&(c[e]=c[e]||[],c[e].push(a[e]))}function Sc(b,a,c){a=a||[];for(let d=b.length-1;0<=d;d--){let e=b[d];e?Array.isArray(e)?Sc(e,a):0>a.indexOf(e)&&(!c||0>c.indexOf(e))&&a.unshift(e):console.warn("behavior is null, check for missing or 404 import")}return a}
function Tc(b,a){for(const c in a){const d=b[c],e=a[c];b[c]=!("value"in e)&&d&&"value"in d?Object.assign({value:d.value},e):e}}function Se(b,a,c){let d;const e={};class f extends a{static _finalizeClass(){if(this.hasOwnProperty("generatedFrom")){if(d)for(let h=0,k;h<d.length;h++)k=d[h],k.properties&&this.createProperties(k.properties),k.observers&&this.createObservers(k.observers,k.properties);b.properties&&this.createProperties(b.properties);b.observers&&this.createObservers(b.observers,b.properties);
this._prepareTemplate()}else a._finalizeClass.call(this)}static get properties(){const h={};if(d)for(let k=0;k<d.length;k++)Tc(h,d[k].properties);Tc(h,b.properties);return h}static get observers(){let h=[];if(d)for(let k=0,l;k<d.length;k++)l=d[k],l.observers&&(h=h.concat(l.observers));b.observers&&(h=h.concat(b.observers));return h}created(){super.created();const h=e.created;if(h)for(let k=0;k<h.length;k++)h[k].call(this)}_registered(){var h=f.prototype;if(!h.hasOwnProperty("__hasRegisterFinished")){h.__hasRegisterFinished=
!0;super._registered();ya&&g(h);h=Object.getPrototypeOf(this);let l=e.beforeRegister;if(l)for(var k=0;k<l.length;k++)l[k].call(h);if(l=e.registered)for(k=0;k<l.length;k++)l[k].call(h)}}_applyListeners(){super._applyListeners();const h=e.listeners;if(h)for(let k=0;k<h.length;k++){const l=h[k];if(l)for(let m in l)this._addMethodEventListenerToNode(this,m,l[m])}}_ensureAttributes(){const h=e.hostAttributes;if(h)for(let k=h.length-1;0<=k;k--){const l=h[k];for(let m in l)this._ensureAttribute(m,l[m])}super._ensureAttributes()}ready(){super.ready();
let h=e.ready;if(h)for(let k=0;k<h.length;k++)h[k].call(this)}attached(){super.attached();let h=e.attached;if(h)for(let k=0;k<h.length;k++)h[k].call(this)}detached(){super.detached();let h=e.detached;if(h)for(let k=0;k<h.length;k++)h[k].call(this)}attributeChanged(h,k,l){super.attributeChanged();let m=e.attributeChanged;if(m)for(let n=0;n<m.length;n++)m[n].call(this,h,k,l)}}if(c){Array.isArray(c)||(c=[c]);let h=a.prototype.behaviors;d=Sc(c,null,h);f.prototype.behaviors=h?h.concat(c):d}const g=h=>
{if(d){var k=d;for(let l=0;l<k.length;l++)Rc(h,k[l],e,Te)}Rc(h,b,e,Uc)};ya||g(f.prototype);f.generatedFrom=b;return f}function Db(b,a,c,d,e){let f;e&&(f="object"===typeof c&&null!==c)&&(d=b.__dataTemp[a]);d=d!==c&&(d===d||c===c);f&&d&&(b.__dataTemp[a]=c);return d}function Eb(){return Fb}function Vc(b,a){for(let c=0;c<a.length;c++){let d=a[c];if(!!b!=!!d.__hideTemplateChildren__)if(d.nodeType===Node.TEXT_NODE)b?(d.__polymerTextContent__=d.textContent,d.textContent=""):d.textContent=d.__polymerTextContent__;
else if("slot"===d.localName)if(b)d.__polymerReplaced__=document.createComment("hidden-slot"),q(q(d).parentNode).replaceChild(d.__polymerReplaced__,d);else{const e=d.__polymerReplaced__;e&&q(q(e).parentNode).replaceChild(d,e)}else d.style&&(b?(d.__polymerDisplay__=d.style.display,d.style.display="none"):d.style.display=d.__polymerDisplay__);d.__hideTemplateChildren__=b;d._showHideChildren&&d._showHideChildren(b)}}function Wc(b){return(b=b.__dataHost)&&b._methodHost||b}function Ue(b,a){return function(c,
d,e){a.call(c.__templatizeOwner,d.substring(6),e[d])}}function Ve(b,a){return function(c,d,e){a.call(c.__templatizeOwner,c,d,e[d])}}function We(){return function(b,a,c){b.__dataHost._setPendingPropertyOrPath("_host_"+a,c[a],!0,!0)}}function za(b,a,c){if(ca&&!Wc(b))throw Error("strictTemplatePolicy: template owner not trusted");c=c||{};if(b.__templatizeOwner)throw Error("A \x3ctemplate\x3e can only be templatized once");b.__templatizeOwner=a;let d=(a?a.constructor:Gb)._parseTemplate(b);var e=d.templatizeInstanceClass;
if(!e){e=c;var f=e.mutableData?Xe:Gb;za.mixin&&(f=za.mixin(f));var g=class extends f{};g.prototype.__templatizeOptions=e;g.prototype._bindTemplate(b);f=g;var h=d.hostProps||{};for(var k in e.instanceProps){delete h[k];var l=e.notifyInstanceProp;l&&f.prototype._addPropertyEffect(k,f.prototype.PROPERTY_EFFECT_TYPES.NOTIFY,{fn:Ve(k,l)})}if(e.forwardHostProp&&b.__dataHost)for(var m in h)d.hasHostProps||(d.hasHostProps=!0),f.prototype._addPropertyEffect(m,f.prototype.PROPERTY_EFFECT_TYPES.NOTIFY,{fn:We()});
e=g;d.templatizeInstanceClass=e}k=Wc(b);f=c;if((g=f.forwardHostProp)&&d.hasHostProps){c="template"==b.localName;m=d.templatizeTemplateClass;if(!m){if(c){m=f.mutableData?Ye:Xc;class u extends m{}m=d.templatizeTemplateClass=u}else{m=b.constructor;class u extends m{}m=d.templatizeTemplateClass=u}h=d.hostProps;for(var n in h)m.prototype._addPropertyEffect("_host_"+n,m.prototype.PROPERTY_EFFECT_TYPES.PROPAGATE,{fn:Ue(n,g)}),m.prototype._createNotifyingProperty("_host_"+n);if(Yc&&k){n=k.constructor._properties;
({propertyEffects:g}=d);({instanceProps:f}=f);for(var p in g)if(!(n[p]||f&&f[p]))for(h=g[p],l=0;l<h.length;l++){const {part:u}=h[l].info;if(!u.signature||!u.signature.static){console.warn(`Property '${p}' used in template but not `+"declared in 'properties'; attribute will not be observed.");break}}}}b.__dataProto&&Object.assign(b.__data,b.__dataProto);if(c){var r=m;Fb=b;Object.setPrototypeOf(b,r.prototype);new r;Fb=null;b.__dataTemp={};b.__dataPending=null;b.__dataOld=null;b._enableProperties()}else for(r in Object.setPrototypeOf(b,
m.prototype),p=d.hostProps,p)r="_host_"+r,r in b&&(p=b[r],delete b[r],b.__data[r]=p)}r=class extends e{};r.prototype._methodHost=k;r.prototype.__dataHost=b;r.prototype.__templatizeOwner=a;r.prototype.__hostProps=d.hostProps;return r}function Hb(){if(ya&&!Ra){if(!Zc){Zc=!0;const b=document.createElement("style");b.textContent="dom-bind,dom-if,dom-repeat{display:none;}";document.head.appendChild(b)}return!0}return!1}class Ze extends HTMLElement{static get version(){return"1.6.0"}}customElements.define("vaadin-lumo-styles",
Ze);let eb=null,$b=window.HTMLImports&&window.HTMLImports.whenReady||null,fb,Sa=null,Aa=null;class R{constructor(){this.customStyles=[];this.enqueued=!1;Zb(()=>{window.ShadyCSS.flushCustomStyles&&window.ShadyCSS.flushCustomStyles()})}enqueueDocumentValidation(){!this.enqueued&&Aa&&(this.enqueued=!0,Zb(Aa))}addCustomStyle(b){b.__seenByShadyCSS||(b.__seenByShadyCSS=!0,this.customStyles.push(b),this.enqueueDocumentValidation())}getStyleForCustomStyle(b){return b.__shadyCSSCachedStyle?b.__shadyCSSCachedStyle:
b.getStyle?b.getStyle():b}processStyles(){const b=this.customStyles;for(let c=0;c<b.length;c++){const d=b[c];if(!d.__shadyCSSCachedStyle){var a=this.getStyleForCustomStyle(d);a&&(a=a.__appliedElement||a,Sa&&Sa(a),d.__shadyCSSCachedStyle=a)}}return b}}R.prototype.addCustomStyle=R.prototype.addCustomStyle;R.prototype.getStyleForCustomStyle=R.prototype.getStyleForCustomStyle;R.prototype.processStyles=R.prototype.processStyles;Object.defineProperties(R.prototype,{transformCallback:{get(){return Sa},set(b){Sa=
b}},validateCallback:{get(){return Aa},set(b){let a=!1;Aa||(a=!0);Aa=b;a&&this.enqueueDocumentValidation()}}});const Ib=/(?:^|[;\s{]\s*)(--[\w-]*?)\s*:\s*(?:((?:'(?:\\'|.)*?'|"(?:\\"|.)*?"|\([^)]*?\)|[^};{])+)|\{([^}]*)\}(?:(?=[;\s}])|$))/gi,Ta=/(?:^|\W+)@apply\s*\(?([^);\n]*)\)?/gi,He=/@media\s(.*)/,Ha=!(window.ShadyDOM&&window.ShadyDOM.inUse);let hb,ua;window.ShadyCSS&&void 0!==window.ShadyCSS.cssBuild&&(ua=window.ShadyCSS.cssBuild);const $c=!(!window.ShadyCSS||!window.ShadyCSS.disableRuntime);
window.ShadyCSS&&void 0!==window.ShadyCSS.nativeCss?hb=window.ShadyCSS.nativeCss:window.ShadyCSS?(bc(window.ShadyCSS),window.ShadyCSS=void 0):bc(window.WebComponents&&window.WebComponents.flags);const Ab=hb,Ua=new R;window.ShadyCSS||(window.ShadyCSS={prepareTemplate(b,a,c){},prepareTemplateDom(b,a){},prepareTemplateStyles(b,a,c){},styleSubtree(b,a){Ua.processStyles();gb(b,a)},styleElement(b){Ua.processStyles()},styleDocument(b){Ua.processStyles();gb(document.body,b)},getComputedStyleValue(b,a){return ac(b,
a)},flushCustomStyles(){},nativeCss:Ab,nativeShadow:Ha,cssBuild:ua,disableRuntime:$c});window.ShadyCSS.CustomStyleInterface=Ua;window.JSCompiler_renameProperty=function(b,a){return b};let he=/(url\()([^)]*)(\))/g,ge=/(^\/[^\/])|(^#)|(^[\w-\d]*:)/,Ia,C;const Ra=!window.ShadyDOM||!window.ShadyDOM.inUse,$e=Ra&&"adoptedStyleSheets"in Document.prototype&&"replaceSync"in CSSStyleSheet.prototype&&(()=>{try{const b=new CSSStyleSheet;b.replaceSync("");const a=document.createElement("div");a.attachShadow({mode:"open"});
a.shadowRoot.adoptedStyleSheets=[b];return a.shadowRoot.adoptedStyleSheets[0]===b}catch(b){return!1}})();let af=window.Polymer&&window.Polymer.rootPath||jb(document.baseURI||window.location.href),La=window.Polymer&&window.Polymer.sanitizeDOMValue||void 0,xe=window.Polymer&&window.Polymer.setPassiveTouchGestures||!1,ca=window.Polymer&&window.Polymer.strictTemplatePolicy||!1,bf=window.Polymer&&window.Polymer.allowTemplateFromDomModule||!1,ya=window.Polymer&&window.Polymer.legacyOptimizations||!1,Yc=
window.Polymer&&window.Polymer.legacyWarnings||!1,cf=window.Polymer&&window.Polymer.syncInitialRender||!1,rb=window.Polymer&&window.Polymer.legacyUndefined||!1,df=window.Polymer&&window.Polymer.orderedComputed||!1,ad=window.Polymer&&window.Polymer.removeNestedTemplates||!1,bd=window.Polymer&&window.Polymer.fastDomIf||!1,Jb=window.Polymer&&window.Polymer.suppressTemplateNotifications||!1,Va=window.Polymer&&window.Polymer.legacyNoObservedAttributes||!1,ef=window.Polymer&&window.Polymer.useAdoptedStyleSheetsWithBuiltCSS||
!1,Ba={},Wa={};class O extends HTMLElement{static get observedAttributes(){return["id"]}static import(b,a){return b?(b=Ba[b]||Wa[b.toLowerCase()])&&a?b.querySelector(a):b:null}attributeChangedCallback(b,a,c,d){a!==c&&this.register()}get assetpath(){if(!this.__assetpath){var b=window.HTMLImports&&HTMLImports.importForElement?HTMLImports.importForElement(this)||document:this.ownerDocument;b=la(this.getAttribute("assetpath")||"",b.baseURI);this.__assetpath=jb(b)}return this.__assetpath}register(b){if(b=
b||this.id){if(ca&&void 0!==(Ba[b]||Wa[b.toLowerCase()]))throw Ba[b]=Wa[b.toLowerCase()]=null,Error(`strictTemplatePolicy: dom-module ${b} re-registered`);this.id=b;Ba[b]=Wa[b.toLowerCase()]=this;this.querySelector("style")&&console.warn("dom-module %s has style outside template",this.id)}}}O.prototype.modules=Ba;customElements.define("dom-module",O);const ff=window.ShadyCSS.CustomStyleInterface;class gf extends HTMLElement{constructor(){super();this._style=null;ff.addCustomStyle(this)}getStyle(){if(this._style)return this._style;
const b=this.querySelector("style");if(!b)return null;this._style=b;var a=b.getAttribute("include");if(a){b.removeAttribute("include");a=a.trim().split(/\s+/);var c="";for(let h=0;h<a.length;h++){{var d=void 0,e=void 0;var f=a[h];let k=O.import(f);if(k&&void 0===k._cssText){e="";d=kb(k);for(var g=0;g<d.length;g++)e+=d[g].textContent;if(g=k.querySelector("template")){d="";g=lb(g,k.assetpath);for(let l=0;l<g.length;l++){let m=g[l];m.parentNode&&m.parentNode.removeChild(m);d+=m.textContent}e+=d}k._cssText=
e||null}k||console.warn("Could not find style data in module named",f);f=k&&k._cssText||""}c+=f}a=c;b.textContent=a+b.textContent}this.ownerDocument!==window.document&&window.document.head.appendChild(this);return this._style}}window.customElements.define("custom-style",gf);const cd=document.createElement("template");cd.innerHTML='\x3ccustom-style\x3e\n  \x3cstyle\x3e\n    html {\n      /* Base (background) */\n      --lumo-base-color: #FFF;\n\n      /* Tint */\n      --lumo-tint-5pct: hsla(0, 0%, 100%, 0.3);\n      --lumo-tint-10pct: hsla(0, 0%, 100%, 0.37);\n      --lumo-tint-20pct: hsla(0, 0%, 100%, 0.44);\n      --lumo-tint-30pct: hsla(0, 0%, 100%, 0.5);\n      --lumo-tint-40pct: hsla(0, 0%, 100%, 0.57);\n      --lumo-tint-50pct: hsla(0, 0%, 100%, 0.64);\n      --lumo-tint-60pct: hsla(0, 0%, 100%, 0.7);\n      --lumo-tint-70pct: hsla(0, 0%, 100%, 0.77);\n      --lumo-tint-80pct: hsla(0, 0%, 100%, 0.84);\n      --lumo-tint-90pct: hsla(0, 0%, 100%, 0.9);\n      --lumo-tint: #FFF;\n\n      /* Shade */\n      --lumo-shade-5pct: hsla(214, 61%, 25%, 0.05);\n      --lumo-shade-10pct: hsla(214, 57%, 24%, 0.1);\n      --lumo-shade-20pct: hsla(214, 53%, 23%, 0.16);\n      --lumo-shade-30pct: hsla(214, 50%, 22%, 0.26);\n      --lumo-shade-40pct: hsla(214, 47%, 21%, 0.38);\n      --lumo-shade-50pct: hsla(214, 45%, 20%, 0.5);\n      --lumo-shade-60pct: hsla(214, 43%, 19%, 0.61);\n      --lumo-shade-70pct: hsla(214, 42%, 18%, 0.72);\n      --lumo-shade-80pct: hsla(214, 41%, 17%, 0.83);\n      --lumo-shade-90pct: hsla(214, 40%, 16%, 0.94);\n      --lumo-shade: hsl(214, 35%, 15%);\n\n      /* Contrast */\n      --lumo-contrast-5pct: var(--lumo-shade-5pct);\n      --lumo-contrast-10pct: var(--lumo-shade-10pct);\n      --lumo-contrast-20pct: var(--lumo-shade-20pct);\n      --lumo-contrast-30pct: var(--lumo-shade-30pct);\n      --lumo-contrast-40pct: var(--lumo-shade-40pct);\n      --lumo-contrast-50pct: var(--lumo-shade-50pct);\n      --lumo-contrast-60pct: var(--lumo-shade-60pct);\n      --lumo-contrast-70pct: var(--lumo-shade-70pct);\n      --lumo-contrast-80pct: var(--lumo-shade-80pct);\n      --lumo-contrast-90pct: var(--lumo-shade-90pct);\n      --lumo-contrast: var(--lumo-shade);\n\n      /* Text */\n      --lumo-header-text-color: var(--lumo-contrast);\n      --lumo-body-text-color: var(--lumo-contrast-90pct);\n      --lumo-secondary-text-color: var(--lumo-contrast-70pct);\n      --lumo-tertiary-text-color: var(--lumo-contrast-50pct);\n      --lumo-disabled-text-color: var(--lumo-contrast-30pct);\n\n      /* Primary */\n      --lumo-primary-color: hsl(214, 90%, 52%);\n      --lumo-primary-color-50pct: hsla(214, 90%, 52%, 0.5);\n      --lumo-primary-color-10pct: hsla(214, 90%, 52%, 0.1);\n      --lumo-primary-text-color: var(--lumo-primary-color);\n      --lumo-primary-contrast-color: #FFF;\n\n      /* Error */\n      --lumo-error-color: hsl(3, 100%, 61%);\n      --lumo-error-color-50pct: hsla(3, 100%, 60%, 0.5);\n      --lumo-error-color-10pct: hsla(3, 100%, 60%, 0.1);\n      --lumo-error-text-color: hsl(3, 92%, 53%);\n      --lumo-error-contrast-color: #FFF;\n\n      /* Success */\n      --lumo-success-color: hsl(145, 80%, 42%); /* hsl(144,82%,37%); */\n      --lumo-success-color-50pct: hsla(145, 76%, 44%, 0.55);\n      --lumo-success-color-10pct: hsla(145, 76%, 44%, 0.12);\n      --lumo-success-text-color: hsl(145, 100%, 32%);\n      --lumo-success-contrast-color: #FFF;\n    }\n  \x3c/style\x3e\n\x3c/custom-style\x3e\x3cdom-module id\x3d"lumo-color"\x3e\n  \x3ctemplate\x3e\n    \x3cstyle\x3e\n      [theme~\x3d"dark"] {\n        /* Base (background) */\n        --lumo-base-color: hsl(214, 35%, 21%);\n\n        /* Tint */\n        --lumo-tint-5pct: hsla(214, 65%, 85%, 0.06);\n        --lumo-tint-10pct: hsla(214, 60%, 80%, 0.14);\n        --lumo-tint-20pct: hsla(214, 64%, 82%, 0.23);\n        --lumo-tint-30pct: hsla(214, 69%, 84%, 0.32);\n        --lumo-tint-40pct: hsla(214, 73%, 86%, 0.41);\n        --lumo-tint-50pct: hsla(214, 78%, 88%, 0.5);\n        --lumo-tint-60pct: hsla(214, 82%, 90%, 0.6);\n        --lumo-tint-70pct: hsla(214, 87%, 92%, 0.7);\n        --lumo-tint-80pct: hsla(214, 91%, 94%, 0.8);\n        --lumo-tint-90pct: hsla(214, 96%, 96%, 0.9);\n        --lumo-tint: hsl(214, 100%, 98%);\n\n        /* Shade */\n        --lumo-shade-5pct: hsla(214, 0%, 0%, 0.07);\n        --lumo-shade-10pct: hsla(214, 4%, 2%, 0.15);\n        --lumo-shade-20pct: hsla(214, 8%, 4%, 0.23);\n        --lumo-shade-30pct: hsla(214, 12%, 6%, 0.32);\n        --lumo-shade-40pct: hsla(214, 16%, 8%, 0.41);\n        --lumo-shade-50pct: hsla(214, 20%, 10%, 0.5);\n        --lumo-shade-60pct: hsla(214, 24%, 12%, 0.6);\n        --lumo-shade-70pct: hsla(214, 28%, 13%, 0.7);\n        --lumo-shade-80pct: hsla(214, 32%, 13%, 0.8);\n        --lumo-shade-90pct: hsla(214, 33%, 13%, 0.9);\n        --lumo-shade: hsl(214, 33%, 13%);\n\n        /* Contrast */\n        --lumo-contrast-5pct: var(--lumo-tint-5pct);\n        --lumo-contrast-10pct: var(--lumo-tint-10pct);\n        --lumo-contrast-20pct: var(--lumo-tint-20pct);\n        --lumo-contrast-30pct: var(--lumo-tint-30pct);\n        --lumo-contrast-40pct: var(--lumo-tint-40pct);\n        --lumo-contrast-50pct: var(--lumo-tint-50pct);\n        --lumo-contrast-60pct: var(--lumo-tint-60pct);\n        --lumo-contrast-70pct: var(--lumo-tint-70pct);\n        --lumo-contrast-80pct: var(--lumo-tint-80pct);\n        --lumo-contrast-90pct: var(--lumo-tint-90pct);\n        --lumo-contrast: var(--lumo-tint);\n\n        /* Text */\n        --lumo-header-text-color: var(--lumo-contrast);\n        --lumo-body-text-color: var(--lumo-contrast-90pct);\n        --lumo-secondary-text-color: var(--lumo-contrast-70pct);\n        --lumo-tertiary-text-color: var(--lumo-contrast-50pct);\n        --lumo-disabled-text-color: var(--lumo-contrast-30pct);\n\n        /* Primary */\n        --lumo-primary-color: hsl(214, 86%, 55%);\n        --lumo-primary-color-50pct: hsla(214, 86%, 55%, 0.5);\n        --lumo-primary-color-10pct: hsla(214, 90%, 63%, 0.1);\n        --lumo-primary-text-color: hsl(214, 100%, 70%);\n        --lumo-primary-contrast-color: #FFF;\n\n        /* Error */\n        --lumo-error-color: hsl(3, 90%, 63%);\n        --lumo-error-color-50pct: hsla(3, 90%, 63%, 0.5);\n        --lumo-error-color-10pct: hsla(3, 90%, 63%, 0.1);\n        --lumo-error-text-color: hsl(3, 100%, 67%);\n\n        /* Success */\n        --lumo-success-color: hsl(145, 65%, 42%);\n        --lumo-success-color-50pct: hsla(145, 65%, 42%, 0.5);\n        --lumo-success-color-10pct: hsla(145, 65%, 42%, 0.1);\n        --lumo-success-text-color: hsl(145, 85%, 47%);\n      }\n\n      html {\n        color: var(--lumo-body-text-color);\n        background-color: var(--lumo-base-color);\n      }\n\n      [theme~\x3d"dark"] {\n        color: var(--lumo-body-text-color);\n        background-color: var(--lumo-base-color);\n      }\n\n      h1,\n      h2,\n      h3,\n      h4,\n      h5,\n      h6 {\n        color: var(--lumo-header-text-color);\n      }\n\n      a {\n        color: var(--lumo-primary-text-color);\n      }\n\n      blockquote {\n        color: var(--lumo-secondary-text-color);\n      }\n\n      code,\n      pre {\n        background-color: var(--lumo-contrast-10pct);\n        border-radius: var(--lumo-border-radius-m);\n      }\n    \x3c/style\x3e\n  \x3c/template\x3e\n\x3c/dom-module\x3e\x3cdom-module id\x3d"lumo-color-legacy"\x3e\n  \x3ctemplate\x3e\n    \x3cstyle include\x3d"lumo-color"\x3e\n      :host {\n        color: var(--lumo-body-text-color) !important;\n        background-color: var(--lumo-base-color) !important;\n      }\n    \x3c/style\x3e\n  \x3c/template\x3e\n\x3c/dom-module\x3e';
document.head.appendChild(cd.content);const dd=document.createElement("template");dd.innerHTML='\x3ccustom-style\x3e\n  \x3cstyle\x3e\n    @font-face {\n      font-family: \'lumo-icons\';\n      src: url(data:application/font-woff;charset\x3dutf-8;base64,d09GRgABAAAAABEgAAsAAAAAIiwAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAABHU1VCAAABCAAAADsAAABUIIslek9TLzIAAAFEAAAAQwAAAFZAIUuKY21hcAAAAYgAAAD4AAADrsCU8d5nbHlmAAACgAAAC2MAABd4h9To2WhlYWQAAA3kAAAAMQAAADYSnCkuaGhlYQAADhgAAAAdAAAAJAbpA35obXR4AAAOOAAAABAAAACspBAAAGxvY2EAAA5IAAAAWAAAAFh55IAsbWF4cAAADqAAAAAfAAAAIAFKAXBuYW1lAAAOwAAAATEAAAIuUUJZCHBvc3QAAA/0AAABKwAAAelm8SzVeJxjYGRgYOBiMGCwY2BycfMJYeDLSSzJY5BiYGGAAJA8MpsxJzM9kYEDxgPKsYBpDiBmg4gCACY7BUgAeJxjYGS+yDiBgZWBgamKaQ8DA0MPhGZ8wGDIyAQUZWBlZsAKAtJcUxgcXjG+0mIO+p/FEMUcxDANKMwIkgMABn8MLQB4nO3SWW6DMABF0UtwCEnIPM/zhLK8LqhfXRybSP14XUYtHV9hGYQwQBNIo3cUIPkhQeM7rib1ekqnXg981XuC1qvy84lzojleh3puxL0hPjGjRU473teloEefAUNGjJkwZcacBUtWrNmwZceeA0dOnLlw5cadB09elPGhGf+j0NTI/65KfXerT6JhqKnpRKtgOpuqaTrtKjPUlqHmhto21I7pL6i6hlqY3q7qGWrfUAeGOjTUkaGODXViqFNDnRnq3FAXhro01JWhrg11Y6hbQ90Z6t5QD4Z6NNSToZ4N9WKoV0O9GerdUB+G+jTUl6GWRvkL24BkEXictVh9bFvVFb/nxvbz+7Rf/N6zHcd2bCfP+Wgc1Z9N0jpNnEL6kbRVS6HA2hQYGh9TGR1CbCqa2rXrWOkQE/sHNJgmtZvoVNZqE1B1DNHxzTQxCehUTYiJTQyENui0qSLezr3PduyQfgmRWOfde8+9551z7rnn/O4jLoJ/bRP0UaKQMLFJjpBAvphLZC3Dk0ok7WBzR2/upJs7Ryw/nfFbln/uuN/apCvwrKLrSvUqRufbm5pn0fs0w4gYxnGVP6qHnO4bWiDQGQgwtS6lm3lB3QoX1M2vwEmuzirF39y+Es2+DJ8d1pkyqBIqoze3D1+Zz4DrFoazxI8dWwMrDlZ2DMqQAR9AROsJU+2cmlTPazTco52F1xTa2a2+K8vvq92dVHmtLoPeQX/AZPRYGthDYOeZjBjKoFsVGulR3lWU95WeCK44qHU7MhWUGUKZDT3oKUcG2GWuh+EDDfUYA/jhAhl0TOsJNYSEu7mQmi3UzfXwZKA4BsVsHLXQYGgRW95uEtpJ1Vfn9XiLriRBlFEqxsDjA09yCNUoQxxwd7KWSTt2y3GTKiflqHRSoWZc3m11Wa/fJdFgXD4sSYfleJBKd8GMz7J8dZn/cGRCcKGDnA2Ge3fKzcvlnTDNthGWLXzX/WaXtUAmRgeLlHSr30r0G9UTXMb0AtmwzOoy73fkSlHZkduw/TYuU9cAD4YutPoxTTsA3797wVr4Z/1NC5zARHr4vtxJjxIfiZMhMkbWk+14BnJZKwqGZwDfswLyxWDSg11rFLJF7Nopxjd1h1/QOT+oezgfu3Yq+Hk+duf5x+40o1GTkaIgikK/IEnC6aYxCUBaZJSN4XTYFjU/YMNIKqJwhDGOCCI8FDXnXmXjtGhGJyShqjAOnBOkW2JG9S7GgYeMWAU5JzhnWmBOaOM+CKEPoqSfFDC2Unq+DLlUgUVUFFLZGJg6jtlojsdsa8kPObPuJdi5dnBdBsLJMGTWDa4t2JvtwuPo9s+Y86suv/W33QG1rAaOAUV+vx4K6f2D04PVKlC7WLSrZzAi45ZV6lIC7WoXqmRyvUqoVwrzUoVsIjeTXWQv+RH5GTlBXiB/In8ln0IbBCAFOajAJrgZYyOHWqOfUe/aHjI12R6OQo1jCgt215l+4f6XPb+0MNou0V+43n2F77tSfRb24d7zitgnKmvYHs69zugaPvBwv6ioXkb2LdL65Atw51uLkXlu1bhMMRcXSPcYoqKIRlh34lQP8/5JbuUFye4vxD6/6MxFF11C0uVLr9Ulgw44tS3pMViNLUExbycFgLIct+QDMibRimx1ydUz8FXZiuOIDBOMVX2nUZc+huNE5XUJ81uiJoiabwqaVF0uacKbau/pl4R2VW0XXlJra6boVrYG646TF5NYzwy4vjENVrDlcNpZPl8DH6XX8XWCx0mvWVZY6KFLrvsY66/zPict5FnxaNUR/juvZCM3TvD60E2W1tZizbXTPDuabcm0nbbzpWKpmA1ayBQ8giedLUM+A0kNjBjQjmuYz7YrgIXYvmF63ZLBwSXrpn9Tb9wwdd/U1H0PMQK3XcO8ul3WT7PyPPdpy0TemKxNRcJNauiXJnnUDpUppQWs4SnUIy0EESGYqJYQLGHxzaGWwVIaS6Y7mQFM8ZjYDQ3axjf61SWjU33JwOZA1pwaG1L9mzf71aHRdX1JHw6Fp0aXhNwbqyeGNg4NbdzGCBxoz4ZXjy4Nu69Zr6sDY6vMrLU5nA1P8JkbdWXJ6ERfMryvNh1JfQ9+T4dIhGvK9w3dxjBBzatsQ/MlOHVIDnYpDz6odAXlQ01t2Pa5Iafd8MMpxAeDKP0C6CjgVLT5osB6icUx01lWjXxzT/GyRF2welEM5Z/7jG3VjQ1SrNn5IbyzOG5dobB3/QHxyZvsXcoz8IoEwS7plCg+zxHQk424q9BfEpkESJbFHQusDBSWFkuBkoPO0kLKwRVYjxGXlHTcTDQMJ/H6TX9afkO7mnraTO1feTnZAXLu4cp7HAXMmNG1yeFk9TgS/NHhZR/4QoBTr/ZB+6hCgyl15Nq1UbN6nE1/ZnP1U2cizCBpvs8cJQZJ4LkYx5N/yZPAUZNQQ0V4f3BQllWrK3YRzl30dOT6RVn2upNur6woSa8CqpdT/aKnBM4o3jNur9d9xqtUT6veBEt9Ca9at+ERzEEhUkR8sa5mQ4aVvJoVeEA8zI4ei5mULXFGyU7z/6TAeYLVcpzSWZY8PYYF5yrTV60sT0+XV141vX++Wf16V2bFeGVPZXxFpkvyeKTWLlzfW0mnKxsY6Y3294/0998SCfX1blm5pbcvFGlq/r07MRAMhYIDiW5JFKWW3vdrEpCsZSJG+om7Zu/PSScZJhNkLbmW5Wsr12pWqW5zKtlwRS4bFOxUw17mCzy6lskCDl1WYOGWDYrADrMA7BDDweWWNd5koiJnR1dz+ytLP2q0SqPB1lnK2ccB7RYe4FSoPks3iB3t4txTSHctb2sy1ivk0pvHuCNm6w1f6wxv3+OCgN78LqdQnUVh7R0oTAp0zOf2rbW770Vu5C2dIyGdTnHo8zSji7dppj0USoVCz+lhRMTh53Teq9VbGfbjuSbAooSdXayY4PYHg374C6f7gl1B/DXuJ4/QXxOBdJFJspFsI3egpoWUUCjlTIFnNYNl+ZyZKmBeYKGHkD1QyDlhaKbKwKcIJqJ4TLJ2OmdY/JWXae4DdGBw8HZ7eXcgFF2zr2SoalDry5iKqoa0Puhe3hPQ2s3elTYM+MI+n3rK0KgL7/La3GeMLt6m7u912vGnvtORiIa0qBmhqVi+XW9XNBmqb8eVgKzIHfGI5bNoG7X0UCzeISmqIcO/nY8FH7U8avX9fx/ST+hx0sezPw9Qy8Mum3GWf2N4Uy/yIYGVBXbJHWIZp7dfTcptdMTr9Qmq7DaiK/ukqCL4kt4RUfS5XPnMtmT22/mQFqF7emSqtrlu8SVElxDRJrZODkpuwe0VfTfjdEp1f7A7v+fozNBXUJ/6WTuK2TtFlpFVZAZ3LcFvUi1Z2p2YT+EMAkGJVStOzLTAPg4IqWIAlzRSjOBkl2zxj3TKycpzT/MnvX3uaSMWM+gU0rkXjohhefVRMaps3/kLMSKv23lT23uxQrkQjyOJleMDsdhAnD6ZGElWZ5MjCXzCE/hkWX+WF4knzGhVOyK2eQZekV3eyo0zL8kuYWCnDCvjjhAkcTPOBDXVdoav3HVcFnQjLvtV9S2p0zA6JegPwMQxt+yFb3ll9zGlq/5dRKb3cEyQYoaNYpharJ7xCB7AWxsLY3jjZXY0XsZj0Wjwc9I6PP/dKABnCZaqHpaZEACxk4ZeLZSKNgZABl+lYQX1sJQOSX3n6r410evcoud5JeAGUXVP9H1tZOKejTq4Ono0z0erro1FrnOpohva1d/hTdtVsQdKN5W9RlT3NjD0nznyKNTgKAMfWNWcyodV0IGLPIHOF0o4JyqufaK4z6WIIzuGh3d8c8cwQg8ER+OVxyrjdm8vNuhts4LoOihGxIMuUdgzwiYN7xhh1+oZnJNuTG7gQZvu4XWZ9GAZZjGEubwePqYhtKDTH+9VQkl17/iGybsnJ+8+sKtyPrcll9ty65Zsdst/9iqpEKh7M5VdBxh3csOdNc6tW3I1uyM1PzOXegSOrLFsFNI2O27M+TF2ApnN9MUv5ud6LjxIvEQnHRzxIu4IsA9MLFkJn2tcZoZ7ON7dXe7ujrc8HrusPKamlqXwd77lQUuLpilau4PUMapueBb7irU4RoUXEYXuVuIGlRGmOp+2lNkaRPVziOqmlaZvaqG4dFgSj0jxEJWrv12IUWntmw+rfQarRE0Aph4ocI6nlUlGqs+u3/+T/ethW62PpHp2eHbZstnh/wOO95yDAHicY2BkYGAAYi2NOJ94fpuvDNzML4AiDNc/fzqEoP+/Zp7KdAvI5WBgAokCAGkcDfgAAAB4nGNgZGBgDvqfBSRfMAAB81QGRgZUoA0AVvYDbwAAAHicY2BgYGB+MTQwAM8EJo8AAAAAAE4AmgDoAQoBLAFOAXABmgHEAe4CGgKcAugEmgS8BNYE8gUOBSoFegXQBf4GRAZmBrYHGAeQCBgIUghqCP4JRgm+CdoKBAo8CoIKuArwC1ALlgu8eJxjYGRgYNBmTGEQZQABJiDmAkIGhv9gPgMAGJQBvAB4nG2RPU7DMBiG3/QP0UoIBGJh8QILavozdmRo9w7d09RpUzlx5LgVvQMn4BAcgoEzcAgOwVvzSZVQbcnf48fvFysJgGt8IcJxROiG9TgauODuj5ukG+EW+UG4jR4ehTv0Q+EunjER7uEWmk+IWpc0d3gVbuAKb8JN+nfhFvlDuI17fAp36L+Fu1jgR7iHp+jF7Arbz1Nb1nO93pnEncSJFtrVuS3VKB6e5EyX2iVer9TyoOr9eux9pjJnCzW1pdfGWFU5u9WpjzfeV5PBIBMfp7aAwQ4FLPrIkbKWqDHn+67pDRK4s4lzbsEux5qHvcIIMb/nueSMyTKkE3jWFdNLHLjW2PPmMa1Hxn3GjGW/wjT0HtOG09JU4WxLk9LH2ISuiv9twJn9y8fh9uIXI+BknAAAAHicbY7ZboMwEEW5CVBCSLrv+76kfJRjTwHFsdGAG+Xvy5JUfehIHp0rnxmNN/D6ir3/a4YBhvARIMQOIowQY4wEE0yxiz3s4wCHOMIxTnCKM5zjApe4wjVucIs73OMBj3jCM17wije84wMzfHqJ0EVmUkmmJo77oOmrHvfIRZbXsTCZplTZldlgb3TYGVHProwFs11t1A57tcON2rErR3PBqcwF1/6ctI6k0GSU4JHMSS6WghdJQ99sTbfuN7QLJ9vQ37dNrgyktnIxlDYLJNuqitpRbYWKFNuyDT6pog6oOYKHtKakeakqKjHXpPwlGRcsC+OqxLIiJpXqoqqDMreG2l5bv9Ri3TRX+c23DZna9WFFgmXuO6Ps1Jm/w6ErW8N3FbHn/QC444j0AA\x3d\x3d) format(\'woff\');\n      font-weight: normal;\n      font-style: normal;\n    }\n\n    html {\n      --lumo-icons-align-center: "\\ea01";\n      --lumo-icons-align-left: "\\ea02";\n      --lumo-icons-align-right: "\\ea03";\n      --lumo-icons-angle-down: "\\ea04";\n      --lumo-icons-angle-left: "\\ea05";\n      --lumo-icons-angle-right: "\\ea06";\n      --lumo-icons-angle-up: "\\ea07";\n      --lumo-icons-arrow-down: "\\ea08";\n      --lumo-icons-arrow-left: "\\ea09";\n      --lumo-icons-arrow-right: "\\ea0a";\n      --lumo-icons-arrow-up: "\\ea0b";\n      --lumo-icons-bar-chart: "\\ea0c";\n      --lumo-icons-bell: "\\ea0d";\n      --lumo-icons-calendar: "\\ea0e";\n      --lumo-icons-checkmark: "\\ea0f";\n      --lumo-icons-chevron-down: "\\ea10";\n      --lumo-icons-chevron-left: "\\ea11";\n      --lumo-icons-chevron-right: "\\ea12";\n      --lumo-icons-chevron-up: "\\ea13";\n      --lumo-icons-clock: "\\ea14";\n      --lumo-icons-cog: "\\ea15";\n      --lumo-icons-cross: "\\ea16";\n      --lumo-icons-download: "\\ea17";\n      --lumo-icons-dropdown: "\\ea18";\n      --lumo-icons-edit: "\\ea19";\n      --lumo-icons-error: "\\ea1a";\n      --lumo-icons-eye: "\\ea1b";\n      --lumo-icons-eye-disabled: "\\ea1c";\n      --lumo-icons-menu: "\\ea1d";\n      --lumo-icons-minus: "\\ea1e";\n      --lumo-icons-ordered-list: "\\ea1f";\n      --lumo-icons-phone: "\\ea20";\n      --lumo-icons-photo: "\\ea21";\n      --lumo-icons-play: "\\ea22";\n      --lumo-icons-plus: "\\ea23";\n      --lumo-icons-redo: "\\ea24";\n      --lumo-icons-reload: "\\ea25";\n      --lumo-icons-search: "\\ea26";\n      --lumo-icons-undo: "\\ea27";\n      --lumo-icons-unordered-list: "\\ea28";\n      --lumo-icons-upload: "\\ea29";\n      --lumo-icons-user: "\\ea2a";\n    }\n  \x3c/style\x3e\n\x3c/custom-style\x3e';
document.head.appendChild(dd.content);const ed=document.createElement("template");ed.innerHTML="\x3ccustom-style\x3e\n  \x3cstyle\x3e\n    html {\n      --lumo-size-xs: 1.625rem;\n      --lumo-size-s: 1.875rem;\n      --lumo-size-m: 2.25rem;\n      --lumo-size-l: 2.75rem;\n      --lumo-size-xl: 3.5rem;\n\n      /* Icons */\n      --lumo-icon-size-s: 1.25em;\n      --lumo-icon-size-m: 1.5em;\n      --lumo-icon-size-l: 2.25em;\n      /* For backwards compatibility */\n      --lumo-icon-size: var(--lumo-icon-size-m);\n    }\n  \x3c/style\x3e\n\x3c/custom-style\x3e";
document.head.appendChild(ed.content);const fd=document.createElement("template");fd.innerHTML="\x3ccustom-style\x3e\n  \x3cstyle\x3e\n    html {\n      /* Square */\n      --lumo-space-xs: 0.25rem;\n      --lumo-space-s: 0.5rem;\n      --lumo-space-m: 1rem;\n      --lumo-space-l: 1.5rem;\n      --lumo-space-xl: 2.5rem;\n\n      /* Wide */\n      --lumo-space-wide-xs: calc(var(--lumo-space-xs) / 2) var(--lumo-space-xs);\n      --lumo-space-wide-s: calc(var(--lumo-space-s) / 2) var(--lumo-space-s);\n      --lumo-space-wide-m: calc(var(--lumo-space-m) / 2) var(--lumo-space-m);\n      --lumo-space-wide-l: calc(var(--lumo-space-l) / 2) var(--lumo-space-l);\n      --lumo-space-wide-xl: calc(var(--lumo-space-xl) / 2) var(--lumo-space-xl);\n\n      /* Tall */\n      --lumo-space-tall-xs: var(--lumo-space-xs) calc(var(--lumo-space-xs) / 2);\n      --lumo-space-tall-s: var(--lumo-space-s) calc(var(--lumo-space-s) / 2);\n      --lumo-space-tall-m: var(--lumo-space-m) calc(var(--lumo-space-m) / 2);\n      --lumo-space-tall-l: var(--lumo-space-l) calc(var(--lumo-space-l) / 2);\n      --lumo-space-tall-xl: var(--lumo-space-xl) calc(var(--lumo-space-xl) / 2);\n    }\n  \x3c/style\x3e\n\x3c/custom-style\x3e";
document.head.appendChild(fd.content);const gd=document.createElement("template");gd.innerHTML="\x3ccustom-style\x3e\n  \x3cstyle\x3e\n    html {\n      /* Border radius */\n      --lumo-border-radius-s: 0.25em; /* Checkbox, badge, date-picker year indicator, etc */\n      --lumo-border-radius-m: var(--lumo-border-radius, 0.25em); /* Button, text field, menu overlay, etc */\n      --lumo-border-radius-l: 0.5em; /* Dialog, notification, etc */\n      --lumo-border-radius: 0.25em; /* Deprecated */\n\n      /* Shadow */\n      --lumo-box-shadow-xs: 0 1px 4px -1px var(--lumo-shade-50pct);\n      --lumo-box-shadow-s: 0 2px 4px -1px var(--lumo-shade-20pct), 0 3px 12px -1px var(--lumo-shade-30pct);\n      --lumo-box-shadow-m: 0 2px 6px -1px var(--lumo-shade-20pct), 0 8px 24px -4px var(--lumo-shade-40pct);\n      --lumo-box-shadow-l: 0 3px 18px -2px var(--lumo-shade-20pct), 0 12px 48px -6px var(--lumo-shade-40pct);\n      --lumo-box-shadow-xl: 0 4px 24px -3px var(--lumo-shade-20pct), 0 18px 64px -8px var(--lumo-shade-40pct);\n\n      /* Clickable element cursor */\n      --lumo-clickable-cursor: default;\n    }\n  \x3c/style\x3e\n\x3c/custom-style\x3e";
document.head.appendChild(gd.content);const hd=document.createElement("template");hd.innerHTML='\x3ccustom-style\x3e\n  \x3cstyle\x3e\n    html {\n      /* Font families */\n      --lumo-font-family: -apple-system, BlinkMacSystemFont, "Roboto", "Segoe UI", Helvetica, Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";\n\n      /* Font sizes */\n      --lumo-font-size-xxs: .75rem;\n      --lumo-font-size-xs: .8125rem;\n      --lumo-font-size-s: .875rem;\n      --lumo-font-size-m: 1rem;\n      --lumo-font-size-l: 1.125rem;\n      --lumo-font-size-xl: 1.375rem;\n      --lumo-font-size-xxl: 1.75rem;\n      --lumo-font-size-xxxl: 2.5rem;\n\n      /* Line heights */\n      --lumo-line-height-xs: 1.25;\n      --lumo-line-height-s: 1.375;\n      --lumo-line-height-m: 1.625;\n    }\n\n  \x3c/style\x3e\n\x3c/custom-style\x3e\x3cdom-module id\x3d"lumo-typography"\x3e\n  \x3ctemplate\x3e\n    \x3cstyle\x3e\n      html {\n        font-family: var(--lumo-font-family);\n        font-size: var(--lumo-font-size, var(--lumo-font-size-m));\n        line-height: var(--lumo-line-height-m);\n        -webkit-text-size-adjust: 100%;\n        -webkit-font-smoothing: antialiased;\n        -moz-osx-font-smoothing: grayscale;\n      }\n\n      /* Can\u2019t combine with the above selector because that doesn\u2019t work in browsers without native shadow dom */\n      :host {\n        font-family: var(--lumo-font-family);\n        font-size: var(--lumo-font-size, var(--lumo-font-size-m));\n        line-height: var(--lumo-line-height-m);\n        -webkit-text-size-adjust: 100%;\n        -webkit-font-smoothing: antialiased;\n        -moz-osx-font-smoothing: grayscale;\n      }\n\n      small,\n      [theme~\x3d"font-size-s"] {\n        font-size: var(--lumo-font-size-s);\n        line-height: var(--lumo-line-height-s);\n      }\n\n      [theme~\x3d"font-size-xs"] {\n        font-size: var(--lumo-font-size-xs);\n        line-height: var(--lumo-line-height-xs);\n      }\n\n      h1,\n      h2,\n      h3,\n      h4,\n      h5,\n      h6 {\n        font-weight: 600;\n        line-height: var(--lumo-line-height-xs);\n        margin-top: 1.25em;\n      }\n\n      h1 {\n        font-size: var(--lumo-font-size-xxxl);\n        margin-bottom: 0.75em;\n      }\n\n      h2 {\n        font-size: var(--lumo-font-size-xxl);\n        margin-bottom: 0.5em;\n      }\n\n      h3 {\n        font-size: var(--lumo-font-size-xl);\n        margin-bottom: 0.5em;\n      }\n\n      h4 {\n        font-size: var(--lumo-font-size-l);\n        margin-bottom: 0.5em;\n      }\n\n      h5 {\n        font-size: var(--lumo-font-size-m);\n        margin-bottom: 0.25em;\n      }\n\n      h6 {\n        font-size: var(--lumo-font-size-xs);\n        margin-bottom: 0;\n        text-transform: uppercase;\n        letter-spacing: 0.03em;\n      }\n\n      p,\n      blockquote {\n        margin-top: 0.5em;\n        margin-bottom: 0.75em;\n      }\n\n      a {\n        text-decoration: none;\n      }\n\n      a:hover {\n        text-decoration: underline;\n      }\n\n      hr {\n        display: block;\n        align-self: stretch;\n        height: 1px;\n        border: 0;\n        padding: 0;\n        margin: var(--lumo-space-s) calc(var(--lumo-border-radius-m) / 2);\n        background-color: var(--lumo-contrast-10pct);\n      }\n\n      blockquote {\n        border-left: 2px solid var(--lumo-contrast-30pct);\n      }\n\n      b,\n      strong {\n        font-weight: 600;\n      }\n\n      /* RTL specific styles */\n\n      blockquote[dir\x3d"rtl"] {\n        border-left: none;\n        border-right: 2px solid var(--lumo-contrast-30pct);\n      }\n\n    \x3c/style\x3e\n  \x3c/template\x3e\n\x3c/dom-module\x3e';
document.head.appendChild(hd.content);const H=function(b,...a){const c=document.createElement("template");c.innerHTML=a.reduce((d,e,f)=>{if(e instanceof HTMLTemplateElement)e=e.innerHTML;else throw Error(`non-template value passed to Polymer's html function: ${e}`);return d+e+b[f+1]},b[0]);return c},hf=H`<dom-module id="lumo-checkbox" theme-for="vaadin-checkbox">
  <template>
    <style include="lumo-checkbox-style lumo-checkbox-effects">
      /* IE11 only */
      ::-ms-backdrop,
      [part="checkbox"] {
        line-height: 1;
      }
    </style>
  </template>
</dom-module><dom-module id="lumo-checkbox-style">
  <template>
    <style>
      :host {
        -webkit-tap-highlight-color: transparent;
        -webkit-user-select: none;
        -moz-user-select: none;
        -ms-user-select: none;
        user-select: none;
        cursor: default;
        outline: none;
      }

      [part="label"]:not([empty]) {
        margin: 0.1875em 0.875em 0.1875em 0.375em;
      }

      [part="checkbox"] {
        width: calc(1em + 2px);
        height: calc(1em + 2px);
        margin: 0.1875em;
        position: relative;
        border-radius: var(--lumo-border-radius-s);
        background-color: var(--lumo-contrast-20pct);
        transition: transform 0.2s cubic-bezier(.12, .32, .54, 2), background-color 0.15s;
        pointer-events: none;
        line-height: 1.2;
      }

      :host([indeterminate]) [part="checkbox"],
      :host([checked]) [part="checkbox"] {
        background-color: var(--lumo-primary-color);
      }

      /* Needed to align the checkbox nicely on the baseline */
      [part="checkbox"]::before {
        content: "\\2003";
      }

      /* Checkmark */
      [part="checkbox"]::after {
        content: "";
        display: inline-block;
        width: 0;
        height: 0;
        border: 0 solid var(--lumo-primary-contrast-color);
        border-width: 0.1875em 0 0 0.1875em;
        box-sizing: border-box;
        transform-origin: 0 0;
        position: absolute;
        top: 0.8125em;
        left: 0.5em;
        transform: scale(0.55) rotate(-135deg);
        opacity: 0;
      }

      :host([checked]) [part="checkbox"]::after {
        opacity: 1;
        width: 0.625em;
        height: 1.0625em;
      }

      /* Indeterminate checkmark */

      :host([indeterminate]) [part="checkbox"]::after {
        transform: none;
        opacity: 1;
        top: 45%;
        height: 10%;
        left: 22%;
        right: 22%;
        width: auto;
        border: 0;
        background-color: var(--lumo-primary-contrast-color);
        transition: opacity 0.25s;
      }

      /* Focus ring */

      :host([focus-ring]) [part="checkbox"] {
        box-shadow: 0 0 0 3px var(--lumo-primary-color-50pct);
      }

      /* Disabled */

      :host([disabled]) {
        pointer-events: none;
        color: var(--lumo-disabled-text-color);
      }

      :host([disabled]) [part="label"] ::slotted(*) {
        color: inherit;
      }

      :host([disabled]) [part="checkbox"] {
        background-color: var(--lumo-contrast-10pct);
      }

      :host([disabled]) [part="checkbox"]::after {
        border-color: var(--lumo-contrast-30pct);
      }

      :host([indeterminate][disabled]) [part="checkbox"]::after {
        background-color: var(--lumo-contrast-30pct);
      }

      /* RTL specific styles */

      :host([dir="rtl"]) [part="label"]:not([empty]) {
        margin: 0.1875em 0.375em 0.1875em 0.875em;
      }
    </style>
  </template>
</dom-module><dom-module id="lumo-checkbox-effects">
  <template>
    <style>
      /* Transition the checkmark if activated with the mouse (disabled for grid select-all this way) */
      :host(:hover) [part="checkbox"]::after {
        transition: width 0.1s, height 0.25s;
      }

      /* Used for activation "halo" */
      [part="checkbox"]::before {
        color: transparent;
        display: inline-block;
        width: 100%;
        height: 100%;
        border-radius: inherit;
        background-color: inherit;
        transform: scale(1.4);
        opacity: 0;
        transition: transform 0.1s, opacity 0.8s;
      }

      /* Hover */

      :host(:not([checked]):not([indeterminate]):not([disabled]):hover) [part="checkbox"] {
        background-color: var(--lumo-contrast-30pct);
      }

      /* Disable hover for touch devices */
      @media (pointer: coarse) {
        :host(:not([checked]):not([indeterminate]):not([disabled]):hover) [part="checkbox"] {
          background-color: var(--lumo-contrast-20pct);
        }
      }

      /* Active */

      :host([active]) [part="checkbox"] {
        transform: scale(0.9);
        transition-duration: 0.05s;
      }

      :host([active][checked]) [part="checkbox"] {
        transform: scale(1.1);
      }

      :host([active]:not([checked])) [part="checkbox"]::before {
        transition-duration: 0.01s, 0.01s;
        transform: scale(0);
        opacity: 0.4;
      }
    </style>
  </template>
</dom-module>`;document.head.appendChild(hf.content);let jf=0;const D=function(b){let a=b.__mixinApplications;a||(a=new WeakMap,b.__mixinApplications=a);let c=jf++;return function(d){let e=d.__mixinSet;if(e&&e[c])return d;let f=a,g=f.get(d);g||(g=b(d),f.set(d,g),d=Object.create(g.__mixinSet||e||null),d[c]=!0,g.__mixinSet=d);return g}},q=window.ShadyDOM&&window.ShadyDOM.noPatch&&window.ShadyDOM.wrap?window.ShadyDOM.wrap:window.ShadyDOM?b=>ShadyDOM.patch(b):b=>b,Ja={},le=/-[a-z]/g,me=/([A-Z])/g;let kf=
0,id=0,da=[],lf=0,Kb=!1,jd=document.createTextNode("");(new window.MutationObserver(function(){Kb=!1;const b=da.length;for(let a=0;a<b;a++){let c=da[a];if(c)try{c()}catch(d){setTimeout(()=>{throw d;})}}da.splice(0,b);id+=b})).observe(jd,{characterData:!0});const E={after(b){return{run(a){return window.setTimeout(a,b)},cancel(a){window.clearTimeout(a)}}},run(b,a){return window.setTimeout(b,a)},cancel(b){window.clearTimeout(b)}},M={run(b){return window.requestAnimationFrame(b)},cancel(b){window.cancelAnimationFrame(b)}},
kd={run(b){return window.requestIdleCallback?window.requestIdleCallback(b):window.setTimeout(b,16)},cancel(b){window.cancelIdleCallback?window.cancelIdleCallback(b):window.clearTimeout(b)}},G={run(b){Kb||(Kb=!0,jd.textContent=lf++);da.push(b);return kf++},cancel(b){const a=b-id;if(0<=a){if(!da[a])throw Error("invalid async handle: "+b);da[a]=null}}},mf=G,ld=D(b=>{class a extends b{static createProperties(c){const d=this.prototype;for(let e in c)e in d||d._createPropertyAccessor(e)}static attributeNameForProperty(c){return c.toLowerCase()}static typeForProperty(c){}_createPropertyAccessor(c,
d){this._addPropertyToAttributeMap(c);this.hasOwnProperty("__dataHasAccessor")||(this.__dataHasAccessor=Object.assign({},this.__dataHasAccessor));this.__dataHasAccessor[c]||(this.__dataHasAccessor[c]=!0,this._definePropertyAccessor(c,d))}_addPropertyToAttributeMap(c){this.hasOwnProperty("__dataAttributes")||(this.__dataAttributes=Object.assign({},this.__dataAttributes));let d=this.__dataAttributes[c];d||(d=this.constructor.attributeNameForProperty(c),this.__dataAttributes[d]=c);return d}_definePropertyAccessor(c,
d){Object.defineProperty(this,c,{get(){return this.__data[c]},set:d?function(){}:function(e){this._setPendingProperty(c,e,!0)&&this._invalidateProperties()}})}constructor(){super();this.__dataInvalid=this.__dataReady=this.__dataEnabled=!1;this.__data={};this.__dataInstanceProps=this.__dataOld=this.__dataPending=null;this.__dataCounter=0;this.__serializing=!1;this._initializeProperties()}ready(){this.__dataReady=!0;this._flushProperties()}_initializeProperties(){for(let c in this.__dataHasAccessor)this.hasOwnProperty(c)&&
(this.__dataInstanceProps=this.__dataInstanceProps||{},this.__dataInstanceProps[c]=this[c],delete this[c])}_initializeInstanceProperties(c){Object.assign(this,c)}_setProperty(c,d){this._setPendingProperty(c,d)&&this._invalidateProperties()}_getProperty(c){return this.__data[c]}_setPendingProperty(c,d,e){e=this.__data[c];let f=this._shouldPropertyChange(c,d,e);f&&(this.__dataPending||(this.__dataPending={},this.__dataOld={}),!this.__dataOld||c in this.__dataOld||(this.__dataOld[c]=e),this.__data[c]=
d,this.__dataPending[c]=d);return f}_isPropertyPending(c){return!(!this.__dataPending||!this.__dataPending.hasOwnProperty(c))}_invalidateProperties(){!this.__dataInvalid&&this.__dataReady&&(this.__dataInvalid=!0,mf.run(()=>{this.__dataInvalid&&(this.__dataInvalid=!1,this._flushProperties())}))}_enableProperties(){this.__dataEnabled||(this.__dataEnabled=!0,this.__dataInstanceProps&&(this._initializeInstanceProperties(this.__dataInstanceProps),this.__dataInstanceProps=null),this.ready())}_flushProperties(){this.__dataCounter++;
const c=this.__data,d=this.__dataPending,e=this.__dataOld;this._shouldPropertiesChange(c,d,e)&&(this.__dataOld=this.__dataPending=null,this._propertiesChanged(c,d,e));this.__dataCounter--}_shouldPropertiesChange(c,d,e){return!!d}_propertiesChanged(c,d,e){}_shouldPropertyChange(c,d,e){return e!==d&&(e===e||d===d)}attributeChangedCallback(c,d,e,f){d!==e&&this._attributeToProperty(c,e);super.attributeChangedCallback&&super.attributeChangedCallback(c,d,e,f)}_attributeToProperty(c,d,e){if(!this.__serializing){const f=
this.__dataAttributes;c=f&&f[c]||c;this[c]=this._deserializeValue(d,e||this.constructor.typeForProperty(c))}}_propertyToAttribute(c,d,e){this.__serializing=!0;e=3>arguments.length?this[c]:e;this._valueToNodeAttribute(this,e,d||this.constructor.attributeNameForProperty(c));this.__serializing=!1}_valueToNodeAttribute(c,d,e){d=this._serializeValue(d);if("class"===e||"name"===e||"slot"===e)c=q(c);void 0===d?c.removeAttribute(e):c.setAttribute(e,d)}_serializeValue(c){switch(typeof c){case "boolean":return c?
"":void 0;default:return null!=c?c.toString():void 0}}_deserializeValue(c,d){switch(d){case Boolean:return null!==c;case Number:return Number(c);default:return c}}}return a}),md={};let Xa=HTMLElement.prototype;for(;Xa;){let b=Object.getOwnPropertyNames(Xa);for(let a=0;a<b.length;a++)md[b[a]]=!0;Xa=Object.getPrototypeOf(Xa)}const nd=D(b=>{b=ld(b);class a extends b{static createPropertiesForAttributes(){let c=this.observedAttributes;for(let d=0;d<c.length;d++)this.prototype._createPropertyAccessor(fc(c[d]))}static attributeNameForProperty(c){return Ka(c)}_initializeProperties(){this.__dataProto&&
(this._initializeProtoProperties(this.__dataProto),this.__dataProto=null);super._initializeProperties()}_initializeProtoProperties(c){for(let d in c)this._setProperty(d,c[d])}_ensureAttribute(c,d){this.hasAttribute(c)||this._valueToNodeAttribute(this,d,c)}_serializeValue(c){switch(typeof c){case "object":if(c instanceof Date)return c.toString();if(c)try{return JSON.stringify(c)}catch(d){return""}default:return super._serializeValue(c)}}_deserializeValue(c,d){let e;switch(d){case Object:try{e=JSON.parse(c)}catch(f){e=
c}break;case Array:try{e=JSON.parse(c)}catch(f){e=null,console.warn(`Polymer::Attributes: couldn't decode Array as JSON: ${c}`)}break;case Date:e=isNaN(c)?String(c):Number(c);e=new Date(e);break;default:e=super._deserializeValue(c,d)}return e}_definePropertyAccessor(c,d){if(!md[c]){let e=this[c];void 0!==e&&(this.__data?this._setPendingProperty(c,e):(this.__dataProto?this.hasOwnProperty("__dataProto")||(this.__dataProto=Object.create(this.__dataProto)):this.__dataProto={},this.__dataProto[c]=e))}super._definePropertyAccessor(c,
d)}_hasAccessor(c){return this.__dataHasAccessor&&this.__dataHasAccessor[c]}_isPropertyPending(c){return!!(this.__dataPending&&c in this.__dataPending)}}return a}),nf={"dom-if":!0,"dom-repeat":!0};let od=!1,pd=!1;const of=D(b=>{class a extends b{static _parseTemplate(c,d){if(!c._templateInfo){let e=c._templateInfo={};e.nodeInfoList=[];e.nestedTemplate=!!d;e.stripWhiteSpace=d&&d.stripWhiteSpace||c.hasAttribute("strip-whitespace");this._parseTemplateContent(c,e,{parent:null})}return c._templateInfo}static _parseTemplateContent(c,
d,e){return this._parseTemplateNode(c.content,d,e)}static _parseTemplateNode(c,d,e){let f=!1;"template"!=c.localName||c.hasAttribute("preserve-content")?"slot"===c.localName&&(d.hasInsertionPoint=!0):f=this._parseTemplateNestedTemplate(c,d,e)||f;if(!od){od=!0;const g=document.createElement("textarea");g.placeholder="a";pd=g.placeholder===g.textContent}pd&&"textarea"===c.localName&&c.placeholder&&c.placeholder===c.textContent&&(c.textContent=null);c.firstChild&&this._parseTemplateChildNodes(c,d,e);
c.hasAttributes&&c.hasAttributes()&&(f=this._parseTemplateNodeAttributes(c,d,e)||f);return f||e.noted}static _parseTemplateChildNodes(c,d,e){if("script"!==c.localName&&"style"!==c.localName)for(let g=c.firstChild,h=0,k;g;g=k){if("template"==g.localName){{var f=g;let l=f.getAttribute("is");if(l&&nf[l]){let m=f;m.removeAttribute("is");f=m.ownerDocument.createElement(l);m.parentNode.replaceChild(f,m);for(f.appendChild(m);m.attributes.length;)f.setAttribute(m.attributes[0].name,m.attributes[0].value),
m.removeAttribute(m.attributes[0].name)}g=f}}k=g.nextSibling;if(g.nodeType===Node.TEXT_NODE){for(f=k;f&&f.nodeType===Node.TEXT_NODE;)g.textContent+=f.textContent,k=f.nextSibling,c.removeChild(f),f=k;if(d.stripWhiteSpace&&!g.textContent.trim()){c.removeChild(g);continue}}f={parentIndex:h,parentInfo:e};this._parseTemplateNode(g,d,f)&&(f.infoIndex=d.nodeInfoList.push(f)-1);g.parentNode&&h++}}static _parseTemplateNestedTemplate(c,d,e){d=this._parseTemplate(c,d);(d.content=c.content.ownerDocument.createDocumentFragment()).appendChild(c.content);
e.templateInfo=d;return!0}static _parseTemplateNodeAttributes(c,d,e){let f=!1,g=Array.from(c.attributes);for(let h=g.length-1,k;k=g[h];h--)f=this._parseTemplateNodeAttribute(c,d,e,k.name,k.value)||f;return f}static _parseTemplateNodeAttribute(c,d,e,f,g){return"on-"===f.slice(0,3)?(c.removeAttribute(f),e.events=e.events||[],e.events.push({name:f.slice(3),value:g}),!0):"id"===f?(e.id=g,!0):!1}static _contentForTemplate(c){let d=c._templateInfo;return d&&d.content||c.content}_stampTemplate(c,d){c&&!c.content&&
window.HTMLTemplateElement&&HTMLTemplateElement.decorate&&HTMLTemplateElement.decorate(c);d=d||this.constructor._parseTemplate(c);let e=d.nodeInfoList;c=document.importNode(d.content||c.content,!0);c.__noInsertionPoint=!d.hasInsertionPoint;let f=c.nodeList=Array(e.length);c.$={};for(let k=0,l=e.length,m;k<l&&(m=e[k]);k++){let n=f[k]=gc(c,m);m.id&&(c.$[m.id]=n);var g=n,h=d;m.templateInfo&&(g._templateInfo=m.templateInfo,g._parentTemplateInfo=h);g=n;h=m;if(h.events&&h.events.length)for(let p=0,r=h.events,
u;p<r.length&&(u=r[p]);p++)this._addMethodEventListenerToNode(g,u.name,u.value,this)}return c}_addMethodEventListenerToNode(c,d,e,f){e=ne(f||c,d,e);this._addEventListenerToNode(c,d,e);return e}_addEventListenerToNode(c,d,e){c.addEventListener(d,e)}_removeEventListenerFromNode(c,d,e){c.removeEventListener(d,e)}}return a});let oa=0;const pa=[],t={COMPUTE:"__computeEffects",REFLECT:"__reflectEffects",NOTIFY:"__notifyEffects",PROPAGATE:"__propagateEffects",OBSERVE:"__observeEffects",READ_ONLY:"__readOnly"},
pf=/[A-Z]/,qd=(b,a,c,d,e)=>{var f=e?K(b):b;if(a=a[f])for(f=0;f<a.length;f++){var g=a[f];if(g.info.lastRun!==oa&&(!e||nb(b,g.trigger))){g.info.lastRun=oa;{g=g.info;var h=c,k=d;let l=0,m=h.length-1,n=-1;for(;l<=m;){const p=l+m>>1,r=k.get(h[p].methodInfo)-k.get(g.methodInfo);if(0>r)l=p+1;else if(0<r)m=p-1;else{n=p;break}}0>n&&(n=m+1);h.splice(n,0,g)}}}},ue=[],rd=/(\[\[|{{)\s*(?:(!)\s*)?((?:[a-zA-Z_$][\w.:$\-*]*)\s*(?:\(\s*(?:(?:(?:((?:[a-zA-Z_$][\w.:$\-*]*)|(?:[-+]?[0-9]*\.?[0-9]+(?:[eE][-+]?[0-9]+)?)|(?:(?:'(?:[^'\\]|\\.)*')|(?:"(?:[^"\\]|\\.)*")))\s*)(?:,\s*(?:((?:[a-zA-Z_$][\w.:$\-*]*)|(?:[-+]?[0-9]*\.?[0-9]+(?:[eE][-+]?[0-9]+)?)|(?:(?:'(?:[^'\\]|\\.)*')|(?:"(?:[^"\\]|\\.)*")))\s*))*)?)\)\s*)?)(?:]]|}})/g,
Ya=D(b=>{const a=of(nd(b));class c extends a{constructor(){super();this.__isPropertyEffectsClient=!0}get PROPERTY_EFFECT_TYPES(){return t}_initializeProperties(){super._initializeProperties();this._registerHost();this.__dataClientsReady=!1;this.__dataLinkedPaths=this.__dataToNotify=this.__dataPendingClients=null;this.__dataHasPaths=!1;this.__dataCompoundStorage=this.__dataCompoundStorage||null;this.__dataHost=this.__dataHost||null;this.__dataTemp={};this.__dataClientsInitialized=!1}_registerHost(){if(Ca.length){let d=
Ca[Ca.length-1];d._enqueueClient(this);this.__dataHost=d}}_initializeProtoProperties(d){this.__data=Object.create(d);this.__dataPending=Object.create(d);this.__dataOld={}}_initializeInstanceProperties(d){let e=this[t.READ_ONLY];for(let f in d)e&&e[f]||(this.__dataPending=this.__dataPending||{},this.__dataOld=this.__dataOld||{},this.__data[f]=this.__dataPending[f]=d[f])}_addPropertyEffect(d,e,f){this._createPropertyAccessor(d,e==t.READ_ONLY);let g=mb(this,e,!0)[d];g||(g=this[e][d]=[]);g.push(f)}_removePropertyEffect(d,
e,f){d=mb(this,e,!0)[d];f=d.indexOf(f);0<=f&&d.splice(f,1)}_hasPropertyEffect(d,e){e=this[e];return!(!e||!e[d])}_hasReadOnlyEffect(d){return this._hasPropertyEffect(d,t.READ_ONLY)}_hasNotifyEffect(d){return this._hasPropertyEffect(d,t.NOTIFY)}_hasReflectEffect(d){return this._hasPropertyEffect(d,t.REFLECT)}_hasComputedEffect(d){return this._hasPropertyEffect(d,t.COMPUTE)}_setPendingPropertyOrPath(d,e,f,g){if(g||K(Array.isArray(d)?d[0]:d)!==d){if(!g&&(g=A(this,d),d=ec(this,d,e),!d||!super._shouldPropertyChange(d,
e,g)))return!1;this.__dataHasPaths=!0;if(this._setPendingProperty(d,e,f)){if(f=this.__dataLinkedPaths)for(let h in f)g=f[h],0===d.indexOf(h+".")?(g+=d.slice(h.length),this._setPendingPropertyOrPath(g,e,!0,!0)):0===d.indexOf(g+".")&&(g=h+d.slice(g.length),this._setPendingPropertyOrPath(g,e,!0,!0));return!0}}else{if(this.__dataHasAccessor&&this.__dataHasAccessor[d])return this._setPendingProperty(d,e,f);this[d]=e}return!1}_setUnmanagedPropertyToNode(d,e,f){if(f!==d[e]||"object"==typeof f)"className"===
e&&(d=q(d)),d[e]=f}_setPendingProperty(d,e,f){let g=this.__dataHasPaths&&0<=d.indexOf(".");if(this._shouldPropertyChange(d,e,(g?this.__dataTemp:this.__data)[d])){this.__dataPending||(this.__dataPending={},this.__dataOld={});d in this.__dataOld||(this.__dataOld[d]=this.__data[d]);g?this.__dataTemp[d]=e:this.__data[d]=e;this.__dataPending[d]=e;if(g||this[t.NOTIFY]&&this[t.NOTIFY][d])this.__dataToNotify=this.__dataToNotify||{},this.__dataToNotify[d]=f;return!0}return!1}_setProperty(d,e){this._setPendingProperty(d,
e,!0)&&this._invalidateProperties()}_invalidateProperties(){this.__dataReady&&this._flushProperties()}_enqueueClient(d){this.__dataPendingClients=this.__dataPendingClients||[];d!==this&&this.__dataPendingClients.push(d)}_flushClients(){this.__dataClientsReady?this.__enableOrFlushClients():(this.__dataClientsReady=!0,this._readyClients(),this.__dataReady=!0)}__enableOrFlushClients(){let d=this.__dataPendingClients;if(d){this.__dataPendingClients=null;for(let e=0;e<d.length;e++){let f=d[e];f.__dataEnabled?
f.__dataPending&&f._flushProperties():f._enableProperties()}}}_readyClients(){this.__enableOrFlushClients()}setProperties(d,e){for(let f in d)!e&&this[t.READ_ONLY]&&this[t.READ_ONLY][f]||this._setPendingPropertyOrPath(f,d[f],!0);this._invalidateProperties()}ready(){this._flushProperties();this.__dataClientsReady||this._flushClients();this.__dataPending&&this._flushProperties()}_propertiesChanged(d,e,f){d=this.__dataHasPaths;this.__dataHasPaths=!1;var g;if(g=this[t.COMPUTE])if(df){oa++;var h=qe(this),
k=[];for(var l in e)qd(l,g,k,h,d);for(;l=k.shift();)jc(this,"",e,f,l)&&qd(l.methodInfo,g,k,h,d);Object.assign(f,this.__dataOld);Object.assign(e,this.__dataPending);this.__dataPending=null}else for(h=e;na(this,g,h,f,d);)Object.assign(f,this.__dataOld),Object.assign(e,this.__dataPending),h=this.__dataPending,this.__dataPending=null;g=this.__dataToNotify;this.__dataToNotify=null;this._propagatePropertyChanges(e,f,d);this._flushClients();na(this,this[t.REFLECT],e,f,d);na(this,this[t.OBSERVE],e,f,d);if(g){{h=
this[t.NOTIFY];let z;k=oa++;for(let S in g)if(g[S]){if(l=h){{var m=h;l=k;var n=S,p=e,r=f,u=d;let sd=!1,qf=u?K(n):n;if(m=m[qf])for(let Lb=0,rf=m.length,T;Lb<rf&&(T=m[Lb]);Lb++)T.info&&T.info.lastRun===l||u&&!nb(n,T.trigger)||(T.info&&(T.info.lastRun=l),T.fn(this,n,p,r,T.info,u,void 0),sd=!0);l=sd}}if(l)z=!0;else{if(l=d)l=S,n=e,p=K(l),p!==l?(p=Ka(p)+"-changed",ic(this,p,n[l],l),l=!0):l=!1;l&&(z=!0)}}let F;z&&(F=this.__dataHost)&&F._invalidateProperties&&F._invalidateProperties()}}1==this.__dataCounter&&
(this.__dataTemp={})}_propagatePropertyChanges(d,e,f){this[t.PROPAGATE]&&na(this,this[t.PROPAGATE],d,e,f);this.__templateInfo&&this._runEffectsForTemplate(this.__templateInfo,d,e,f)}_runEffectsForTemplate(d,e,f,g){const h=(k,l)=>{na(this,d.propertyEffects,k,f,l,d.nodeList);for(let m=d.firstChild;m;m=m.nextSibling)this._runEffectsForTemplate(m,k,f,l)};d.runEffects?d.runEffects(h,e,g):h(e,g)}linkPaths(d,e){d=ma(d);e=ma(e);this.__dataLinkedPaths=this.__dataLinkedPaths||{};this.__dataLinkedPaths[d]=e}unlinkPaths(d){d=
ma(d);this.__dataLinkedPaths&&delete this.__dataLinkedPaths[d]}notifySplices(d,e){let f={path:""};d=A(this,d,f);oc(this,d,f.path,e)}get(d,e){return A(e||this,d)}set(d,e,f){f?ec(f,d,e):this[t.READ_ONLY]&&this[t.READ_ONLY][d]||this._setPendingPropertyOrPath(d,e,!0)&&this._invalidateProperties()}push(d,...e){let f={path:""};d=A(this,d,f);let g=d.length,h=d.push(...e);e.length&&qa(this,d,f.path,g,e.length,[]);return h}pop(d){let e={path:""};d=A(this,d,e);let f=!!d.length,g=d.pop();f&&qa(this,d,e.path,
d.length,0,[g]);return g}splice(d,e,f,...g){let h={path:""},k=A(this,d,h);0>e?e=k.length-Math.floor(-e):e&&(e=Math.floor(e));let l;l=2===arguments.length?k.splice(e):k.splice(e,f,...g);(g.length||l.length)&&qa(this,k,h.path,e,g.length,l);return l}shift(d){let e={path:""};d=A(this,d,e);let f=!!d.length,g=d.shift();f&&qa(this,d,e.path,0,0,[g]);return g}unshift(d,...e){let f={path:""};d=A(this,d,f);let g=d.unshift(...e);e.length&&qa(this,d,f.path,0,e.length,[]);return g}notifyPath(d,e){if(1==arguments.length){var f=
{path:""};e=A(this,d,f);f=f.path}else f=Array.isArray(d)?ma(d):d;this._setPendingPropertyOrPath(f,e,!0,!0)&&this._invalidateProperties()}_createReadOnlyProperty(d,e){this._addPropertyEffect(d,t.READ_ONLY);e&&(this["_set"+we(d)]=function(f){this._setProperty(d,f)})}_createPropertyObserver(d,e,f){let g={property:d,method:e,dynamicFn:!!f};this._addPropertyEffect(d,t.OBSERVE,{fn:hc,info:g,trigger:{name:d}});f&&this._addPropertyEffect(e,t.OBSERVE,{fn:hc,info:g,trigger:{name:e}})}_createMethodObserver(d,
e){let f=qb(d);if(!f)throw Error("Malformed observer expression '"+d+"'");lc(this,f,t.OBSERVE,ob,null,e)}_createNotifyingProperty(d){this._addPropertyEffect(d,t.NOTIFY,{fn:oe,info:{eventName:Ka(d)+"-changed",property:d}})}_createReflectedProperty(d){let e=this.constructor.attributeNameForProperty(d);"-"===e[0]?console.warn("Property "+d+" cannot be reflected to attribute "+e+' because "-" is not a valid starting attribute name. Use a lowercase first letter for the property instead.'):this._addPropertyEffect(d,
t.REFLECT,{fn:pe,info:{attrName:e}})}_createComputedProperty(d,e,f){let g=qb(e);if(!g)throw Error("Malformed computed expression '"+e+"'");e=lc(this,g,t.COMPUTE,jc,d,f);mb(this,"__computeInfo")[d]=e}_marshalArgs(d,e,f){const g=this.__data,h=[];for(let k=0,l=d.length;k<l;k++){let {name:m,structured:n,wildcard:p,value:r,literal:u}=d[k];if(!u)if(p){const z=0===e.indexOf(m+"."),F=nc(g,f,z?e:m);r={path:z?e:m,value:F,base:z?A(g,m):F}}else r=n?nc(g,f,m):g[m];if(rb&&!this._overrideLegacyUndefined&&void 0===
r&&1<d.length)return pa;h[k]=r}return h}static addPropertyEffect(d,e,f){this.prototype._addPropertyEffect(d,e,f)}static createPropertyObserver(d,e,f){this.prototype._createPropertyObserver(d,e,f)}static createMethodObserver(d,e){this.prototype._createMethodObserver(d,e)}static createNotifyingProperty(d){this.prototype._createNotifyingProperty(d)}static createReadOnlyProperty(d,e){this.prototype._createReadOnlyProperty(d,e)}static createReflectedProperty(d){this.prototype._createReflectedProperty(d)}static createComputedProperty(d,
e,f){this.prototype._createComputedProperty(d,e,f)}static bindTemplate(d){return this.prototype._bindTemplate(d)}_bindTemplate(d,e){let f=this.constructor._parseTemplate(d),g=this.__preBoundTemplateInfo==f;if(!g)for(let h in f.propertyEffects)this._createPropertyAccessor(h);e?(f=Object.create(f),f.wasPreBound=g,this.__templateInfo?(d=d._parentTemplateInfo||this.__templateInfo,e=d.lastChild,f.parent=d,d.lastChild=f,(f.previousSibling=e)?e.nextSibling=f:d.firstChild=f):this.__templateInfo=f):this.__preBoundTemplateInfo=
f;return f}static _addTemplatePropertyEffect(d,e,f){(d.hostProps=d.hostProps||{})[e]=!0;d=d.propertyEffects=d.propertyEffects||{};(d[e]=d[e]||[]).push(f)}_stampTemplate(d,e){e=e||this._bindTemplate(d,!0);Ca.push(this);d=super._stampTemplate(d,e);Ca.pop();e.nodeList=d.nodeList;if(!e.wasPreBound){var f=e.childNodes=[];for(var g=d.firstChild;g;g=g.nextSibling)f.push(g)}d.templateInfo=e;{let {nodeList:k,nodeInfoList:l}=e;if(l.length)for(let m=0;m<l.length;m++){let n=k[m],p=l[m].bindings;if(p)for(let r=
0;r<p.length;r++){let u=p[r];f=n;g=u;if(g.isCompound){let z=f.__dataCompoundStorage||(f.__dataCompoundStorage={});var h=g.parts;let F=Array(h.length);for(let S=0;S<h.length;S++)F[S]=h[S].literal;h=g.target;z[h]=F;g.literal&&"property"==g.kind&&("className"===h&&(f=q(f)),f[h]=g.literal)}te(n,this,u)}n.__dataHost=this}}this.__dataClientsReady&&(this._runEffectsForTemplate(e,this.__data,null,!1),this._flushClients());return d}_removeBoundDom(d){d=d.templateInfo;const {previousSibling:e,nextSibling:f,
parent:g}=d;e?e.nextSibling=f:g&&(g.firstChild=f);f?f.previousSibling=e:g&&(g.lastChild=e);d.nextSibling=d.previousSibling=null;d=d.childNodes;for(let h=0;h<d.length;h++){let k=d[h];q(q(k).parentNode).removeChild(k)}}static _parseTemplateNode(d,e,f){let g=a._parseTemplateNode.call(this,d,e,f);if(d.nodeType===Node.TEXT_NODE){let h=this._parseBindings(d.textContent,e);h&&(d.textContent=mc(h)||" ",pb(this,e,f,"text","textContent",h),g=!0)}return g}static _parseTemplateNodeAttribute(d,e,f,g,h){let k=
this._parseBindings(h,e);if(k){h=g;let l="property";pf.test(g)?l="attribute":"$"==g[g.length-1]&&(g=g.slice(0,-1),l="attribute");let m=mc(k);m&&"attribute"==l&&("class"==g&&d.hasAttribute("class")&&(m+=" "+d.getAttribute(g)),d.setAttribute(g,m));"attribute"==l&&"disable-upgrade$"==h&&d.setAttribute(g,"");"input"===d.localName&&"value"===h&&d.setAttribute(h,"");d.removeAttribute(h);"property"===l&&(g=fc(g));pb(this,e,f,l,g,k,m);return!0}return a._parseTemplateNodeAttribute.call(this,d,e,f,g,h)}static _parseTemplateNestedTemplate(d,
e,f){let g=a._parseTemplateNestedTemplate.call(this,d,e,f);const h=d.parentNode,k=f.templateInfo,l="dom-if"===h.localName,m="dom-repeat"===h.localName;ad&&(l||m)&&(h.removeChild(d),f=f.parentInfo,f.templateInfo=k,f.noted=!0,g=!1);d=k.hostProps;if(bd&&l)d&&(e.hostProps=Object.assign(e.hostProps||{},d),ad||(f.parentInfo.noted=!0));else for(let n in d)pb(this,e,f,"property","_host_"+n,[{mode:"{",source:n,dependencies:[n],hostProp:!0}]);return g}static _parseBindings(d,e){let f=[];for(var g=0,h;null!==
(h=rd.exec(d));){h.index>g&&f.push({literal:d.slice(g,h.index)});g=h[1][0];let m=!!h[2];h=h[3].trim();let n=!1,p="";var k=-1;"{"==g&&0<(k=h.indexOf("::"))&&(p=h.substring(k+2),h=h.substring(0,k),n=!0);k=qb(h);let r=[];if(k){let {args:u,methodName:z}=k;for(var l=0;l<u.length;l++){let F=u[l];F.literal||r.push(F)}if((l=e.dynamicFns)&&l[z]||k.static)r.push(z),k.dynamicFn=!0}else r.push(h);f.push({source:h,mode:g,negate:m,customEvent:n,signature:k,dependencies:r,event:p});g=rd.lastIndex}g&&g<d.length&&
(d=d.substring(g))&&f.push({literal:d});return f.length?f:null}static _evaluateBinding(d,e,f,g,h,k){d=e.signature?ob(d,f,g,h,e.signature):f!=e.source?A(d,e.source):k&&0<=f.indexOf(".")?A(d,f):d.__data[f];e.negate&&(d=!d);return d}}return c}),Ca=[],sf=D(b=>{function a(e){e=Object.getPrototypeOf(e);return e.prototype instanceof d?e:null}function c(e){if(!e.hasOwnProperty("__ownProperties")){var f=null;if(e.hasOwnProperty("properties")){var g=e.properties;if(g){f=g;g={};for(let h in f){const k=f[h];
g[h]="function"===typeof k?{type:k}:k}f=g}}e.__ownProperties=f}return e.__ownProperties}b=ld(b);class d extends b{static get observedAttributes(){if(!this.hasOwnProperty("__observedAttributes")){const e=this._properties;this.__observedAttributes=e?Object.keys(e).map(f=>this.prototype._addPropertyToAttributeMap(f)):[]}return this.__observedAttributes}static finalize(){if(!this.hasOwnProperty("__finalized")){const e=a(this);e&&e.finalize();this.__finalized=!0;this._finalizeClass()}}static _finalizeClass(){const e=
c(this);e&&this.createProperties(e)}static get _properties(){if(!this.hasOwnProperty("__properties")){const e=a(this);this.__properties=Object.assign({},e&&e._properties,c(this))}return this.__properties}static typeForProperty(e){return(e=this._properties[e])&&e.type}_initializeProperties(){this.constructor.finalize();super._initializeProperties()}connectedCallback(){super.connectedCallback&&super.connectedCallback();this._enableProperties()}disconnectedCallback(){super.disconnectedCallback&&super.disconnectedCallback()}}
return d}),Mb=window.ShadyCSS&&window.ShadyCSS.cssBuild,Za=D(b=>{function a(e,f,g,h){if(!Mb){const p=f.content.querySelectorAll("style"),r=lb(f);var k=ke(g),l=f.content.firstElementChild;for(var m=0;m<k.length;m++){var n=k[m];n.textContent=e._processStyleText(n.textContent,h);f.content.insertBefore(n,l)}k=0;for(l=0;l<r.length;l++)m=r[l],n=p[k],n!==m?(m=m.cloneNode(!0),n.parentNode.insertBefore(m,n)):k++,m.textContent=e._processStyleText(m.textContent,h)}window.ShadyCSS&&window.ShadyCSS.prepareTemplate(f,
g);if(ef&&Mb&&$e&&(f=f.content.querySelectorAll("style"))){let p="";Array.from(f).forEach(r=>{p+=r.textContent;r.parentNode.removeChild(r)});e._styleSheet=new CSSStyleSheet;e._styleSheet.replaceSync(p)}}const c=sf(Ya(b));class d extends c{static get polymerElementVersion(){return"3.4.1"}static _finalizeClass(){c._finalizeClass.call(this);var e;this.hasOwnProperty("__ownObservers")||(this.__ownObservers=this.hasOwnProperty("observers")?this.observers:null);(e=this.__ownObservers)&&this.createObservers(e,
this._properties);this._prepareTemplate()}static _prepareTemplate(){let e=this.template;e&&("string"===typeof e?(console.error("template getter must return HTMLTemplateElement"),e=null):ya||(e=e.cloneNode(!0)));this.prototype._template=e}static createProperties(e){for(let l in e){var f=this.prototype,g=l,h=e[l],k=e;h.computed&&(h.readOnly=!0);h.computed&&(f._hasReadOnlyEffect(g)?console.warn(`Cannot redefine computed property '${g}'.`):f._createComputedProperty(g,h.computed,k));h.readOnly&&!f._hasReadOnlyEffect(g)?
f._createReadOnlyProperty(g,!h.computed):!1===h.readOnly&&f._hasReadOnlyEffect(g)&&console.warn(`Cannot make readOnly property '${g}' non-readOnly.`);h.reflectToAttribute&&!f._hasReflectEffect(g)?f._createReflectedProperty(g):!1===h.reflectToAttribute&&f._hasReflectEffect(g)&&console.warn(`Cannot make reflected property '${g}' non-reflected.`);h.notify&&!f._hasNotifyEffect(g)?f._createNotifyingProperty(g):!1===h.notify&&f._hasNotifyEffect(g)&&console.warn(`Cannot make notify property '${g}' non-notify.`);
h.observer&&f._createPropertyObserver(g,h.observer,k[h.observer]);f._addPropertyToAttributeMap(g)}}static createObservers(e,f){const g=this.prototype;for(let h=0;h<e.length;h++)g._createMethodObserver(e[h],f)}static get template(){if(!this.hasOwnProperty("_template")){var e=this.prototype.hasOwnProperty("_template")?this.prototype._template:void 0;if(void 0===e){if(e=this.hasOwnProperty("is")){{e=this.is;let f=null;if(e&&(!ca||bf)&&(f=O.import(e,"template"),ca&&!f))throw Error(`strictTemplatePolicy: expecting dom-module or null template for ${e}`);
e=f}}e=e||Object.getPrototypeOf(this.prototype).constructor.template}this._template=e}return this._template}static set template(e){this._template=e}static get importPath(){if(!this.hasOwnProperty("_importPath")){var e=this.importMeta;this._importPath=e?jb(e.url):(e=O.import(this.is))&&e.assetpath||Object.getPrototypeOf(this.prototype).constructor.importPath}return this._importPath}constructor(){super()}_initializeProperties(){this.constructor.finalize();this.constructor._finalizeTemplate(this.localName);
super._initializeProperties();this.rootPath=af;this.importPath=this.constructor.importPath;var e=this.constructor;if(!e.hasOwnProperty("__propertyDefaults")){e.__propertyDefaults=null;var f=e._properties;for(let g in f){let h=f[g];"value"in h&&(e.__propertyDefaults=e.__propertyDefaults||{},e.__propertyDefaults[g]=h)}}if(e=e.__propertyDefaults)for(let g in e)f=e[g],this._canApplyPropertyDefault(g)&&(f="function"==typeof f.value?f.value.call(this):f.value,this._hasAccessor(g)?this._setPendingProperty(g,
f,!0):this[g]=f)}_canApplyPropertyDefault(e){return!this.hasOwnProperty(e)}static _processStyleText(e,f){return ib(e,f)}static _finalizeTemplate(e){const f=this.prototype._template;if(f&&!f.__polymerFinalized){f.__polymerFinalized=!0;var g=this.importPath;g=g?la(g):"";a(this,f,e,g);this.prototype._bindTemplate(f)}}connectedCallback(){window.ShadyCSS&&this._template&&window.ShadyCSS.styleElement(this);super.connectedCallback()}ready(){this._template&&(this.root=this._stampTemplate(this._template),
this.$=this.root.$);super.ready()}_readyClients(){this._template&&(this.root=this._attachDom(this.root));super._readyClients()}_attachDom(e){const f=q(this);if(f.attachShadow)return e?(f.shadowRoot||(f.attachShadow({mode:"open",shadyUpgradeFragment:e}),f.shadowRoot.appendChild(e),this.constructor._styleSheet&&(f.shadowRoot.adoptedStyleSheets=[this.constructor._styleSheet])),cf&&window.ShadyDOM&&window.ShadyDOM.flushInitial(f.shadowRoot),f.shadowRoot):null;throw Error("ShadowDOM not available. PolymerElement can create dom as children instead of in ShadowDOM by setting `this.root \x3d this;` before `ready`.");
}updateStyles(e){window.ShadyCSS&&window.ShadyCSS.styleSubtree(this,e)}resolveUrl(e,f){!f&&this.importPath&&(f=la(this.importPath));return la(e,f)}static _parseTemplateContent(e,f,g){f.dynamicFns=f.dynamicFns||this._properties;return c._parseTemplateContent.call(this,e,f,g)}static _addTemplatePropertyEffect(e,f,g){!Yc||f in this._properties||g.info.part.signature&&g.info.part.signature.static||g.info.part.hostProp||e.nestedTemplate||console.warn(`Property '${f}' used in template but not declared in 'properties'; `+
"attribute will not be observed.");return c._addTemplatePropertyEffect.call(this,e,f,g)}}return d}),I=Za(HTMLElement);class v{constructor(){this._timer=this._callback=this._asyncModule=null}setConfig(b,a){this._asyncModule=b;this._callback=a;this._timer=this._asyncModule.run(()=>{this._timer=null;U.delete(this);this._callback()})}cancel(){this.isActive()&&(this._cancelAsync(),U.delete(this))}_cancelAsync(){this.isActive()&&(this._asyncModule.cancel(this._timer),this._timer=null)}flush(){this.isActive()&&
(this.cancel(),this._callback())}isActive(){return null!=this._timer}static debounce(b,a,c){b instanceof v?b._cancelAsync():b=new v;b.setConfig(a,c);return b}}let U=new Set;const tf=function(){const b=!!U.size;U.forEach(a=>{try{a.flush()}catch(c){setTimeout(()=>{throw c;})}});return b};let sb="string"===typeof document.head.style.touchAction,Ma=["mousedown","mousemove","mouseup","click"],ze=[0,1,4,2];try{var td=1===(new MouseEvent("test",{buttons:1})).buttons}catch(b){td=!1}let ye=td,tb=!1;(function(){try{let b=
Object.defineProperty({},"passive",{get(){tb=!0}});window.addEventListener("test",null,b);window.removeEventListener("test",null,b)}catch(b){}})();let rc=navigator.userAgent.match(/iP(?:[oa]d|hone)|Android/);const ub=[],uf={button:!0,input:!0,keygen:!0,meter:!0,output:!0,textarea:!0,progress:!0,select:!0},Be={button:!0,command:!0,fieldset:!0,input:!0,keygen:!0,optgroup:!0,option:!0,select:!0,textarea:!0};let sc=function(b){var a=b.sourceCapabilities;if(!a||a.firesTouchEvents)if(b.__polymerGesturesHandled=
{skip:!0},"click"===b.type){a=!1;let f=Na(b);for(let g=0;g<f.length;g++){if(f[g].nodeType===Node.ELEMENT_NODE)if("label"===f[g].localName)ub.push(f[g]);else if(uf[f[g].localName]){var c=f[g];var d=Array.prototype.slice.call(c.labels||[]);if(!d.length){d=[];var e=c.getRootNode();if(c.id)for(c=e.querySelectorAll(`label[for = ${c.id}]`),e=0;e<c.length;e++)d.push(c[e])}for(c=0;c<d.length;c++)a=a||-1<ub.indexOf(d[c])}if(f[g]===y.mouse.target)return}a||(b.preventDefault(),b.stopPropagation())}},y={mouse:{target:null,
mouseIgnoreJob:null},touch:{x:0,y:0,id:-1,scrollDecided:!1}};document.addEventListener("touchend",function(b){y.mouse.mouseIgnoreJob||qc(!0);y.mouse.target=Na(b)[0];y.mouse.mouseIgnoreJob=v.debounce(y.mouse.mouseIgnoreJob,E.after(2500),function(){qc();y.mouse.target=null;y.mouse.mouseIgnoreJob=null})},tb?{passive:!0}:!1);const Na=window.ShadyDOM&&window.ShadyDOM.noPatch?window.ShadyDOM.composedPath:b=>b.composedPath&&b.composedPath()||[],ra={},Y=[];wb({name:"downup",deps:["mousedown","touchstart",
"touchend"],flow:{start:["mousedown","touchstart"],end:["mouseup","touchend"]},emits:["down","up"],info:{movefn:null,upfn:null},reset:function(){aa(this.info)},mousedown:function(b){if(X(b)){var a=P(b),c=this;tc(this.info,function(d){X(d)||(sa("up",a,d),aa(c.info))},function(d){X(d)&&sa("up",a,d);aa(c.info)});sa("down",a,b)}},touchstart:function(b){sa("down",P(b),b.changedTouches[0],b)},touchend:function(b){sa("up",P(b),b.changedTouches[0],b)}});wb({name:"track",touchAction:"none",deps:["mousedown",
"touchstart","touchmove","touchend"],flow:{start:["mousedown","touchstart"],end:["mouseup","touchend"]},emits:["track"],info:{x:0,y:0,state:"start",started:!1,moves:[],addMove:function(b){2<this.moves.length&&this.moves.shift();this.moves.push(b)},movefn:null,upfn:null,prevent:!1},reset:function(){this.info.state="start";this.info.started=!1;this.info.moves=[];this.info.x=0;this.info.y=0;this.info.prevent=!1;aa(this.info)},mousedown:function(b){if(X(b)){var a=P(b),c=this,d=function(e){let f=e.clientX,
g=e.clientY;wc(c.info,f,g)&&(c.info.state=c.info.started?"mouseup"===e.type?"end":"track":"start","start"===c.info.state&&Oa("tap"),c.info.addMove({x:f,y:g}),X(e)||(c.info.state="end",aa(c.info)),a&&yb(c.info,a,e),c.info.started=!0)};tc(this.info,d,function(e){c.info.started&&d(e);aa(c.info)});this.info.x=b.clientX;this.info.y=b.clientY}},touchstart:function(b){b=b.changedTouches[0];this.info.x=b.clientX;this.info.y=b.clientY},touchmove:function(b){let a=P(b);b=b.changedTouches[0];let c=b.clientX,
d=b.clientY;wc(this.info,c,d)&&("start"===this.info.state&&Oa("tap"),this.info.addMove({x:c,y:d}),yb(this.info,a,b),this.info.state="track",this.info.started=!0)},touchend:function(b){let a=P(b);b=b.changedTouches[0];this.info.started&&(this.info.state="end",this.info.addMove({x:b.clientX,y:b.clientY}),yb(this.info,a,b))}});wb({name:"tap",deps:["mousedown","click","touchstart","touchend"],flow:{start:["mousedown","touchstart"],end:["click","touchend"]},emits:["tap"],info:{x:NaN,y:NaN,prevent:!1},
reset:function(){this.info.x=NaN;this.info.y=NaN;this.info.prevent=!1},mousedown:function(b){X(b)&&(this.info.x=b.clientX,this.info.y=b.clientY)},click:function(b){X(b)&&xc(this.info,b)},touchstart:function(b){b=b.changedTouches[0];this.info.x=b.clientX;this.info.y=b.clientY},touchend:function(b){xc(this.info,b.changedTouches[0],b)}});const Da=D(b=>{class a extends b{_addEventListenerToNode(c,d,e){vb(c,d,e)||super._addEventListenerToNode(c,d,e)}_removeEventListenerFromNode(c,d,e){if(ra[d]){{var f=
ra[d];var g=f.deps;f=f.name;let h=c.__polymerGestures;if(h)for(let k=0,l,m;k<g.length;k++)l=g[k],(m=h[l])&&m[f]&&(m[f]=(m[f]||1)-1,m._count=(m._count||1)-1,0===m._count&&c.removeEventListener(l,uc,pc(l)));c.removeEventListener(d,e)}g=!0}else g=!1;g||super._removeEventListenerFromNode(c,d,e)}}return a}),vf=b=>class extends b{static get properties(){return{theme:{type:String,readOnly:!0}}}attributeChangedCallback(a,c,d){super.attributeChangedCallback(a,c,d);"theme"===a&&this._setTheme(d)}},Nb=b=>class extends vf(b){static finalize(){super.finalize();
const a=this.prototype._template,c=this.template&&this.template.parentElement&&this.template.parentElement.id===this.is,d=Object.getPrototypeOf(this.prototype)._template;d&&!c&&Array.from(d.content.querySelectorAll("style[include]")).forEach(e=>{this._includeStyle(e.getAttribute("include"),a)});this._includeMatchingThemes(a)}static _includeMatchingThemes(a){const c=O.prototype.modules;let d=!1;const e=this.is+"-default-theme";Object.keys(c).sort((f,g)=>{const h=0===f.indexOf("vaadin-"),k=0===g.indexOf("vaadin-");
var l=["lumo-","material-"];const m=0<l.filter(n=>0===f.indexOf(n)).length;l=0<l.filter(n=>0===g.indexOf(n)).length;return h!==k?h?-1:1:m!==l?m?-1:1:0}).forEach(f=>{if(f!==e){const g=c[f].getAttribute("theme-for");g&&g.split(" ").forEach(h=>{(new RegExp("^"+h.split("*").join(".*")+"$")).test(this.is)&&(d=!0,this._includeStyle(f,a))})}});!d&&c[e]&&this._includeStyle(e,a)}static _includeStyle(a,c){if(c&&!c.content.querySelector(`style[include="${a}"]`)){const d=document.createElement("style");d.setAttribute("include",
a);c.content.appendChild(d)}}},wf=b=>class extends b{static get properties(){var a={tabindex:{type:Number,value:0,reflectToAttribute:!0,observer:"_tabindexChanged"}};window.ShadyDOM&&(a.tabIndex=a.tabindex);return a}},xf=b=>class extends wf(b){static get properties(){return{autofocus:{type:Boolean},_previousTabIndex:{type:Number},disabled:{type:Boolean,observer:"_disabledChanged",reflectToAttribute:!0},_isShiftTabbing:{type:Boolean}}}ready(){this.addEventListener("focusin",c=>{c.composedPath()[0]===
this?this.contains(c.relatedTarget)||this._focus():-1===c.composedPath().indexOf(this.focusElement)||this.disabled||this._setFocused(!0)});this.addEventListener("focusout",c=>this._setFocused(!1));super.ready();const a=c=>{c.composed||c.target.dispatchEvent(new CustomEvent(c.type,{bubbles:!0,composed:!0,cancelable:!1}))};this.shadowRoot.addEventListener("focusin",a);this.shadowRoot.addEventListener("focusout",a);this.addEventListener("keydown",c=>{if(!c.defaultPrevented&&9===c.keyCode)if(c.shiftKey)this._isShiftTabbing=
!0,HTMLElement.prototype.focus.apply(this),this._setFocused(!1),setTimeout(()=>this._isShiftTabbing=!1,0);else if((c=window.navigator.userAgent.match(/Firefox\/(\d\d\.\d)/))&&63<=parseFloat(c[1])&&66>parseFloat(c[1])&&this.parentNode&&this.nextSibling){const d=document.createElement("input");d.style.position="absolute";d.style.opacity="0";d.tabIndex=this.tabIndex;this.parentNode.insertBefore(d,this.nextSibling);d.focus();d.addEventListener("focusout",()=>this.parentNode.removeChild(d))}});this.autofocus&&
!this.disabled&&window.requestAnimationFrame(()=>{this._focus();this._setFocused(!0);this.setAttribute("focus-ring","")});this._boundKeydownListener=this._bodyKeydownListener.bind(this);this._boundKeyupListener=this._bodyKeyupListener.bind(this)}connectedCallback(){super.connectedCallback();document.body.addEventListener("keydown",this._boundKeydownListener,!0);document.body.addEventListener("keyup",this._boundKeyupListener,!0)}disconnectedCallback(){super.disconnectedCallback();document.body.removeEventListener("keydown",
this._boundKeydownListener,!0);document.body.removeEventListener("keyup",this._boundKeyupListener,!0);this.hasAttribute("focused")&&this._setFocused(!1)}_setFocused(a){a?this.setAttribute("focused",""):this.removeAttribute("focused");a&&this._tabPressed?this.setAttribute("focus-ring",""):this.removeAttribute("focus-ring")}_bodyKeydownListener(a){this._tabPressed=9===a.keyCode}_bodyKeyupListener(){this._tabPressed=!1}get focusElement(){window.console.warn(`Please implement the 'focusElement' property in <${this.localName}>`);
return this}_focus(){this.focusElement&&!this._isShiftTabbing&&(this.focusElement.focus(),this._setFocused(!0))}focus(){this.focusElement&&!this.disabled&&(this.focusElement.focus(),this._setFocused(!0))}blur(){this.focusElement&&(this.focusElement.blur(),this._setFocused(!1))}_disabledChanged(a){(this.focusElement.disabled=a)?(this.blur(),this._previousTabIndex=this.tabindex,this.tabindex=-1,this.setAttribute("aria-disabled","true")):("undefined"!==typeof this._previousTabIndex&&(this.tabindex=this._previousTabIndex),
this.removeAttribute("aria-disabled"))}_tabindexChanged(a){void 0!==a&&(this.focusElement.tabIndex=a);this.disabled&&this.tabindex&&(-1!==this.tabindex&&(this._previousTabIndex=this.tabindex),this.tabindex=a=void 0);window.ShadyDOM&&this.setProperties({tabIndex:a,tabindex:a})}click(){this.disabled||super.click()}},ea=function(){let b,a;do b=window.ShadyDOM&&ShadyDOM.flush(),window.ShadyCSS&&window.ShadyCSS.ScopingShim&&window.ShadyCSS.ScopingShim.flush(),a=tf();while(b||a)};class Ob{static detectScrollType(){const b=
document.createElement("div");b.textContent="ABCD";b.dir="rtl";b.style.fontSize="14px";b.style.width="4px";b.style.height="1px";b.style.position="absolute";b.style.top="-1000px";b.style.overflow="scroll";document.body.appendChild(b);let a="reverse";0<b.scrollLeft?a="default":(b.scrollLeft=1,0===b.scrollLeft&&(a="negative"));document.body.removeChild(b);return a}static getNormalizedScrollLeft(b,a,c){const {scrollLeft:d}=c;if("rtl"!==a||!b)return d;switch(b){case "negative":return c.scrollWidth-c.clientWidth+
d;case "reverse":return c.scrollWidth-c.clientWidth-d}return d}static setNormalizedScrollLeft(b,a,c,d){if("rtl"===a&&b)switch(b){case "negative":c.scrollLeft=c.clientWidth-c.scrollWidth+d;break;case "reverse":c.scrollLeft=c.scrollWidth-c.clientWidth-d;break;default:c.scrollLeft=d}else c.scrollLeft=d}}const V=[];let $a;(new MutationObserver(function(){const b=fa();V.forEach(a=>{Pb(a,b)})})).observe(document.documentElement,{attributes:!0,attributeFilter:["dir"]});const Pb=function(b,a){a?b.setAttribute("dir",
a):b.removeAttribute("dir")},fa=function(){return document.documentElement.getAttribute("dir")},Qb=b=>class extends b{static get properties(){return{dir:{type:String,readOnly:!0}}}static finalize(){super.finalize();$a||($a=Ob.detectScrollType())}connectedCallback(){super.connectedCallback();this.hasAttribute("dir")||(this.__subscribe(),Pb(this,fa()))}attributeChangedCallback(a,c,d){super.attributeChangedCallback(a,c,d);if("dir"===a){a=d===fa()&&-1===V.indexOf(this);var e=!d&&c&&-1===V.indexOf(this);
c=d!==fa()&&c===fa();a||e?(this.__subscribe(),Pb(this,fa())):c&&this.__subscribe(!1)}}disconnectedCallback(){super.disconnectedCallback();this.__subscribe(!1);this.removeAttribute("dir")}__subscribe(a=!0){a?-1===V.indexOf(this)&&V.push(this):-1<V.indexOf(this)&&V.splice(V.indexOf(this),1)}__getNormalizedScrollLeft(a){return Ob.getNormalizedScrollLeft($a,this.getAttribute("dir")||"ltr",a)}__setNormalizedScrollLeft(a,c){return Ob.setNormalizedScrollLeft($a,this.getAttribute("dir")||"ltr",a,c)}},Ee=
/\/\*\*\s+vaadin-dev-mode:start([\s\S]*)vaadin-dev-mode:end\s+\*\*\//i,Pa=window.Vaadin&&window.Vaadin.Flow&&window.Vaadin.Flow.clients;window.Vaadin=window.Vaadin||{};const ud=function(b,a){if(window.Vaadin.developmentMode)return yc(b,a)};if(void 0===window.Vaadin.developmentMode){var yf=window.Vaadin;try{var vd=localStorage.getItem("vaadin.developmentmode.force")?!0:0<=["localhost","127.0.0.1"].indexOf(window.location.hostname)?Pa?!De():!Ce():!1}catch(b){vd=!1}yf.developmentMode=vd}const wd=function(){if("function"===
typeof ud)return ud(Fe)};window.Vaadin||(window.Vaadin={});window.Vaadin.registrations=window.Vaadin.registrations||[];window.Vaadin.developmentModeCallback=window.Vaadin.developmentModeCallback||{};window.Vaadin.developmentModeCallback["vaadin-usage-statistics"]=function(){wd&&wd()};let Rb;const xd=new Set,yd=b=>class extends Qb(b){static finalize(){super.finalize();const {is:a}=this;a&&!xd.has(a)&&(window.Vaadin.registrations.push(this),xd.add(a),window.Vaadin.developmentModeCallback&&(Rb=v.debounce(Rb,
kd,()=>{window.Vaadin.developmentModeCallback["vaadin-usage-statistics"]()}),U.add(Rb)))}constructor(){super();null===document.doctype&&console.warn('Vaadin components require the "standards mode" declaration. Please add \x3c!DOCTYPE html\x3e to the HTML document.')}};class zd extends yd(xf(Nb(Da(I)))){static get template(){return H`
    <style>
      :host {
        display: inline-block;
      }

      :host([hidden]) {
        display: none !important;
      }

      label {
        display: inline-flex;
        align-items: baseline;
        outline: none;
      }

      [part="checkbox"] {
        position: relative;
        display: inline-block;
        flex: none;
      }

      input[type="checkbox"] {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        width: 100%;
        height: 100%;
        opacity: 0;
        cursor: inherit;
        margin: 0;
      }

      :host([disabled]) {
        -webkit-tap-highlight-color: transparent;
      }
    </style>

    <label>
      <span part="checkbox">
        <input type="checkbox" checked="{{checked::change}}" disabled\$="[[disabled]]" indeterminate="{{indeterminate::change}}" role="presentation" tabindex="-1">
      </span>

      <span part="label">
        <slot></slot>
      </span>
    </label>
`}static get is(){return"vaadin-checkbox"}static get version(){return"2.3.0"}static get properties(){return{checked:{type:Boolean,value:!1,notify:!0,observer:"_checkedChanged",reflectToAttribute:!0},indeterminate:{type:Boolean,notify:!0,observer:"_indeterminateChanged",reflectToAttribute:!0,value:!1},value:{type:String,value:"on"},_nativeCheckbox:{type:Object}}}constructor(){super()}get name(){return this.checked?this._storedName:""}set name(b){this._storedName=b}ready(){super.ready();this.setAttribute("role",
"checkbox");this._nativeCheckbox=this.shadowRoot.querySelector('input[type\x3d"checkbox"]');this.addEventListener("click",this._handleClick.bind(this));this._addActiveListeners();const b=this.getAttribute("name");b&&(this.name=b);this.shadowRoot.querySelector('[part~\x3d"label"]').querySelector("slot").addEventListener("slotchange",this._updateLabelAttribute.bind(this));this._updateLabelAttribute()}_updateLabelAttribute(){const b=this.shadowRoot.querySelector('[part~\x3d"label"]'),a=b.firstElementChild.assignedNodes();
this._isAssignedNodesEmpty(a)?b.setAttribute("empty",""):b.removeAttribute("empty")}_isAssignedNodesEmpty(b){return 0===b.length||1==b.length&&b[0].nodeType==Node.TEXT_NODE&&""===b[0].textContent.trim()}_checkedChanged(b){this.indeterminate?this.setAttribute("aria-checked","mixed"):this.setAttribute("aria-checked",!!b)}_indeterminateChanged(b){b?this.setAttribute("aria-checked","mixed"):this.setAttribute("aria-checked",this.checked)}_addActiveListeners(){this._addEventListenerToNode(this,"down",b=>
{this.__interactionsAllowed(b)&&this.setAttribute("active","")});this._addEventListenerToNode(this,"up",()=>this.removeAttribute("active"));this.addEventListener("keydown",b=>{this.__interactionsAllowed(b)&&32===b.keyCode&&(b.preventDefault(),this.setAttribute("active",""))});this.addEventListener("keyup",b=>{this.__interactionsAllowed(b)&&32===b.keyCode&&(b.preventDefault(),this._toggleChecked(),this.removeAttribute("active"),this.indeterminate&&(this.indeterminate=!1))})}get focusElement(){return this.shadowRoot.querySelector("input")}__interactionsAllowed(b){return this.disabled||
"a"===b.target.localName?!1:!0}_handleClick(b){this.__interactionsAllowed(b)&&(this.indeterminate?(this.indeterminate=!1,b.preventDefault(),this._toggleChecked()):b.composedPath()[0]!==this._nativeCheckbox&&(b.preventDefault(),this._toggleChecked()))}_toggleChecked(){this.checked=!this.checked;this.dispatchEvent(new CustomEvent("change",{composed:!1,bubbles:!0}))}}customElements.define(zd.is,zd);const zf=H`<dom-module id="lumo-grid" theme-for="vaadin-grid">
  <template>
    <style>
      :host {
        font-family: var(--lumo-font-family);
        font-size: var(--lumo-font-size-m);
        line-height: var(--lumo-line-height-s);
        color: var(--lumo-body-text-color);
        background-color: var(--lumo-base-color);
        box-sizing: border-box;
        -webkit-text-size-adjust: 100%;
        -webkit-tap-highlight-color: transparent;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;

        /* For internal use only */
        --_lumo-grid-border-color: var(--lumo-contrast-20pct);
        --_lumo-grid-secondary-border-color: var(--lumo-contrast-10pct);
        --_lumo-grid-border-width: 1px;
        --_lumo-grid-selected-row-color: var(--lumo-primary-color-10pct);
      }

      /* No (outer) border */

      :host(:not([theme~="no-border"])) {
        border: var(--_lumo-grid-border-width) solid var(--_lumo-grid-border-color);
      }

      /* Cell styles */

      [part~="cell"] {
        min-height: var(--lumo-size-m);
        background-color: var(--lumo-base-color);
      }

      [part~="cell"] ::slotted(vaadin-grid-cell-content) {
        cursor: default;
        padding: var(--lumo-space-xs) var(--lumo-space-m);
      }

      /* Apply row borders by default and introduce the "no-row-borders" variant */
      :host(:not([theme~="no-row-borders"])) [part~="cell"]:not([part~="details-cell"]) {
        border-top: var(--_lumo-grid-border-width) solid var(--_lumo-grid-secondary-border-color);
      }

      /* Hide first body row top border */
      :host(:not([theme~="no-row-borders"])) [part="row"][first] [part~="cell"]:not([part~="details-cell"]) {
        border-top: 0;
        min-height: calc(var(--lumo-size-m) - var(--_lumo-grid-border-width));
      }

      /* Focus-ring */

      [part~="cell"]:focus {
        outline: none;
      }

      :host([navigating]) [part~="cell"]:focus::before {
        content: "";
        position: absolute;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        pointer-events: none;
        box-shadow: inset 0 0 0 2px var(--lumo-primary-color-50pct);
      }

      /* Drag and Drop styles */
      :host([dragover])::after {
        content: "";
        position: absolute;
        z-index: 100;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        pointer-events: none;
        box-shadow: inset 0 0 0 2px var(--lumo-primary-color-50pct);
      }

      [part~="row"][dragover] {
        z-index: 100 !important;
      }

      [part~="row"][dragover] [part~="cell"] {
        overflow: visible;
      }

      [part~="row"][dragover] [part~="cell"]::after {
        content: "";
        position: absolute;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        height: calc(var(--_lumo-grid-border-width) + 2px);
        pointer-events: none;
        background: var(--lumo-primary-color-50pct);
      }

      :host([theme~="no-row-borders"]) [dragover] [part~="cell"]::after {
        height: 2px;
      }

      [part~="row"][dragover="below"] [part~="cell"]::after {
        top: 100%;
        bottom: auto;
        margin-top: -1px;
      }

      [part~="row"][dragover="above"] [part~="cell"]::after {
        top: auto;
        bottom: 100%;
        margin-bottom: -1px;
      }

      [part~="row"][details-opened][dragover="below"] [part~="cell"]:not([part~="details-cell"])::after,
      [part~="row"][details-opened][dragover="above"] [part~="details-cell"]::after {
        display: none;
      }

      [part~="row"][dragover][dragover="on-top"] [part~="cell"]::after {
        height: 100%;
      }

      [part~="row"][dragstart] {
        /* Add bottom-space to the row so the drag number doesn't get clipped. Needed for IE/Edge */
        border-bottom: 100px solid transparent;
        z-index: 100 !important;
        opacity: 0.9;
      }

      [part~="row"][dragstart] [part~="cell"] {
        border: none !important;
        box-shadow: none !important;
      }

      [part~="row"][dragstart] [part~="cell"][last-column] {
        border-radius: 0 var(--lumo-border-radius-s) var(--lumo-border-radius-s) 0;
      }

      [part~="row"][dragstart] [part~="cell"][first-column] {
        border-radius: var(--lumo-border-radius-s) 0 0 var(--lumo-border-radius-s);
      }

      [ios] [part~="row"][dragstart] [part~="cell"] {
        background: var(--lumo-primary-color-50pct);
      }

      #scroller:not([ios]) [part~="row"][dragstart]:not([dragstart=""])::after {
        display: block;
        position: absolute;
        left: var(--_grid-drag-start-x);
        top: var(--_grid-drag-start-y);
        z-index: 100;
        content: attr(dragstart);
        align-items: center;
        justify-content: center;
        box-sizing: border-box;
        padding: calc(var(--lumo-space-xs) * 0.8);
        color: var(--lumo-error-contrast-color);
        background-color: var(--lumo-error-color);
        border-radius: var(--lumo-border-radius-m);
        font-family: var(--lumo-font-family);
        font-size: var(--lumo-font-size-xxs);
        line-height: 1;
        font-weight: 500;
        text-transform: initial;
        letter-spacing: initial;
        min-width: calc(var(--lumo-size-s) * 0.7);
        text-align: center;
      }

      /* Headers and footers */

      [part~="header-cell"] ::slotted(vaadin-grid-cell-content),
      [part~="footer-cell"] ::slotted(vaadin-grid-cell-content),
      [part~="reorder-ghost"] {
        font-size: var(--lumo-font-size-s);
        font-weight: 500;
      }

      [part~="footer-cell"] ::slotted(vaadin-grid-cell-content) {
        font-weight: 400;
      }

      [part="row"]:only-child [part~="header-cell"] {
        min-height: var(--lumo-size-xl);
      }

      /* Header borders */

      /* Hide first header row top border */
      :host(:not([theme~="no-row-borders"])) [part="row"]:first-child [part~="header-cell"] {
        border-top: 0;
      }

      [part="row"]:last-child [part~="header-cell"] {
        border-bottom: var(--_lumo-grid-border-width) solid transparent;
      }

      :host(:not([theme~="no-row-borders"])) [part="row"]:last-child [part~="header-cell"] {
        border-bottom-color: var(--_lumo-grid-secondary-border-color);
      }

      /* Overflow uses a stronger border color */
      :host([overflow~="top"]) [part="row"]:last-child [part~="header-cell"] {
        border-bottom-color: var(--_lumo-grid-border-color);
      }

      /* Footer borders */

      [part="row"]:first-child [part~="footer-cell"] {
        border-top: var(--_lumo-grid-border-width) solid transparent;
      }

      :host(:not([theme~="no-row-borders"])) [part="row"]:first-child [part~="footer-cell"] {
        border-top-color: var(--_lumo-grid-secondary-border-color);
      }

      /* Overflow uses a stronger border color */
      :host([overflow~="bottom"]) [part="row"]:first-child [part~="footer-cell"] {
        border-top-color: var(--_lumo-grid-border-color);
      }

      /* Column reordering */

      :host([reordering]) [part~="cell"] {
        background: linear-gradient(var(--lumo-shade-20pct), var(--lumo-shade-20pct)) var(--lumo-base-color);
      }

      :host([reordering]) [part~="cell"][reorder-status="allowed"] {
        background: var(--lumo-base-color);
      }

      :host([reordering]) [part~="cell"][reorder-status="dragging"] {
        background: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct)) var(--lumo-base-color);
      }

      [part~="reorder-ghost"] {
        opacity: 0.85;
        box-shadow: var(--lumo-box-shadow-s);
        /* TODO Use the same styles as for the cell element (reorder-ghost copies styles from the cell element) */
        padding: var(--lumo-space-s) var(--lumo-space-m) !important;
      }

      /* Column resizing */

      [part="resize-handle"] {
        width: 3px;
        background-color: var(--lumo-primary-color-50pct);
        opacity: 0;
        transition: opacity 0.2s;
      }

      :host(:not([reordering])) *:not([column-resizing]) [part~="cell"]:hover [part="resize-handle"],
      [part="resize-handle"]:active {
        opacity: 1;
        transition-delay: 0.15s;
      }

      /* Column borders */

      :host([theme~="column-borders"]) [part~="cell"]:not([last-column]):not([part~="details-cell"]) {
        border-right: var(--_lumo-grid-border-width) solid var(--_lumo-grid-secondary-border-color);
      }

      /* Frozen columns */

      [last-frozen] {
        border-right: var(--_lumo-grid-border-width) solid transparent;
        overflow: hidden;
      }

      :host([overflow~="left"]) [part~="cell"][last-frozen]:not([part~="details-cell"]) {
        border-right-color: var(--_lumo-grid-border-color);
      }

      /* Row stripes */

      :host([theme~="row-stripes"]) [part~="row"]:not([odd]) [part~="body-cell"],
      :host([theme~="row-stripes"]) [part~="row"]:not([odd]) [part~="details-cell"] {
        background-image: linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
        background-repeat: repeat-x;
      }

      /* Selected row */

      /* Raise the selected rows above unselected rows (so that box-shadow can cover unselected rows) */
      :host(:not([reordering])) [part~="row"][selected] {
        z-index: 1;
      }

      :host(:not([reordering])) [part~="row"][selected] [part~="body-cell"]:not([part~="details-cell"]) {
        background-image: linear-gradient(var(--_lumo-grid-selected-row-color), var(--_lumo-grid-selected-row-color));
        background-repeat: repeat;
      }

      /* Cover the border of an unselected row */
      :host(:not([theme~="no-row-borders"])) [part~="row"][selected] [part~="cell"]:not([part~="details-cell"]) {
        box-shadow: 0 var(--_lumo-grid-border-width) 0 0 var(--_lumo-grid-selected-row-color);
      }

      /* Compact */

      :host([theme~="compact"]) [part="row"]:only-child [part~="header-cell"] {
        min-height: var(--lumo-size-m);
      }

      :host([theme~="compact"]) [part~="cell"] {
        min-height: var(--lumo-size-s);
      }

      :host([theme~="compact"]) [part="row"][first] [part~="cell"]:not([part~="details-cell"]) {
        min-height: calc(var(--lumo-size-s) - var(--_lumo-grid-border-width));
      }

      :host([theme~="compact"]) [part~="cell"] ::slotted(vaadin-grid-cell-content) {
        padding: var(--lumo-space-xs) var(--lumo-space-s);
      }

      /* Wrap cell contents */

      :host([theme~="wrap-cell-content"]) [part~="cell"] ::slotted(vaadin-grid-cell-content) {
        white-space: normal;
      }

      /* RTL specific styles */

      :host([dir="rtl"]) [part~="row"][dragstart] [part~="cell"][last-column] {
        border-radius: var(--lumo-border-radius-s) 0 0 var(--lumo-border-radius-s);
      }

      :host([dir="rtl"]) [part~="row"][dragstart] [part~="cell"][first-column] {
        border-radius: 0 var(--lumo-border-radius-s) var(--lumo-border-radius-s) 0;
      }

      :host([dir="rtl"][theme~="column-borders"]) [part~="cell"]:not([last-column]):not([part~="details-cell"]) {
        border-right: none;
        border-left: var(--_lumo-grid-border-width) solid var(--_lumo-grid-secondary-border-color);
      }

      :host([dir="rtl"]) [last-frozen] {
        border-right: none;
        border-left: var(--_lumo-grid-border-width) solid transparent;
      }

      :host([dir="rtl"][overflow~="right"]) [part~="cell"][last-frozen]:not([part~="details-cell"]) {
        border-left-color: var(--_lumo-grid-border-color);
      }
    </style>
  </template>
</dom-module><dom-module theme-for="vaadin-checkbox" id="vaadin-grid-select-all-checkbox-lumo">
  <template>
    <style>
      :host(.vaadin-grid-select-all-checkbox) {
        font-size: var(--lumo-font-size-m);
      }
   </style>
  </template>
</dom-module>`;document.head.appendChild(zf.content);class Ac{constructor(){this.end=this.start=0;this.rules=this.parent=this.previous=null;this.cssText=this.parsedCssText="";this.atRule=!1;this.type=0;this.parsedSelector=this.selector=this.keyframesName=""}}const Q={STYLE_RULE:1,KEYFRAMES_RULE:7,MEDIA_RULE:4,MIXIN_RULE:1E3},L={comments:/\/\*[^*]*\*+([^/*][^*]*\*+)*\//gim,port:/@import[^;]*;/gim,customProp:/(?:^[^;\-\s}]+)?--[^;{}]*?:[^{};]*?(?:[;\n]|$)/gim,mixinProp:/(?:^[^;\-\s}]+)?--[^;{}]*?:[^{};]*?{[^}]*?}(?:[;\n]|$)?/gim,
mixinApply:/@apply\s*\(?[^);]*\)?\s*(?:[;\n]|$)?/gim,varApply:/[^;:]*?:[^;]*?var\([^;]*\)(?:[;\n]|$)?/gim,keyframesRule:/^@[^\s]*keyframes/,multipleSpaces:/\s+/g},Ad=new Set,Af=/;\s*/m,Bf=/^\s*(initial)|(inherit)\s*$/,Bd=/\s*!important/;class Cf{constructor(){this._map={}}set(b,a){b=b.trim();this._map[b]={properties:a,dependants:{}}}get(b){b=b.trim();return this._map[b]||null}}let ab=null;class B{constructor(){this._measureElement=this._currentElement=null;this._map=new Cf}detectMixin(b){b=Ta.test(b)||
Ib.test(b);Ta.lastIndex=0;Ib.lastIndex=0;return b}gatherStyles(b){var a=[];var c=b.content.querySelectorAll("style");for(let d=0;d<c.length;d++){const e=c[d];if(e.hasAttribute("shady-unscoped")){if(!Ha){{const f=e.textContent;if(!Ad.has(f)){Ad.add(f);const g=document.createElement("style");g.setAttribute("shady-unscoped","");g.textContent=f;document.head.appendChild(g)}}e.parentNode.removeChild(e)}}else a.push(e.textContent),e.parentNode.removeChild(e)}return(a=a.join("").trim())?(c=document.createElement("style"),
c.textContent=a,b.content.insertBefore(c,b.content.firstChild),c):null}transformTemplate(b,a){void 0===b._gatheredStyle&&(b._gatheredStyle=this.gatherStyles(b));return(b=b._gatheredStyle)?this.transformStyle(b,a):null}transformStyle(b,a=""){let c=Dc(b);this.transformRules(c,a);b.textContent=zb(c);return c}transformCustomStyle(b){let a=Dc(b);ta(a,c=>{":root"===c.selector&&(c.selector="html");this.transformRule(c)});b.textContent=zb(a);return a}transformRules(b,a){this._currentElement=a;ta(b,c=>{this.transformRule(c)});
this._currentElement=null}transformRule(b){b.cssText=this.transformCssText(b.parsedCssText,b);":root"===b.selector&&(b.selector=":host \x3e *")}transformCssText(b,a){b=b.replace(Ib,(c,d,e,f)=>this._produceCssProperties(c,d,e,f,a));return this._consumeCssProperties(b,a)}_getInitialValueForProperty(b){this._measureElement||(this._measureElement=document.createElement("meta"),this._measureElement.setAttribute("apply-shim-measure",""),this._measureElement.style.all="initial",document.head.appendChild(this._measureElement));
return window.getComputedStyle(this._measureElement).getPropertyValue(b)}_fallbacksFromPreviousRules(b){let a=b;for(;a.parent;)a=a.parent;const c={};let d=!1;ta(a,e=>{(d=d||e===b)||e.selector===b.selector&&Object.assign(c,this._cssTextToMap(e.parsedCssText))});return c}_consumeCssProperties(b,a){for(var c=null;c=Ta.exec(b);){var d=c[0],e=c[1];c=c.index;var f=c+d.indexOf("@apply");let g=c+d.length;d=b.slice(0,f);b=b.slice(g);f=a?this._fallbacksFromPreviousRules(a):{};Object.assign(f,this._cssTextToMap(d));
e=this._atApplyToCssProperties(e,f);b=`${d}${e}${b}`;Ta.lastIndex=c+e.length}return b}_atApplyToCssProperties(b,a){b=b.replace(Af,"");let c=[];var d=this._map.get(b);d||(this._map.set(b,{}),d=this._map.get(b));if(d){this._currentElement&&(d.dependants[this._currentElement]=!0);let e,f;const g=d.properties;for(e in g)f=a&&a[e],d=[e,": var(",b,"_-_",e],f&&d.push(",",f.replace(Bd,"")),d.push(")"),Bd.test(g[e])&&d.push(" !important"),c.push(d.join(""))}return c.join("; ")}_replaceInitialOrInherit(b,a){let c=
Bf.exec(a);c&&(a=c[1]?this._getInitialValueForProperty(b):"apply-shim-inherit");return a}_cssTextToMap(b,a=!1){b=b.split(";");let c,d,e={};for(let f=0,g,h;f<b.length;f++)if(g=b[f])h=g.split(":"),1<h.length&&(c=h[0].trim(),d=h.slice(1).join(":"),a&&(d=this._replaceInitialOrInherit(c,d)),e[c]=d);return e}_invalidateMixinEntry(b){if(ab)for(let a in b.dependants)a!==this._currentElement&&ab(a)}_produceCssProperties(b,a,c,d,e){c&&Ec(c,(r,u)=>{u&&this._map.get(u)&&(d=`@apply ${u};`)});if(!d)return b;var f=
this._consumeCssProperties(""+d,e);e=b.slice(0,b.indexOf("--"));let g=f=this._cssTextToMap(f,!0),h=this._map.get(a),k=h&&h.properties;k?g=Object.assign(Object.create(k),f):this._map.set(a,g);let l=[],m,n,p=!1;for(m in g)n=f[m],void 0===n&&(n="initial"),!k||m in k||(p=!0),l.push(`${a}${"_-_"}${m}: ${n}`);p&&this._invalidateMixinEntry(h);h&&(h.properties=g);c&&(e=`${b};${e}`);return`${e}${l.join("; ")};`}}B.prototype.detectMixin=B.prototype.detectMixin;B.prototype.transformStyle=B.prototype.transformStyle;
B.prototype.transformCustomStyle=B.prototype.transformCustomStyle;B.prototype.transformRules=B.prototype.transformRules;B.prototype.transformRule=B.prototype.transformRule;B.prototype.transformTemplate=B.prototype.transformTemplate;B.prototype._separator="_-_";Object.defineProperty(B.prototype,"invalidCallback",{get(){return ab},set(b){ab=b}});const Bb={},Ke=Promise.resolve(),Ea=new B;class Df{constructor(){this.customStyleInterface=null;Ea.invalidCallback=Ie}ensure(){!this.customStyleInterface&&
window.ShadyCSS.CustomStyleInterface&&(this.customStyleInterface=window.ShadyCSS.CustomStyleInterface,this.customStyleInterface.transformCallback=b=>{Ea.transformCustomStyle(b)},this.customStyleInterface.validateCallback=()=>{requestAnimationFrame(()=>{this.customStyleInterface.enqueued&&this.flushCustomStyles()})})}prepareTemplate(b,a){this.ensure();""===Fc(b)&&(Bb[a]=b,a=Ea.transformTemplate(b,a),b._styleAst=a)}flushCustomStyles(){this.ensure();if(this.customStyleInterface){var b=this.customStyleInterface.processStyles();
if(this.customStyleInterface.enqueued){for(let a=0;a<b.length;a++){let c=this.customStyleInterface.getStyleForCustomStyle(b[a]);c&&Ea.transformCustomStyle(c)}this.customStyleInterface.enqueued=!1}}}styleSubtree(b,a){this.ensure();a&&gb(b,a);if(b.shadowRoot)for(this.styleElement(b),b=b.shadowRoot.children||b.shadowRoot.childNodes,a=0;a<b.length;a++)this.styleSubtree(b[a]);else for(b=b.children||b.childNodes,a=0;a<b.length;a++)this.styleSubtree(b[a])}styleElement(b){this.ensure();{var a=b.localName;
var c="";let d="";a?-1<a.indexOf("-")?c=a:(d=a,c=b.getAttribute&&b.getAttribute("is")||""):(c=b.is,d=b.extends);a={is:c,typeExtension:d}}({is:c}=a);a=Bb[c];if((!a||""===Fc(a))&&a&&!Gc(a)){if(Gc(a)||a._applyShimValidatingVersion!==a._applyShimNextVersion)this.prepareTemplate(a,c),Je(a);if(b=b.shadowRoot)if(b=b.querySelector("style"))b.__cssRules=a._styleAst,b.textContent=zb(a._styleAst)}}styleDocument(b){this.ensure();this.styleSubtree(document.body,b)}}if(!window.ShadyCSS||!window.ShadyCSS.ScopingShim){const b=
new Df;let a=window.ShadyCSS&&window.ShadyCSS.CustomStyleInterface;window.ShadyCSS={prepareTemplate(c,d,e){b.flushCustomStyles();b.prepareTemplate(c,d)},prepareTemplateStyles(c,d,e){window.ShadyCSS.prepareTemplate(c,d,e)},prepareTemplateDom(c,d){},styleSubtree(c,d){b.flushCustomStyles();b.styleSubtree(c,d)},styleElement(c){b.flushCustomStyles();b.styleElement(c)},styleDocument(c){b.flushCustomStyles();b.styleDocument(c)},getComputedStyleValue(c,d){return ac(c,d)},flushCustomStyles(){b.flushCustomStyles()},
nativeCss:Ab,nativeShadow:Ha,cssBuild:ua,disableRuntime:$c};a&&(window.ShadyCSS.CustomStyleInterface=a)}window.ShadyCSS.ApplyShim=Ea;const Ef=/:host\(:dir\((ltr|rtl)\)\)/g,Ff=/([\s\w-#\.\[\]\*]*):dir\((ltr|rtl)\)/g,Gf=/:dir\((?:ltr|rtl)\)/,Cd=!(!window.ShadyDOM||!window.ShadyDOM.inUse),wa=[];let Fa=null,va="";const Hf=D(b=>{Cd||Fa||(va=document.documentElement.getAttribute("dir"),Fa=new MutationObserver(Hc),Fa.observe(document.documentElement,{attributes:!0,attributeFilter:["dir"]}));const a=nd(b);
class c extends a{static _processStyleText(d,e){d=a._processStyleText.call(this,d,e);!Cd&&Gf.test(d)&&(d=this._replaceDirInCssText(d),this.__activateDir=!0);return d}static _replaceDirInCssText(d){d=d.replace(Ef,':host([dir\x3d"$1"])');return d=d.replace(Ff,':host([dir\x3d"$2"]) $1')}constructor(){super();this.__autoDirOptOut=!1}ready(){super.ready();this.__autoDirOptOut=this.hasAttribute("dir")}connectedCallback(){a.prototype.connectedCallback&&super.connectedCallback();this.constructor.__activateDir&&
(Fa&&Fa.takeRecords().length&&Hc(),wa.push(this),this.__autoDirOptOut||this.setAttribute("dir",va))}disconnectedCallback(){a.prototype.disconnectedCallback&&super.disconnectedCallback();if(this.constructor.__activateDir){const d=wa.indexOf(this);-1<d&&wa.splice(d,1)}}}c.__activateDir=!1;return c});let Qa=!1,Jc=[],Kc=[];"interactive"===document.readyState||"complete"===document.readyState?Nc():window.addEventListener("DOMContentLoaded",Nc);let Z=class{static getFlattenedNodes(b){const a=q(b);return ba(b)?
a.assignedNodes({flatten:!0}):Array.from(a.childNodes).map(c=>ba(c)?q(c).assignedNodes({flatten:!0}):[c]).reduce((c,d)=>c.concat(d),[])}constructor(b,a){this._nativeChildrenObserver=this._shadyChildrenObserver=null;this._connected=!1;this._target=b;this.callback=a;this._effectiveNodes=[];this._observer=null;this._scheduled=!1;this._boundSchedule=()=>{this._schedule()};this.connect();this._schedule()}connect(){ba(this._target)?this._listenSlots([this._target]):q(this._target).children&&(this._listenSlots(q(this._target).children),
window.ShadyDOM?this._shadyChildrenObserver=window.ShadyDOM.observeChildren(this._target,b=>{this._processMutations(b)}):(this._nativeChildrenObserver=new MutationObserver(b=>{this._processMutations(b)}),this._nativeChildrenObserver.observe(this._target,{childList:!0})));this._connected=!0}disconnect(){ba(this._target)?this._unlistenSlots([this._target]):q(this._target).children&&(this._unlistenSlots(q(this._target).children),window.ShadyDOM&&this._shadyChildrenObserver?(window.ShadyDOM.unobserveChildren(this._shadyChildrenObserver),
this._shadyChildrenObserver=null):this._nativeChildrenObserver&&(this._nativeChildrenObserver.disconnect(),this._nativeChildrenObserver=null));this._connected=!1}_schedule(){this._scheduled||(this._scheduled=!0,G.run(()=>this.flush()))}_processMutations(b){this._processSlotMutations(b);this.flush()}_processSlotMutations(b){if(b)for(let a=0;a<b.length;a++){let c=b[a];c.addedNodes&&this._listenSlots(c.addedNodes);c.removedNodes&&this._unlistenSlots(c.removedNodes)}}flush(){if(!this._connected)return!1;
window.ShadyDOM&&ShadyDOM.flush();this._nativeChildrenObserver?this._processSlotMutations(this._nativeChildrenObserver.takeRecords()):this._shadyChildrenObserver&&this._processSlotMutations(this._shadyChildrenObserver.takeRecords());this._scheduled=!1;let b={target:this._target,addedNodes:[],removedNodes:[]};var a=this.constructor.getFlattenedNodes(this._target);var c=this._effectiveNodes;c=Oc(a,0,a.length,c,0,c.length);for(let d=0,e;d<c.length&&(e=c[d]);d++)for(let f=0,g;f<e.removed.length&&(g=e.removed[f]);f++)b.removedNodes.push(g);
for(let d=0,e;d<c.length&&(e=c[d]);d++)for(let f=e.index;f<e.index+e.addedCount;f++)b.addedNodes.push(a[f]);this._effectiveNodes=a;a=!1;if(b.addedNodes.length||b.removedNodes.length)a=!0,this.callback.call(this._target,b);return a}_listenSlots(b){for(let a=0;a<b.length;a++){let c=b[a];ba(c)&&c.addEventListener("slotchange",this._boundSchedule)}}_unlistenSlots(b){for(let a=0;a<b.length;a++){let c=b[a];ba(c)&&c.removeEventListener("slotchange",this._boundSchedule)}}};const ha=Element.prototype,Dd=ha.matches||
ha.matchesSelector||ha.mozMatchesSelector||ha.msMatchesSelector||ha.oMatchesSelector||ha.webkitMatchesSelector;class ia{constructor(b){window.ShadyDOM&&window.ShadyDOM.inUse&&window.ShadyDOM.patch(b);this.node=b}observeNodes(b){return new Z(this.node,b)}unobserveNodes(b){b.disconnect()}notifyObserver(){}deepContains(b){if(q(this.node).contains(b))return!0;let a=b;for(b=b.ownerDocument;a&&a!==b&&a!==this.node;)a=q(a).parentNode||q(a).host;return a===this.node}getOwnerRoot(){return q(this.node).getRootNode()}getDistributedNodes(){return"slot"===
this.node.localName?q(this.node).assignedNodes({flatten:!0}):[]}getDestinationInsertionPoints(){let b=[],a=q(this.node).assignedSlot;for(;a;)b.push(a),a=q(a).assignedSlot;return b}importNode(b,a){return q(this.node instanceof Document?this.node:this.node.ownerDocument).importNode(b,a)}getEffectiveChildNodes(){return Z.getFlattenedNodes(this.node)}queryDistributedElements(b){let a=this.getEffectiveChildNodes(),c=[];for(let d=0,e=a.length,f;d<e&&(f=a[d]);d++)f.nodeType===Node.ELEMENT_NODE&&Dd.call(f,
b)&&c.push(f);return c}get activeElement(){let b=this.node;return void 0!==b._activeElement?b._activeElement:b.activeElement}}class Sb{constructor(b){this.event=b}get rootTarget(){return this.path[0]}get localTarget(){return this.event.target}get path(){return this.event.composedPath()}}let Tb=ia;if(window.ShadyDOM&&window.ShadyDOM.inUse&&window.ShadyDOM.noPatch&&window.ShadyDOM.Wrapper){class b extends window.ShadyDOM.Wrapper{}Object.getOwnPropertyNames(ia.prototype).forEach(a=>{"activeElement"!=
a&&(b.prototype[a]=ia.prototype[a])});Pc(b.prototype,["classList"]);Tb=b;Object.defineProperties(Sb.prototype,{localTarget:{get(){var a=this.event.currentTarget;a=a&&N(a).getOwnerRoot();const c=this.path;for(let d=0;d<c.length;d++){const e=c[d];if(N(e).getOwnerRoot()===a)return e}},configurable:!0},path:{get(){return window.ShadyDOM.composedPath(this.event)},configurable:!0}})}else Oe(ia.prototype,"cloneNode appendChild insertBefore removeChild replaceChild setAttribute removeAttribute querySelector querySelectorAll".split(" ")),
Pc(ia.prototype,"parentNode firstChild lastChild nextSibling previousSibling firstElementChild lastElementChild nextElementSibling previousElementSibling childNodes children classList".split(" ")),Pe(ia.prototype,["textContent","innerHTML","className"]);const N=function(b){b=b||document;if(b instanceof Tb||b instanceof Sb)return b;let a=b.__domApi;a||(a=b instanceof Event?new Sb(b):new Tb(b),b.__domApi=a);return a},Cb=window.ShadyDOM,Qc=window.ShadyCSS,Ed=b=>{for(;b;){const a=Object.getOwnPropertyDescriptor(b,
"observedAttributes");if(a)return a.get;b=Object.getPrototypeOf(b.prototype).constructor}return()=>[]};D(b=>{b=Za(b);let a=Ed(b);class c extends b{constructor(){super()}static get observedAttributes(){return a.call(this).concat("disable-upgrade")}_initializeProperties(){this.hasAttribute("disable-upgrade")?this.__isUpgradeDisabled=!0:super._initializeProperties()}_enableProperties(){this.__isUpgradeDisabled||super._enableProperties()}_canApplyPropertyDefault(d){return super._canApplyPropertyDefault(d)&&
!(this.__isUpgradeDisabled&&this._isPropertyPending(d))}attributeChangedCallback(d,e,f,g){"disable-upgrade"==d?this.__isUpgradeDisabled&&null==f&&(super._initializeProperties(),this.__isUpgradeDisabled=!1,q(this).isConnected&&super.connectedCallback()):super.attributeChangedCallback(d,e,f,g)}connectedCallback(){this.__isUpgradeDisabled||super.connectedCallback()}disconnectedCallback(){this.__isUpgradeDisabled||super.disconnectedCallback()}}return c});let If=window.ShadyCSS;const Fd=D(b=>{b=Da(Za(b));
b=Mb?b:Hf(b);const a=Ed(b),c={x:"pan-x",y:"pan-y",none:"none",all:"auto"};class d extends b{constructor(){super()}static get importMeta(){return this.prototype.importMeta}created(){}__attributeReaction(e,f,g){(this.__dataAttributes&&this.__dataAttributes[e]||"disable-upgrade"===e)&&this.attributeChangedCallback(e,f,g,null)}setAttribute(e,f){if(Va&&!this._legacyForceObservedAttributes){const g=this.getAttribute(e);super.setAttribute(e,f);this.__attributeReaction(e,g,String(f))}else super.setAttribute(e,
f)}removeAttribute(e){if(Va&&!this._legacyForceObservedAttributes){const f=this.getAttribute(e);super.removeAttribute(e);this.__attributeReaction(e,f,null)}else super.removeAttribute(e)}static get observedAttributes(){return Va&&!this.prototype._legacyForceObservedAttributes?(this.hasOwnProperty("__observedAttributes")||(this.__observedAttributes=[]),this.__observedAttributes):a.call(this).concat("disable-upgrade")}_enableProperties(){this.__isUpgradeDisabled||super._enableProperties()}_canApplyPropertyDefault(e){return super._canApplyPropertyDefault(e)&&
!(this.__isUpgradeDisabled&&this._isPropertyPending(e))}connectedCallback(){this.__needsAttributesAtConnected&&this._takeAttributes();this.__isUpgradeDisabled||(super.connectedCallback(),this.isAttached=!0,this.attached())}attached(){}disconnectedCallback(){this.__isUpgradeDisabled||(super.disconnectedCallback(),this.isAttached=!1,this.detached())}detached(){}attributeChangedCallback(e,f,g,h){f!==g&&("disable-upgrade"==e?this.__isUpgradeDisabled&&null==g&&(this._initializeProperties(),this.__isUpgradeDisabled=
!1,q(this).isConnected&&this.connectedCallback()):(super.attributeChangedCallback(e,f,g,h),this.attributeChanged(e,f,g)))}attributeChanged(e,f,g){}_initializeProperties(){if(ya&&this.hasAttribute("disable-upgrade"))this.__isUpgradeDisabled=!0;else{let e=Object.getPrototypeOf(this);e.hasOwnProperty("__hasRegisterFinished")||(this._registered(),e.__hasRegisterFinished=!0);super._initializeProperties();this.root=this;this.created();Va&&!this._legacyForceObservedAttributes&&(this.hasAttributes()?this._takeAttributes():
this.parentNode||(this.__needsAttributesAtConnected=!0));this._applyListeners()}}_takeAttributes(){const e=this.attributes;for(let f=0,g=e.length;f<g;f++){const h=e[f];this.__attributeReaction(h.name,null,h.value)}}_registered(){}ready(){this._ensureAttributes();super.ready()}_ensureAttributes(){}_applyListeners(){}serialize(e){return this._serializeValue(e)}deserialize(e,f){return this._deserializeValue(e,f)}reflectPropertyToAttribute(e,f,g){this._propertyToAttribute(e,f,g)}serializeValueToAttribute(e,
f,g){this._valueToNodeAttribute(g||this,e,f)}extend(e,f){if(!e||!f)return e||f;let g=Object.getOwnPropertyNames(f);for(let h=0,k;h<g.length&&(k=g[h]);h++){let l=Object.getOwnPropertyDescriptor(f,k);l&&Object.defineProperty(e,k,l)}return e}mixin(e,f){for(let g in f)e[g]=f[g];return e}chainObject(e,f){e&&f&&e!==f&&(e.__proto__=f);return e}instanceTemplate(e){e=this.constructor._contentForTemplate(e);return document.importNode(e,!0)}fire(e,f,g){g=g||{};f=null===f||void 0===f?{}:f;e=new Event(e,{bubbles:void 0===
g.bubbles?!0:g.bubbles,cancelable:!!g.cancelable,composed:void 0===g.composed?!0:g.composed});e.detail=f;q(g.node||this).dispatchEvent(e);return e}listen(e,f,g){e=e||this;var h=this.__boundListeners||(this.__boundListeners=new WeakMap);let k=h.get(e);k||(k={},h.set(e,k));h=f+g;k[h]||(k[h]=this._addMethodEventListenerToNode(e,f,g,this))}unlisten(e,f,g){e=e||this;let h=this.__boundListeners&&this.__boundListeners.get(e);g=f+g;let k=h&&h[g];k&&(this._removeEventListenerFromNode(e,f,k),h[g]=null)}setScrollDirection(e,
f){vc(f||this,c[e]||"auto")}$$(e){return this.root.querySelector(e)}get domHost(){let e=q(this).getRootNode();return e instanceof DocumentFragment?e.host:e}distributeContent(){const e=N(this);window.ShadyDOM&&e.shadowRoot&&ShadyDOM.flush()}getEffectiveChildNodes(){return N(this).getEffectiveChildNodes()}queryDistributedElements(e){return N(this).queryDistributedElements(e)}getEffectiveChildren(){return this.getEffectiveChildNodes().filter(function(e){return e.nodeType===Node.ELEMENT_NODE})}getEffectiveTextContent(){let e=
this.getEffectiveChildNodes(),f=[];for(let g=0,h;h=e[g];g++)h.nodeType!==Node.COMMENT_NODE&&f.push(h.textContent);return f.join("")}queryEffectiveChildren(e){return(e=this.queryDistributedElements(e))&&e[0]}queryAllEffectiveChildren(e){return this.queryDistributedElements(e)}getContentChildNodes(e){return(e=this.root.querySelector(e||"slot"))?N(e).getDistributedNodes():[]}getContentChildren(e){return this.getContentChildNodes(e).filter(function(f){return f.nodeType===Node.ELEMENT_NODE})}isLightDescendant(e){return this!==
e&&q(this).contains(e)&&q(this).getRootNode()===q(e).getRootNode()}isLocalDescendant(e){return this.root===q(e).getRootNode()}scopeSubtree(e,f=!1){return Qe(e,f)}getComputedStyleValue(e){return If.getComputedStyleValue(this,e)}debounce(e,f,g){this._debouncers=this._debouncers||{};return this._debouncers[e]=v.debounce(this._debouncers[e],0<g?E.after(g):G,f.bind(this))}isDebouncerActive(e){this._debouncers=this._debouncers||{};e=this._debouncers[e];return!(!e||!e.isActive())}flushDebouncer(e){this._debouncers=
this._debouncers||{};(e=this._debouncers[e])&&e.flush()}cancelDebouncer(e){this._debouncers=this._debouncers||{};(e=this._debouncers[e])&&e.cancel()}async(e,f){return 0<f?E.run(e.bind(this),f):~G.run(e.bind(this))}cancelAsync(e){0>e?G.cancel(~e):E.cancel(e)}create(e,f){e=document.createElement(e);if(f)if(e.setProperties)e.setProperties(f);else for(let g in f)e[g]=f[g];return e}elementMatches(e,f){return Dd.call(f||this,e)}toggleAttribute(e,f){let g=this;3===arguments.length&&(g=arguments[2]);1==arguments.length&&
(f=!g.hasAttribute(e));if(f)return q(g).setAttribute(e,""),!0;q(g).removeAttribute(e);return!1}toggleClass(e,f,g){g=g||this;1==arguments.length&&(f=!g.classList.contains(e));f?g.classList.add(e):g.classList.remove(e)}transform(e,f){f=f||this;f.style.webkitTransform=e;f.style.transform=e}translate3d(e,f,g,h){this.transform("translate3d("+e+","+f+","+g+")",h||this)}arrayDelete(e,f){if(Array.isArray(e)){if(f=e.indexOf(f),0<=f)return e.splice(f,1)}else if(f=A(this,e).indexOf(f),0<=f)return this.splice(e,
f,1);return null}_logger(e,f){Array.isArray(f)&&1===f.length&&Array.isArray(f[0])&&(f=f[0]);switch(e){case "log":case "warn":case "error":console[e](...f)}}_log(...e){this._logger("log",e)}_warn(...e){this._logger("warn",e)}_error(...e){this._logger("error",e)}_logf(e,...f){return["[%s::%s]",this.is,e,...f]}}d.prototype.is="";return d}),Re={attached:!0,detached:!0,ready:!0,created:!0,beforeRegister:!0,registered:!0,attributeChanged:!0,listeners:!0,hostAttributes:!0},Uc={attached:!0,detached:!0,ready:!0,
created:!0,beforeRegister:!0,registered:!0,attributeChanged:!0,behaviors:!0,_noAccessors:!0},Te=Object.assign({listeners:!0,hostAttributes:!0,properties:!0,observers:!0},Uc),Gd=Fd(HTMLElement),Ub=D(b=>{class a extends b{_shouldPropertyChange(c,d,e){return Db(this,c,d,e,!0)}}return a}),Hd=D(b=>{class a extends b{static get properties(){return{mutableData:Boolean}}_shouldPropertyChange(c,d,e){return Db(this,c,d,e,this.mutableData)}}return a});Ub._mutablePropertyChange=Db;let Fb=null;Eb.prototype=Object.create(HTMLTemplateElement.prototype,
{constructor:{value:Eb,writable:!0}});const Xc=Ya(Eb),Ye=Ub(Xc),Jf=Ya(class{});class Gb extends Jf{constructor(b){super();this._configureProperties(b);this.root=this._stampTemplate(this.__dataHost);var a=[];this.children=a;for(let c=this.root.firstChild;c;c=c.nextSibling)a.push(c),c.__templatizeInstance=this;this.__templatizeOwner&&this.__templatizeOwner.__hideTemplateChildren__&&this._showHideChildren(!0);a=this.__templatizeOptions;(b&&a.instanceProps||!a.instanceProps)&&this._enableProperties()}_configureProperties(b){if(this.__templatizeOptions.forwardHostProp)for(let a in this.__hostProps)this._setPendingProperty(a,
this.__dataHost["_host_"+a]);for(let a in b)this._setPendingProperty(a,b[a])}forwardHostProp(b,a){this._setPendingPropertyOrPath(b,a,!1,!0)&&this.__dataHost._enqueueClient(this)}_addEventListenerToNode(b,a,c){if(this._methodHost&&this.__templatizeOptions.parentModel)this._methodHost._addEventListenerToNode(b,a,d=>{d.model=this;c(d)});else{let d=this.__dataHost.__dataHost;d&&d._addEventListenerToNode(b,a,c)}}_showHideChildren(b){Vc(b,this.children)}_setUnmanagedPropertyToNode(b,a,c){b.__hideTemplateChildren__&&
b.nodeType==Node.TEXT_NODE&&"textContent"==a?b.__polymerTextContent__=c:super._setUnmanagedPropertyToNode(b,a,c)}get parentModel(){let b=this.__parentModel;if(!b){let a;b=this;do b=b.__dataHost.__dataHost;while((a=b.__templatizeOptions)&&!a.parentModel);this.__parentModel=b}return b}dispatchEvent(b){return!0}}const Xe=Ub(Gb);let Zc=!1;const Kf=Da(Hd(Ya(HTMLElement)));class Lf extends Kf{static get observedAttributes(){return["mutable-data"]}constructor(){super();if(ca)throw Error("strictTemplatePolicy: dom-bind not allowed");
this.__children=this.$=this.root=null}attributeChangedCallback(b,a,c,d){this.mutableData=!0}connectedCallback(){Hb()||(this.style.display="none");this.render()}disconnectedCallback(){this.__removeChildren()}__insertChildren(){q(q(this).parentNode).insertBefore(this.root,this)}__removeChildren(){if(this.__children)for(let b=0;b<this.__children.length;b++)this.root.appendChild(this.__children[b])}render(){let b;if(!this.__children){b=b||this.querySelector("template");if(!b){let a=new MutationObserver(()=>
{if(b=this.querySelector("template"))a.disconnect(),this.render();else throw Error("dom-bind requires a \x3ctemplate\x3e child");});a.observe(this,{childList:!0});return}this.root=this._stampTemplate(b);this.$=this.root.$;this.__children=[];for(let a=this.root.firstChild;a;a=a.nextSibling)this.__children[this.__children.length]=a;this._enableProperties()}this.__insertChildren();this.dispatchEvent(new CustomEvent("dom-change",{bubbles:!0,composed:!0}))}}customElements.define("dom-bind",Lf);const Mf=
Hd(I);class Id extends Mf{static get is(){return"dom-repeat"}static get template(){return null}static get properties(){return{items:{type:Array},as:{type:String,value:"item"},indexAs:{type:String,value:"index"},itemsIndexAs:{type:String,value:"itemsIndex"},sort:{type:Function,observer:"__sortChanged"},filter:{type:Function,observer:"__filterChanged"},observe:{type:String,observer:"__observeChanged"},delay:Number,renderedItemCount:{type:Number,notify:!Jb,readOnly:!0},initialCount:{type:Number},targetFramerate:{type:Number,
value:20},_targetFrameTime:{type:Number,computed:"__computeFrameTime(targetFramerate)"},notifyDomChange:{type:Boolean},reuseChunkedInstances:{type:Boolean}}}static get observers(){return["__itemsChanged(items.*)"]}constructor(){super();this.__instances=[];this.__renderDebouncer=null;this.__itemsIdxToInstIdx={};this.__renderStartTime=this.__chunkCount=null;this.__shouldContinueChunking=this.__shouldMeasureChunk=this.__itemsArrayChanged=!1;this.__chunkingId=0;this.__ctor=this.__observePaths=this.__filterFn=
this.__sortFn=null;this.__isDetached=!0;this.template=null}disconnectedCallback(){super.disconnectedCallback();this.__isDetached=!0;for(let b=0;b<this.__instances.length;b++)this.__detachInstance(b)}connectedCallback(){super.connectedCallback();Hb()||(this.style.display="none");if(this.__isDetached){this.__isDetached=!1;let b=q(q(this).parentNode);for(let a=0;a<this.__instances.length;a++)this.__attachInstance(a,b)}}__ensureTemplatized(){if(!this.__ctor){let b=this.template=this._templateInfo?this:
this.querySelector("template");if(!b){let c=new MutationObserver(()=>{if(this.querySelector("template"))c.disconnect(),this.__render();else throw Error("dom-repeat requires a \x3ctemplate\x3e child");});c.observe(this,{childList:!0});return!1}let a={};a[this.as]=!0;a[this.indexAs]=!0;a[this.itemsIndexAs]=!0;this.__ctor=za(b,this,{mutableData:this.mutableData,parentModel:!0,instanceProps:a,forwardHostProp:function(c,d){let e=this.__instances;for(let f=0,g;f<e.length&&(g=e[f]);f++)g.forwardHostProp(c,
d)},notifyInstanceProp:function(c,d,e){var f=this.as;if(f===d||0===f.indexOf(d+".")||0===d.indexOf(f+"."))c=c[this.itemsIndexAs],d==this.as&&(this.items[c]=e),d=`${"items"}.${c}`+d.slice(this.as.length),this.notifyPath(d,e)}})}return!0}__getMethodHost(){return this.__dataHost._methodHost||this.__dataHost}__functionFromPropertyValue(b){if("string"===typeof b){let a=this.__getMethodHost();return function(){return a[b].apply(a,arguments)}}return b}__sortChanged(b){this.__sortFn=this.__functionFromPropertyValue(b);
this.items&&this.__debounceRender(this.__render)}__filterChanged(b){this.__filterFn=this.__functionFromPropertyValue(b);this.items&&this.__debounceRender(this.__render)}__computeFrameTime(b){return Math.ceil(1E3/b)}__observeChanged(){this.__observePaths=this.observe&&this.observe.replace(".*",".").split(" ")}__handleObservedPaths(b){if(this.__sortFn||this.__filterFn)if(!b)this.__debounceRender(this.__render,this.delay);else if(this.__observePaths){let a=this.__observePaths;for(let c=0;c<a.length;c++)0===
b.indexOf(a[c])&&this.__debounceRender(this.__render,this.delay)}}__itemsChanged(b){this.items&&!Array.isArray(this.items)&&console.warn("dom-repeat expected array for `items`, found",this.items);this.__handleItemPath(b.path,b.value)||("items"===b.path&&(this.__itemsArrayChanged=!0),this.__debounceRender(this.__render))}__debounceRender(b,a=0){this.__renderDebouncer=v.debounce(this.__renderDebouncer,0<a?E.after(a):G,b.bind(this));U.add(this.__renderDebouncer)}render(){this.__debounceRender(this.__render);
ea()}__render(){if(this.__ensureTemplatized()){var b=this.items||[],a=this.__sortAndFilterItems(b),c=this.__calculateLimit(a.length);this.__updateInstances(b,c,a);this.initialCount&&(this.__shouldMeasureChunk||this.__shouldContinueChunking)&&(cancelAnimationFrame(this.__chunkingId),this.__chunkingId=requestAnimationFrame(()=>this.__continueChunking()));this._setRenderedItemCount(this.__instances.length);Jb&&!this.notifyDomChange||this.dispatchEvent(new CustomEvent("dom-change",{bubbles:!0,composed:!0}))}}__sortAndFilterItems(b){let a=
Array(b.length);for(let c=0;c<b.length;c++)a[c]=c;this.__filterFn&&(a=a.filter((c,d,e)=>this.__filterFn(b[c],d,e)));this.__sortFn&&a.sort((c,d)=>this.__sortFn(b[c],b[d]));return a}__calculateLimit(b){let a=b;const c=this.__instances.length;if(this.initialCount){let d;!this.__chunkCount||this.__itemsArrayChanged&&!this.reuseChunkedInstances?(a=Math.min(b,this.initialCount),this.__chunkCount=(d=Math.max(a-c,0))||1):(d=Math.min(Math.max(b-c,0),this.__chunkCount),a=Math.min(c+d,b));this.__shouldMeasureChunk=
d===this.__chunkCount;this.__shouldContinueChunking=a<b;this.__renderStartTime=performance.now()}this.__itemsArrayChanged=!1;return a}__continueChunking(){if(this.__shouldMeasureChunk){const b=performance.now()-this.__renderStartTime;this.__chunkCount=Math.round(this._targetFrameTime/b*this.__chunkCount)||1}this.__shouldContinueChunking&&this.__debounceRender(this.__render)}__updateInstances(b,a,c){const d=this.__itemsIdxToInstIdx={};let e;for(e=0;e<a;e++){let f=this.__instances[e],g=c[e],h=b[g];
d[g]=e;f?(f._setPendingProperty(this.as,h),f._setPendingProperty(this.indexAs,e),f._setPendingProperty(this.itemsIndexAs,g),f._flushProperties()):this.__insertInstance(h,e,g)}for(b=this.__instances.length-1;b>=e;b--)this.__detachAndRemoveInstance(b)}__detachInstance(b){b=this.__instances[b];const a=q(b.root);for(let c=0;c<b.children.length;c++)a.appendChild(b.children[c]);return b}__attachInstance(b,a){a.insertBefore(this.__instances[b].root,this)}__detachAndRemoveInstance(b){this.__detachInstance(b);
this.__instances.splice(b,1)}__stampInstance(b,a,c){let d={};d[this.as]=b;d[this.indexAs]=a;d[this.itemsIndexAs]=c;return new this.__ctor(d)}__insertInstance(b,a,c){b=this.__stampInstance(b,a,c);c=(c=this.__instances[a+1])?c.children[0]:this;q(q(this).parentNode).insertBefore(b.root,c);return this.__instances[a]=b}_showHideChildren(b){for(let a=0;a<this.__instances.length;a++)this.__instances[a]._showHideChildren(b)}__handleItemPath(b,a){var c=b.slice(6);let d=c.indexOf(".");b=0>d?c:c.substring(0,
d);if(b==parseInt(b,10)){c=0>d?"":c.substring(d+1);this.__handleObservedPaths(c);if(b=this.__instances[this.__itemsIdxToInstIdx[b]])b._setPendingPropertyOrPath(this.as+(c?"."+c:""),a,!1,!0),b._flushProperties();return!0}}itemForElement(b){return(b=this.modelForElement(b))&&b[this.as]}indexForElement(b){return(b=this.modelForElement(b))&&b[this.indexAs]}modelForElement(b){a:{var a=this.template;let c;for(;b;)if(c=b.__dataHost?b:b.__templatizeInstance)if(c.__dataHost!=a)b=c.__dataHost;else{a=c;break a}else b=
q(b).parentNode;a=null}return a}}customElements.define(Id.is,Id);class Jd extends I{static get is(){return"dom-if"}static get template(){return null}static get properties(){return{if:{type:Boolean,observer:"__debounceRender"},restamp:{type:Boolean,observer:"__debounceRender"},notifyDomChange:{type:Boolean}}}constructor(){super();this.__renderDebouncer=null;this.__hideTemplateChildren__=this._lastIf=!1}__debounceRender(){this.__renderDebouncer=v.debounce(this.__renderDebouncer,G,()=>this.__render());
U.add(this.__renderDebouncer)}disconnectedCallback(){super.disconnectedCallback();const b=q(this).parentNode;b&&(b.nodeType!=Node.DOCUMENT_FRAGMENT_NODE||q(b).host)||this.__teardownInstance()}connectedCallback(){super.connectedCallback();Hb()||(this.style.display="none");this.if&&this.__debounceRender()}__ensureTemplate(){if(!this.__template){let b=this._templateInfo?this:q(this).querySelector("template");if(!b){let a=new MutationObserver(()=>{if(q(this).querySelector("template"))a.disconnect(),this.__render();
else throw Error("dom-if requires a \x3ctemplate\x3e child");});a.observe(this,{childList:!0});return!1}this.__template=b}return!0}__ensureInstance(){let b=q(this).parentNode;if(this.__hasInstance()){let a=this.__getInstanceNodes();if(a&&a.length&&q(this).previousSibling!==a[a.length-1])for(let c=0,d;c<a.length&&(d=a[c]);c++)q(b).insertBefore(d,this)}else{if(!b||!this.__ensureTemplate())return!1;this.__createAndInsertInstance(b)}return!0}render(){ea()}__render(){if(this.if){if(!this.__ensureInstance())return}else this.restamp&&
this.__teardownInstance();this._showHideChildren();Jb&&!this.notifyDomChange||this.if==this._lastIf||(this.dispatchEvent(new CustomEvent("dom-change",{bubbles:!0,composed:!0})),this._lastIf=this.if)}__hasInstance(){}__getInstanceNodes(){}__createAndInsertInstance(b){}__teardownInstance(){}_showHideChildren(){}}class Nf extends Jd{constructor(){super();this.__syncInfo=this.__instance=null}__hasInstance(){return!!this.__instance}__getInstanceNodes(){return this.__instance.templateInfo.childNodes}__createAndInsertInstance(b){const a=
this.__dataHost||this;if(ca&&!this.__dataHost)throw Error("strictTemplatePolicy: template owner not trusted");const c=a._bindTemplate(this.__template,!0);c.runEffects=(d,e,f)=>{let g=this.__syncInfo;if(this.if)g&&(this.__syncInfo=null,this._showHideChildren(),e=Object.assign(g.changedProps,e)),d(e,f);else if(this.__instance)if(g||(g=this.__syncInfo={runEffects:d,changedProps:{}}),f)for(const h in e)d=K(h),g.changedProps[d]=this.__dataHost[d];else Object.assign(g.changedProps,e)};this.__instance=a._stampTemplate(this.__template,
c);q(b).insertBefore(this.__instance,this)}__syncHostProperties(){const b=this.__syncInfo;b&&(this.__syncInfo=null,b.runEffects(b.changedProps,!1))}__teardownInstance(){const b=this.__dataHost||this;this.__instance&&(b._removeBoundDom(this.__instance),this.__syncInfo=this.__instance=null)}_showHideChildren(){const b=this.__hideTemplateChildren__||!this.if;this.__instance&&!!this.__instance.__hidden!==b&&(this.__instance.__hidden=b,Vc(b,this.__instance.templateInfo.childNodes));b||this.__syncHostProperties()}}
class Of extends Jd{constructor(){super();this.__invalidProps=this.__instance=this.__ctor=null}__hasInstance(){return!!this.__instance}__getInstanceNodes(){return this.__instance.children}__createAndInsertInstance(b){this.__ctor||(this.__ctor=za(this.__template,this,{mutableData:!0,forwardHostProp:function(a,c){this.__instance&&(this.if?this.__instance.forwardHostProp(a,c):(this.__invalidProps=this.__invalidProps||Object.create(null),this.__invalidProps[K(a)]=!0))}}));this.__instance=new this.__ctor;
q(b).insertBefore(this.__instance.root,this)}__teardownInstance(){if(this.__instance){let b=this.__instance.children;if(b&&b.length){let a=q(b[0]).parentNode;if(a){a=q(a);for(let c=0,d;c<b.length&&(d=b[c]);c++)a.removeChild(d)}}this.__instance=this.__invalidProps=null}}__syncHostProperties(){let b=this.__invalidProps;if(b){this.__invalidProps=null;for(let a in b)this.__instance._setPendingProperty(a,this.__dataHost[a]);this.__instance._flushProperties()}}_showHideChildren(){const b=this.__hideTemplateChildren__||
!this.if;this.__instance&&!!this.__instance.__hidden!==b&&(this.__instance.__hidden=b,this.__instance._showHideChildren(b));b||this.__syncHostProperties()}}const Kd=bd?Nf:Of;customElements.define(Kd.is,Kd);let Pf=D(b=>{b=Za(b);class a extends b{static get properties(){return{items:{type:Array},multi:{type:Boolean,value:!1},selected:{type:Object,notify:!0},selectedItem:{type:Object,notify:!0},toggle:{type:Boolean,value:!1}}}static get observers(){return["__updateSelection(multi, items.*)"]}constructor(){super();
this.__selectedMap=this.__lastMulti=this.__lastItems=null}__updateSelection(c,d){var e=d.path;"items"==e?(d=d.base||[],e=this.__lastItems,c!==this.__lastMulti&&this.clearSelection(),e&&(e=Oc(d,0,d.length,e,0,e.length),this.__applySplices(e)),this.__lastItems=d,this.__lastMulti=c):"items.splices"==d.path?this.__applySplices(d.value.indexSplices):(c=e.slice(6),d=parseInt(c,10),0>c.indexOf(".")&&c==d&&this.__deselectChangedIdx(d))}__applySplices(c){let d=this.__selectedMap;for(let f=0;f<c.length;f++){let g=
c[f];d.forEach((h,k)=>{h<g.index||(h>=g.index+g.removed.length?d.set(k,h+g.addedCount-g.removed.length):d.set(k,-1))});for(let h=0;h<g.addedCount;h++){let k=g.index+h;d.has(this.items[k])&&d.set(this.items[k],k)}}this.__updateLinks();let e=0;d.forEach((f,g)=>{0>f?(this.multi?this.splice("selected",e,1):this.selected=this.selectedItem=null,d.delete(g)):e++})}__updateLinks(){this.__dataLinkedPaths={};if(this.multi){let c=0;this.__selectedMap.forEach(d=>{0<=d&&this.linkPaths(`${"items"}.${d}`,`${"selected"}.${c++}`)})}else this.__selectedMap.forEach(c=>
{this.linkPaths("selected",`${"items"}.${c}`);this.linkPaths("selectedItem",`${"items"}.${c}`)})}clearSelection(){this.__dataLinkedPaths={};this.__selectedMap=new Map;this.selected=this.multi?[]:null;this.selectedItem=null}isSelected(c){return this.__selectedMap.has(c)}isIndexSelected(c){return this.isSelected(this.items[c])}__deselectChangedIdx(c){let d=this.__selectedIndexForItemIndex(c);if(0<=d){let e=0;this.__selectedMap.forEach((f,g)=>{d==e++&&this.deselect(g)})}}__selectedIndexForItemIndex(c){if(c=
this.__dataLinkedPaths[`${"items"}.${c}`])return parseInt(c.slice(9),10)}deselect(c){let d=this.__selectedMap.get(c);if(0<=d){this.__selectedMap.delete(c);let e;this.multi&&(e=this.__selectedIndexForItemIndex(d));this.__updateLinks();this.multi?this.splice("selected",e,1):this.selected=this.selectedItem=null}}deselectIndex(c){this.deselect(this.items[c])}select(c){this.selectIndex(this.items.indexOf(c))}selectIndex(c){let d=this.items[c];this.isSelected(d)?this.toggle&&this.deselectIndex(c):(this.multi||
this.__selectedMap.clear(),this.__selectedMap.set(d,c),this.__updateLinks(),this.multi?this.push("selected",d):this.selected=this.selectedItem=d)}}return a})(I);class Ld extends Pf{static get is(){return"array-selector"}static get template(){return null}}customElements.define(Ld.is,Ld);const ja=Fd(HTMLElement).prototype;var bb=new Set;const Qf={properties:{_parentResizable:{type:Object,observer:"_parentResizableChanged"},_notifyingDescendant:{type:Boolean,value:!1}},listeners:{"iron-request-resize-notifications":"_onIronRequestResizeNotifications"},
created:function(){this._interestedResizables=[];this._boundNotifyResize=this.notifyResize.bind(this);this._boundOnDescendantIronResize=this._onDescendantIronResize.bind(this)},attached:function(){this._requestResizeNotifications()},detached:function(){this._parentResizable?this._parentResizable.stopResizeNotificationsFor(this):(bb.delete(this),window.removeEventListener("resize",this._boundNotifyResize));this._parentResizable=null},notifyResize:function(){this.isAttached&&(this._interestedResizables.forEach(function(b){this.resizerShouldNotify(b)&&
this._notifyDescendant(b)},this),this._fireResize())},assignParentResizable:function(b){this._parentResizable&&this._parentResizable.stopResizeNotificationsFor(this);(this._parentResizable=b)&&-1===b._interestedResizables.indexOf(this)&&(b._interestedResizables.push(this),b._subscribeIronResize(this))},stopResizeNotificationsFor:function(b){var a=this._interestedResizables.indexOf(b);-1<a&&(this._interestedResizables.splice(a,1),this._unsubscribeIronResize(b))},_subscribeIronResize:function(b){b.addEventListener("iron-resize",
this._boundOnDescendantIronResize)},_unsubscribeIronResize:function(b){b.removeEventListener("iron-resize",this._boundOnDescendantIronResize)},resizerShouldNotify:function(b){return!0},_onDescendantIronResize:function(b){this._notifyingDescendant?b.stopPropagation():Ra||this._fireResize()},_fireResize:function(){this.fire("iron-resize",null,{node:this,bubbles:!1})},_onIronRequestResizeNotifications:function(b){var a=N(b).rootTarget;a!==this&&(a.assignParentResizable(this),this._notifyDescendant(a),
b.stopPropagation())},_parentResizableChanged:function(b){b&&window.removeEventListener("resize",this._boundNotifyResize)},_notifyDescendant:function(b){this.isAttached&&(this._notifyingDescendant=!0,b.notifyResize(),this._notifyingDescendant=!1)},_requestResizeNotifications:function(){if(this.isAttached)if("loading"===document.readyState){var b=this._requestResizeNotifications.bind(this);document.addEventListener("readystatechange",function c(){document.removeEventListener("readystatechange",c);
b()})}else this._findParent(),this._parentResizable?this._parentResizable._interestedResizables.forEach(function(a){a!==this&&a._findParent()},this):(bb.forEach(function(a){a!==this&&a._findParent()},this),window.addEventListener("resize",this._boundNotifyResize),this.notifyResize())},_findParent:function(){this.assignParentResizable(null);this.fire("iron-request-resize-notifications",null,{node:this,bubbles:!0,cancelable:!0});this._parentResizable?bb.delete(this):bb.add(this)}},Rf={properties:{scrollTarget:{type:HTMLElement,
value:function(){return this._defaultScrollTarget}}},observers:["_scrollTargetChanged(scrollTarget, isAttached)"],_shouldHaveListener:!0,_scrollTargetChanged:function(b,a){this._oldScrollTarget&&(this._toggleScrollListener(!1,this._oldScrollTarget),this._oldScrollTarget=null);a&&("document"===b?this.scrollTarget=this._doc:"string"===typeof b?this.scrollTarget=(a=this.domHost)&&a.$?a.$[b]:N(this.ownerDocument).querySelector("#"+b):this._isValidScrollTarget()&&(this._oldScrollTarget=b,this._toggleScrollListener(this._shouldHaveListener,
b)))},_scrollHandler:function(){},get _defaultScrollTarget(){return this._doc},get _doc(){return this.ownerDocument.documentElement},get _scrollTop(){return this._isValidScrollTarget()?this.scrollTarget===this._doc?window.pageYOffset:this.scrollTarget.scrollTop:0},get _scrollLeft(){return this._isValidScrollTarget()?this.scrollTarget===this._doc?window.pageXOffset:this.scrollTarget.scrollLeft:0},set _scrollTop(b){this.scrollTarget===this._doc?window.scrollTo(window.pageXOffset,b):this._isValidScrollTarget()&&
(this.scrollTarget.scrollTop=b)},set _scrollLeft(b){this.scrollTarget===this._doc?window.scrollTo(b,window.pageYOffset):this._isValidScrollTarget()&&(this.scrollTarget.scrollLeft=b)},scroll:function(b,a){if("object"===typeof b){var c=b.left;a=b.top}else c=b;c=c||0;a=a||0;this.scrollTarget===this._doc?window.scrollTo(c,a):this._isValidScrollTarget()&&(this.scrollTarget.scrollLeft=c,this.scrollTarget.scrollTop=a)},get _scrollTargetWidth(){return this._isValidScrollTarget()?this.scrollTarget===this._doc?
window.innerWidth:this.scrollTarget.offsetWidth:0},get _scrollTargetHeight(){return this._isValidScrollTarget()?this.scrollTarget===this._doc?window.innerHeight:this.scrollTarget.offsetHeight:0},_isValidScrollTarget:function(){return this.scrollTarget instanceof HTMLElement},_toggleScrollListener:function(b,a){a=a===this._doc?window:a;b?this._boundScrollHandler||(this._boundScrollHandler=this._scrollHandler.bind(this),a.addEventListener("scroll",this._boundScrollHandler)):this._boundScrollHandler&&
(a.removeEventListener("scroll",this._boundScrollHandler),this._boundScrollHandler=null)},toggleScrollListener:function(b){this._shouldHaveListener=b;this._toggleScrollListener(b,this.scrollTarget)}};var Md=navigator.userAgent.match(/iP(?:hone|ad;(?: U;)? CPU) OS (\d+)/),Sf=Md&&8<=Md[1],Nd=G;const Tf=function(b,a){b||console.warn("Polymer.Class requires `info` argument");a=a?a(Gd):Gd;a=Se(b,a,b.behaviors);a.is=a.prototype.is=b.is;return a}({behaviors:[Qf,Rf],_ratio:.5,_scrollerPaddingTop:0,_scrollPosition:0,
_physicalSize:0,_physicalAverage:0,_physicalAverageCount:0,_physicalTop:0,_virtualCount:0,_estScrollHeight:0,_scrollHeight:0,_viewportHeight:0,_viewportWidth:0,_physicalItems:null,_physicalSizes:null,_firstVisibleIndexVal:null,_collection:null,_lastVisibleIndexVal:null,_maxPages:2,_focusedVirtualIndex:-1,_itemsPerRow:1,_rowHeight:0,_templateCost:0,get _physicalBottom(){return this._physicalTop+this._physicalSize},get _scrollBottom(){return this._scrollPosition+this._viewportHeight},get _virtualEnd(){return this._virtualStart+
this._physicalCount-1},get _hiddenContentSize(){return(this.grid?this._physicalRows*this._rowHeight:this._physicalSize)-this._viewportHeight},get _maxScrollTop(){return this._estScrollHeight-this._viewportHeight+this._scrollOffset},get _maxVirtualStart(){var b=this._convertIndexToCompleteRow(this._virtualCount);return Math.max(0,b-this._physicalCount)},set _virtualStart(b){b=this._clamp(b,0,this._maxVirtualStart);this.grid&&(b-=b%this._itemsPerRow);this._virtualStartVal=b},get _virtualStart(){return this._virtualStartVal||
0},set _physicalStart(b){b%=this._physicalCount;0>b&&(b=this._physicalCount+b);this.grid&&(b-=b%this._itemsPerRow);this._physicalStartVal=b},get _physicalStart(){return this._physicalStartVal||0},get _physicalEnd(){return(this._physicalStart+this._physicalCount-1)%this._physicalCount},set _physicalCount(b){this._physicalCountVal=b},get _physicalCount(){return this._physicalCountVal||0},get _optPhysicalSize(){return 0===this._viewportHeight?Infinity:this._viewportHeight*this._maxPages},get _isVisible(){return!(!this.offsetWidth&&
!this.offsetHeight)},get firstVisibleIndex(){var b=this._firstVisibleIndexVal;if(null==b){var a=this._physicalTop+this._scrollOffset;this._firstVisibleIndexVal=b=this._iterateItems(function(c,d){a+=this._getPhysicalSizeIncrement(c);if(a>this._scrollPosition)return this.grid?d-d%this._itemsPerRow:d;if(this.grid&&this._virtualCount-1===d)return d-d%this._itemsPerRow})||0}return b},get lastVisibleIndex(){var b=this._lastVisibleIndexVal;if(null==b){if(this.grid)b=Math.min(this._virtualCount,this.firstVisibleIndex+
this._estRowsInView*this._itemsPerRow-1);else{var a=this._physicalTop+this._scrollOffset;this._iterateItems(function(c,d){a<this._scrollBottom&&(b=d);a+=this._getPhysicalSizeIncrement(c)})}this._lastVisibleIndexVal=b}return b},get _scrollOffset(){return this._scrollerPaddingTop},attached:function(){this._debounce("_render",this._render,M);this.listen(this,"iron-resize","_resizeHandler")},detached:function(){this.unlisten(this,"iron-resize","_resizeHandler")},updateViewportBoundaries:function(){var b=
window.getComputedStyle(this);this._scrollerPaddingTop=this.scrollTarget===this?0:parseInt(b["padding-top"],10);this._isRTL="rtl"===b.direction;this._viewportWidth=this.$.items.offsetWidth;this._viewportHeight=this._scrollTargetHeight;this.grid&&this._updateGridMetrics()},_scrollHandler:function(){var b=Math.max(0,Math.min(this._maxScrollTop,this._scrollTop)),a=b-this._scrollPosition,c=0<=a;this._scrollPosition=b;this._lastVisibleIndexVal=this._firstVisibleIndexVal=null;Math.abs(a)>this._physicalSize&&
0<this._physicalSize?(a-=this._scrollOffset,c=Math.round(a/this._physicalAverage)*this._itemsPerRow,this._virtualStart+=c,this._physicalStart+=c,this._physicalTop=Math.floor(this._virtualStart/this._itemsPerRow)*this._physicalAverage,this._update()):0<this._physicalCount&&(b=this._getReusables(c),c?(this._physicalTop=b.physicalTop,this._virtualStart+=b.indexes.length,this._physicalStart+=b.indexes.length):(this._virtualStart-=b.indexes.length,this._physicalStart-=b.indexes.length),this._update(b.indexes,
c?null:b.indexes),this._debounce("_increasePoolIfNeeded",this._increasePoolIfNeeded.bind(this,0),Nd))},_getReusables:function(b){var a=[],c=this._hiddenContentSize*this._ratio,d=this._virtualStart,e=this._virtualEnd,f=this._physicalCount,g=this._physicalTop+this._scrollOffset;var h=this._physicalBottom+this._scrollOffset;var k=this._scrollTop,l=this._scrollBottom;if(b){var m=this._physicalStart;h=k-g}else m=this._physicalEnd,h-=l;for(;;){var n=this._getPhysicalSizeIncrement(m);h-=n;if(a.length>=f||
h<=c)break;if(b){if(e+a.length+1>=this._virtualCount)break;if(g+n>=k-this._scrollOffset)break;a.push(m);g+=n;m=(m+1)%f}else{if(0>=d-a.length)break;if(g+this._physicalSize-n<=l)break;a.push(m);g-=n;m=0===m?f-1:m-1}}return{indexes:a,physicalTop:g-this._scrollOffset}},_update:function(b,a){if(!(b&&0===b.length||0===this._physicalCount)){this._manageFocus();this._assignModels(b);this._updateMetrics(b);if(a)for(;a.length;)b=a.pop(),this._physicalTop-=this._getPhysicalSizeIncrement(b);this._positionItems();
this._updateScrollerSize()}},_isClientFull:function(){return 0!=this._scrollBottom&&this._physicalBottom-1>=this._scrollBottom&&this._physicalTop<=this._scrollPosition},_increasePoolIfNeeded:function(b){b=this._clamp(this._physicalCount+b,3,this._virtualCount-this._virtualStart);b=this._convertIndexToCompleteRow(b);b-=this._physicalCount;var a=Math.round(.5*this._physicalCount);if(!(0>b)){if(0<b){a=window.performance.now();[].push.apply(this._physicalItems,this._createPool(b));for(var c=0;c<b;c++)this._physicalSizes.push(0);
this._physicalCount+=b;this._physicalStart>this._physicalEnd&&this._isIndexRendered(this._focusedVirtualIndex)&&this._getPhysicalIndex(this._focusedVirtualIndex)<this._physicalEnd&&(this._physicalStart+=b);this._update();this._templateCost=(window.performance.now()-a)/b;a=Math.round(.5*this._physicalCount)}this._virtualEnd>=this._virtualCount-1||0===a||(this._isClientFull()?this._physicalSize<this._optPhysicalSize&&this._debounce("_increasePoolIfNeeded",this._increasePoolIfNeeded.bind(this,this._clamp(Math.round(50/
this._templateCost),1,a)),kd):this._debounce("_increasePoolIfNeeded",this._increasePoolIfNeeded.bind(this,a),Nd))}},_render:function(){if(this.isAttached&&this._isVisible)if(0!==this._physicalCount){var b=this._getReusables(!0);this._physicalTop=b.physicalTop;this._virtualStart+=b.indexes.length;this._physicalStart+=b.indexes.length;this._update(b.indexes);this._update();this._increasePoolIfNeeded(0)}else 0<this._virtualCount&&(this.updateViewportBoundaries(),this._increasePoolIfNeeded(3))},_itemsChanged:function(b){"items"===
b.path&&(this._physicalTop=this._virtualStart=0,this._virtualCount=this.items?this.items.length:0,this._collection=null,this._physicalIndexForKey={},this._lastVisibleIndexVal=this._firstVisibleIndexVal=null,this._physicalCount=this._physicalCount||0,this._physicalItems=this._physicalItems||[],this._physicalSizes=this._physicalSizes||[],this._physicalStart=0,this._scrollTop>this._scrollOffset&&this._resetScrollPosition(0),this._removeFocusedItem(),this._debounce("_render",this._render,M))},_iterateItems:function(b,
a){var c,d;if(2===arguments.length&&a)for(d=0;d<a.length;d++){var e=a[d];var f=this._computeVidx(e);if(null!=(c=b.call(this,e,f)))return c}else{e=this._physicalStart;for(f=this._virtualStart;e<this._physicalCount;e++,f++)if(null!=(c=b.call(this,e,f)))return c;for(e=0;e<this._physicalStart;e++,f++)if(null!=(c=b.call(this,e,f)))return c}},_computeVidx:function(b){return b>=this._physicalStart?this._virtualStart+(b-this._physicalStart):this._virtualStart+(this._physicalCount-this._physicalStart)+b},
_updateMetrics:function(b){ea();var a=0,c=0,d=this._physicalAverageCount,e=this._physicalAverage;this._iterateItems(function(f,g){c+=this._physicalSizes[f];this._physicalSizes[f]=this._physicalItems[f].offsetHeight;a+=this._physicalSizes[f];this._physicalAverageCount+=this._physicalSizes[f]?1:0},b);this.grid?(this._updateGridMetrics(),this._physicalSize=Math.ceil(this._physicalCount/this._itemsPerRow)*this._rowHeight):(c=1===this._itemsPerRow?c:Math.ceil(this._physicalCount/this._itemsPerRow)*this._rowHeight,
this._physicalSize=this._physicalSize+a-c,this._itemsPerRow=1);this._physicalAverageCount!==d&&(this._physicalAverage=Math.round((e*d+a)/this._physicalAverageCount))},_positionItems:function(){this._adjustScrollPosition();var b=this._physicalTop;this._iterateItems(function(a,c){this.translate3d(0,b+"px",0,this._physicalItems[a]);b+=this._physicalSizes[a]})},_getPhysicalSizeIncrement:function(b){return this.grid?this._computeVidx(b)%this._itemsPerRow!==this._itemsPerRow-1?0:this._rowHeight:this._physicalSizes[b]},
_adjustScrollPosition:function(){var b=0===this._virtualStart?this._physicalTop:Math.min(this._scrollPosition+this._physicalTop,0);if(0!==b){this._physicalTop-=b;var a=this._scrollTop;!Sf&&0<a&&this._resetScrollPosition(a-b)}},_resetScrollPosition:function(b){this.scrollTarget&&0<=b&&(this._scrollPosition=this._scrollTop=b)},_updateScrollerSize:function(b){this._estScrollHeight=this.grid?this._virtualRowCount*this._rowHeight:this._physicalBottom+Math.max(this._virtualCount-this._physicalCount-this._virtualStart,
0)*this._physicalAverage;if((b=(b=(b=b||0===this._scrollHeight)||this._scrollPosition>=this._estScrollHeight-this._physicalSize)||this.grid&&this.$.items.style.height<this._estScrollHeight)||Math.abs(this._estScrollHeight-this._scrollHeight)>=this._viewportHeight)this.$.items.style.height=this._estScrollHeight+"px",this._scrollHeight=this._estScrollHeight},scrollToIndex:function(b){if(!("number"!==typeof b||0>b||b>this.items.length-1)&&(ea(),0!==this._physicalCount)){b=this._clamp(b,0,this._virtualCount-
1);if(!this._isIndexRendered(b)||b>=this._maxVirtualStart)this._virtualStart=this.grid?b-2*this._itemsPerRow:b-1;this._manageFocus();this._assignModels();this._updateMetrics();this._physicalTop=Math.floor(this._virtualStart/this._itemsPerRow)*this._physicalAverage;for(var a=this._physicalStart,c=this._virtualStart,d=0,e=this._hiddenContentSize;c<b&&d<=e;)d+=this._getPhysicalSizeIncrement(a),a=(a+1)%this._physicalCount,c++;this._updateScrollerSize(!0);this._positionItems();this._resetScrollPosition(this._physicalTop+
this._scrollOffset+d);this._increasePoolIfNeeded(0);this._lastVisibleIndexVal=this._firstVisibleIndexVal=null}},_resetAverage:function(){this._physicalAverageCount=this._physicalAverage=0},_resizeHandler:function(){this._debounce("_render",function(){this._lastVisibleIndexVal=this._firstVisibleIndexVal=null;this.updateViewportBoundaries();this._isVisible?(this.toggleScrollListener(!0),this._resetAverage(),this._render()):this.toggleScrollListener(!1)},M)},_convertIndexToCompleteRow:function(b){this._itemsPerRow=
this._itemsPerRow||1;return this.grid?Math.ceil(b/this._itemsPerRow)*this._itemsPerRow:b},_isIndexRendered:function(b){return b>=this._virtualStart&&b<=this._virtualEnd},_getPhysicalIndex:function(b){return(this._physicalStart+(b-this._virtualStart))%this._physicalCount},_clamp:function(b,a,c){return Math.min(c,Math.max(a,b))},_debounce:function(b,a,c){this._debouncers=this._debouncers||{};this._debouncers[b]=v.debounce(this._debouncers[b],c,a.bind(this));U.add(this._debouncers[b])}});class Vb extends Tf{static get is(){return"vaadin-grid-scroller"}static get properties(){return{size:{type:Number,
notify:!0},_vidxOffset:{value:0}}}static get observers(){return["_effectiveSizeChanged(_effectiveSize)"]}connectedCallback(){super.connectedCallback();this._scrollHandler()}_updateScrollerItem(b,a){}_afterScroll(){}_getRowTarget(){}_createScrollerRows(){}_canPopulate(){}scrollToIndex(b){this._warnPrivateAPIAccess("scrollToIndex");0<b&&(this._pendingScrollToIndex=null);!parseInt(this.$.items.style.borderTopWidth)&&0<b&&(this._pendingScrollToIndex=b);this._scrollingToIndex=!0;b=Math.min(Math.max(b,
0),this._effectiveSize-1);this.$.table.scrollTop=b/this._effectiveSize*(this.$.table.scrollHeight-this.$.table.offsetHeight);this._scrollHandler();this._accessIronListAPI(()=>this._maxScrollTop)&&this._virtualCount<this._effectiveSize&&this._adjustVirtualIndexOffset(1E6);this._accessIronListAPI(()=>super.scrollToIndex(b-this._vidxOffset));this._scrollHandler();var a=Array.from(this.$.items.children).filter(c=>c.index===b)[0];a&&(a=a.getBoundingClientRect().top-this.$.header.getBoundingClientRect().bottom,
1<Math.abs(a)&&(this.$.table.scrollTop+=a,this._scrollHandler()));this._scrollingToIndex=!1}_effectiveSizeChanged(b){let a,c=0;this._iterateItems((d,e)=>{e===this._firstVisibleIndex&&(d=this._physicalItems[d],a=d.index,c=d.getBoundingClientRect().top)});this.items&&b<this.items.length&&(this._scrollTop=0);Array.isArray(this.items)||(this.items={length:Math.min(b,this._edge||this._ie?3E4:1E5)});this._accessIronListAPI(()=>super._itemsChanged({path:"items"}));this._virtualCount=Math.min(this.items.length,
b)||0;0===this._scrollTop&&(this._accessIronListAPI(()=>this._scrollToIndex(Math.min(b-1,a))),this._iterateItems((d,e)=>{d=this._physicalItems[d];d.index===a&&(this.$.table.scrollTop+=Math.round(d.getBoundingClientRect().top-c));d.index===this._focusedItemIndex&&this._itemsFocusable&&this.$.items.contains(this.shadowRoot.activeElement)&&(e=Array.from(this._itemsFocusable.parentElement.children).indexOf(this._itemsFocusable),d.children[e].focus())}));this._assignModels();requestAnimationFrame(()=>
this._update())}_positionItems(){this._adjustScrollPosition();let b;isNaN(this._physicalTop)&&(b=!0,this._physicalTop=0);let a=this._physicalTop;this._iterateItems((c,d)=>{this._physicalItems[c].style.transform=`translateY(${a}px)`;a+=this._physicalSizes[c]});b&&this._scrollToIndex(0)}_increasePoolIfNeeded(b){0===b&&this._scrollingToIndex||!this._canPopulate()||!this._effectiveSize||(this._initialPoolCreated?Infinity!==this._optPhysicalSize&&(this._debounceIncreasePool=v.debounce(this._debounceIncreasePool,
M,()=>{this._updateMetrics();let a=Math.ceil((this._optPhysicalSize-this._physicalSize)/this._physicalAverage);this._physicalCount+a>this._effectiveSize&&(a=Math.max(0,this._effectiveSize-this._physicalCount));this._physicalSize&&0<a&&Infinity!==this._optPhysicalSize&&(super._increasePoolIfNeeded(a),this.__reorderChildNodes())})):(this._initialPoolCreated=!0,super._increasePoolIfNeeded(25)))}__reorderChildNodes(){const b=Array.from(this.$.items.childNodes);b.reduce((a,c,d,e)=>{if(0===d||e[d-1].index===
c.index-1)return a},!0)||b.sort((a,c)=>a.index-c.index).forEach(a=>this.$.items.appendChild(a))}_createPool(b){const a=document.createDocumentFragment();b=this._createScrollerRows(b);b.forEach(d=>a.appendChild(d));this._getRowTarget().appendChild(a);const c=this.querySelector("[slot]");if(c){const d=c.getAttribute("slot");c.setAttribute("slot","foo-bar");c.setAttribute("slot",d)}this._updateHeaderFooterMetrics();Mc(this,()=>this.notifyResize());return b}_assignModels(b){this._iterateItems((a,c)=>
{a=this._physicalItems[a];this._toggleAttribute("hidden",c>=this._effectiveSize,a);this._updateScrollerItem(a,c+(this._vidxOffset||0))},b)}_scrollHandler(){const b=this.$.table.scrollTop-this._scrollPosition;this._accessIronListAPI(super._scrollHandler);const a=this._vidxOffset;this._accessIronListAPI(()=>this._maxScrollTop)&&this._virtualCount<this._effectiveSize&&this._adjustVirtualIndexOffset(b);this._vidxOffset!==a&&this._update();this._afterScroll()}_adjustVirtualIndexOffset(b){if(1E4<Math.abs(b))this._noScale?
this._noScale=!1:(b=this.$.table.scrollTop/(this.$.table.scrollHeight-this.$.table.offsetHeight),this._vidxOffset=Math.round(b*this._effectiveSize-b*this._virtualCount));else{b=this._vidxOffset||0;0===this._scrollTop?(this._vidxOffset=0,b!==this._vidxOffset&&super.scrollToIndex(0)):1E3>this.firstVisibleIndex&&0<this._vidxOffset&&(this._vidxOffset-=Math.min(this._vidxOffset,100),b!==this._vidxOffset&&super.scrollToIndex(this.firstVisibleIndex+(b-this._vidxOffset)),this._noScale=!0);const a=this._effectiveSize-
this._virtualCount;this._scrollTop>=this._maxScrollTop&&0<this._maxScrollTop?(this._vidxOffset=a,b!==this._vidxOffset&&super.scrollToIndex(this._virtualCount)):this.firstVisibleIndex>this._virtualCount-1E3&&this._vidxOffset<a&&(this._vidxOffset+=Math.min(a-this._vidxOffset,100),b!==this._vidxOffset&&super.scrollToIndex(this.firstVisibleIndex-(this._vidxOffset-b)),this._noScale=!0)}}_accessIronListAPI(b){this._warnPrivateAPIAccessAsyncEnabled=!1;b=b.apply(this);this._debouncerWarnPrivateAPIAccess=
v.debounce(this._debouncerWarnPrivateAPIAccess,M,()=>this._warnPrivateAPIAccessAsyncEnabled=!0);return b}_debounceRender(b,a){super._debounceRender(()=>this._accessIronListAPI(b),a)}_warnPrivateAPIAccess(b){this._warnPrivateAPIAccessAsyncEnabled&&console.warn(`Accessing private API (${b})!`)}_render(){this._accessIronListAPI(super._render)}_createFocusBackfillItem(){}_multiSelectionChanged(){}clearSelection(){}_itemsChanged(){}_manageFocus(){}_removeFocusedItem(){}get _firstVisibleIndex(){return this._accessIronListAPI(()=>
super.firstVisibleIndex)}get _lastVisibleIndex(){return this._accessIronListAPI(()=>super.lastVisibleIndex)}_scrollToIndex(b){this._accessIronListAPI(()=>this.scrollToIndex(b))}get firstVisibleIndex(){this._warnPrivateAPIAccess("firstVisibleIndex");return super.firstVisibleIndex}set firstVisibleIndex(b){this._warnPrivateAPIAccess("firstVisibleIndex");super.firstVisibleIndex=b}get lastVisibleIndex(){this._warnPrivateAPIAccess("lastVisibleIndex");return super.lastVisibleIndex}set lastVisibleIndex(b){this._warnPrivateAPIAccess("lastVisibleIndex");
super.lastVisibleIndex=b}updateViewportBoundaries(){this._warnPrivateAPIAccess("updateViewportBoundaries");super.updateViewportBoundaries.apply(this,arguments)}_resizeHandler(){super._resizeHandler();ea()}}customElements.define(Vb.is,Vb);const Uf=b=>class extends b{static get observers(){return["_a11yUpdateGridSize(size, _columnTree, _columnTree.*)"]}_a11yGetHeaderRowCount(a){return a.filter(c=>c.some(d=>d._headerTemplate||d.headerRenderer||d.path||d.header)).length}_a11yGetFooterRowCount(a){return a.filter(c=>
c.some(d=>d._headerTemplate||d.headerRenderer)).length}_a11yUpdateGridSize(a,c){if(void 0!==a&&void 0!==c){var d=c[c.length-1];this.$.table.setAttribute("aria-rowcount",a+this._a11yGetHeaderRowCount(c)+this._a11yGetFooterRowCount(c));this.$.table.setAttribute("aria-colcount",d&&d.length||0);this._a11yUpdateHeaderRows();this._a11yUpdateFooterRows()}}_a11yUpdateHeaderRows(){Array.from(this.$.header.children).forEach((a,c)=>a.setAttribute("aria-rowindex",c+1))}_a11yUpdateFooterRows(){Array.from(this.$.footer.children).forEach((a,
c)=>a.setAttribute("aria-rowindex",this._a11yGetHeaderRowCount(this._columnTree)+this.size+c+1))}_a11yUpdateRowRowindex(a,c){a.setAttribute("aria-rowindex",c+this._a11yGetHeaderRowCount(this._columnTree)+1)}_a11yUpdateRowSelected(a,c){a.setAttribute("aria-selected",!!c);Array.from(a.children).forEach(d=>d.setAttribute("aria-selected",!!c))}_a11yUpdateRowLevel(a,c){a.setAttribute("aria-level",c+1)}_a11yUpdateRowDetailsOpened(a,c){Array.from(a.children).forEach(d=>{"boolean"===typeof c?d.setAttribute("aria-expanded",
c):d.hasAttribute("aria-expanded")&&d.removeAttribute("aria-expanded")})}_a11ySetRowDetailsCell(a,c){Array.from(a.children).forEach(d=>{d!==c&&d.setAttribute("aria-controls",c.id)})}_a11yUpdateCellColspan(a,c){a.setAttribute("aria-colspan",Number(c))}_a11yUpdateSorters(){Array.from(this.querySelectorAll("vaadin-grid-sorter")).forEach(a=>{let c=a.parentNode;for(;c&&"vaadin-grid-cell-content"!==c.localName;)c=c.parentNode;c&&c.assignedSlot&&c.assignedSlot.parentNode.setAttribute("aria-sort",{asc:"ascending",
desc:"descending"}[String(a.direction)]||"none")})}},Vf=b=>class extends b{static get properties(){return{activeItem:{type:Object,notify:!0,value:null}}}ready(){super.ready();this.$.scroller.addEventListener("click",this._onClick.bind(this));this.addEventListener("cell-activate",this._activateItem.bind(this))}_activateItem(a){if(a=(a=a.detail.model)?a.item:null)this.activeItem=this._itemsEqual(this.activeItem,a)?null:a}_onClick(a){if(!a.defaultPrevented){var c=a.composedPath();if((c=c[c.indexOf(this.$.table)-
3])&&!(-1<c.getAttribute("part").indexOf("details-cell"))){var d=c._content,e=this.getRootNode().activeElement;d.contains(e)&&(!this._ie||this._isFocusable(e))||this._isFocusable(a.target)||this.dispatchEvent(new CustomEvent("cell-activate",{detail:{model:this.__getRowModel(c.parentElement)}}))}}}_isFocusable(a){if(!a.parentNode)return!1;const c=-1!==Array.from(a.parentNode.querySelectorAll("[tabindex], button, input, select, textarea, object, iframe, label, a[href], area[href]")).filter(d=>"cell body-cell"!==
d.getAttribute("part")).indexOf(a);return!a.disabled&&c}},Wf=b=>class extends b{static get properties(){return{items:Array}}static get observers(){return["_itemsChanged(items, items.*, isAttached)"]}_itemsChanged(a,c,d){if(d)if(Array.isArray(a))this.size=a.length,this.dataProvider=this.dataProvider||this._arrayDataProvider,this.clearCache(),this._ensureFirstPageLoaded();else{if(void 0===a||null===a)this.size=0;this.dataProvider===this._arrayDataProvider&&(this.dataProvider=void 0)}}_arrayDataProvider(a,
c){let d=(Array.isArray(this.items)?this.items:[]).slice(0);this._filters&&this._checkPaths(this._filters,"filtering",d)&&(d=this._filter(d));this.size=d.length;a.sortOrders.length&&this._checkPaths(this._sorters,"sorting",d)&&(d=d.sort(this._multiSort.bind(this)));const e=a.page*a.pageSize;a=d.slice(e,e+a.pageSize);c(a,d.length)}_checkPaths(a,c,d){if(!d.length)return!1;let e=!0;for(var f in a){const g=a[f].path;if(!g||-1===g.indexOf("."))continue;const h=g.replace(/\.[^\.]*$/,"");void 0===ja.get(h,
d[0])&&(console.warn(`Path "${g}" used for ${c} does not exist in all of the items, ${c} is disabled.`),e=!1)}return e}_multiSort(a,c){return this._sorters.map(d=>"asc"===d.direction?this._compare(ja.get(d.path,a),ja.get(d.path,c)):"desc"===d.direction?this._compare(ja.get(d.path,c),ja.get(d.path,a)):0).reduce((d,e)=>d?d:e,0)}_normalizeEmptyValue(a){return 0<=[void 0,null].indexOf(a)?"":isNaN(a)?a.toString():a}_compare(a,c){a=this._normalizeEmptyValue(a);c=this._normalizeEmptyValue(c);return a<c?
-1:a>c?1:0}_filter(a){return a.filter((c,d)=>0===this._filters.filter(e=>{const f=this._normalizeEmptyValue(ja.get(e.path,c));e=this._normalizeEmptyValue(e.value).toString().toLowerCase();return-1===f.toString().toLowerCase().indexOf(e)}).length)}},Xf=b=>class extends Da(b){ready(){super.ready();const a=this.$.scroller;vb(a,"track",this._onHeaderTrack.bind(this));a.addEventListener("touchmove",c=>a.hasAttribute("column-resizing")&&c.preventDefault());a.addEventListener("contextmenu",c=>"resize-handle"==
c.target.getAttribute("part")&&c.preventDefault());a.addEventListener("mousedown",c=>"resize-handle"===c.target.getAttribute("part")&&c.preventDefault())}_onHeaderTrack(a){var c=a.target;if("resize-handle"===c.getAttribute("part")){let f=c.parentElement._column;for(this._toggleAttribute("column-resizing",!0,this.$.scroller);"vaadin-grid-column-group"===f.localName;)f=Array.prototype.slice.call(f._childColumns,0).sort(function(g,h){return g._order-h._order}).filter(function(g){return!g.hidden}).pop();
c=Array.from(this.$.header.querySelectorAll('[part~\x3d"row"]:last-child [part~\x3d"cell"]'));var d=c.filter(g=>g._column===f)[0];if(d.offsetWidth){var e=window.getComputedStyle(d);e=10+parseInt(e.paddingLeft)+parseInt(e.paddingRight)+parseInt(e.borderLeftWidth)+parseInt(e.borderRightWidth)+parseInt(e.marginLeft)+parseInt(e.marginRight);const g=d.offsetWidth+(this.__isRTL?d.getBoundingClientRect().left-a.detail.x:a.detail.x-d.getBoundingClientRect().right);f.width=Math.max(e,g)+"px";f.flexGrow=0}c.sort(function(g,
h){return g._column._order-h._column._order}).forEach(function(g,h,k){h<k.indexOf(d)&&(g._column.width=g.offsetWidth+"px",g._column.flexGrow=0)});"end"===a.detail.state&&(this._toggleAttribute("column-resizing",!1,this.$.scroller),this.dispatchEvent(new CustomEvent("column-resize",{detail:{resizedColumn:f}})));this._resizeHandler()}}},Od=class b{constructor(a,c,d){this.grid=a;this.parentCache=c;this.parentItem=d;this.itemCaches={};this.items={};this.size=this.effectiveSize=0;this.pendingRequests=
{}}isLoading(){return Object.keys(this.pendingRequests).length||Object.keys(this.itemCaches).filter(a=>this.itemCaches[a].isLoading())[0]}getItemForIndex(a){const {cache:c,scaledIndex:d}=this.getCacheAndIndex(a);return c.items[d]}updateSize(){this.effectiveSize=!this.parentItem||this.grid._isExpanded(this.parentItem)?this.size+Object.keys(this.itemCaches).reduce((a,c)=>{c=this.itemCaches[c];c.updateSize();return a+c.effectiveSize},0):0}ensureSubCacheForScaledIndex(a){if(!this.itemCaches[a]){const c=
new b(this.grid,this,this.items[a]);this.itemCaches[a]=c;this.grid._loadPage(0,c)}}getCacheAndIndex(a){const c=Object.keys(this.itemCaches);for(var d=0;d<c.length;d++){const e=Number(c[d]),f=this.itemCaches[e];if(a<=e)break;else if(a<=e+f.effectiveSize)return f.getCacheAndIndex(a-e-1);a-=f.effectiveSize}return{cache:this,scaledIndex:a}}},Yf=b=>class extends b{static get properties(){return{pageSize:{type:Number,value:50,observer:"_pageSizeChanged"},dataProvider:{type:Object,notify:!0,observer:"_dataProviderChanged"},
loading:{type:Boolean,notify:!0,readOnly:!0,reflectToAttribute:!0},_cache:{type:Object,value:function(){return new Od(this)}},itemIdPath:{type:String,value:null},expandedItems:{type:Object,notify:!0,value:()=>[]}}}static get observers(){return["_sizeChanged(size)","_itemIdPathChanged(itemIdPath)","_expandedItemsChanged(expandedItems.*)"]}_sizeChanged(a){a-=this._cache.size;this._cache.size+=a;this._cache.effectiveSize+=a;this._effectiveSize=this._cache.effectiveSize}_getItem(a,c){if(!(a>=this._effectiveSize)){c.index=
a;var {cache:d,scaledIndex:e}=this._cache.getCacheAndIndex(a);(a=d.items[e])?(this._toggleAttribute("loading",!1,c),this._updateItem(c,a),this._isExpanded(a)&&d.ensureSubCacheForScaledIndex(e)):(this._toggleAttribute("loading",!0,c),this._loadPage(this._getPageForIndex(e),d))}}_expandedInstanceChangedCallback(a,c){void 0!==a.item&&(c?this.expandItem(a.item):this.collapseItem(a.item))}getItemId(a){return this.itemIdPath?this.get(this.itemIdPath,a):a}_isExpanded(a){return this.__expandedKeys.has(this.getItemId(a))}_expandedItemsChanged(a){this.__cacheExpandedKeys();
this._cache.updateSize();this._effectiveSize=this._cache.effectiveSize;this._assignModels()}_itemIdPathChanged(a){this.__cacheExpandedKeys()}__cacheExpandedKeys(){this.expandedItems&&(this.__expandedKeys=new Set,this.expandedItems.forEach(a=>{this.__expandedKeys.add(this.getItemId(a))}))}expandItem(a){this._isExpanded(a)||this.push("expandedItems",a)}collapseItem(a){this._isExpanded(a)&&this.splice("expandedItems",this._getItemIndexInArray(a,this.expandedItems),1)}_getIndexLevel(a){({cache:a}=this._cache.getCacheAndIndex(a));
let c=0;for(;a.parentCache;)a=a.parentCache,c++;return c}_canPopulate(){return this._hasData&&this._columnTree}_loadPage(a,c){if(!c.pendingRequests[a]&&this.dataProvider){this._setLoading(!0);c.pendingRequests[a]=!0;const d={page:a,pageSize:this.pageSize,sortOrders:this._mapSorters(),filters:this._mapFilters(),parentItem:c.parentItem};this.dataProvider(d,(e,f)=>{void 0!==f?c.size=f:d.parentItem&&(c.size=e.length);const g=Array.from(this.$.items.children).map(h=>h._item);e.forEach((h,k)=>{k=a*this.pageSize+
k;c.items[k]=h;this._isExpanded(h)&&-1<g.indexOf(h)&&c.ensureSubCacheForScaledIndex(k)});this._hasData=!0;delete c.pendingRequests[a];this._setLoading(!1);this._cache.updateSize();this._effectiveSize=this._cache.effectiveSize;Array.from(this.$.items.children).filter(h=>!h.hidden).forEach(h=>{const k=this._cache.getItemForIndex(h.index);k&&(this._toggleAttribute("loading",!1,h),this._updateItem(h,k))});this._increasePoolIfNeeded(0);this.__itemsReceived()})}}_getPageForIndex(a){return Math.floor(a/
this.pageSize)}clearCache(){this._cache=new Od(this);Array.from(this.$.items.children).forEach(a=>{Array.from(a.children).forEach(c=>{c._instance&&c._instance._setPendingProperty("item",{},!1)})});this._cache.size=this.size||0;this._cache.updateSize();this._hasData=!1;this._assignModels();this._effectiveSize||this._loadPage(0,this._cache)}_pageSizeChanged(a,c){void 0!==c&&a!==c&&this.clearCache()}_checkSize(){void 0===this.size&&0===this._effectiveSize&&console.warn("The \x3cvaadin-grid\x3e needs the total number of items in order to display rows. Set the total number of items to the `size` property, or provide the total number of items in the second argument of the `dataProvider`\u2019s `callback` call.")}_dataProviderChanged(a,
c){void 0!==c&&this.clearCache();a&&this.items&&this.items.length&&this._scrollToIndex(this._firstVisibleIndex);this._ensureFirstPageLoaded();this._debouncerCheckSize=v.debounce(this._debouncerCheckSize,E.after(2E3),this._checkSize.bind(this));this._scrollHandler()}_ensureFirstPageLoaded(){this._hasData||this._loadPage(0,this._cache,()=>{const a=this._hasData;this._hasData=!0;a||this.notifyResize()})}_itemsEqual(a,c){return this.getItemId(a)===this.getItemId(c)}_getItemIndexInArray(a,c){let d=-1;
c.forEach((e,f)=>{this._itemsEqual(e,a)&&(d=f)});return d}},Zf=b=>class extends b{ready(){super.ready();this._addNodeObserver()}_hasColumnGroups(a){for(let c=0;c<a.length;c++)if("vaadin-grid-column-group"===a[c].localName)return!0;return!1}_getChildColumns(a){return Z.getFlattenedNodes(a).filter(this._isColumnElement)}_flattenColumnGroups(a){return a.map(c=>"vaadin-grid-column-group"===c.localName?this._getChildColumns(c):[c]).reduce((c,d)=>c.concat(d),[])}_getColumnTree(){for(var a=[],c=Z.getFlattenedNodes(this).filter(this._isColumnElement);;){a.push(c);
if(!this._hasColumnGroups(c))break;c=this._flattenColumnGroups(c)}return a}_updateColumnTree(){var a=this._getColumnTree();this._arrayEquals(a,this._columnTree)||(this._columnTree=a)}_addNodeObserver(){this._observer=new Z(this,a=>{const c=a.addedNodes.filter(d=>"template"===d.localName&&d.classList.contains("row-details"))[0];c&&this._rowDetailsTemplate!==c&&(this._rowDetailsTemplate=c);(0<a.addedNodes.filter(this._isColumnElement).length||0<a.removedNodes.filter(this._isColumnElement).length)&&
this._updateColumnTree();this._debouncerCheckImports=v.debounce(this._debouncerCheckImports,E.after(2E3),this._checkImports.bind(this));this._ensureFirstPageLoaded()})}_arrayEquals(a,c){if(!a||!c||a.length!=c.length)return!1;for(var d=0,e=a.length;d<e;d++)if(a[d]instanceof Array&&c[d]instanceof Array){if(!this._arrayEquals(a[d],c[d]))return!1}else if(a[d]!=c[d])return!1;return!0}_checkImports(){"vaadin-grid-column-group vaadin-grid-filter vaadin-grid-filter-column vaadin-grid-tree-toggle vaadin-grid-selection-column vaadin-grid-sort-column vaadin-grid-sorter".split(" ").forEach(a=>
{var c=this.querySelector(a);!c||c instanceof I||console.warn(`Make sure you have imported the required module for <${a}> element.`)})}_updateFirstAndLastColumn(){Array.from(this.shadowRoot.querySelectorAll("tr")).forEach(a=>this._updateFirstAndLastColumnForRow(a))}_updateFirstAndLastColumnForRow(a){Array.from(a.querySelectorAll('[part~\x3d"cell"]:not([part~\x3d"details-cell"])')).sort((c,d)=>c._column._order-d._column._order).forEach((c,d,e)=>{this._toggleAttribute("first-column",0===d,c);this._toggleAttribute("last-column",
d===e.length-1,c)})}_isColumnElement(a){return a.nodeType===Node.ELEMENT_NODE&&/\bcolumn\b/.test(a.localName)}},$f=b=>class extends b{getEventContext(a){const c={};a=a.composedPath();const d=a[a.indexOf(this.$.table)-3];if(!d)return c;c.section=["body","header","footer","details"].filter(e=>-1<d.getAttribute("part").indexOf(e))[0];d._column&&(c.column=d._column);"body"!==c.section&&"details"!==c.section||Object.assign(c,this.__getRowModel(d.parentElement));return c}},ag=b=>class extends b{static get properties(){return{_filters:{type:Array,
value:function(){return[]}}}}ready(){super.ready();this.addEventListener("filter-changed",this._filterChanged.bind(this))}_filterChanged(a){-1===this._filters.indexOf(a.target)&&this._filters.push(a.target);a.stopPropagation();this.dataProvider&&this.clearCache()}_mapFilters(){return this._filters.map(a=>({path:a.path,value:a.value}))}};class cb extends class extends I{}{static get is(){return"vaadin-grid-templatizer"}static get properties(){return{dataHost:Object,template:Object,_templateInstances:{type:Array,
value:function(){return[]}},_parentPathValues:{value:function(){return{}}},_grid:Object}}static get observers(){return["_templateInstancesChanged(_templateInstances.*, _parentPathValues.*)"]}constructor(){super();this._instanceProps={detailsOpened:!0,index:!0,item:!0,selected:!0,expanded:!0,level:!0}}createInstance(){this._ensureTemplatized();const b=new this._TemplateClass({});this.addInstance(b);return b}addInstance(b){-1===this._templateInstances.indexOf(b)&&(this._templateInstances.push(b),requestAnimationFrame(()=>
this.notifyPath("_templateInstances.*",this._templateInstances)))}removeInstance(b){b=this._templateInstances.indexOf(b);this.splice("_templateInstances",b,1)}_ensureTemplatized(){this._TemplateClass||(this._TemplateClass=za(this.template,this,{instanceProps:this._instanceProps,parentModel:!0,forwardHostProp:function(b,a){this._forwardParentProp(b,a);this._templateInstances&&this._templateInstances.forEach(c=>c.notifyPath(b,a))},notifyInstanceProp:function(b,a,c){if("index"!==a&&"item"!==a){var d=
`__${a}__`;if(b[d]!==c){b[d]=c;var e=Array.from(this._grid.$.items.children).filter(f=>this._grid._itemsEqual(f._item,b.item))[0];e&&Array.from(e.children).forEach(f=>{f._instance&&(f._instance[d]=c,f._instance.notifyPath(a,c))});if(Array.isArray(this._grid.items)&&0===a.indexOf("item.")){e=this._grid.items.indexOf(b.item);const f=a.slice(5);this._grid.notifyPath(`items.${e}.${f}`,c)}e=`_${a}InstanceChangedCallback`;if(this._grid&&this._grid[e])this._grid[e](b,c)}}}}))}_forwardParentProp(b,a){this._parentPathValues[b]=
a;this._templateInstances.forEach(c=>c.notifyPath(b,a))}_templateInstancesChanged(b,a){let c,d;if("_templateInstances"===b.path)c=0,d=this._templateInstances.length;else if("_templateInstances.splices"===b.path)c=b.value.index,d=b.value.addedCount;else return;Object.keys(this._parentPathValues||{}).forEach(e=>{for(var f=c;f<c+d;f++)this._templateInstances[f].set(e,this._parentPathValues[e])})}}customElements.define(cb.is,cb);const bg=b=>class extends b{static get properties(){return{detailsOpenedItems:{type:Array,
value:function(){return[]}},_rowDetailsTemplate:Object,rowDetailsRenderer:Function,_detailsCells:{type:Array}}}static get observers(){return["_detailsOpenedItemsChanged(detailsOpenedItems.*, _rowDetailsTemplate, rowDetailsRenderer)","_rowDetailsTemplateOrRendererChanged(_rowDetailsTemplate, rowDetailsRenderer)"]}_rowDetailsTemplateOrRendererChanged(a,c){if(a&&c)throw Error("You should only use either a renderer or a template for row details");if(a||c)a&&!a.templatizer&&(c=new cb,c._grid=this,c.dataHost=
this.dataHost,c.template=a,a.templatizer=c),this._columnTree&&Array.from(this.$.items.children).forEach(d=>{d.querySelector("[part~\x3ddetails-cell]")||(this._updateRow(d,this._columnTree[this._columnTree.length-1]),this._a11yUpdateRowDetailsOpened(d,!1));delete d.querySelector("[part~\x3ddetails-cell]")._instance}),this.detailsOpenedItems.length&&(Array.from(this.$.items.children).forEach(this._toggleDetailsCell,this),this._update())}_detailsOpenedItemsChanged(a,c,d){"detailsOpenedItems.length"!==
a.path&&a.value&&Array.from(this.$.items.children).forEach(e=>{this._toggleDetailsCell(e,e._item);this._a11yUpdateRowDetailsOpened(e,this._isDetailsOpened(e._item));this._toggleAttribute("details-opened",this._isDetailsOpened(e._item),e)})}_configureDetailsCell(a){a.setAttribute("part","cell details-cell");this._toggleAttribute("frozen",!0,a)}_toggleDetailsCell(a,c){const d=a.querySelector('[part~\x3d"details-cell"]');if(d){var e=!this._isDetailsOpened(c),f=!!d.hidden!==e;if(!d._instance&&!d._renderer||
d.hidden!==e)(d.hidden=e)?a.style.removeProperty("padding-bottom"):(this.rowDetailsRenderer?(d._renderer=this.rowDetailsRenderer,d._renderer.call(this,d._content,this,{index:a.index,item:c})):this._rowDetailsTemplate&&!d._instance&&(d._instance=this._rowDetailsTemplate.templatizer.createInstance(),d._content.innerHTML="",d._content.appendChild(d._instance.root),this._updateItem(a,c)),ea(),a.style.setProperty("padding-bottom",`${d.offsetHeight}px`),requestAnimationFrame(()=>this.notifyResize()));f&&
(this._updateMetrics(),this._positionItems())}}_updateDetailsCellHeights(){Array.from(this.$.items.querySelectorAll('[part~\x3d"details-cell"]:not([hidden])')).forEach(a=>{a.parentElement.style.setProperty("padding-bottom",`${a.offsetHeight}px`)})}_isDetailsOpened(a){return this.detailsOpenedItems&&-1!==this._getItemIndexInArray(a,this.detailsOpenedItems)}openItemDetails(a){this._isDetailsOpened(a)||this.push("detailsOpenedItems",a)}closeItemDetails(a){this._isDetailsOpened(a)&&this.splice("detailsOpenedItems",
this._getItemIndexInArray(a,this.detailsOpenedItems),1)}_detailsOpenedInstanceChangedCallback(a,c){super._detailsOpenedInstanceChangedCallback&&super._detailsOpenedInstanceChangedCallback(a,c);c?this.openItemDetails(a.item):this.closeItemDetails(a.item)}},cg=b=>class extends b{get _timeouts(){return{SCROLL_PERIOD:1E3,WHEEL_SCROLLING:200,SCROLLING:100,IGNORE_WHEEL:500}}static get properties(){return{_frozenCells:{type:Array,value:function(){return[]}},_scrollbarWidth:{type:Number,value:function(){var a=
document.createElement("div");a.style.width="100px";a.style.height="100px";a.style.overflow="scroll";a.style.position="absolute";a.style.top="-9999px";document.body.appendChild(a);var c=a.offsetWidth-a.clientWidth;document.body.removeChild(a);return c}},_rowWithFocusedElement:Element,_deltaYAcc:{type:Number,value:0}}}static get observers(){return["_scrollHeightUpdated(_estScrollHeight)","_scrollViewportHeightUpdated(_viewportHeight)"]}set _scrollTop(a){this.$.table.scrollTop=a}get _scrollTop(){return this.$.table.scrollTop}constructor(){super();
this._scrollLineHeight=this._getScrollLineHeight()}_getScrollLineHeight(){const a=document.createElement("div");a.style.fontSize="initial";a.style.display="none";document.body.appendChild(a);const c=window.getComputedStyle(a).fontSize;document.body.removeChild(a);return c?window.parseInt(c):void 0}_scrollViewportHeightUpdated(a){this._scrollPageHeight=a-this.$.header.clientHeight-this.$.footer.clientHeight-this._scrollLineHeight}ready(){super.ready();this.scrollTarget=this.$.table;this.addEventListener("wheel",
a=>{this._wheelScrolling=!0;this._debouncerWheelScrolling=v.debounce(this._debouncerWheelScrolling,E.after(this._timeouts.WHEEL_SCROLLING),()=>this._wheelScrolling=!1);this._onWheel(a)});this.$.table.addEventListener("scroll",a=>{this.$.outerscroller.outerScrolling&&a.stopImmediatePropagation()},!0);this.$.items.addEventListener("focusin",a=>{const c=a.composedPath().indexOf(this.$.items);this._rowWithFocusedElement=a.composedPath()[c-1]});this.$.items.addEventListener("focusout",()=>this._rowWithFocusedElement=
void 0)}scrollToIndex(a){this._accessIronListAPI(()=>super.scrollToIndex(a))}_onWheel(a){if(!a.ctrlKey&&!this._hasScrolledAncestor(a.target,a.deltaX,a.deltaY)){var c=this.$.table,d=a.deltaY;a.deltaMode===WheelEvent.DOM_DELTA_LINE?d*=this._scrollLineHeight:a.deltaMode===WheelEvent.DOM_DELTA_PAGE&&(d*=this._scrollPageHeight);if(this._wheelAnimationFrame)this._deltaYAcc+=d,a.preventDefault();else{d+=this._deltaYAcc;this._deltaYAcc=0;this._wheelAnimationFrame=!0;this._debouncerWheelAnimationFrame=v.debounce(this._debouncerWheelAnimationFrame,
M,()=>this._wheelAnimationFrame=!1);var e=Math.abs(a.deltaX)+Math.abs(d);this._canScroll(c,a.deltaX,d)?(a.preventDefault(),c.scrollTop+=d,c.scrollLeft+=a.deltaX,this._scrollHandler(),this._ignoreNewWheel=this._hasResidualMomentum=!0,this._debouncerIgnoreNewWheel=v.debounce(this._debouncerIgnoreNewWheel,E.after(this._timeouts.IGNORE_WHEEL),()=>this._ignoreNewWheel=!1)):this._hasResidualMomentum&&e<=this._previousMomentum||this._ignoreNewWheel?a.preventDefault():e>this._previousMomentum&&(this._hasResidualMomentum=
!1);this._previousMomentum=e}}}_hasScrolledAncestor(a,c,d){if("vaadin-grid-cell-content"===a.localName)return!1;if(this._canScroll(a,c,d)&&-1!==["auto","scroll"].indexOf(getComputedStyle(a).overflow))return!0;if(a!==this&&a.parentElement)return this._hasScrolledAncestor(a.parentElement,c,d)}_canScroll(a,c,d){return 0<d&&a.scrollTop<a.scrollHeight-a.offsetHeight||0>d&&0<a.scrollTop||0<c&&a.scrollLeft<a.scrollWidth-a.offsetWidth||0>c&&0<a.scrollLeft}_scheduleScrolling(){this._scrollingFrame||(this._scrollingFrame=
requestAnimationFrame(()=>this._toggleAttribute("scrolling",!0,this.$.scroller)));this._debounceScrolling=v.debounce(this._debounceScrolling,E.after(this._timeouts.SCROLLING),()=>{cancelAnimationFrame(this._scrollingFrame);delete this._scrollingFrame;this._toggleAttribute("scrolling",!1,this.$.scroller);this.$.outerscroller.outerScrolling||this._reorderRows()});this._scrollPeriodFrame||(this._scrollPeriodFrame=requestAnimationFrame(()=>this._toggleAttribute("scroll-period",!0,this.$.scroller)));this._debounceScrollPeriod=
v.debounce(this._debounceScrollPeriod,E.after(this._timeouts.SCROLL_PERIOD),()=>{cancelAnimationFrame(this._scrollPeriodFrame);delete this._scrollPeriodFrame;this._toggleAttribute("scroll-period",!1,this.$.scroller)})}_afterScroll(){this._translateStationaryElements();this.hasAttribute("reordering")||this._scheduleScrolling();const a=this.$.outerscroller;this._ios||!this._wheelScrolling&&!a.passthrough||a.syncOuterScroller();this._ios&&(this.$.items.style.transform=`translateY(${Math.max(-a.scrollTop,
0)||Math.min(0,a.scrollHeight-a.scrollTop-a.offsetHeight)}px)`);this._updateOverflow()}_updateOverflow(){let a="";const c=this.$.table;c.scrollTop<c.scrollHeight-c.clientHeight&&(a+=" bottom");0<c.scrollTop&&(a+=" top");c.scrollLeft<c.scrollWidth-c.clientWidth&&(a+=" right");0<c.scrollLeft&&(a+=" left");this._debounceOverflow=v.debounce(this._debounceOverflow,M,()=>{const d=a.trim();0<d.length&&this.getAttribute("overflow")!==d?this.setAttribute("overflow",d):0==d.length&&this.hasAttribute("overflow")&&
this.removeAttribute("overflow")})}_reorderRows(){const a=this.$.items,c=a.querySelectorAll("tr");if(c.length){var d=this._virtualStart+this._vidxOffset,e=this._rowWithFocusedElement||Array.from(c).filter(f=>!f.hidden)[0];if(e)if(d=e.index-d,e=Array.from(c).indexOf(e)-d,0<e)for(d=0;d<e;d++)a.appendChild(c[d]);else if(0>e)for(e=c.length+e;e<c.length;e++)a.insertBefore(c[e],c[0])}}_frozenCellsChanged(){this._debouncerCacheElements=v.debounce(this._debouncerCacheElements,G,()=>{Array.from(this.root.querySelectorAll('[part~\x3d"cell"]')).forEach(function(a){a.style.transform=
""});this._frozenCells=Array.prototype.slice.call(this.$.table.querySelectorAll("[frozen]"));this._updateScrollerMeasurements();this._translateStationaryElements()});this._updateLastFrozen()}_updateScrollerMeasurements(){0<this._frozenCells.length&&this.__isRTL&&(this.__scrollerMetrics={scrollWidth:this.$.outerscroller.scrollWidth,clientWidth:this.$.outerscroller.clientWidth})}_updateLastFrozen(){if(this._columnTree){var a=this._columnTree[this._columnTree.length-1].slice(0);a.sort((d,e)=>d._order-
e._order);var c=a.reduce((d,e,f)=>{e._lastFrozen=!1;return e.frozen&&!e.hidden?f:d},void 0);void 0!==c&&(a[c]._lastFrozen=!0)}}_translateStationaryElements(){this._edge&&!this._scrollbarWidth?(this.$.items.style.transform=this._getTranslate(-this._scrollLeft||0,-this._scrollTop||0),this.$.footer.style.transform=this.$.header.style.transform=this._getTranslate(-this._scrollLeft||0,0)):this.$.footer.style.transform=this.$.header.style.transform=this._getTranslate(0,this._scrollTop);if(0<this._frozenCells.length){var a=
this.__isRTL?this.__getNormalizedScrollLeft(this.$.table)+this.__scrollerMetrics.clientWidth-this.__scrollerMetrics.scrollWidth:this._scrollLeft;a=this._getTranslate(a,0);for(var c=0;c<this._frozenCells.length;c++)this._frozenCells[c].style.transform=a}}_getTranslate(a,c){return"translate("+a+"px,"+c+"px)"}_scrollHeightUpdated(a){this.$.outersizer.style.top=this.$.fixedsizer.style.top=a+"px"}},dg=b=>class extends b{static get properties(){return{selectedItems:{type:Object,notify:!0,value:()=>[]}}}static get observers(){return["_selectedItemsChanged(selectedItems.*)"]}_isSelected(a){return this.selectedItems&&
-1<this._getItemIndexInArray(a,this.selectedItems)}selectItem(a){this._isSelected(a)||this.push("selectedItems",a)}deselectItem(a){a=this._getItemIndexInArray(a,this.selectedItems);-1<a&&this.splice("selectedItems",a,1)}_toggleItem(a){-1===this._getItemIndexInArray(a,this.selectedItems)?this.selectItem(a):this.deselectItem(a)}_selectedItemsChanged(a){!this.$.items.children.length||"selectedItems"!==a.path&&"selectedItems.splices"!==a.path||Array.from(this.$.items.children).forEach(c=>{this._updateItem(c,
c._item)})}_selectedInstanceChangedCallback(a,c){super._selectedInstanceChangedCallback&&super._selectedInstanceChangedCallback(a,c);c?this.selectItem(a.item):this.deselectItem(a.item)}},eg=b=>class extends b{static get properties(){return{multiSort:{type:Boolean,value:!1},_sorters:{type:Array,value:function(){return[]}},_previousSorters:{type:Array,value:function(){return[]}}}}ready(){super.ready();this.addEventListener("sorter-changed",this._onSorterChanged);window.ShadyDOM&&G.run(()=>{const a=
this.querySelectorAll("vaadin-grid-sorter");Array.from(a).forEach(c=>{c instanceof I&&c.dispatchEvent(new CustomEvent("sorter-changed",{bubbles:!0,composed:!0}))})})}_onSorterChanged(a){const c=a.target;this._removeArrayItem(this._sorters,c);c._order=null;this.multiSort?(c.direction&&this._sorters.unshift(c),this._sorters.forEach((d,e)=>d._order=1<this._sorters.length?e:null,this)):c.direction&&(this._sorters.forEach(d=>{d._order=null;d.direction=null}),this._sorters=[c]);a.stopPropagation();this.dataProvider&&
JSON.stringify(this._previousSorters)!==JSON.stringify(this._mapSorters())&&this.clearCache();this._a11yUpdateSorters();this._previousSorters=this._mapSorters()}_mapSorters(){return this._sorters.map(a=>({path:a.path,direction:a.direction}))}_removeArrayItem(a,c){c=a.indexOf(c);-1<c&&a.splice(c,1)}},fg=b=>class extends b{static get properties(){return{cellClassNameGenerator:Function}}static get observers(){return["__cellClassNameGeneratorChanged(cellClassNameGenerator)"]}__cellClassNameGeneratorChanged(a){this.generateCellClassNames()}generateCellClassNames(){Array.from(this.$.items.children).filter(a=>
!a.hidden).forEach(a=>this._generateCellClassNames(a,this.__getRowModel(a)))}_generateCellClassNames(a,c){Array.from(a.children).forEach(d=>{d.__generatedClasses&&d.__generatedClasses.forEach(e=>d.classList.remove(e));if(this.cellClassNameGenerator){const e=this.cellClassNameGenerator(d._column,c);d.__generatedClasses=e&&e.split(" ").filter(f=>0<f.length);d.__generatedClasses&&d.__generatedClasses.forEach(f=>d.classList.add(f))}})}},gg=b=>class extends b{static get properties(){return{dropMode:String,
rowsDraggable:Boolean,dragFilter:Function,dropFilter:Function,__dndAutoScrollThreshold:{value:50}}}static get observers(){return["_dragDropAccessChanged(rowsDraggable, dropMode, dragFilter, dropFilter)"]}ready(){super.ready();this.$.table.addEventListener("dragstart",this._onDragStart.bind(this));this.$.table.addEventListener("dragend",this._onDragEnd.bind(this));this.$.table.addEventListener("dragover",this._onDragOver.bind(this));this.$.table.addEventListener("dragleave",this._onDragLeave.bind(this));
this.$.table.addEventListener("drop",this._onDrop.bind(this));this.$.table.addEventListener("dragenter",a=>{this.dropMode&&(a.preventDefault(),a.stopPropagation())})}_onDragStart(a){if(this.rowsDraggable){let e=a.target;"vaadin-grid-cell-content"===e.localName&&(e=e.assignedSlot.parentNode.parentNode);if(e.parentNode===this.$.items){a.stopPropagation();this._toggleAttribute("dragging-rows",!0,this);if(this._safari){const f=e.style.transform;e.style.top=/translateY\((.*)\)/.exec(f)[1];e.style.transform=
"none";requestAnimationFrame(()=>{e.style.top="";e.style.transform=f})}var c=e.getBoundingClientRect();window.ShadyDOM||(this._ios?a.dataTransfer.setDragImage(e):a.dataTransfer.setDragImage(e,a.clientX-c.left,a.clientY-c.top));var d=[e];this._isSelected(e._item)&&(d=this.__getViewportRows().filter(f=>this._isSelected(f._item)).filter(f=>!this.dragFilter||this.dragFilter(this.__getRowModel(f))));a.dataTransfer.setData("text",this.__formatDefaultTransferData(d));e.setAttribute("dragstart",1<d.length?
d.length:"");this.updateStyles({"--_grid-drag-start-x":`${a.clientX-c.left+20}px`,"--_grid-drag-start-y":`${a.clientY-c.top+10}px`});requestAnimationFrame(()=>{e.removeAttribute("dragstart");this.updateStyles({"--_grid-drag-start-x":"","--_grid-drag-start-y":""})});c=new CustomEvent("grid-dragstart",{detail:{draggedItems:d.map(f=>f._item),setDragData:(f,g)=>a.dataTransfer.setData(f,g),setDraggedItemsCount:f=>e.setAttribute("dragstart",f)}});c.originalEvent=a;this.dispatchEvent(c)}}}_onDragEnd(a){this._toggleAttribute("dragging-rows",
!1,this);a.stopPropagation();const c=new CustomEvent("grid-dragend");c.originalEvent=a;this.dispatchEvent(c)}_onDragLeave(a){a.stopPropagation();this._clearDragStyles()}_onDragOver(a){if(this.dropMode)if(this._dragOverItem=this._dropLocation=void 0,this.__dndAutoScroll(a.clientY))this._clearDragStyles();else{var c=a.composedPath().filter(d=>"tr"===d.localName)[0];if(this._effectiveSize&&"on-grid"!==this.dropMode)if(c&&c.parentNode===this.$.items){const d=c.getBoundingClientRect();this._dropLocation=
"on-top";"between"===this.dropMode?this._dropLocation=a.clientY-d.top<d.bottom-a.clientY?"above":"below":"on-top-or-between"===this.dropMode&&(a.clientY-d.top<d.height/3?this._dropLocation="above":a.clientY-d.top>d.height/3*2&&(this._dropLocation="below"))}else{if(c||"between"!==this.dropMode&&"on-top-or-between"!==this.dropMode)return;c=Array.from(this.$.items.children).filter(d=>!d.hidden).pop();this._dropLocation="below"}else this._dropLocation="empty";c&&c.hasAttribute("drop-disabled")?this._dropLocation=
void 0:(a.stopPropagation(),a.preventDefault(),"empty"===this._dropLocation?this._toggleAttribute("dragover",!0,this):c?(this._dragOverItem=c._item,c.getAttribute("dragover")!==this._dropLocation&&c.setAttribute("dragover",this._dropLocation)):this._clearDragStyles())}}__dndAutoScroll(a){if(this.__dndAutoScrolling)return!0;var c=this.$.header.getBoundingClientRect().bottom,d=this.$.footer.getBoundingClientRect().top;c=c-a+this.__dndAutoScrollThreshold;d=a-d+this.__dndAutoScrollThreshold;a=0;0<d?a=
2*d:0<c&&(a=2*-c);if(a&&(c=this.$.table.scrollTop,this.$.table.scrollTop+=a,c!==this.$.table.scrollTop))return this.__dndAutoScrolling=!0,setTimeout(()=>this.__dndAutoScrolling=!1,20),this._scrollHandler(),!0}__getViewportRows(){const a=this.$.header.getBoundingClientRect().bottom,c=this.$.footer.getBoundingClientRect().top;return Array.from(this.$.items.children).filter(d=>{d=d.getBoundingClientRect();return d.bottom>a&&d.top<c})}_clearDragStyles(){this.removeAttribute("dragover");Array.from(this.$.items.children).forEach(a=>
a.removeAttribute("dragover"))}_onDrop(a){if(this.dropMode){a.stopPropagation();a.preventDefault();var c=a.dataTransfer.types&&Array.from(a.dataTransfer.types).map(d=>({type:d,data:a.dataTransfer.getData(d)}));this._clearDragStyles();c=new CustomEvent("grid-drop",{bubbles:a.bubbles,cancelable:a.cancelable,detail:{dropTargetItem:this._dragOverItem,dropLocation:this._dropLocation,dragData:c}});c.originalEvent=a;this.dispatchEvent(c)}}__formatDefaultTransferData(a){return a.map(c=>Array.from(c.children).filter(d=>
!d.hidden&&-1===d.getAttribute("part").indexOf("details-cell")).sort((d,e)=>d._column._order>e._column._order?1:-1).map(d=>d._content.textContent.trim()).filter(d=>d).join("\t")).join("\n")}_dragDropAccessChanged(a,c,d,e){this.filterDragAndDrop()}filterDragAndDrop(){Array.from(this.$.items.children).filter(a=>!a.hidden).forEach(a=>{this._filterDragAndDrop(a,this.__getRowModel(a))})}_filterDragAndDrop(a,c){const d=!this.rowsDraggable||this.dragFilter&&!this.dragFilter(c);c=!this.dropMode||this.dropFilter&&
!this.dropFilter(c);(window.ShadyDOM?[a]:Array.from(a.children).map(e=>e._content)).forEach(e=>{d?e.removeAttribute("draggable"):e.setAttribute("draggable",!0)});this._toggleAttribute("drag-disabled",d,a);this._toggleAttribute("drop-disabled",c,a)}},hg=b=>class extends b{static get properties(){return{_headerFocusable:{type:Object,observer:"_focusableChanged"},_itemsFocusable:{type:Object,observer:"_focusableChanged"},_footerFocusable:{type:Object,observer:"_focusableChanged"},_navigatingIsHidden:Boolean,
_focusedItemIndex:{type:Number,value:0},_focusedColumnOrder:Number}}ready(){super.ready();this._ios||this._android||(this.addEventListener("keydown",this._onKeyDown),this.addEventListener("keyup",this._onKeyUp),this.addEventListener("focusin",this._onFocusIn),this.addEventListener("focusout",this._onFocusOut),this.$.table.addEventListener("focusin",this._onCellFocusIn.bind(this)),this.$.table.addEventListener("focusout",this._onCellFocusOut.bind(this)),this.addEventListener("mousedown",()=>{this._toggleAttribute("navigating",
!1,this);this._isMousedown=!0}),this.addEventListener("mouseup",()=>this._isMousedown=!1))}_focusableChanged(a,c){c&&c.setAttribute("tabindex","-1");a&&a.setAttribute("tabindex","0")}_onKeyDown(a){let c=a.key;if("Up"===c||"Down"===c||"Left"===c||"Right"===c)c="Arrow"+c;"Esc"===c&&(c="Escape");"Spacebar"===c&&(c=" ");let d;switch(c){case "ArrowUp":case "ArrowDown":case "ArrowLeft":case "ArrowRight":case "PageUp":case "PageDown":case "Home":case "End":d="Navigation";break;case "Enter":case "Escape":case "F2":d=
"Interaction";break;case "Tab":d="Tab";break;case " ":d="Space"}this._detectInteracting(a);this.hasAttribute("interacting")&&"Interaction"!==d&&(d=void 0);if(d)this[`_on${d}KeyDown`](a,c)}_ensureScrolledToIndex(a){Array.from(this.$.items.children).filter(c=>c.index===a)[0]||this._scrollToIndex(a)}_onNavigationKeyDown(a,c){a.preventDefault();var d=this._lastVisibleIndex-this._firstVisibleIndex-1,e=0,f=0;switch(c){case "ArrowRight":e=this.__isRTL?-1:1;break;case "ArrowLeft":e=this.__isRTL?1:-1;break;
case "Home":e=-Infinity;a.ctrlKey&&(f=-Infinity);break;case "End":e=Infinity;a.ctrlKey&&(f=Infinity);break;case "ArrowDown":f=1;break;case "ArrowUp":f=-1;break;case "PageDown":f=d;break;case "PageUp":f=-d}a=a.composedPath()[0];var g=Array.prototype.indexOf.call(a.parentNode.children,a);c=this._elementMatches(a,'[part~\x3d"details-cell"]');var h=a.parentNode;a=h.parentNode;var k=(a===this.$.items?this._effectiveSize:a.children.length)-1,l=a===this.$.items?void 0!==this._focusedItemIndex?this._focusedItemIndex:
h.index:Array.prototype.indexOf.call(h.parentNode.children,h);let m=Math.max(0,Math.min(l+f,k));d=!1;a===this.$.items&&(d=h._item,h=this._cache.getItemForIndex(m),d=c?0===f:1===f&&this._isDetailsOpened(d)||-1===f&&m!==l&&this._isDetailsOpened(h),d!==c&&(1===f&&d||-1===f&&!d)&&(m=l));if(a!==this.$.items)if(m>l)for(;m<k&&a.children[m].hidden;)m++;else if(m<l)for(;0<m&&a.children[m].hidden;)m--;void 0===this._focusedColumnOrder&&(this._focusedColumnOrder=c?0:this._getColumns(a,l).filter(n=>!n.hidden)[g]._order);
g=this._getColumns(a,m).filter(n=>!n.hidden);k=g.map(n=>n._order).sort((n,p)=>n-p);h=k.length-1;l=k.indexOf(k.slice(0).sort((n,p)=>Math.abs(n-this._focusedColumnOrder)-Math.abs(p-this._focusedColumnOrder))[0]);e=0===f&&c?l:Math.max(0,Math.min(l+e,h));e!==l&&(this._focusedColumnOrder=void 0);a===this.$.items&&this._ensureScrolledToIndex(m);this._toggleAttribute("navigating",!0,this);e=g.reduce((n,p,r)=>(n[p._order]=r,n),{})[k[e]];if(f=a===this.$.items?Array.from(a.children).filter(n=>n.index===m)[0]:
a.children[m])e=d?Array.from(f.children).filter(n=>this._elementMatches(n,'[part~\x3d"details-cell"]'))[0]:f.children[e],this._scrollHorizontallyToCell(e),a===this.$.items&&(this._focusedItemIndex=m),a===this.$.items&&(f=e.getBoundingClientRect(),a=this.$.footer.getBoundingClientRect().top,c=this.$.header.getBoundingClientRect().bottom,f.bottom>a?(this.$.table.scrollTop+=f.bottom-a,this._scrollHandler()):f.top<c&&(this.$.table.scrollTop-=c-f.top,this._scrollHandler())),e.focus()}_parseEventPath(a){const c=
a.indexOf(this.$.table);return{rowGroup:a[c-1],row:a[c-2],cell:a[c-3]}}_onInteractionKeyDown(a,c){var d=a.composedPath()[0];d="input"===d.localName&&!/^(button|checkbox|color|file|image|radio|range|reset|submit)$/i.test(d.type);switch(c){case "Enter":var e=this.hasAttribute("interacting")?!d:!0;break;case "Escape":e=!1;break;case "F2":e=!this.hasAttribute("interacting")}({cell:c}=this._parseEventPath(a.composedPath()));if(this.hasAttribute("interacting")!==e)if(e){if(e=c._content.querySelector("[focus-target]")||
c._content.firstElementChild)a.preventDefault(),e.focus(),this._toggleAttribute("interacting",!0,this),this._toggleAttribute("navigating",!1,this)}else a.preventDefault(),this._focusedColumnOrder=void 0,c.focus(),this._toggleAttribute("interacting",!1,this),this._toggleAttribute("navigating",!0,this)}_predictFocusStepTarget(a,c){const d=[this.$.table,this._headerFocusable,this._itemsFocusable,this._footerFocusable,this.$.focusexit];a=d.indexOf(a);for(a+=c;0<=a&&a<=d.length-1&&(!d[a]||d[a].parentNode.hidden);)a+=
c;return d[a]}_onTabKeyDown(a){var c=this._predictFocusStepTarget(a.composedPath()[0],a.shiftKey?-1:1);if(c===this.$.table)this.$.table.focus();else if(c===this.$.focusexit)this.$.focusexit.focus();else if(c===this._itemsFocusable){var d=this._itemsFocusable.parentNode;this._ensureScrolledToIndex(this._focusedItemIndex);if(d.index!==this._focusedItemIndex){d=Array.from(d.children).indexOf(this._itemsFocusable);const e=Array.from(this.$.items.children).filter(f=>f.index===this._focusedItemIndex)[0];
e&&(c=e.children[d])}a.preventDefault();c.focus()}else a.preventDefault(),c.focus();this._toggleAttribute("navigating",!0,this)}_onSpaceKeyDown(a){a.preventDefault();a=a.composedPath()[0];a._content&&a._content.firstElementChild||this.dispatchEvent(new CustomEvent("cell-activate",{detail:{model:this.__getRowModel(a.parentElement)}}))}_onKeyUp(a){if(/^( |SpaceBar)$/.test(a.key)&&(a.preventDefault(),a=a.composedPath()[0],a._content&&a._content.firstElementChild)){const c=this.hasAttribute("navigating");
a._content.firstElementChild.click();this._toggleAttribute("navigating",c,this)}}_onFocusIn(a){this._isMousedown||this._toggleAttribute("navigating",!0,this);const c=a.composedPath()[0];c===this.$.table||c===this.$.focusexit?(this._predictFocusStepTarget(c,c===this.$.table?1:-1).focus(),this._toggleAttribute("interacting",!1,this)):this._detectInteracting(a)}_onFocusOut(a){this._toggleAttribute("navigating",!1,this);this._detectInteracting(a)}_onCellFocusIn(a){this._detectInteracting(a);if(3===a.composedPath().indexOf(this.$.table)){const c=
a.composedPath()[0];this._activeRowGroup=c.parentNode.parentNode;this._activeRowGroup===this.$.header?this._headerFocusable=c:this._activeRowGroup===this.$.items?this._itemsFocusable=c:this._activeRowGroup===this.$.footer&&(this._footerFocusable=c);c._content.dispatchEvent(new CustomEvent("cell-focusin",{bubbles:!1}))}this._detectFocusedItemIndex(a)}_onCellFocusOut(a){3===a.composedPath().indexOf(this.$.table)&&a.composedPath()[0]._content.dispatchEvent(new CustomEvent("cell-focusout",{bubbles:!1}))}_detectInteracting(a){this._toggleAttribute("interacting",
a.composedPath().some(c=>"vaadin-grid-cell-content"===c.localName),this)}_detectFocusedItemIndex(a){const {rowGroup:c,row:d}=this._parseEventPath(a.composedPath());c===this.$.items&&(this._focusedItemIndex=d.index)}_preventScrollerRotatingCellFocus(a,c){a.index===this._focusedItemIndex&&this.hasAttribute("navigating")&&this._activeRowGroup===this.$.items&&(this._navigatingIsHidden=!0,this._toggleAttribute("navigating",!1,this));c===this._focusedItemIndex&&this._navigatingIsHidden&&(this._navigatingIsHidden=
!1,this._toggleAttribute("navigating",!0,this))}_getColumns(a,c){let d=this._columnTree.length-1;a===this.$.header?d=c:a===this.$.footer&&(d=this._columnTree.length-1-c);return this._columnTree[d]}_resetKeyboardNavigation(){this.$.header.firstElementChild&&(this._headerFocusable=Array.from(this.$.header.firstElementChild.children).filter(a=>!a.hidden)[0]);if(this.$.items.firstElementChild){const a=this._iterateItems((c,d)=>{if(this._firstVisibleIndex===d)return this.$.items.children[c]});a&&(this._itemsFocusable=
Array.from(a.children).filter(c=>!c.hidden)[0])}this.$.footer.firstElementChild&&(this._footerFocusable=Array.from(this.$.footer.firstElementChild.children).filter(a=>!a.hidden)[0])}_scrollHorizontallyToCell(a){if(!a.hasAttribute("frozen")&&!this._elementMatches(a,'[part~\x3d"details-cell"]')){var c=a.getBoundingClientRect(),d=a.parentNode,e=Array.from(d.children).indexOf(a),f=this.$.table.getBoundingClientRect();a=f.left;f=f.right;for(var g=e-1;0<=g;g--){const h=d.children[g];if(!h.hasAttribute("hidden")&&
!this._elementMatches(h,'[part~\x3d"details-cell"]')&&h.hasAttribute("frozen")){a=h.getBoundingClientRect().right;break}}for(e+=1;e<d.children.length;e++)if(g=d.children[e],!g.hasAttribute("hidden")&&!this._elementMatches(g,'[part~\x3d"details-cell"]')&&g.hasAttribute("frozen")){f=g.getBoundingClientRect().left;break}c.left<a&&(this.$.table.scrollLeft+=Math.round(c.left-a));c.right>f&&(this.$.table.scrollLeft+=Math.round(c.right-f))}}_elementMatches(a,c){return a.matches?a.matches(c):-1!==Array.from(a.parentNode.querySelectorAll(c)).indexOf(a)}},
ig=b=>class extends Da(b){static get properties(){return{columnReorderingAllowed:{type:Boolean,value:!1},_orderBaseScope:{type:Number,value:1E7}}}static get observers(){return["_updateOrders(_columnTree, _columnTree.*)"]}ready(){super.ready();vb(this,"track",this._onTrackEvent);this._reorderGhost=this.shadowRoot.querySelector('[part\x3d"reorder-ghost"]');this.addEventListener("touchstart",this._onTouchStart.bind(this));this.addEventListener("touchmove",this._onTouchMove.bind(this));this.addEventListener("touchend",
this._onTouchEnd.bind(this));this.addEventListener("contextmenu",this._onContextMenu.bind(this))}_onContextMenu(a){this.hasAttribute("reordering")&&a.preventDefault()}_onTouchStart(a){this._startTouchReorderTimeout=setTimeout(()=>{this._onTrackStart({detail:{x:a.touches[0].clientX,y:a.touches[0].clientY}})},100)}_onTouchMove(a){this._draggedColumn&&a.preventDefault();clearTimeout(this._startTouchReorderTimeout)}_onTouchEnd(){clearTimeout(this._startTouchReorderTimeout);this._onTrackEnd()}_onTrackEvent(a){if("start"===
a.detail.state){var c=a.composedPath();const d=c[c.indexOf(this.$.header)-2];d&&d._content&&(c=this.getRootNode().activeElement,(!d._content.contains(this.getRootNode().activeElement)||this._ie&&this._isFocusable(c))&&!this.$.scroller.hasAttribute("column-resizing")&&(this._touchDevice||this._onTrackStart(a)))}else"track"===a.detail.state?this._onTrack(a):"end"===a.detail.state&&this._onTrackEnd(a)}_onTrackStart(a){if(this.columnReorderingAllowed){var c=a.path||N(a).path;if((!c||!c.filter(d=>d.hasAttribute&&
d.hasAttribute("draggable"))[0])&&(c=this._cellFromPoint(a.detail.x,a.detail.y))&&-1!==c.getAttribute("part").indexOf("header-cell")){this._toggleAttribute("reordering",!0,this);for(this._draggedColumn=c._column;1===this._draggedColumn.parentElement.childElementCount;)this._draggedColumn=this._draggedColumn.parentElement;this._setSiblingsReorderStatus(this._draggedColumn,"allowed");this._draggedColumn._reorderStatus="dragging";this._updateGhost(c);this._reorderGhost.style.visibility="visible";this._updateGhostPosition(a.detail.x,
this._touchDevice?a.detail.y-50:a.detail.y);this._autoScroller()}}}_onTrack(a){if(this._draggedColumn){var c=this._cellFromPoint(a.detail.x,a.detail.y);c&&(c=this._getTargetColumn(c,this._draggedColumn),this._isSwapAllowed(this._draggedColumn,c)&&this._isSwappableByPosition(c,a.detail.x)&&this._swapColumnOrders(this._draggedColumn,c),this._updateGhostPosition(a.detail.x,this._touchDevice?a.detail.y-50:a.detail.y),this._lastDragClientX=a.detail.x)}}_onTrackEnd(){this._draggedColumn&&(this._toggleAttribute("reordering",
!1,this),this._draggedColumn._reorderStatus="",this._setSiblingsReorderStatus(this._draggedColumn,""),this._lastDragClientX=this._draggedColumn=null,this._reorderGhost.style.visibility="hidden",this.dispatchEvent(new CustomEvent("column-reorder",{detail:{columns:this._getColumnsInOrder()}})))}_getColumnsInOrder(){return this._columnTree.slice(0).pop().filter(a=>!a.hidden).sort((a,c)=>a._order-c._order)}_cellFromPoint(a,c){a=a||0;c=c||0;this._draggedColumn||this._toggleAttribute("no-content-pointer-events",
!0,this.$.scroller);Ra?a=this.shadowRoot.elementFromPoint(a,c):(a=document.elementFromPoint(a,c),"vaadin-grid-cell-content"===a.localName&&(a=a.assignedSlot.parentNode));this._toggleAttribute("no-content-pointer-events",!1,this.$.scroller);if(a&&a._column)return a}_updateGhostPosition(a,c){const d=this._reorderGhost.getBoundingClientRect();a-=d.width/2;c-=d.height/2;const e=parseInt(this._reorderGhost._left||0),f=parseInt(this._reorderGhost._top||0);this._reorderGhost._left=e-(d.left-a);this._reorderGhost._top=
f-(d.top-c);this._reorderGhost.style.transform=`translate(${this._reorderGhost._left}px, ${this._reorderGhost._top}px)`}_getInnerText(a){return a.localName?"none"===getComputedStyle(a).display?"":Array.from(a.childNodes).map(c=>this._getInnerText(c)).join(""):a.textContent}_updateGhost(a){const c=this._reorderGhost;c.textContent=this._getInnerText(a._content);const d=window.getComputedStyle(a);"boxSizing display width height background alignItems padding border flex-direction overflow".split(" ").forEach(e=>
c.style[e]=d[e]);return c}_updateOrders(a,c){void 0!==a&&void 0!==c&&(a[0].forEach((d,e)=>d._order=0),a[0].forEach((d,e)=>d._order=(e+1)*this._orderBaseScope))}_setSiblingsReorderStatus(a,c){Array.from(a.parentNode.children).filter(d=>/column/.test(d.localName)&&this._isSwapAllowed(d,a)).forEach(d=>d._reorderStatus=c)}_autoScroller(){if(this._lastDragClientX){const a=this._lastDragClientX-this.getBoundingClientRect().right+50,c=this.getBoundingClientRect().left-this._lastDragClientX+50;0<a?this.$.table.scrollLeft+=
a/10:0<c&&(this.$.table.scrollLeft-=c/10);this._scrollHandler()}this._draggedColumn&&this.async(this._autoScroller,10)}_isSwapAllowed(a,c){if(a&&c){const d=a.parentElement===c.parentElement,e=a.frozen===c.frozen;return a!==c&&d&&e}}_isSwappableByPosition(a,c){var d=Array.from(this.$.header.querySelectorAll('tr:not([hidden]) [part~\x3d"cell"]')).filter(f=>a.contains(f._column))[0];const e=this.$.header.querySelector("tr:not([hidden]) [reorder-status\x3ddragging]").getBoundingClientRect();d=d.getBoundingClientRect();
return d.left>e.left?c>d.right-e.width:c<d.left+e.width}_swapColumnOrders(a,c){const d=a._order;a._order=c._order;c._order=d;this._updateLastFrozen();this._updateFirstAndLastColumn()}_getTargetColumn(a,c){if(a&&c){let d=a._column;for(;d.parentElement!==c.parentElement&&d!==this;)d=d.parentElement;return d.parentElement===c.parentElement?d:a._column}}},jg=b=>class extends b{static get properties(){return{resizable:{type:Boolean,value:function(){if("vaadin-grid-column-group"!==this.localName){var a=
this.parentNode;return a&&"vaadin-grid-column-group"===a.localName?a.resizable||!1:!1}}},_headerTemplate:{type:Object},_footerTemplate:{type:Object},frozen:{type:Boolean,value:!1},hidden:{type:Boolean},header:{type:String},textAlign:{type:String},_lastFrozen:{type:Boolean,value:!1},_order:Number,_reorderStatus:Boolean,_emptyCells:Array,_headerCell:Object,_footerCell:Object,_grid:Object,headerRenderer:Function,footerRenderer:Function}}static get observers(){return"_widthChanged(width, _headerCell, _footerCell, _cells.*);_frozenChanged(frozen, _headerCell, _footerCell, _cells.*);_flexGrowChanged(flexGrow, _headerCell, _footerCell, _cells.*);_pathOrHeaderChanged(path, header, _headerCell, _footerCell, _cells.*, renderer, headerRenderer, _bodyTemplate, _headerTemplate);_textAlignChanged(textAlign, _cells.*, _headerCell, _footerCell);_orderChanged(_order, _headerCell, _footerCell, _cells.*);_lastFrozenChanged(_lastFrozen);_setBodyTemplateOrRenderer(_bodyTemplate, renderer, _cells, _cells.*);_setHeaderTemplateOrRenderer(_headerTemplate, headerRenderer, _headerCell);_setFooterTemplateOrRenderer(_footerTemplate, footerRenderer, _footerCell);_resizableChanged(resizable, _headerCell);_reorderStatusChanged(_reorderStatus, _headerCell, _footerCell, _cells.*);_hiddenChanged(hidden, _headerCell, _footerCell, _cells.*)".split(";")}connectedCallback(){super.connectedCallback();
this._bodyTemplate&&(this._bodyTemplate.templatizer._grid=this._grid);this._headerTemplate&&(this._headerTemplate.templatizer._grid=this._grid);this._footerTemplate&&(this._footerTemplate.templatizer._grid=this._grid);this._templateObserver.flush();this._bodyTemplate||this._templateObserver.callback();requestAnimationFrame(()=>{this._allCells.forEach(a=>{a._content.parentNode||this._grid&&this._grid.appendChild(a._content)})})}disconnectedCallback(){super.disconnectedCallback();requestAnimationFrame(()=>
{this._findHostGrid()||this._allCells.forEach(a=>{a._content.parentNode&&a._content.parentNode.removeChild(a._content)})});this._gridValue=void 0}_findHostGrid(){let a=this;for(;a&&!/^vaadin.*grid(-pro)?$/.test(a.localName);)a=a.assignedSlot?a.assignedSlot.parentNode:a.parentNode;return a||void 0}get _grid(){this._gridValue||(this._gridValue=this._findHostGrid());return this._gridValue}get _allCells(){return[].concat(this._cells||[]).concat(this._emptyCells||[]).concat(this._headerCell).concat(this._footerCell).filter(a=>
a)}constructor(){super();this._templateObserver=new Z(this,a=>{this._headerTemplate=this._prepareHeaderTemplate();this._footerTemplate=this._prepareFooterTemplate();this._bodyTemplate=this._prepareBodyTemplate()})}_prepareHeaderTemplate(){return this._prepareTemplatizer(this._findTemplate(!0)||null,{})}_prepareFooterTemplate(){return this._prepareTemplatizer(this._findTemplate(!1,!0)||null,{})}_prepareBodyTemplate(){return this._prepareTemplatizer(this._findTemplate()||null)}_prepareTemplatizer(a,
c){if(a&&!a.templatizer){const d=new cb;d._grid=this._grid;d.dataHost=this.dataHost;d._instanceProps=c||d._instanceProps;d.template=a;a.templatizer=d}return a}_renderHeaderAndFooter(){this.headerRenderer&&this._headerCell&&this.__runRenderer(this.headerRenderer,this._headerCell);this.footerRenderer&&this._footerCell&&this.__runRenderer(this.footerRenderer,this._footerCell)}__runRenderer(a,c,d){c=[c._content,this];d&&d.item&&c.push(d);a.apply(this,c)}__setColumnTemplateOrRenderer(a,c,d){if(a&&c)throw Error("You should only use either a renderer or a template");
d.forEach(e=>{const f=this._grid.__getRowModel(e.parentElement);if(c)e._renderer=c,(f.item||c===this.headerRenderer||c===this.footerRenderer)&&this.__runRenderer(c,e,f);else if(e._template!==a){e._template=a;e._content.innerHTML="";a.templatizer._grid=a.templatizer._grid||this._grid;const g=a.templatizer.createInstance();e._content.appendChild(g.root);e._instance=g;f.item&&e._instance.setProperties(f)}})}_setBodyTemplateOrRenderer(a,c,d,e){(a||c)&&d&&this.__setColumnTemplateOrRenderer(a,c,d)}_setHeaderTemplateOrRenderer(a,
c,d){(a||c)&&d&&this.__setColumnTemplateOrRenderer(a,c,[d])}_setFooterTemplateOrRenderer(a,c,d){(a||c)&&d&&(this.__setColumnTemplateOrRenderer(a,c,[d]),this._grid.__updateHeaderFooterRowVisibility(d.parentElement))}_selectFirstTemplate(a=!1,c=!1){return Z.getFlattenedNodes(this).filter(d=>"template"===d.localName&&d.classList.contains("header")===a&&d.classList.contains("footer")===c)[0]}_findTemplate(a,c){(a=this._selectFirstTemplate(a,c))&&this.dataHost&&(a._rootDataHost=this.dataHost._rootDataHost||
this.dataHost);return a}_flexGrowChanged(a,c,d,e){this.parentElement&&this.parentElement._columnPropChanged&&this.parentElement._columnPropChanged("flexGrow");this._allCells.forEach(f=>f.style.flexGrow=a)}_orderChanged(a,c,d,e){this._allCells.forEach(f=>f.style.order=a)}_widthChanged(a,c,d,e){this.parentElement&&this.parentElement._columnPropChanged&&this.parentElement._columnPropChanged("width");this._allCells.forEach(f=>f.style.width=a);this._grid&&this._grid.__forceReflow&&this._grid.__forceReflow()}_frozenChanged(a,
c,d,e){this.parentElement&&this.parentElement._columnPropChanged&&this.parentElement._columnPropChanged("frozen",a);this._allCells.forEach(f=>this._toggleAttribute("frozen",a,f));this._grid&&this._grid._frozenCellsChanged&&this._grid._frozenCellsChanged()}_lastFrozenChanged(a){this._allCells.forEach(c=>this._toggleAttribute("last-frozen",a,c));this.parentElement&&this.parentElement._columnPropChanged&&(this.parentElement._lastFrozen=a)}_pathOrHeaderChanged(a,c,d,e,f,g,h,k,l){e=void 0!==c;!h&&!l&&
e&&d&&this.__setTextContent(d._content,c);a&&f.value&&(g||k||this.__setColumnTemplateOrRenderer(void 0,(m,n,{item:p})=>this.__setTextContent(m,this.get(a,p)),f.value),h||l||e||!d||null===c||this.__setTextContent(d._content,this._generateHeader(a)));d&&this._grid.__updateHeaderFooterRowVisibility(d.parentElement)}__setTextContent(a,c){a.textContent!==c&&(a.textContent=c)}_generateHeader(a){return a.substr(a.lastIndexOf(".")+1).replace(/([A-Z])/g,"-$1").toLowerCase().replace(/-/g," ").replace(/^./,
c=>c.toUpperCase())}_toggleAttribute(a,c,d){d.hasAttribute(a)===!c&&(c?d.setAttribute(a,""):d.removeAttribute(a))}_reorderStatusChanged(a,c,d,e){this._allCells.forEach(f=>f.setAttribute("reorder-status",a))}_resizableChanged(a,c){void 0!==a&&void 0!==c&&c&&[c].concat(this._emptyCells).forEach(d=>{if(d){var e=d.querySelector('[part~\x3d"resize-handle"]');e&&d.removeChild(e);a&&(e=document.createElement("div"),e.setAttribute("part","resize-handle"),d.appendChild(e))}})}_textAlignChanged(a,c,d,e){if(void 0!==
a)if(-1===["start","end","center"].indexOf(a))console.warn('textAlign can only be set as "start", "end" or "center"');else{var f;"ltr"===getComputedStyle(this._grid).direction?"start"===a?f="left":"end"===a&&(f="right"):"start"===a?f="right":"end"===a&&(f="left");this._allCells.forEach(g=>{g._content.style.textAlign=a;getComputedStyle(g._content).textAlign!==a&&(g._content.style.textAlign=f)})}}_hiddenChanged(a,c,d,e){this.parentElement&&this.parentElement._columnPropChanged&&this.parentElement._columnPropChanged("hidden",
a);!!a!==!!this._previousHidden&&this._grid&&(!0===a&&this._allCells.forEach(f=>{f._content.parentNode&&f._content.parentNode.removeChild(f._content)}),this._grid._debouncerHiddenChanged=v.debounce(this._grid._debouncerHiddenChanged,M,()=>{this._grid&&this._grid._renderColumnTree&&this._grid._renderColumnTree(this._grid._columnTree)}),this._grid._updateLastFrozen&&this._grid._updateLastFrozen(),this._grid.notifyResize&&this._grid.notifyResize(),this._grid._resetKeyboardNavigation&&this._grid._resetKeyboardNavigation());
this._previousHidden=a}};class db extends jg(Qb(I)){static get is(){return"vaadin-grid-column"}static get properties(){return{width:{type:String,value:"100px"},flexGrow:{type:Number,value:1},renderer:Function,path:{type:String},autoWidth:{type:Boolean,value:!1},_bodyTemplate:{type:Object},_cells:Array}}}customElements.define(db.is,db);class Pd extends class extends I{}{static get template(){return H`
    <style>
      :host {
        display: block;
        height: 100%;
        width: 100%;
        position: absolute;
        top: 0;
        box-sizing: border-box;
        overflow: auto;
      }

      :host([passthrough]) {
        pointer-events: none;
      }
    </style>

    <slot></slot>
`}static get is(){return"vaadin-grid-outer-scroller"}static get properties(){return{scrollTarget:{type:Object},scrollHandler:{type:Object},passthrough:{type:Boolean,reflectToAttribute:!0,value:!0},outerScrolling:Boolean,noScrollbars:Boolean,_touchDevice:Boolean}}ready(){super.ready();this.addEventListener("scroll",()=>this._syncScrollTarget());this.parentElement.addEventListener("mousemove",this._onMouseMove.bind(this));this.style.webkitOverflowScrolling="touch";this.addEventListener("mousedown",
b=>this.outerScrolling=!0);this.addEventListener("mouseup",b=>{this.outerScrolling=!1;this.scrollHandler._scrollHandler()})}_onMouseMove(b){this._touchDevice||(this.noScrollbars&&this.parentElement.hasAttribute("scroll-period")?this.passthrough=b.offsetY<=this.clientHeight-20&&b.offsetX<=this.clientWidth-20:this.passthrough=b.offsetY<=this.clientHeight&&b.offsetX<=this.clientWidth)}syncOuterScroller(){this.scrollTop=this.scrollTarget.scrollTop;this.scrollLeft=this.scrollTarget.scrollLeft}_syncScrollTarget(){requestAnimationFrame(()=>
{this.scrollTarget.scrollTop=this.scrollTop;this.scrollTarget.scrollLeft=this.scrollLeft;this.scrollHandler._scrollHandler()})}}customElements.define(Pd.is,Pd);const Wb=document.createElement("dom-module");Wb.appendChild(H`
  <style>
    @keyframes vaadin-grid-appear {
      to {
        opacity: 1;
      }
    }

    :host {
      display: block;
      animation: 1ms vaadin-grid-appear;
      height: 400px;
      flex: 1 1 auto;
      align-self: stretch;
      position: relative;
    }

    :host([hidden]) {
      display: none !important;
    }

    #scroller {
      display: block;
      transform: translateY(0);
      width: auto;
      height: auto;
      position: absolute;
      top: 0;
      right: 0;
      bottom: 0;
      left: 0;
    }

    :host([height-by-rows]) {
      height: auto;
      align-self: flex-start;
      flex-grow: 0;
      width: 100%;
    }

    :host([height-by-rows]) #scroller {
      width: 100%;
      height: 100%;
      position: relative;
    }

    #table {
      display: block;
      width: 100%;
      height: 100%;
      overflow: auto;
      z-index: -2;
      position: relative;
      outline: none;
    }

    #header {
      display: block;
      position: absolute;
      top: 0;
      width: 100%;
    }

    th {
      text-align: inherit;
    }

    /* Safari doesn't work with "inherit" */
    [safari] th {
      text-align: initial;
    }

    #footer {
      display: block;
      position: absolute;
      bottom: 0;
      width: 100%;
    }

    #items {
      display: block;
      width: 100%;
      position: relative;
      z-index: -1;
    }

    #items,
    #outersizer,
    #fixedsizer {
      border-top: 0 solid transparent;
      border-bottom: 0 solid transparent;
    }

    [part~="row"] {
      display: flex;
      width: 100%;
      box-sizing: border-box;
      margin: 0;
    }

    [part~="row"][loading] [part~="body-cell"] ::slotted(vaadin-grid-cell-content) {
      opacity: 0;
    }

    #items [part~="row"] {
      position: absolute;
    }

    #items [part~="row"]:empty {
      height: 1em;
    }

    [part~="cell"]:not([part~="details-cell"]) {
      flex-shrink: 0;
      flex-grow: 1;
      box-sizing: border-box;
      display: flex;
      width: 100%;
      position: relative;
      align-items: center;
      padding: 0;
      white-space: nowrap;
    }

    [part~="details-cell"] {
      position: absolute;
      bottom: 0;
      width: 100%;
      box-sizing: border-box;
      padding: 0;
    }

    [part~="cell"] ::slotted(vaadin-grid-cell-content) {
      display: block;
      width: 100%;
      box-sizing: border-box;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    [hidden] {
      display: none !important;
    }

    [frozen] {
      z-index: 2;
      will-change: transform;
    }

    #outerscroller {
      /* Needed (at least) for Android Chrome */
      z-index: 0;
    }

    #scroller:not([safari]) #outerscroller {
      /* Needed for Android Chrome (#1020). Can't be applied to Safari
      since it would re-introduce the sub-pixel overflow bug (#853) */
      will-change: transform;
    }

    [no-scrollbars]:not([safari]):not([firefox]) #outerscroller,
    [no-scrollbars][safari] #table,
    [no-scrollbars][firefox] #table {
      overflow: hidden;
    }

    [no-scrollbars]:not([safari]):not([firefox]) #outerscroller {
      pointer-events: none;
    }

    /* Reordering styles */
    :host([reordering]) [part~="cell"] ::slotted(vaadin-grid-cell-content),
    :host([reordering]) [part~="resize-handle"],
    #scroller[no-content-pointer-events] [part~="cell"] ::slotted(vaadin-grid-cell-content) {
      pointer-events: none;
    }

    [part~="reorder-ghost"] {
      visibility: hidden;
      position: fixed;
      pointer-events: none;
      opacity: 0.5;

      /* Prevent overflowing the grid in Firefox */
      top: 0;
      left: 0;
    }

    :host([reordering]) {
      -moz-user-select: none;
      -webkit-user-select: none;
      user-select: none;
    }

    #scroller[ie][column-reordering-allowed] [part~="header-cell"] {
      -ms-user-select: none;
    }

    :host([reordering]) #outerscroller {
      -webkit-overflow-scrolling: auto !important;
    }

    /* Resizing styles */
    [part~="resize-handle"] {
      position: absolute;
      top: 0;
      right: 0;
      height: 100%;
      cursor: col-resize;
      z-index: 1;
    }

    [part~="resize-handle"]::before {
      position: absolute;
      content: "";
      height: 100%;
      width: 35px;
      transform: translateX(-50%);
    }

    [last-column] [part~="resize-handle"]::before,
    [last-frozen] [part~="resize-handle"]::before {
      width: 18px;
      transform: none;
      right: 0;
    }

    #scroller[column-resizing] {
      -ms-user-select: none;
      -moz-user-select: none;
      -webkit-user-select: none;
      user-select: none;
    }

    /* Sizer styles */
    .sizer {
      display: flex;
      position: relative;
      width: 100%;
      visibility: hidden;
    }

    .sizer [part~="details-cell"] {
      display: none !important;
    }

    .sizer [part~="cell"][hidden] {
      display: none !important;
    }

    .sizer [part~="cell"] {
      display: block;
      flex-shrink: 0;
      line-height: 0;
      margin-top: -1em;
      height: 0 !important;
      min-height: 0 !important;
      max-height: 0 !important;
      padding: 0 !important;
    }

    .sizer [part~="cell"]::before {
      content: "-";
    }

    .sizer [part~="cell"] ::slotted(vaadin-grid-cell-content) {
      display: none !important;
    }

    /* Fixed mode (Tablet Edge) */
    #fixedsizer {
      position: absolute;
    }

    :not([edge][no-scrollbars]) #fixedsizer {
      display: none;
    }

    [edge][no-scrollbars] {
      /* Any value other than \u2018none\u2019 for the transform results in the creation of both a stacking context and
      a containing block. The object acts as a containing block for fixed positioned descendants. */
      transform: translateZ(0);
      overflow: hidden;
    }

    [edge][no-scrollbars] #header,
    [edge][no-scrollbars] #footer {
      position: fixed;
    }

    [edge][no-scrollbars] #items {
      position: fixed;
      width: 100%;
      will-change: transform;
    }

    /* RTL specific styles */

    :host([dir="rtl"]) [part~="reorder-ghost"] {
      left: auto;
      right: 0;
    }

    :host([dir="rtl"]) [part~="resize-handle"] {
      left: 0;
      right: auto;
    }

    :host([dir="rtl"]) [part~="resize-handle"]::before {
      transform: translateX(50%);
    }

    :host([dir="rtl"]) [last-column] [part~="resize-handle"]::before,
    :host([dir="rtl"]) [last-frozen] [part~="resize-handle"]::before {
      left: 0;
      right: auto;
    }
  </style>
`);const kg=/^((?!chrome|android).)*safari/i.test(navigator.userAgent),lg=-1<navigator.userAgent.toLowerCase().indexOf("firefox");if(kg||lg){const b=document.createElement("style");b.textContent="\n    [scrolling][safari] #outerscroller,\n    [scrolling][firefox] #outerscroller {\n      pointer-events: auto;\n    }\n\n    [ios] #outerscroller {\n      pointer-events: auto;\n      z-index: -3;\n    }\n\n    [ios][scrolling] #outerscroller {\n      z-index: 0;\n    }\n\n    [ios] [frozen] {\n      will-change: auto;\n    }\n  ";
Wb.querySelector("template").content.appendChild(b)}Wb.register("vaadin-grid-styles");try{document.createEvent("TouchEvent");var Qd=!0}catch(b){Qd=!1}const mg=Qd;class Rd extends yd(Nb(Yf(Wf(Zf(Vf(cg(dg(eg(bg(hg(Uf(ag(ig(Xf($f(gg(fg(Vb)))))))))))))))))){static get template(){return H`
    <style include="vaadin-grid-styles"></style>

    <div id="scroller" no-scrollbars\$="[[!_scrollbarWidth]]" wheel-scrolling\$="[[_wheelScrolling]]" safari\$="[[_safari]]" ios\$="[[_ios]]" loading\$="[[loading]]" edge\$="[[_edge]]" firefox\$="[[_firefox]]" ie\$="[[_ie]]" column-reordering-allowed\$="[[columnReorderingAllowed]]">

      <table id="table" role="grid" aria-multiselectable="true" tabindex="0">
        <caption id="fixedsizer" class="sizer" part="row"></caption>
        <thead id="header" role="rowgroup"></thead>
        <tbody id="items" role="rowgroup"></tbody>
        <tfoot id="footer" role="rowgroup"></tfoot>
      </table>

      <div part="reorder-ghost"></div>
      <vaadin-grid-outer-scroller id="outerscroller" _touch-device="[[_touchDevice]]" scroll-target="[[scrollTarget]]" scroll-handler="[[_this]]" no-scrollbars="[[!_scrollbarWidth]]">
        <div id="outersizer" class="sizer" part="row"></div>
      </vaadin-grid-outer-scroller>
    </div>

    <!-- The template needs at least one slot or else shady doesn't distribute -->
    <slot name="nodistribute"></slot>

    <div id="focusexit" tabindex="0"></div>
`}static get is(){return"vaadin-grid"}static get version(){return"5.6.6"}static get observers(){return["_columnTreeChanged(_columnTree, _columnTree.*)"]}static get properties(){return{_this:{type:Object,value:function(){return this}},_safari:{type:Boolean,value:/^((?!chrome|android).)*safari/i.test(navigator.userAgent)},_ios:{type:Boolean,value:/iPad|iPhone|iPod/.test(navigator.userAgent)&&!window.MSStream||"MacIntel"===navigator.platform&&1<navigator.maxTouchPoints},_edge:{type:Boolean,value:"undefined"!==
typeof CSS&&CSS.supports("(-ms-ime-align:auto)")},_ie:{type:Boolean,value:!(!navigator.userAgent.match(/Trident/)||navigator.userAgent.match(/MSIE/))},_firefox:{type:Boolean,value:-1<navigator.userAgent.toLowerCase().indexOf("firefox")},_android:{type:Boolean,value:/android/i.test(navigator.userAgent)},_touchDevice:{type:Boolean,value:mg},heightByRows:{type:Boolean,value:!1,reflectToAttribute:!0,observer:"_heightByRowsChanged"},_recalculateColumnWidthOnceLoadingFinished:{type:Boolean,value:!0}}}constructor(){super();
this.addEventListener("animationend",this._onAnimationEnd)}connectedCallback(){super.connectedCallback();this.recalculateColumnWidths()}attributeChangedCallback(b,a,c){super.attributeChangedCallback(b,a,c);"dir"===b&&(this.__isRTL="rtl"===c,this._updateScrollerMeasurements())}__hasRowsWithClientHeight(){return!!Array.from(this.$.items.children).filter(b=>b.clientHeight).length}__itemsReceived(){this._recalculateColumnWidthOnceLoadingFinished&&!this._cache.isLoading()&&this.__hasRowsWithClientHeight()&&
(this._recalculateColumnWidthOnceLoadingFinished=!1,this.recalculateColumnWidths())}_recalculateColumnWidths(b){b.forEach(a=>{a.width="auto";a._origFlexGrow=a.flexGrow;a.flexGrow=0});b.forEach(a=>{a._currentWidth=0;a._allCells.forEach(c=>{a._currentWidth=Math.max(a._currentWidth,c.offsetWidth+1)})});b.forEach(a=>{a.width=`${a._currentWidth}px`;a.flexGrow=a._origFlexGrow;a._currentWidth=void 0;a._origFlexGrow=void 0})}recalculateColumnWidths(){if(this._columnTree)if(this._cache.isLoading())this._recalculateColumnWidthOnceLoadingFinished=
!0;else{const b=this._getColumns().filter(a=>!a.hidden&&a.autoWidth);this._recalculateColumnWidths(b)}}_createScrollerRows(b){const a=[];for(var c=0;c<b;c++){const d=document.createElement("tr");d.setAttribute("part","row");d.setAttribute("role","row");this._columnTree&&this._updateRow(d,this._columnTree[this._columnTree.length-1],"body",!1,!0);a.push(d)}this._columnTree&&this._columnTree[this._columnTree.length-1].forEach(d=>d.notifyPath&&d.notifyPath("_cells.*",d._cells));Me(this,()=>{this._updateFirstAndLastColumn();
this._resetKeyboardNavigation()});return a}_getRowTarget(){return this.$.items}_createCell(b){const a="vaadin-grid-cell-content-"+(this._contentIndex=this._contentIndex+1||0),c=document.createElement("vaadin-grid-cell-content");c.setAttribute("slot",a);const d=document.createElement(b);d.id=a.replace("-content-","-");d.setAttribute("tabindex","-1");d.setAttribute("role","td"===b?"gridcell":"columnheader");b=document.createElement("slot");b.setAttribute("name",a);d.appendChild(b);d._content=c;c.addEventListener("mousedown",
()=>{if(window.chrome){const e=()=>{c.contains(this.getRootNode().activeElement)||d.focus();document.removeEventListener("mouseup",e,!0)};document.addEventListener("mouseup",e,!0)}else setTimeout(()=>{c.contains(this.getRootNode().activeElement)||d.focus()})});return d}_updateRow(b,a,c,d,e){c=c||"body";const f=document.createDocumentFragment();Array.from(b.children).forEach(g=>g._vacant=!0);b.innerHTML="";"outersizer"!==b.id&&"fixedsizer"!==b.id&&(b.hidden=!0);a.filter(g=>!g.hidden).forEach((g,h,
k)=>{if("body"===c){g._cells=g._cells||[];var l=g._cells.filter(m=>m._vacant)[0];l||(l=this._createCell("td"),g._cells.push(l));l.setAttribute("part","cell body-cell");b.appendChild(l);h===k.length-1&&(this._rowDetailsTemplate||this.rowDetailsRenderer)&&(this._detailsCells=this._detailsCells||[],h=this._detailsCells.filter(m=>m._vacant)[0]||this._createCell("td"),-1===this._detailsCells.indexOf(h)&&this._detailsCells.push(h),h._content.parentElement||f.appendChild(h._content),this._configureDetailsCell(h),
b.appendChild(h),this._a11ySetRowDetailsCell(b,h),h._vacant=!1);g.notifyPath&&!e&&g.notifyPath("_cells.*",g._cells)}else l="header"===c?"th":"td",d||"vaadin-grid-column-group"===g.localName?(l=g[`_${c}Cell`]||this._createCell(l),l._column=g,b.appendChild(l),g[`_${c}Cell`]=l):(g._emptyCells=g._emptyCells||[],l=g._emptyCells.filter(m=>m._vacant)[0]||this._createCell(l),l._column=g,b.appendChild(l),-1===g._emptyCells.indexOf(l)&&g._emptyCells.push(l)),l.setAttribute("part",`cell ${c}-cell`),this.__updateHeaderFooterRowVisibility(b);
l._content.parentElement||f.appendChild(l._content);l._vacant=!1;l._column=g});this.appendChild(f);this._frozenCellsChanged();this._updateFirstAndLastColumnForRow(b)}__updateHeaderFooterRowVisibility(b){if(b){var a=Array.from(b.children).filter(c=>{const d=c._column;if(d._emptyCells&&-1<d._emptyCells.indexOf(c))return!1;if(b.parentElement===this.$.header){if(d.headerRenderer||d._headerTemplate)return!0;if(null===d.header)return!1;if(d.path||void 0!==d.header)return!0}else if(d.footerRenderer||d._footerTemplate)return!0});
b.hidden!==!a.length&&(b.hidden=!a.length,this.notifyResize())}}_updateScrollerItem(b,a){this._preventScrollerRotatingCellFocus(b,a);this._columnTree&&(this._toggleAttribute("first",0===a,b),this._toggleAttribute("odd",a%2,b),this._a11yUpdateRowRowindex(b,a),this._getItem(a,b))}_columnTreeChanged(b,a){this._renderColumnTree(b);this.recalculateColumnWidths()}_renderColumnTree(b){for(Array.from(this.$.items.children).forEach(c=>this._updateRow(c,b[b.length-1],null,!1,!0));this.$.header.children.length<
b.length;){var a=document.createElement("tr");a.setAttribute("part","row");a.setAttribute("role","row");this.$.header.appendChild(a);a=document.createElement("tr");a.setAttribute("part","row");a.setAttribute("role","row");this.$.footer.appendChild(a)}for(;this.$.header.children.length>b.length;)this.$.header.removeChild(this.$.header.firstElementChild),this.$.footer.removeChild(this.$.footer.firstElementChild);Array.from(this.$.header.children).forEach((c,d)=>this._updateRow(c,b[d],"header",d===b.length-
1));Array.from(this.$.footer.children).forEach((c,d)=>this._updateRow(c,b[b.length-1-d],"footer",0===d));this._updateRow(this.$.outersizer,b[b.length-1],null,!1,!0);this._updateRow(this.$.fixedsizer,b[b.length-1]);this._resizeHandler();this._frozenCellsChanged();this._updateFirstAndLastColumn();this._resetKeyboardNavigation();this._a11yUpdateHeaderRows();this._a11yUpdateFooterRows()}_updateItem(b,a){b._item=a;const c=this.__getRowModel(b);this._toggleAttribute("selected",c.selected,b);this._a11yUpdateRowSelected(b,
c.selected);this._a11yUpdateRowLevel(b,c.level);this._toggleAttribute("expanded",c.expanded,b);(this._rowDetailsTemplate||this.rowDetailsRenderer)&&this._toggleDetailsCell(b,a);this._generateCellClassNames(b,c);this._filterDragAndDrop(b,c);Array.from(b.children).forEach(d=>{if(d._renderer){const e=d._column||this;d._renderer.call(e,d._content,e,c)}else d._instance&&(d._instance.__detailsOpened__=c.detailsOpened,d._instance.__selected__=c.selected,d._instance.__level__=c.level,d._instance.__expanded__=
c.expanded,d._instance.setProperties(c))});this._debouncerUpdateHeights=v.debounce(this._debouncerUpdateHeights,E.after(1),()=>{this._updateMetrics();this._positionItems();this._updateScrollerSize()})}_resizeHandler(){this._updateDetailsCellHeights();this._accessIronListAPI(super._resizeHandler,!0);this._updateScrollerMeasurements();this._updateHeaderFooterMetrics()}_updateHeaderFooterMetrics(){const b=this.$.header.clientHeight+"px",a=this.$.footer.clientHeight+"px";[this.$.outersizer,this.$.fixedsizer,
this.$.items].forEach(c=>{c.style.borderTopWidth=b;c.style.borderBottomWidth=a});Mc(this.$.header,()=>{this._pendingScrollToIndex&&this._scrollToIndex(this._pendingScrollToIndex)})}_onAnimationEnd(b){0===b.animationName.indexOf("vaadin-grid-appear")&&(this._render(),this._updateHeaderFooterMetrics(),b.stopPropagation(),this.notifyResize(),this.__itemsReceived())}_toggleAttribute(b,a,c){c.hasAttribute(b)===!a&&(a?c.setAttribute(b,""):c.removeAttribute(b))}__getRowModel(b){return{index:b.index,item:b._item,
level:this._getIndexLevel(b.index),expanded:this._isExpanded(b._item),selected:this._isSelected(b._item),detailsOpened:!(!this._rowDetailsTemplate&&!this.rowDetailsRenderer)&&this._isDetailsOpened(b._item)}}render(){this._columnTree&&(this._columnTree.forEach(b=>{b.forEach(a=>a._renderHeaderAndFooter())}),this._update())}notifyResize(){super.notifyResize()}_heightByRowsChanged(b,a){(b||a)&&this.notifyResize()}__forceReflow(){this._debouncerForceReflow=v.debounce(this._debouncerForceReflow,M,()=>{this.$.scroller.style.overflow=
"hidden";setTimeout(()=>this.$.scroller.style.overflow="")})}}customElements.define(Rd.is,Rd);class Sd extends db{static get template(){return H`
    <template class="header" id="defaultHeaderTemplate">
      <vaadin-checkbox class="vaadin-grid-select-all-checkbox" aria-label="Select All" hidden\$="[[_selectAllHidden]]" on-checked-changed="_onSelectAllCheckedChanged" checked="[[_isChecked(selectAll, _indeterminate)]]" indeterminate="[[_indeterminate]]"></vaadin-checkbox>
    </template>
    <template id="defaultBodyTemplate">
      <vaadin-checkbox aria-label="Select Row" checked="{{selected}}"></vaadin-checkbox>
    </template>
`}static get is(){return"vaadin-grid-selection-column"}static get properties(){return{width:{type:String,value:"58px"},flexGrow:{type:Number,value:0},selectAll:{type:Boolean,value:!1,notify:!0},autoSelect:{type:Boolean,value:!1},_indeterminate:Boolean,_previousActiveItem:Object,_selectAllHidden:Boolean}}static get observers(){return["_onSelectAllChanged(selectAll)"]}_pathOrHeaderChanged(b,a,c,d,e,f,g,h,k){!e.value||void 0===b&&void 0===f||(this._bodyTemplate=h=void 0,this.__cleanCellsOfTemplateProperties(e.value));
!c||void 0===a&&void 0===g||(this._headerTemplate=k=void 0,this.__cleanCellsOfTemplateProperties([c]));super._pathOrHeaderChanged(b,a,c,d,e,f,g,h,k)}__cleanCellsOfTemplateProperties(b){b.forEach(a=>{a._content.innerHTML="";delete a._instance;delete a._template})}_prepareHeaderTemplate(){const b=this._prepareTemplatizer(this._findTemplate(!0)||this.$.defaultHeaderTemplate);b.templatizer.dataHost=b===this.$.defaultHeaderTemplate?this:this.dataHost;return b}_prepareBodyTemplate(){const b=this._prepareTemplatizer(this._findTemplate()||
this.$.defaultBodyTemplate);b.templatizer.dataHost=b===this.$.defaultBodyTemplate?this:this.dataHost;return b}constructor(){super();this._boundOnActiveItemChanged=this._onActiveItemChanged.bind(this);this._boundOnDataProviderChanged=this._onDataProviderChanged.bind(this);this._boundOnSelectedItemsChanged=this._onSelectedItemsChanged.bind(this)}disconnectedCallback(){this._grid.removeEventListener("active-item-changed",this._boundOnActiveItemChanged);this._grid.removeEventListener("data-provider-changed",
this._boundOnDataProviderChanged);this._grid.removeEventListener("filter-changed",this._boundOnSelectedItemsChanged);this._grid.removeEventListener("selected-items-changed",this._boundOnSelectedItemsChanged);if(/^((?!chrome|android).)*safari/i.test(navigator.userAgent)&&window.ShadyDOM&&this.parentElement){const b=this.parentElement,a=this.nextElementSibling;b.removeChild(this);a?b.insertBefore(this,a):b.appendChild(this)}super.disconnectedCallback()}connectedCallback(){super.connectedCallback();
this._grid&&(this._grid.addEventListener("active-item-changed",this._boundOnActiveItemChanged),this._grid.addEventListener("data-provider-changed",this._boundOnDataProviderChanged),this._grid.addEventListener("filter-changed",this._boundOnSelectedItemsChanged),this._grid.addEventListener("selected-items-changed",this._boundOnSelectedItemsChanged))}_onSelectAllChanged(b){void 0!==b&&this._grid&&!this._selectAllChangeLock&&(this._grid.selectedItems=b&&Array.isArray(this._grid.items)?this._grid._filter(this._grid.items):
[])}_arrayContains(b,a){for(var c=0;b&&a&&a[c]&&0<=b.indexOf(a[c]);c++);return c==a.length}_onSelectAllCheckedChanged(b){this.selectAll=this._indeterminate||b.target.checked}_isChecked(b,a){return a||b}_onActiveItemChanged(b){b=b.detail.value;if(this.autoSelect){const a=b||this._previousActiveItem;a&&this._grid._toggleItem(a)}this._previousActiveItem=b}_onSelectedItemsChanged(b){this._selectAllChangeLock=!0;Array.isArray(this._grid.items)&&(this._grid.selectedItems.length?this._arrayContains(this._grid.selectedItems,
this._grid._filter(this._grid.items))?(this.selectAll=!0,this._indeterminate=!1):(this.selectAll=!1,this._indeterminate=!0):this._indeterminate=this.selectAll=!1);this._selectAllChangeLock=!1}_onDataProviderChanged(b){this._selectAllHidden=!Array.isArray(this._grid.items)}}customElements.define(Sd.is,Sd);const ng=H`<dom-module id="lumo-grid-sorter" theme-for="vaadin-grid-sorter">
  <template>
    <style>
      :host {
        justify-content: flex-start;
        align-items: baseline;
        -webkit-user-select: none;
        -moz-user-select: none;
        user-select: none;
      }

      [part="content"] {
        display: inline-block;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      [part="indicators"] {
        margin-left: var(--lumo-space-s);
      }

      :host(:not([direction])) [part="indicators"]::before {
        opacity: 0.2;
      }

      :host([direction]) {
        color: var(--lumo-primary-text-color);
      }

      [part="order"] {
        font-size: var(--lumo-font-size-xxs);
        line-height: 1;
      }

      /* RTL specific styles */

      :host([dir="rtl"]) [part="indicators"] {
        margin-right: var(--lumo-space-s);
        margin-left: 0;
      }
    </style>
  </template>
</dom-module>`;document.head.appendChild(ng.content);const Td=document.createElement("template");Td.innerHTML="\x3ccustom-style\x3e\n  \x3cstyle\x3e\n    @font-face {\n      font-family: 'vaadin-grid-sorter-icons';\n      src: url(data:application/font-woff;charset\x3dutf-8;base64,d09GRgABAAAAAAQwAA0AAAAABuwAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAABGRlRNAAAEFAAAABkAAAAcfep+mUdERUYAAAP4AAAAHAAAAB4AJwAOT1MvMgAAAZgAAAA/AAAAYA8TBPpjbWFwAAAB7AAAAFUAAAFeF1fZ4mdhc3AAAAPwAAAACAAAAAgAAAAQZ2x5ZgAAAlgAAABcAAAAnMvguMloZWFkAAABMAAAAC8AAAA2C5Ap72hoZWEAAAFgAAAAHQAAACQGbQPHaG10eAAAAdgAAAAUAAAAHAoAAABsb2NhAAACRAAAABIAAAASAIwAYG1heHAAAAGAAAAAFgAAACAACwAKbmFtZQAAArQAAAECAAACZxWCgKhwb3N0AAADuAAAADUAAABZCrApUXicY2BkYGAA4rDECVrx/DZfGbhZGEDgyqNPOxH0/wNMq5kPALkcDEwgUQBWRA0dAHicY2BkYGA+8P8AAwMLAwgwrWZgZEAFbABY4QM8AAAAeJxjYGRgYOAAQiYGEICQSAAAAi8AFgAAeJxjYGY6yziBgZWBgWkm0xkGBoZ+CM34msGYkZMBFTAKoAkwODAwvmRiPvD/AIMDMxCD1CDJKjAwAgBktQsXAHicY2GAAMZQCM0EwqshbAALxAEKeJxjYGBgZoBgGQZGBhCIAPIYwXwWBhsgzcXAwcAEhIwMCi+Z/v/9/x+sSuElA4T9/4k4K1gHFwMMMILMY2QDYmaoABOQYGJABUA7WBiGNwAAJd4NIQAAAAAAAAAACAAIABAAGAAmAEAATgAAeJyNjLENgDAMBP9tIURJwQCMQccSZgk2i5fIYBDAidJjycXr7x5EPwE2wY8si7jmyBNXGo/bNBerxJNrpxhbO3/fEFpx8ZICpV+ghxJ74fAMe+h7Ox14AbrsHB14nK2QQWrDMBRER4mTkhQK3ZRQKOgCNk7oGQqhhEIX2WSlWEI1BAlkJ5CDdNsj5Ey9Rncdi38ES+jzNJo/HwTgATcoDEthhY3wBHc4CE+pfwsX5F/hGe7Vo/AcK/UhvMSz+mGXKhZU6pww8ISz3oWn1BvhgnwTnuEJf8Jz1OpFeIlX9YULDLdFi4ASHolkSR0iuYdjLak1vAequBhj21D61Nqyi6l3qWybGPjySbPHGScGJl6dP58MYcQRI0bts7mjebBqrFENH7t3qWtj0OuqHnXcW7b0HOTZFnKryRGW2hFX1m0O2vEM3opNMfTau+CS6Z3Vx6veNnEXY6jwDxhsc2gAAHicY2BiwA84GBgYmRiYGJkZmBlZGFkZ2djScyoLMgzZS/MyDQwMwLSrpYEBlIbxjQDrzgsuAAAAAAEAAf//AA94nGNgZGBg4AFiMSBmYmAEQnYgZgHzGAAD6wA2eJxjYGBgZACCKyoz1cD0o087YTQATOcIewAAAA\x3d\x3d) format('woff');\n      font-weight: normal;\n      font-style: normal;\n    }\n  \x3c/style\x3e\n\x3c/custom-style\x3e";
document.head.appendChild(Td.content);class Ud extends Nb(Qb(I)){static get template(){return H`
    <style>
      :host {
        display: inline-flex;
        cursor: pointer;
        max-width: 100%;
      }

      [part="content"] {
        flex: 1 1 auto;
      }

      [part="indicators"] {
        position: relative;
        align-self: center;
        flex: none;
      }

      [part="order"] {
        display: inline;
        vertical-align: super;
      }

      [part="indicators"]::before {
        font-family: 'vaadin-grid-sorter-icons';
        display: inline-block;
      }

      :host(:not([direction])) [part="indicators"]::before {
        content: "\\e901";
      }

      :host([direction=asc]) [part="indicators"]::before {
        content: "\\e900";
      }

      :host([direction=desc]) [part="indicators"]::before {
        content: "\\e902";
      }
    </style>

    <div part="content">
      <slot></slot>
    </div>
    <div part="indicators">
      <span part="order">[[_getDisplayOrder(_order)]]</span>
    </div>
`}static get is(){return"vaadin-grid-sorter"}static get properties(){return{path:String,direction:{type:String,reflectToAttribute:!0,notify:!0,value:null},_order:{type:Number,value:null},_isConnected:{type:Boolean,value:!1}}}static get observers(){return["_pathOrDirectionChanged(path, direction, _isConnected)","_directionOrOrderChanged(direction, _order)"]}ready(){super.ready();this.addEventListener("click",this._onClick.bind(this))}connectedCallback(){super.connectedCallback();this._isConnected=
!0}disconnectedCallback(){super.disconnectedCallback();this._isConnected=!1}_pathOrDirectionChanged(b,a,c){void 0!==b&&void 0!==a&&void 0!==c&&c&&this.dispatchEvent(new CustomEvent("sorter-changed",{bubbles:!0,composed:!0}))}_getDisplayOrder(b){return null===b?"":b+1}_onClick(b){const a=this.getRootNode().activeElement;this!==a&&this.contains(a)||(b.preventDefault(),this.direction="asc"===this.direction?"desc":"desc"===this.direction?null:"asc")}_directionOrOrderChanged(b,a){void 0!==b&&void 0!==
a&&/^((?!chrome|android).)*safari/i.test(navigator.userAgent)&&this.root&&this.root.querySelectorAll("*").forEach(function(c){c.style["-webkit-backface-visibility"]="visible";c.style["-webkit-backface-visibility"]=""})}}customElements.define(Ud.is,Ud);class Vd extends db{static get template(){return H`
    <template class="header" id="headerTemplate">
      <vaadin-grid-sorter path="[[path]]" direction="{{direction}}">[[_getHeader(header, path)]]</vaadin-grid-sorter>
    </template>
`}static get is(){return"vaadin-grid-sort-column"}static get properties(){return{path:String,direction:{type:String,notify:!0}}}_prepareHeaderTemplate(){const b=this._prepareTemplatizer(this.$.headerTemplate);b.templatizer.dataHost=this;return b}_getHeader(b,a){return b||this._generateHeader(a)}}customElements.define(Vd.is,Vd);const Wd={selectionColumn:!0};let w=function(b){function a(d,e){d=b.call(this,d,e)||this;d._grid=null;d._headerStyles="display: flex; font-weight: 400;";d._hostStyles='font-family: "Avenir Next", "Helvetica Neue", Helvetica, Arial, sans-serif; font-size: 1em;';
d._rowHoverStyles="background: #e2f1fb;";d.cellClassNameGenerator=null;d.columnReorderingEnabled=!0;d.dataProvider=null;d.itemIdPath=null;d.label=void 0;d.messages=null;d.pageSize=50;d.selectedItems=new ae;d.size=null;d.rowDetailsRenderer=null;d.store=null;d.viewModel=new fe;d.visibleElements={...Wd};return d}Xd._inheritsLoose(a,b);var c=a.prototype;c.initialize=function(){this.handles.add([Yb.watch(this,"viewModel.size",()=>this._updateGridSize()),Yb.watch(this,"store.state",(d,e)=>{"ready"===d&&
"loaded"===e&&this.refreshCache()})])};c.castVisibleElements=function(d){return{...Wd,...d}};c.render=function(){return ka.jsx("div",{bind:this,class:this.classes("esri-grid","esri-widget")},ka.jsx("div",{bind:this,class:"esri-grid__content"},this.renderGrid()))};c.renderGrid=function(){return ka.jsx("vaadin-grid",Object.assign({},this.getGridProps()),this.renderAllColumns())};c.renderAllColumns=function(){if("disabled"!==this.viewModel.state&&this.columns&&this.columns.length)return[this.renderSelectionColumn(),
this.renderColumns()]};c.renderSelectionColumn=function(){return ka.jsx("vaadin-grid-selection-column",{_selectAllHidden:!0,selectAll:!1,bind:this,hidden:!this.visibleElements.selectionColumn,sortable:!1,frozen:!ce.isRTL()})};c.renderColumns=function(){return this.columns.items.map((d,e)=>ka.jsx("vaadin-grid-column",Object.assign({},this.getColumnProps(d,e))))};c.getGridProps=function(){const {columnReorderingEnabled:d,id:e,pageSize:f,size:g}=this;return{_safari:!1,class:"esri-grid__grid",id:`${e}_grid`,
theme:"compact column-borders",ref:"grid",bind:this,afterCreate:this._afterGridCreate,afterUpdate:this._afterGridUpdate,columnReorderingAllowed:d,pageSize:f,size:g}};c.getColumnProps=function(d,e){const {id:f}=this,{autoWidth:g,direction:h,flexGrow:k,frozen:l,header:m,hidden:n,path:p,resizable:r,textAlign:u,width:z}=d;d=`${f}_${name}_${Yd.isSome(e)?e:p}`;return{autoWidth:g,direction:h,flexGrow:k,frozen:l,header:m,hidden:n,key:d,path:p,resizable:r,"text-align":u,width:"number"===typeof z?`${z}px`:
z,bind:this,afterCreate:this._afterColumnCreateOrUpdate,afterUpdate:this._afterColumnCreateOrUpdate}};c.clearSelection=function(){this._clearSelection();this.scheduleRender()};c.clearSort=function(){this.columns.forEach(d=>d.direction=null);this._grid&&(this._grid._sorters=[]);this.scheduleRender()};c.deselectItem=function(d){this._deselectRowByItem(d)};c.deselectRow=function(d){(d=this.viewModel.getRowItemAt(d))&&this._deselectRowByItem(d)};c.findColumn=function(){};c.generateCellClassNames=function(){var d;
null==(d=this._grid)?void 0:d.generateCellClassNames()};c.getSlotElementByName=function(d){var e,f;return null==(e=this._grid)?void 0:null==(f=e.shadowRoot)?void 0:f.querySelector(`slot[name='${d}']`)};c.hideColumn=function(d){var e;null==(e=this.viewModel)?void 0:e.hideColumn(d);this.scheduleRender()};c.notifyResize=function(){var d;null==(d=this._grid)?void 0:d.notifyResize()};c.recalculateColumnWidths=function(){var d;null==(d=this._grid)?void 0:d.recalculateColumnWidths()};c.refresh=async function(){var d,
e;this._clearSelection();null==(d=this.store)?void 0:d.reset();await (null==(e=this.store)?void 0:e.load());this.refreshCache()};c.refreshCache=function(){var d;null==(d=this._grid)?void 0:d.clearCache()};c.selectItem=function(d){this._selectRowByItem(d)};c.selectRow=function(d){(d=this.viewModel.getRowItemAt(d))&&this._selectRowByItem(d)};c.showColumn=function(d){var e;null==(e=this.viewModel)?void 0:e.showColumn(d);this.scheduleRender()};c.sort=function({path:d,direction:e}){var f;null==(f=this.viewModel)?
void 0:f.sortColumn(d,e)};c._afterGridCreate=function(d){const {cellClassNameGenerator:e,dataProvider:f,itemIdPath:g,rowDetailsRenderer:h}=this;d.cellClassNameGenerator=e;d.dataProvider=f;d.rowDetailsRenderer=h;d.itemIdPath=g;this._grid=d;customElements.whenDefined("vaadin-grid").then(()=>{this._appendStyles();this._addGridEventListeners()})};c._afterGridUpdate=function(d){this._grid||(this._grid=d)};c._afterColumnCreateOrUpdate=function(d){this._syncColumnRenderers(d)};c._appendStyles=function(){var d;
const e=null==(d=this._grid)?void 0:d.shadowRoot;d=document.createElement("style");d.textContent=`
      :host { ${this._hostStyles} }
      [part~="header-cell"] ::slotted(vaadin-grid-cell-content) { ${this._headerStyles} }
      [part~="row"]:hover [part~="body-cell"] { ${this._rowHoverStyles} }
    `;null==e?void 0:e.appendChild(d)};c._updateGridSize=function(){this._grid&&(this._grid.size=this.size||0,this.scheduleRender())};c._addGridEventListeners=function(){const {_grid:d}=this;this.handles.add([Xb.on(d,"click",e=>this._onRowClick(e)),Xb.on(d,"selected-items-changed",e=>this._onSelectionChange(e))])};c._onRowClick=function(d){var {_grid:e}=this;e=e.getEventContext(event);const {item:f,section:g}=e;f&&g&&"details"!==g&&"header"!==g&&this.emit("row-click",{context:e,native:d})};c._selectRowByItem=
function(d){var e;null==(e=this._grid)?void 0:e.selectItem(d)};c._deselectRowByItem=function(d){var e;null==(e=this._grid)?void 0:e.deselectItem(d)};c._clearSelection=function(){var d;null!=(d=this._grid)&&d.selectedItems&&(this._grid.selectedItems.slice().forEach(e=>this._deselectRowByItem(e)),this._updateSelectionProps())};c._onSelectionChange=function(d){this._updateSelectionProps();if("selectedItems.splices"===d.detail.path){const {removed:e,index:f,object:g}=d.detail.value.indexSplices[0];this.emit("selection-change",
{index:f,added:g,removed:e})}};c._updateSelectionProps=function(){this.selectedItems.length&&this.selectedItems.removeAll();this._grid&&this.selectedItems.addMany(this._grid.selectedItems)};c._syncColumnRenderers=function(d){const e=d.getAttribute("path"),f=this.viewModel.findColumn(e);if(f)try{f.renderFunction&&(d.renderer=(g,h,k)=>f.renderFunction({root:g,column:h,rowData:k})),f.footerRenderFunction&&(d.footerRenderer=(g,h)=>f.footerRenderFunction({root:g,column:h})),f.headerRenderFunction&&(d.headerRenderer=
(g,h)=>f.headerRenderFunction({root:g,column:h}))}catch(g){}};return a}(be.HandleOwnerMixin(ee));x.__decorate([W.property()],w.prototype,"_grid",void 0);x.__decorate([J.aliasOf("viewModel.cellClassNameGenerator")],w.prototype,"cellClassNameGenerator",void 0);x.__decorate([J.aliasOf("viewModel.columns")],w.prototype,"columns",void 0);x.__decorate([J.aliasOf("viewModel.columnReorderingEnabled")],w.prototype,"columnReorderingEnabled",void 0);x.__decorate([J.aliasOf("viewModel.dataProvider")],w.prototype,
"dataProvider",void 0);x.__decorate([W.property()],w.prototype,"itemIdPath",void 0);x.__decorate([W.property({aliasOf:{source:"messages.widgetLabel",overridable:!0}})],w.prototype,"label",void 0);x.__decorate([W.property(),Ga.renderable(),de.messageBundle("esri/widgets/FeatureTable/t9n/FeatureTable")],w.prototype,"messages",void 0);x.__decorate([J.aliasOf("viewModel.pageSize")],w.prototype,"pageSize",void 0);x.__decorate([W.property({readOnly:!0}),Ga.renderable()],w.prototype,"selectedItems",void 0);
x.__decorate([J.aliasOf("viewModel.size")],w.prototype,"size",void 0);x.__decorate([J.aliasOf("viewModel.rowDetailsRenderer")],w.prototype,"rowDetailsRenderer",void 0);x.__decorate([J.aliasOf("viewModel.store")],w.prototype,"store",void 0);x.__decorate([W.property(),Ga.renderable("viewModel.cellClassNameGenerator viewModel.columnReorderingEnabled viewModel.columns viewModel.dataProvider viewModel.pageSize viewModel.rowDetailsRenderer viewModel.size viewModel.state viewModel.store".split(" "))],w.prototype,
"viewModel",void 0);x.__decorate([W.property(),Ga.renderable()],w.prototype,"visibleElements",void 0);x.__decorate([Zd.cast("visibleElements")],w.prototype,"castVisibleElements",null);x.__decorate([J.aliasOf("viewModel.findColumn")],w.prototype,"findColumn",null);return w=x.__decorate([$d.subclass("esri.widgets.FeatureTable.Grid.Grid")],w)});