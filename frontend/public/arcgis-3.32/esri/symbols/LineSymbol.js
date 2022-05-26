// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/symbols/LineSymbol","dojo/_base/declare dojo/_base/lang dojo/has dojox/gfx/_base ../kernel ./Symbol".split(" "),function(b,c,e,d,f,g){b=b(g,{declaredClass:"esri.symbol.LineSymbol",constructor:function(a){c.isObject(a)?this.width=d.pt2px(this.width):this.width=12},setWidth:function(a){this.width=a;return this},toJson:function(){var a=d.px2pt(this.width),a=isNaN(a)?void 0:a;return c.mixin(this.inherited("toJson",arguments),{width:a})}});e("extend-esri")&&c.setObject("symbol.LineSymbol",
b,f);return b});