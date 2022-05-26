/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/dnd/TimedMoveable",["../_base/declare","./Moveable"],function(e,c){var d=c.prototype.onMove;return e("dojo.dnd.TimedMoveable",c,{timeout:40,constructor:function(a,b){b||(b={});b.timeout&&"number"==typeof b.timeout&&0<=b.timeout&&(this.timeout=b.timeout)},onMoveStop:function(a){a._timer&&(clearTimeout(a._timer),d.call(this,a,a._leftTop));c.prototype.onMoveStop.apply(this,arguments)},onMove:function(a,b){a._leftTop=b;if(!a._timer){var c=this;a._timer=setTimeout(function(){a._timer=null;
d.call(c,a,a._leftTop)},this.timeout)}}})});