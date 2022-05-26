/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/fx/Toggler",["../_base/lang","../_base/declare","../_base/fx","../aspect"],function(b,e,c,d){return e("dojo.fx.Toggler",null,{node:null,showFunc:c.fadeIn,hideFunc:c.fadeOut,showDuration:200,hideDuration:200,constructor:function(a){b.mixin(this,a);this.node=a.node;this._showArgs=b.mixin({},a);this._showArgs.node=this.node;this._showArgs.duration=this.showDuration;this.showAnim=this.showFunc(this._showArgs);this._hideArgs=b.mixin({},a);this._hideArgs.node=this.node;this._hideArgs.duration=
this.hideDuration;this.hideAnim=this.hideFunc(this._hideArgs);d.after(this.showAnim,"beforeBegin",b.hitch(this.hideAnim,"stop",!0),!0);d.after(this.hideAnim,"beforeBegin",b.hitch(this.showAnim,"stop",!0),!0)},show:function(a){return this.showAnim.play(a||0)},hide:function(a){return this.hideAnim.play(a||0)}})});