// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/deferredUtils",["dojo/_base/lang","dojo/has","./kernel"],function(e,f,g){var d={_dfdCanceller:function(a){a.canceled=!0;var b=a._pendingDfd,c;a.isFulfilled()||!b||b.isFulfilled()||(b.cancel(),c=b.results&&b.results[1]);a._pendingDfd=null;return c},_fixDfd:function(a){var b=a.then;a.then=function(a,h,d){if(a){var c=a;a=function(a){return a&&a._argsArray?c.apply(null,a):c(a)}}return b.call(this,a,h,d)};return a},_resDfd:function(a,b,c){var d=b.length;1===d?c?a.errback(b[0]):a.callback(b[0]):
1<d?(b._argsArray=!0,a.callback(b)):a.callback()}};f("extend-esri")&&e.mixin(g,d);return d});