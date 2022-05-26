/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/aspect",[],function(){function n(e,d,h,k){var c=e[d],g="around"==d,a;if(g){var l=h(function(){return c.advice(this,arguments)});a={remove:function(){l&&(l=e=h=null)},advice:function(a,b){return l?l.apply(a,b):c.advice(a,b)}}}else a={remove:function(){if(a.advice){var f=a.previous,b=a.next;b||f?(f?f.next=b:e[d]=b,b&&(b.previous=f)):delete e[d];e=h=a.advice=null}},id:e.nextId++,advice:h,receiveArguments:k};if(c&&!g)if("after"==d){for(;c.next&&(c=c.next););c.next=a;a.previous=c}else"before"==
d&&(e[d]=a,a.next=c,c.previous=a);else e[d]=a;return a}function m(e){return function(d,h,k,c){var g=d[h],a;g&&g.target==d||(d[h]=a=function(){for(var d=a.nextId,f=arguments,b=a.before;b;)b.advice&&(f=b.advice.apply(this,f)||f),b=b.next;if(a.around)var c=a.around.advice(this,f);for(b=a.after;b&&b.id<d;){if(b.advice)if(b.receiveArguments)var e=b.advice.apply(this,f),c=void 0===e?c:e;else c=b.advice.call(this,c,f);b=b.next}return c},g&&(a.around={advice:function(a,c){return g.apply(a,c)}}),a.target=
d,a.nextId=a.nextId||0);d=n(a||g,e,k,c);k=null;return d}}var p=m("after"),q=m("before"),r=m("around");return{before:q,around:r,after:p}});