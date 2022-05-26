/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/dnd/Mover","../_base/array ../_base/declare ../_base/lang ../sniff ../_base/window ../dom ../dom-geometry ../dom-style ../Evented ../on ../touch ./common ./autoscroll".split(" "),function(m,n,e,k,p,q,g,r,t,c,h,u,l){return n("dojo.dnd.Mover",[t],{constructor:function(a,b,d){function f(a){a.preventDefault();a.stopPropagation()}this.node=q.byId(a);this.marginBox={l:b.pageX,t:b.pageY};this.mouseButton=b.button;b=this.host=d;a=a.ownerDocument;this.events=[c(a,h.move,e.hitch(this,"onFirstMove")),
c(a,h.move,e.hitch(this,"onMouseMove")),c(a,h.release,e.hitch(this,"onMouseUp")),c(a,"dragstart",f),c(a.body,"selectstart",f)];l.autoScrollStart(a);if(b&&b.onMoveStart)b.onMoveStart(this)},onMouseMove:function(a){l.autoScroll(a);var b=this.marginBox;this.host.onMove(this,{l:b.l+a.pageX,t:b.t+a.pageY},a);a.preventDefault();a.stopPropagation()},onMouseUp:function(a){(k("webkit")&&k("mac")&&2==this.mouseButton?0==a.button:this.mouseButton==a.button)&&this.destroy();a.preventDefault();a.stopPropagation()},
onFirstMove:function(a){var b=this.node.style,d,f=this.host;switch(b.position){case "relative":case "absolute":d=Math.round(parseFloat(b.left))||0;b=Math.round(parseFloat(b.top))||0;break;default:b.position="absolute";b=g.getMarginBox(this.node);d=p.doc.body;var c=r.getComputedStyle(d),e=g.getMarginBox(d,c),c=g.getContentBox(d,c);d=b.l-(c.l-e.l);b=b.t-(c.t-e.t)}this.marginBox.l=d-this.marginBox.l;this.marginBox.t=b-this.marginBox.t;if(f&&f.onFirstMove)f.onFirstMove(this,a);this.events.shift().remove()},
destroy:function(){m.forEach(this.events,function(a){a.remove()});var a=this.host;if(a&&a.onMoveStop)a.onMoveStop(this);this.events=this.node=this.host=null}})});