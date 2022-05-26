// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/FeatureLayerQueryResult",["../lang","dojo/_base/lang","dojo/_base/kernel","dojo/_base/Deferred"],function(h,e,f,g){var d=function(a){function b(c){a[c]||(a[c]=function(){var b=arguments;return g.when(a,function(a){Array.prototype.unshift.call(b,a.features||a);return new d(f[c].apply(f,b))})})}if(!a)return a;a.then&&(a=e.delegate(a));a.total||(a.total=g.when(a,function(a){return h.isDefined(a.total)?a.total:a.length||0}));b("forEach");b("filter");b("map");b("some");b("every");return a};
e.setObject("dijit.FeatureLayerQueryResult",d);return d});