// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/metadata/form/InputNumber","dojo/_base/declare dojo/_base/lang dojo/has ./InputText dojo/i18n!../nls/i18nBase ../../../kernel".split(" "),function(a,c,d,e,b,f){a=a([e],{_isGxeInputNumber:!0,hint:b.hints.number,integerOnly:!1,minValue:null,maxValue:null,minValueIsExclusive:!1,maxValueIsExclusive:!1,small:!0,postCreate:function(){this.inherited(arguments)},postMixInProperties:function(){this.inherited(arguments);this.integerOnly&&this.hint===b.hints.number&&(this.hint=b.hints.integer)}});
d("extend-esri")&&c.setObject("dijit.metadata.form.InputNumber",a,f);return a});