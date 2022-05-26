/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/_base/window",["./kernel","./lang","../sniff"],function(a,k,d){var c={global:a.global,doc:a.global.document||null,body:function(b){b=b||a.doc;return b.body||b.getElementsByTagName("body")[0]},setContext:function(b,e){a.global=c.global=b;a.doc=c.doc=e},withGlobal:function(b,e,d,l){var f=a.global;try{return a.global=c.global=b,c.withDoc.call(null,b.document,e,d,l)}finally{a.global=c.global=f}},withDoc:function(b,e,f,l){var k=c.doc,n=d("quirks"),p=d("ie"),g,h,m;try{return a.doc=c.doc=b,
a.isQuirks=d.add("quirks","BackCompat"==a.doc.compatMode,!0,!0),d("ie")&&(m=b.parentWindow)&&m.navigator&&(g=parseFloat(m.navigator.appVersion.split("MSIE ")[1])||void 0,(h=b.documentMode)&&5!=h&&Math.floor(g)!=h&&(g=h),a.isIE=d.add("ie",g,!0,!0)),f&&"string"==typeof e&&(e=f[e]),e.apply(f,l||[])}finally{a.doc=c.doc=k,a.isQuirks=d.add("quirks",n,!0,!0),a.isIE=d.add("ie",p,!0,!0)}}};k.mixin(a,c);return c});