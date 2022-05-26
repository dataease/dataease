/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/i18n","./_base/kernel require ./has ./_base/array ./_base/config ./_base/lang ./_base/xhr ./json module".split(" "),function(m,k,q,r,F,p,C,G,H){q.add("dojo-preload-i18n-Api",1);var u=m.i18n={},I=/(^.*(^|\/)nls)(\/|$)([^\/]*)\/?([^\/]*)/,J=function(a,b,e,c){var d=[e+c];b=b.split("-");for(var n="",l=0;l<b.length;l++)if(n+=(n?"-":"")+b[l],!a||a[n])d.push(e+n+"/"+c),d.specificity=n;return d},h={},D=function(a,b,e){e=e?e.toLowerCase():m.locale;a=a.replace(/\./g,"/");b=b.replace(/\./g,"/");
return/root/i.test(e)?a+"/nls/"+b:a+"/nls/"+e+"/"+b},K=m.getL10nName=function(a,b,e){return a=H.id+"!"+D(a,b,e)},M=function(a,b,e,c,d,n){a([b],function(l){var f=p.clone(l.root||l.ROOT),g=J(!l._v1x&&l,d,e,c);a(g,function(){for(var a=1;a<g.length;a++)f=p.mixin(p.clone(f),arguments[a]);h[b+"/"+d]=f;f.$locale=g.specificity;n()})})},N=function(a){var b=F.extraLocale||[],b=p.isArray(b)?b:[b];b.push(a);return b},y=function(a,b,e){var c=I.exec(a),d=c[1]+"/",n=c[5]||c[4],l=d+n,f=(c=c[5]&&c[4])||m.locale||
"",g=l+"/"+f,c=c?[f]:N(f),L=c.length,k=function(){--L||e(p.delegate(h[g]))},f=a.split("*"),w="preload"==f[1];if(q("dojo-preload-i18n-Api")){if(w&&(h[a]||(h[a]=1,O(f[2],G.parse(f[3]),1,b)),e(1)),(f=w)||(t&&x.push([a,b,e]),f=t&&!h[g]),f)return}else if(w){e(1);return}r.forEach(c,function(a){var c=l+"/"+a;q("dojo-preload-i18n-Api")&&v(c);h[c]?k():M(b,l,d,n,a,k)})};q("dojo-preload-i18n-Api");var P=u.normalizeLocale=function(a){a=a?a.toLowerCase():m.locale;return"root"==a?"ROOT":a},t=0,x=[],O=u._preloadLocalizations=
function(a,b,e,c){function d(a,b){c.isXdUrl(k.toUrl(a+".js"))||e?c([a],b):E([a],b,c)}function n(a,b){for(a=a.split("-");a.length;){if(b(a.join("-")))return;a.pop()}b("ROOT")}function l(){for(--t;!t&&x.length;)y.apply(null,x.shift())}function f(e){e=P(e);n(e,function(f){if(0<=r.indexOf(b,f)){var g=a.replace(/\./g,"/")+"_"+f;t++;d(g,function(a){for(var b in a){var d=a[b],g=b.match(/(.+)\/([^\/]+)$/),m;if(g&&(m=g[2],g=g[1]+"/",d._localized)){var q;if("ROOT"===f){var r=q=d._localized;delete d._localized;
r.root=d;h[k.toAbsMid(b)]=r}else q=d._localized,h[k.toAbsMid(g+m+"/"+f)]=d;f!==e&&function(a,b,d,f){var g=[],m=[];n(e,function(c){f[c]&&(g.push(k.toAbsMid(a+c+"/"+b)),m.push(k.toAbsMid(a+b+"/"+c)))});g.length?(t++,c(g,function(){for(var c=g.length-1;0<=c;c--)d=p.mixin(p.clone(d),arguments[c]),h[m[c]]=d;h[k.toAbsMid(a+b+"/"+e)]=p.clone(d);l()})):h[k.toAbsMid(a+b+"/"+e)]=d}(g,m,d,q)}}l()});return!0}return!1})}c=c||k;f();r.forEach(m.config.extraLocale,f)},v=function(){},z={},A={},B,E=function(a,b,e){var c=
[];r.forEach(a,function(a){function b(b){B||(B=new Function("__bundle","__checkForLegacyModules","__mid","__amdValue","var define \x3d function(mid, factory){define.called \x3d 1; __amdValue.result \x3d factory || mid;},\t   require \x3d function(){define.called \x3d 1;};try{define.called \x3d 0;eval(__bundle);if(define.called\x3d\x3d1)return __amdValue;if((__checkForLegacyModules \x3d __checkForLegacyModules(__mid)))return __checkForLegacyModules;}catch(e){}try{return eval('('+__bundle+')');}catch(e){return e;}"));
b=B(b,v,a,z);b===z?c.push(h[d]=z.result):(b instanceof Error&&(console.error("failed to evaluate i18n bundle; url\x3d"+d,b),b={}),c.push(h[d]=/nls\/[^\/]+\/[^\/]+$/.test(d)?b:{root:b,_v1x:1}))}var d=e.toUrl(a+".js");if(h[d])c.push(h[d]);else{var f=e.syncLoadNls(a);f||(f=v(a.replace(/nls\/([^\/]*)\/([^\/]*)$/,"nls/$2/$1")));if(f)c.push(f);else if(C)C.get({url:d,sync:!0,load:b,error:function(){c.push(h[d]={})}});else try{e.getText(d,!0,b)}catch(g){c.push(h[d]={})}}});b&&b.apply(null,c)},v=function(a){for(var b,
e=a.split("/"),c=m.global[e[0]],d=1;c&&d<e.length-1;c=c[e[d++]]);c&&((b=c[e[d]])||(b=c[e[d].replace(/-/g,"_")]),b&&(h[a]=b));return b};u.getLocalization=function(a,b,e){var c,d=D(a,b,e);if(A[d])return A[d];y(d,k.isXdUrl(k.toUrl(d+".js"))?k:function(a,b){E(a,b,k)},function(a){c=A[d]=a});return c};return p.mixin(u,{dynamic:!0,normalize:function(a,b){return/^\./.test(a)?b(a):a},load:y,cache:h,getL10nName:K})});