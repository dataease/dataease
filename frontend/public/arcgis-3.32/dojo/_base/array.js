/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/_base/array",["./kernel","../has","./lang"],function(q,u,r){function l(a){return k[a]=new Function("item","index","array",a)}function n(a){var d=!a;return function(f,b,c){var g=0,e=f&&f.length||0,h;e&&"string"==typeof f&&(f=f.split(""));"string"==typeof b&&(b=k[b]||l(b));if(c)for(;g<e;++g){if(h=!b.call(c,f[g],g,f),a^h)return!h}else for(;g<e;++g)if(h=!b(f[g],g,f),a^h)return!h;return d}}function p(a){var d=1,f=0,b=0;a||(d=f=b=-1);return function(c,g,e,h){if(h&&0<d)return m.lastIndexOf(c,
g,e);h=c&&c.length||0;var k=a?h+b:f;e===t?e=a?f:h+b:0>e?(e=h+e,0>e&&(e=f)):e=e>=h?h+b:e;for(h&&"string"==typeof c&&(c=c.split(""));e!=k;e+=d)if(c[e]==g)return e;return-1}}var k={},t,m={every:n(!1),some:n(!0),indexOf:p(!0),lastIndexOf:p(!1),forEach:function(a,d,f){var b=0,c=a&&a.length||0;c&&"string"==typeof a&&(a=a.split(""));"string"==typeof d&&(d=k[d]||l(d));if(f)for(;b<c;++b)d.call(f,a[b],b,a);else for(;b<c;++b)d(a[b],b,a)},map:function(a,d,f,b){var c=0,g=a&&a.length||0;b=new (b||Array)(g);g&&
"string"==typeof a&&(a=a.split(""));"string"==typeof d&&(d=k[d]||l(d));if(f)for(;c<g;++c)b[c]=d.call(f,a[c],c,a);else for(;c<g;++c)b[c]=d(a[c],c,a);return b},filter:function(a,d,f){var b=0,c=a&&a.length||0,g=[],e;c&&"string"==typeof a&&(a=a.split(""));"string"==typeof d&&(d=k[d]||l(d));if(f)for(;b<c;++b)e=a[b],d.call(f,e,b,a)&&g.push(e);else for(;b<c;++b)e=a[b],d(e,b,a)&&g.push(e);return g},clearCache:function(){k={}}};r.mixin(q,m);return m});