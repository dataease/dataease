/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/NodeList-fx",["./query","./_base/lang","./aspect","./_base/fx","./fx"],function(b,e,k,h,f){b=b.NodeList;e.extend(b,{_anim:function(a,b,d){d=d||{};var g=f.combine(this.map(function(c){c={node:c};e.mixin(c,d);return a[b](c)}));return d.auto?g.play()&&this:g},wipeIn:function(a){return this._anim(f,"wipeIn",a)},wipeOut:function(a){return this._anim(f,"wipeOut",a)},slideTo:function(a){return this._anim(f,"slideTo",a)},fadeIn:function(a){return this._anim(h,"fadeIn",a)},fadeOut:function(a){return this._anim(h,
"fadeOut",a)},animateProperty:function(a){return this._anim(h,"animateProperty",a)},anim:function(a,b,d,g,c){var e=f.combine(this.map(function(c){return h.animateProperty({node:c,properties:a,duration:b||350,easing:d})}));g&&k.after(e,"onEnd",g,!0);return e.play(c||0)}});return b});