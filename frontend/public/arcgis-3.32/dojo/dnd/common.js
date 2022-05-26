/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/dnd/common",["../sniff","../_base/kernel","../_base/lang","../dom"],function(c,d,e,f){var b=e.getObject("dojo.dnd",!0);b.getCopyKeyState=function(a){return a[c("mac")?"metaKey":"ctrlKey"]};b._uniqueId=0;b.getUniqueId=function(){var a;do a=d._scopeName+"Unique"+ ++b._uniqueId;while(f.byId(a));return a};b._empty={};b.isFormElement=function(a){a=a.target;3==a.nodeType&&(a=a.parentNode);return 0<=" a button textarea input select option ".indexOf(" "+a.tagName.toLowerCase()+" ")};return b});