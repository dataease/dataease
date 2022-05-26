/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/_base/event",["./kernel","../on","../has","../dom-geometry"],function(d,b,e,f){if(b._fixEvent){var g=b._fixEvent;b._fixEvent=function(a,b){(a=g(a,b))&&f.normalizeEvent(a);return a}}var c={fix:function(a,c){return b._fixEvent?b._fixEvent(a,c):a},stop:function(a){e("dom-addeventlistener")||a&&a.preventDefault?(a.preventDefault(),a.stopPropagation()):(a=a||window.event,a.cancelBubble=!0,b._preventDefault.call(a))}};d.fixEvent=c.fix;d.stopEvent=c.stop;return c});