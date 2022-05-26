/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/io-query",["./_base/lang"],function(k){var l={};return{objectToQuery:function(d){var e=encodeURIComponent,c=[],b;for(b in d){var a=d[b];if(a!=l[b]){var f=e(b)+"\x3d";if(k.isArray(a))for(var g=0,h=a.length;g<h;++g)c.push(f+e(a[g]));else c.push(f+e(a))}}return c.join("\x26")},queryToObject:function(d){var e=decodeURIComponent;d=d.split("\x26");for(var c={},b,a,f=0,g=d.length;f<g;++f)if(a=d[f],a.length){var h=a.indexOf("\x3d");0>h?(b=e(a),a=""):(b=e(a.slice(0,h)),a=e(a.slice(h+1)));"string"==
typeof c[b]&&(c[b]=[c[b]]);k.isArray(c[b])?c[b].push(a):c[b]=a}return c}}});