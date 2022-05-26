/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/_base/json",["./kernel","../json"],function(a,c){a.fromJson=function(a){return eval("("+a+")")};a._escapeString=c.stringify;a.toJsonIndentStr="\t";a.toJson=function(d,e){return c.stringify(d,function(a,b){return b&&(a=b.__json__||b.json,"function"==typeof a)?a.call(b):b},e&&a.toJsonIndentStr)};return a});