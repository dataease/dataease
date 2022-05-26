/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/dnd/move","../_base/declare ../dom-geometry ../dom-style ./common ./Mover ./Moveable".split(" "),function(f,g,m,h,k,n){h=f("dojo.dnd.move.constrainedMoveable",n,{constraints:function(){},within:!1,constructor:function(c,a){a||(a={});this.constraints=a.constraints;this.within=a.within},onFirstMove:function(c){var a=this.constraintBox=this.constraints.call(this,c);a.r=a.l+a.w;a.b=a.t+a.h;this.within&&(c=g.getMarginSize(c.node),a.r-=c.w,a.b-=c.h)},onMove:function(c,a){var d=this.constraintBox,
l=c.node.style;this.onMoving(c,a);a.l=a.l<d.l?d.l:d.r<a.l?d.r:a.l;a.t=a.t<d.t?d.t:d.b<a.t?d.b:a.t;l.left=a.l+"px";l.top=a.t+"px";this.onMoved(c,a)}});k=f("dojo.dnd.move.boxConstrainedMoveable",h,{box:{},constructor:function(c,a){var d=a&&a.box;this.constraints=function(){return d}}});f=f("dojo.dnd.move.parentConstrainedMoveable",h,{area:"content",constructor:function(c,a){var d=a&&a.area;this.constraints=function(){var a=this.node.parentNode,c=m.getComputedStyle(a),b=g.getMarginBox(a,c);if("margin"==
d)return b;var e=g.getMarginExtents(a,c);b.l+=e.l;b.t+=e.t;b.w-=e.w;b.h-=e.h;if("border"==d)return b;e=g.getBorderExtents(a,c);b.l+=e.l;b.t+=e.t;b.w-=e.w;b.h-=e.h;if("padding"==d)return b;e=g.getPadExtents(a,c);b.l+=e.l;b.t+=e.t;b.w-=e.w;b.h-=e.h;return b}}});return{constrainedMoveable:h,boxConstrainedMoveable:k,parentConstrainedMoveable:f}});