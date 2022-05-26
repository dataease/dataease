// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/metadata/types/arcgis/base/Codelists","dojo/_base/declare dojo/_base/lang dojo/_base/array dojo/has ../../../../../kernel ./codelistData dojo/i18n!../../../nls/i18nArcGIS".split(" "),function(a,c,g,d,e,f,h){a=a(null,{constructor:function(b){c.mixin(this,b)},makeCodelist:function(b,a){b=f.makeCodelist(a);"CountryCode"!==a&&"LanguageCode"!==a&&"MonetaryUnits"!==a||b.codes.sort(function(a,b){return""===a.value?-1:""===b.value?1:"zxx"===a.value?-1:"zxx"===b.value?1:a.label===b.label?
0:a.label>b.label?1:-1});return b}});d("extend-esri")&&c.setObject("dijit.metadata.types.arcgis.base.Codelists",a,e);return a});