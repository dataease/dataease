/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/debounce",[],function(){return function(b,c){var a;return function(){a&&clearTimeout(a);var d=this,e=arguments;a=setTimeout(function(){b.apply(d,e)},c)}}});