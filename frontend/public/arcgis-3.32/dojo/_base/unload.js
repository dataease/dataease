/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/_base/unload",["./kernel","./lang","../on"],function(b,e,c){var d=window,a={addOnWindowUnload:function(a,f){b.windowUnloaded||c(d,"unload",b.windowUnloaded=function(){});c(d,"unload",e.hitch(a,f))},addOnUnload:function(a,b){c(d,"beforeunload",e.hitch(a,b))}};b.addOnWindowUnload=a.addOnWindowUnload;b.addOnUnload=a.addOnUnload;return a});