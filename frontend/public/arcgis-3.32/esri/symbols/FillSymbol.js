// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/symbols/FillSymbol","dojo/_base/declare dojo/_base/lang dojo/has ../kernel ./Symbol ./SimpleLineSymbol".split(" "),function(b,c,d,e,f,g){b=b(f,{declaredClass:"esri.symbol.FillSymbol",constructor:function(a){a&&c.isObject(a)&&a.outline&&(this.outline=new g(a.outline))},setOutline:function(a){this.outline=a;return this},toJson:function(){var a=this.inherited("toJson",arguments);this.outline&&(a.outline=this.outline.toJson());return a}});d("extend-esri")&&c.setObject("symbol.FillSymbol",
b,e);return b});