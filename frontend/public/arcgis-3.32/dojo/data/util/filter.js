/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/data/util/filter",["../../_base/lang"],function(e){var b={};e.setObject("dojo.data.util.filter",b);b.patternToRegExp=function(b,e){for(var a="^",d=null,c=0;c<b.length;c++)switch(d=b.charAt(c),d){case "\\":a+=d;c++;a+=b.charAt(c);break;case "*":a+=".*";break;case "?":a+=".";break;case "$":case "^":case "/":case "+":case ".":case "|":case "(":case ")":case "{":case "}":case "[":case "]":a+="\\";default:a+=d}a+="$";return e?new RegExp(a,"mi"):new RegExp(a,"m")};return b});