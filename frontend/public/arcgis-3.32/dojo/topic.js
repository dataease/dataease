/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/topic",["./Evented"],function(b){var a=new b;return{publish:function(b,c){return a.emit.apply(a,arguments)},subscribe:function(b,c){return a.on.apply(a,arguments)}}});