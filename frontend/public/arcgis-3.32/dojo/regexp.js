/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/regexp",["./_base/kernel","./_base/lang"],function(g,d){var c={};d.setObject("dojo.regexp",c);c.escapeString=function(a,b){return a.replace(/([\.$?*|{}\(\)\[\]\\\/\+\-^])/g,function(a){return b&&-1!=b.indexOf(a)?a:"\\"+a})};c.buildGroupRE=function(a,b,d){if(!(a instanceof Array))return b(a);for(var f=[],e=0;e<a.length;e++)f.push(b(a[e]));return c.group(f.join("|"),d)};c.group=function(a,b){return"("+(b?"?:":"")+a+")"};return c});