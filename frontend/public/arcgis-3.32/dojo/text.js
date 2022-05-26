/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/text",["./_base/kernel","require","./has","./request"],function(r,t,v,u){var k;k=function(a,c,b){u(a,{sync:!!c,headers:{"X-Requested-With":null}}).then(b)};var d={},l=function(a){if(a){a=a.replace(/^\s*<\?xml(\s)+version=[\'\"](\d)*.(\d)*[\'\"](\s)*\?>/im,"");var c=a.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);c&&(a=c[1])}else a="";return a},p={},h={};r.cache=function(a,c,b){var e;"string"==typeof a?/\//.test(a)?(e=a,b=c):e=t.toUrl(a.replace(/\./g,"/")+(c?"/"+c:"")):(e=a+"",b=c);a=
void 0!=b&&"string"!=typeof b?b.value:b;b=b&&b.sanitize;if("string"==typeof a)return d[e]=a,b?l(a):a;if(null===a)return delete d[e],null;e in d||k(e,!0,function(a){d[e]=a});return b?l(d[e]):d[e]};return{dynamic:!0,normalize:function(a,c){a=a.split("!");var b=a[0];return(/^\./.test(b)?c(b):b)+(a[1]?"!"+a[1]:"")},load:function(a,c,b){a=a.split("!");var e=1<a.length,m=a[0],f=c.toUrl(a[0]);a="url:"+f;var g=p,n=function(a){b(e?l(a):a)};m in d?g=d[m]:c.cache&&a in c.cache?g=c.cache[a]:f in d&&(g=d[f]);
if(g===p)if(h[f])h[f].push(n);else{var q=h[f]=[n];k(f,!c.async,function(a){d[m]=d[f]=a;for(var b=0;b<q.length;)q[b++](a);delete h[f]})}else n(g)}}});