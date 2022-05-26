/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/dnd/autoscroll","../_base/lang ../sniff ../_base/window ../dom-geometry ../dom-style ../window".split(" "),function(v,n,l,r,w,q){var a={};v.setObject("dojo.dnd.autoscroll",a);a.getViewport=q.getBox;a.V_TRIGGER_AUTOSCROLL=32;a.H_TRIGGER_AUTOSCROLL=32;a.V_AUTOSCROLL_VALUE=16;a.H_AUTOSCROLL_VALUE=16;var p,e=l.doc,t=Infinity,u=Infinity;a.autoScrollStart=function(a){e=a;p=q.getBox(e);a=l.body(e).parentNode;t=Math.max(a.scrollHeight-p.h,0);u=Math.max(a.scrollWidth-p.w,0)};a.autoScroll=function(g){var f=
p||q.getBox(e),k=l.body(e).parentNode,b=0,d=0;g.clientX<a.H_TRIGGER_AUTOSCROLL?b=-a.H_AUTOSCROLL_VALUE:g.clientX>f.w-a.H_TRIGGER_AUTOSCROLL&&(b=Math.min(a.H_AUTOSCROLL_VALUE,u-k.scrollLeft));g.clientY<a.V_TRIGGER_AUTOSCROLL?d=-a.V_AUTOSCROLL_VALUE:g.clientY>f.h-a.V_TRIGGER_AUTOSCROLL&&(d=Math.min(a.V_AUTOSCROLL_VALUE,t-k.scrollTop));window.scrollBy(b,d)};a._validNodes={div:1,p:1,td:1};a._validOverflow={auto:1,scroll:1};a.autoScrollNodes=function(g){for(var f,k,b,d,h,e=0,m=0,c=g.target;c;){if(1==c.nodeType&&
c.tagName.toLowerCase()in a._validNodes){b=w.getComputedStyle(c);h=b.overflowX.toLowerCase()in a._validOverflow;d=b.overflowY.toLowerCase()in a._validOverflow;if(h||d)f=r.getContentBox(c,b),k=r.position(c,!0);if(h){b=Math.min(a.H_TRIGGER_AUTOSCROLL,f.w/2);h=g.pageX-k.x;if(n("webkit")||n("opera"))h+=l.body().scrollLeft;e=0;0<h&&h<f.w&&(h<b?e=-b:h>f.w-b&&(e=b),c.scrollLeft+=e)}if(d){d=Math.min(a.V_TRIGGER_AUTOSCROLL,f.h/2);b=g.pageY-k.y;if(n("webkit")||n("opera"))b+=l.body().scrollTop;m=0;0<b&&b<f.h&&
(b<d?m=-d:b>f.h-d&&(m=d),c.scrollTop+=m)}if(e||m)return}try{c=c.parentNode}catch(x){c=null}}a.autoScroll(g)};return a});