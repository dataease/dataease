// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/utils/htmlToSvg/supportClasses/StyleParser",[],function(){return{_splitTrim:function(b,d,c){b=String(b).split(d).map(function(a){return a.trim()});return c?b.filter(function(a){return!!a}):b},parseStyleString:function(b){var d=this,c={};this._splitTrim(b||"",";",!0).forEach(function(a){a=d._splitTrim(a,":");c[a[0]]=a[1]});return c}}});