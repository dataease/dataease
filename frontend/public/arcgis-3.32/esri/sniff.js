// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/sniff",["dojo/_base/sniff","dojo/global","./kernel"],function(a,c,d){var e=a("ff"),g=a("ie"),n=void 0===g&&7<=a("trident"),p=a("edge"),k=a("webkit"),h=a("opera"),m=a("chrome"),q=a("safari"),b=navigator.userAgent,f;(f=b.match(/(iPhone|iPad|CPU)\s+OS\s+(\d+\_\d+)/i))&&a.add("esri-iphone",parseFloat(f[2].replace("_",".")));(f=b.match(/Android\s+(\d+(\.\d+)*)/i))&&a.add("esri-android",parseFloat(f[1]));(f=b.match(/Fennec\/(\d+\.\d+)/i))&&a.add("esri-fennec",parseFloat(f[1]));0<=b.indexOf("BlackBerry")&&
0<=b.indexOf("WebKit")&&a.add("esri-blackberry",1);a.add("esri-touch",a("esri-iphone")||a("esri-android")||a("esri-blackberry")||6<=a("esri-fennec")||(e||k)&&(document.createTouch||"ontouchstart"in c||c.TouchEvent&&0<navigator.maxTouchPoints)?!0:!1);(f=b.match(/Android|webOS|iPhone|iPad|iPod|BlackBerry|Opera Mini|IEMobile/i))&&a.add("esri-mobile",!!f);a.add("esri-pointer",!e&&(navigator.pointerEnabled||navigator.msPointerEnabled||!!c.PointerEvent));d._getDOMAccessor=function(a){var c="";e?c="Moz":
k?c="Webkit":g?c="ms":h&&(c="O");return c+a.charAt(0).toUpperCase()+a.substr(1)};a.add("esri-phonegap",!!c.cordova);a.add("esri-cors",a("esri-phonegap")||c.XMLHttpRequest&&"withCredentials"in new XMLHttpRequest);a.add("esri-file-upload",c.FormData&&c.FileList?!0:!1);a.add("esri-script-sandbox",function(){return"MessageChannel"in c&&"HTMLIFrameElement"in c&&"sandbox"in HTMLIFrameElement.prototype});a.add("esri-secure-context",function(){if("isSecureContext"in c)return c.isSecureContext;if(c.location&&
c.location.origin)return 0===c.location.origin.indexOf("https:")});a.add("esri-wasm","WebAssembly"in c);a.add("esri-workers",c.Worker?!0:!1);a.add("esri-featurelayer-webgl",!1);if(b=a("esri-featurelayer-webgl")){var l=a("esri-mobile");f=l?1:2;l=l?1:3;"object"===typeof b?(b.maxDrillLevel=null==b.maxDrillLevel?f:b.maxDrillLevel,b.maxRecordCountFactor=null==b.maxRecordCountFactor?l:b.maxRecordCountFactor,b.enablePBFQuery=null==b.enablePBFQuery?!0:b.enablePBFQuery):a.add("esri-featurelayer-webgl",{maxDrillLevel:f,
maxRecordCountFactor:l,enablePBFQuery:!0},null,!0)}a.add("esri-featurelayer-webgl-labeling",!1);a.add("esri-pbf",!g||10<=g);a.add("esri-featurelayer-pbf",!0);a.add("esri-transforms",n||p||9<=g||3.5<=e||4<=m||3.1<=q||10.5<=h||3.2<=a("esri-iphone")||2.1<=a("esri-android"));a.add("esri-transitions",n||p||10<=g||4<=e||4<=m||3.1<=q||10.5<=h||3.2<=a("esri-iphone")||2.1<=a("esri-android"));a.add("esri-transforms3d",n||p||10<=e||12<=m||4<=q||3.2<=a("esri-iphone")||3<=a("esri-android"));a.add("esri-url-encodes-apostrophe",
function(){if(!c.document)return!1;var a=c.document.createElement("a");a.href="?'";return-1<a.href.indexOf("?%27")});3>a("esri-android")&&(a.add("esri-transforms",!1,!1,!0),a.add("esri-transitions",!1,!1,!0),a.add("esri-transforms3d",!1,!1,!0));a.add("esri-will-change",a("esri-transforms")&&(52<=m||11.1<=q));d._css=function(c){var b=a("esri-transforms3d");void 0!==c&&null!==c?b=c:b&&(m||q&&!a("esri-iphone"))&&(b=!1);var d=b?"translate3d(":"translate(",f=b?m?",-1px)":",0px)":")",l=b?"scale3d(":"scale(",
n=b?",1)":")",p=b?"rotate3d(0,0,1,":"rotate(",r=b?"matrix3d(":"matrix(",t=b?",0,0,":",",u=b?",0,0,0,0,1,0,":",",v=b?",0,1)":")";return{names:{transition:k&&"-webkit-transition"||e&&"MozTransition"||h&&"OTransition"||g&&"msTransition"||"transition",transform:k&&"-webkit-transform"||e&&"MozTransform"||h&&"OTransform"||g&&"msTransform"||"transform",transformName:k&&"-webkit-transform"||e&&"-moz-transform"||h&&"-o-transform"||g&&"-ms-transform"||"transform",origin:k&&"-webkit-transform-origin"||e&&"MozTransformOrigin"||
h&&"OTransformOrigin"||g&&"msTransformOrigin"||"transformOrigin",endEvent:k&&"webkitTransitionEnd"||e&&"transitionend"||h&&"oTransitionEnd"||g&&"MSTransitionEnd"||"transitionend"},translate:function(a,b){return d+a+"px,"+b+"px"+f},scale:function(a){return l+a+","+a+n},rotate:function(a){return p+a+"deg)"},matrix:function(a){return r+a.xx+","+a.xy+t+a.yx+","+a.yy+u+a.dx.toFixed(10)+(e&&59>=e?"px,":",")+a.dy.toFixed(10)+(e&&59>=e?"px":"")+v},getScaleFromMatrix:function(a){if(!a)return 1;a=a.toLowerCase();
var b=-1<a.indexOf("matrix3d")?"matrix3d(":"matrix(";return Number(a.substring(b.length,a.indexOf(",")))}}};a("extend-esri")&&(d.isiPhone=a("esri-iphone"),d.isAndroid=a("esri-android"),d.isFennec=a("esri-fennec"),d.isBlackBerry=a("esri-blackberry"),d.isTouchEnabled=a("esri-touch"),d.isPointerEnabled=a("esri-pointer"),d._hasCors=a("esri-cors"),d._hasFileUpload=a("esri-file-upload"),d._hasTransforms=a("esri-transforms"),d._hasTransitions=a("esri-transitions"),d._has3DTransforms=a("esri-transforms3d"));
return a});