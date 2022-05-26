/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/request/notify",["../Evented","../_base/lang","./util"],function(d,g,h){function e(a,b){return f.on(a,b)}var b=0,k=[].slice,f=g.mixin(new d,{onsend:function(a){b||this.emit("start");b++},_onload:function(a){this.emit("done",a)},_onerror:function(a){this.emit("done",a)},_ondone:function(a){0>=--b&&(b=0,this.emit("stop"))},emit:function(a,b){var c=d.prototype.emit.apply(this,arguments);this["_on"+a]&&this["_on"+a].apply(this,k.call(arguments,1));return c}});e.emit=function(a,b,c){return f.emit(a,
b,c)};return h.notify=e});