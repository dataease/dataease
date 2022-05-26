/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/node",["./_base/kernel","./has","require"],function(c,h,g){var e=c.global.require&&c.global.require.nodeRequire;if(!e)throw Error("Cannot find the Node.js require");var d=e("module");return{load:function(a,b,c){d._findPath&&d._nodeModulePaths&&(b=d._findPath(a,d._nodeModulePaths(b.toUrl("."))),!1!==b&&(a=b));b=define;var f;define=void 0;try{f=e(a)}finally{define=b}c(f)},normalize:function(a,b){"."===a.charAt(0)&&(a=g.toUrl(b("./"+a)));return a}}});