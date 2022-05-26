// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/utils/onClickTouch",["dojo/_base/lang","dojo/on","dojo/sniff"],function(g,b,h){var k=h("touch");return g.mixin(function(d,a,c,e){if(k){switch(a){case "click":a="touchstart,click";break;case "mousedown":a="touchstart,mousedown";break;case "mouseup":a="touchend,mouseup";break;case "clickend":a="touchend,click";break;default:return b(d,a,c,e)}var f=!1;return b(d,a,function(a){"touchstart"===a.type||"touchend"===a.type?(c.call(this,a),f=!0,setTimeout(function(){f=!1},
500)):f||c.call(this,a)},e)}"clickend"===a&&(a="click");return b(d,a,c,e)},b)});