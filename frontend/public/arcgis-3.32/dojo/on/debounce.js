/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/on/debounce",["../debounce","../on","./asyncEventListener"],function(a,b,c){return function(d,e){return function(f,g){return b(f,d,c(a(g,e)))}}});