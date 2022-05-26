/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/currency","./_base/array ./_base/lang ./number ./i18n ./i18n!./cldr/nls/currency ./cldr/monetary".split(" "),function(g,f,c,h,l,k){var b={};f.setObject("dojo.currency",b);b._mixInDefaults=function(a){a=a||{};a.type="currency";var b=h.getLocalization("dojo.cldr","currency",a.locale)||{},c=a.currency,e=k.getData(c);g.forEach(["displayName","symbol","group","decimal"],function(a){e[a]=b[c+"_"+a]});e.fractional=[!0,!1];return f.mixin(e,a)};b.format=function(a,d){return c.format(a,b._mixInDefaults(d))};
b.regexp=function(a){return c.regexp(b._mixInDefaults(a))};b.parse=function(a,d){return c.parse(a,b._mixInDefaults(d))};return b});