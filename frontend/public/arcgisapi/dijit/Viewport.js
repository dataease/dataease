//>>built
define(["dojo/Evented","dojo/on","dojo/domReady","dojo/sniff","dojo/window"],function(k,f,l,g,h){var e=new k,b;l(function(){var a=h.getBox();e._rlh=f(window,"resize",function(){var d=h.getBox();if(a.h!=d.h||a.w!=d.w)a=d,e.emit("resize")});if(8==g("ie")){var c=screen.deviceXDPI;setInterval(function(){screen.deviceXDPI!=c&&(c=screen.deviceXDPI,e.emit("resize"))},500)}g("ios")&&(f(document,"focusin",function(d){b=d.target}),f(document,"focusout",function(d){b=null}))});e.getEffectiveBox=function(a){a=
h.getBox(a);var c=b&&b.tagName&&b.tagName.toLowerCase();g("ios")&&b&&!b.readOnly&&("textarea"==c||"input"==c&&/^(color|email|number|password|search|tel|text|url)$/.test(b.type))&&(a.h*=0==orientation||180==orientation?.66:.4,c=b.getBoundingClientRect(),a.h=Math.max(a.h,c.top+c.height));return a};return e});