/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/dom",["./sniff","./_base/window","./_base/kernel"],function(e,g,f){if(7>=e("ie"))try{document.execCommand("BackgroundImageCache",!1,!0)}catch(a){}var d={};e("ie")?d.byId=function(a,b){if("string"!=typeof a)return a;var c=b||g.doc;b=a&&c.getElementById(a);if(!b||b.attributes.id.value!=a&&b.id!=a){c=c.all[a];if(!c||c.nodeName)c=[c];for(var d=0;b=c[d++];)if(b.attributes&&b.attributes.id&&b.attributes.id.value==a||b.id==a)return b}else return b}:d.byId=function(a,b){return("string"==typeof a?
(b||g.doc).getElementById(a):a)||null};f=f.global.document||null;e.add("dom-contains",!(!f||!f.contains));d.isDescendant=e("dom-contains")?function(a,b){return!(!(b=d.byId(b))||!b.contains(d.byId(a)))}:function(a,b){try{for(a=d.byId(a),b=d.byId(b);a;){if(a==b)return!0;a=a.parentNode}}catch(c){}return!1};e.add("css-user-select",function(a,b,c){if(!c)return!1;a=c.style;b=["Khtml","O","Moz","Webkit"];c=b.length;var d="userSelect";do if("undefined"!==typeof a[d])return d;while(c--&&(d=b[c]+"UserSelect"));
return!1});var h=e("css-user-select");d.setSelectable=h?function(a,b){d.byId(a).style[h]=b?"":"none"}:function(a,b){a=d.byId(a);var c=a.getElementsByTagName("*"),e=c.length;if(b)for(a.removeAttribute("unselectable");e--;)c[e].removeAttribute("unselectable");else for(a.setAttribute("unselectable","on");e--;)c[e].setAttribute("unselectable","on")};return d});