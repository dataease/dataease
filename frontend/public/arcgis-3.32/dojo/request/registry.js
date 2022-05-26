/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/request/registry",["require","../_base/array","./default!platform","./util"],function(h,k,g,l){function c(b,d){for(var a=e.slice(0),m=0,f;f=a[m++];)if(f(b,d))return f.request.call(null,b,d);return g.apply(null,arguments)}function n(b,d){var a;d?(a=b.test?function(a){return b.test(a)}:b.apply&&b.call?function(){return b.apply(null,arguments)}:function(a){return a===b},a.request=d):(a=function(){return!0},a.request=b);return a}var e=[];c.register=function(b,d,a){var c=n(b,d);e[a?"unshift":
"push"](c);return{remove:function(){var a;~(a=k.indexOf(e,c))&&e.splice(a,1)}}};c.load=function(b,d,a,e){b?h([b],function(b){g=b;a(c)}):a(c)};l.addCommonMethods(c);return c});