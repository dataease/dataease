/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/Evented",["./aspect","./on"],function(e,c){function a(){}var f=e.after;a.prototype={on:function(a,d){return c.parse(this,a,d,function(b,a){return f(b,"on"+a,d,!0)})},emit:function(a,d){var b=[this];b.push.apply(b,arguments);return c.emit.apply(c,b)}};return a});