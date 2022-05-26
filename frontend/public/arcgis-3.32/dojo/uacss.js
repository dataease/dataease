/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/uacss",["./dom-geometry","./_base/lang","./domReady","./sniff","./_base/window"],function(h,k,n,a,c){var d=c.doc.documentElement;c=a("ie");var l=a("trident"),b=a("opera"),f=Math.floor,m=a("ff"),p=h.boxModel.replace(/-/,""),b={dj_quirks:a("quirks"),dj_opera:b,dj_khtml:a("khtml"),dj_webkit:a("webkit"),dj_safari:a("safari"),dj_chrome:a("chrome"),dj_edge:a("edge"),dj_gecko:a("mozilla"),dj_ios:a("ios"),dj_android:a("android")};c&&(b.dj_ie=!0,b["dj_ie"+f(c)]=!0,b.dj_iequirks=a("quirks"));l&&
(b.dj_trident=!0,b["dj_trident"+f(l)]=!0);m&&(b["dj_ff"+f(m)]=!0);b["dj_"+p]=!0;var e="",g;for(g in b)b[g]&&(e+=g+" ");d.className=k.trim(d.className+" "+e);n(function(){if(!h.isBodyLtr()){var a="dj_rtl dijitRtl "+e.replace(/ /g,"-rtl ");d.className=k.trim(d.className+" "+a+"dj_rtl dijitRtl "+e.replace(/ /g,"-rtl "))}});return a});