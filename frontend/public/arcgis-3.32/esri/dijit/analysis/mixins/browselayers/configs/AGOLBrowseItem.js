// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/analysis/mixins/browselayers/configs/AGOLBrowseItem",["dojo/_base/lang","dojo/has","../../../../../kernel","./EnterpriseBrowseItem"],function(c,d,e,f){var b={getConfig:function(a){a=f.getConfig(a);a.baseSections.push("all");a.baseSections.push("subscription");return a}};d("extend-esri")&&c.setObject("dijit.analysis.mixins.browselayers.configs.AGOLBrowseItem",b,e);return b});