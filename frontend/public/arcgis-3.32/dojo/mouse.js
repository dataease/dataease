/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/mouse",["./_base/kernel","./on","./has","./dom","./_base/window"],function(e,g,c,k,b){function d(a,b){var c=function(c,h){return g(c,a,function(a){if(b)return b(a,h);if(!k.isDescendant(a.relatedTarget,c))return h.call(this,a)})};c.bubble=function(b){return d(a,function(a,c){var f=b(a.target),d=a.relatedTarget;if(f&&f!=(d&&1==d.nodeType&&b(d)))return c.call(f,a)})};return c}c.add("dom-quirks",b.doc&&"BackCompat"==b.doc.compatMode);c.add("events-mouseenter",b.doc&&"onmouseenter"in b.doc.createElement("div"));
c.add("events-mousewheel",b.doc&&"onmousewheel"in b.doc);b=c("dom-quirks")&&c("ie")||!c("dom-addeventlistener")?{LEFT:1,MIDDLE:4,RIGHT:2,isButton:function(a,b){return a.button&b},isLeft:function(a){return a.button&1},isMiddle:function(a){return a.button&4},isRight:function(a){return a.button&2}}:{LEFT:0,MIDDLE:1,RIGHT:2,isButton:function(a,b){return a.button==b},isLeft:function(a){return 0==a.button},isMiddle:function(a){return 1==a.button},isRight:function(a){return 2==a.button}};e.mouseButtons=
b;e=c("events-mousewheel")?"mousewheel":function(a,b){return g(a,"DOMMouseScroll",function(a){a.wheelDelta=-a.detail;b.call(this,a)})};return{_eventHandler:d,enter:d("mouseover"),leave:d("mouseout"),wheel:e,isLeft:b.isLeft,isMiddle:b.isMiddle,isRight:b.isRight}});