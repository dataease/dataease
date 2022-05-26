/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/has",["./global","require","module"],function(k,b,h){var a=b.has||function(){};if(!a("dojo-has-api")){var g="undefined"!=typeof window&&"undefined"!=typeof location&&"undefined"!=typeof document&&window.location==location&&window.document==document&&document,l=g&&g.createElement("DiV"),c=h.config&&h.config()||{},a=function(a){return"function"==typeof c[a]?c[a]=c[a](k,g,l):c[a]};a.cache=c;a.add=function(d,b,m,e){("undefined"==typeof c[d]||e)&&(c[d]=b);return m&&a(d)}}a.add("dom-addeventlistener",
!!document.addEventListener);a.add("touch","ontouchstart"in document||"onpointerdown"in document&&0<navigator.maxTouchPoints||window.navigator.msMaxTouchPoints);a.add("touch-events","ontouchstart"in document);a.add("pointer-events","pointerEnabled"in window.navigator?window.navigator.pointerEnabled:"PointerEvent"in window);a.add("MSPointer",window.navigator.msPointerEnabled);a.add("touch-action",a("touch")&&a("pointer-events"));a.add("device-width",screen.availWidth||innerWidth);b=document.createElement("form");
a.add("dom-attributes-explicit",0==b.attributes.length);a.add("dom-attributes-specified-flag",0<b.attributes.length&&40>b.attributes.length);a.clearElement=function(a){a.innerHTML="";return a};a.normalize=function(d,b){var c=d.match(/[\?:]|[^:\?]*/g),e=0,f=function(d){var b=c[e++];if(":"==b)return 0;if("?"==c[e++]){if(!d&&a(b))return f();f(!0);return f(d)}return b||0};return(d=f())&&b(d)};a.load=function(a,b,c){a?b([a],c):c()};return a});