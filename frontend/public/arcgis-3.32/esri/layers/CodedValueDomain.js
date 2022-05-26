// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/CodedValueDomain","dojo/_base/declare dojo/_base/lang dojo/_base/array dojo/has ../kernel ../lang ./Domain".split(" "),function(a,b,d,e,f,g,h){a=a([h],{declaredClass:"esri.layers.CodedValueDomain",constructor:function(c){c&&b.isObject(c)&&(this.codedValues=c.codedValues)},getName:function(c){var a;d.some(this.codedValues,function(b){b.code==c&&(a=b.name);return!!a});return a},toJson:function(){var a=this.inherited(arguments);a.codedValues=b.clone(this.codedValues);return g.fixJson(a)}});
e("extend-esri")&&b.setObject("layers.CodedValueDomain",a,f);return a});