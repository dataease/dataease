/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/string",["./_base/kernel","./_base/lang"],function(h,e){var k=/[&<>'"\/]/g,l={"\x26":"\x26amp;","\x3c":"\x26lt;","\x3e":"\x26gt;",'"':"\x26quot;","'":"\x26#x27;","/":"\x26#x2F;"},c={};e.setObject("dojo.string",c);c.escape=function(a){return a?a.replace(k,function(a){return l[a]}):""};c.rep=function(a,b){if(0>=b||!a)return"";for(var d=[];;){b&1&&d.push(a);if(!(b>>=1))break;a+=a}return d.join("")};c.pad=function(a,b,d,m){d||(d="0");a=String(a);b=c.rep(d,Math.ceil((b-a.length)/d.length));
return m?a+b:b+a};c.substitute=function(a,b,d,c){c=c||h.global;d=d?e.hitch(c,d):function(a){return a};return a.replace(/\$\{([^\s\:\}]*)(?:\:([^\s\:\}]+))?\}/g,function(a,f,g){if(""==f)return"$";a=e.getObject(f,!1,b);g&&(a=e.getObject(g,!1,c).call(c,a,f));g=d(a,f);if("undefined"===typeof g)throw Error('string.substitute could not find key "'+f+'" in template');return g.toString()})};c.trim=String.prototype.trim?e.trim:function(a){a=a.replace(/^\s+/,"");for(var b=a.length-1;0<=b;b--)if(/\S/.test(a.charAt(b))){a=
a.substring(0,b+1);break}return a};return c});