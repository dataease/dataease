/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/ready",["./_base/kernel","./has","require","./domReady","./_base/lang"],function(a,h,b,c,k){var p=0,f=[],l=0;h=function(){p=1;a._postLoad=a.config.afterOnLoad=!0;g()};var g=function(){if(!l){for(l=1;p&&(!c||0==c._Q.length)&&(b.idle?b.idle():1)&&f.length;){var a=f.shift();try{a()}catch(d){if(d.info=d.message,b.signal)b.signal("error",d);else throw d;}}l=0}};b.on&&b.on("idle",g);c&&(c._onQEmpty=g);var m=a.ready=a.addOnLoad=function(b,d,c){var e=k._toArray(arguments);"number"!=typeof b?
(c=d,d=b,b=1E3):e.shift();c=c?k.hitch.apply(a,e):function(){d()};c.priority=b;for(e=0;e<f.length&&b>=f[e].priority;e++);f.splice(e,0,c);g()},n=a.config.addOnLoad;if(n)m[k.isArray(n)?"apply":"call"](a,n);a.config.parseOnLoad&&!a.isAsync&&m(99,function(){a.parser||(a.deprecated("Add explicit require(['dojo/parser']);","","2.0"),b(["dojo/parser"]))});c?c(h):h();return m});