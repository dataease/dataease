// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/dataProvider/supportClasses/PortalManager",["esri/dijit/geoenrichment/when","esri/arcgis/Portal"],function(d,e){var b={_cache:{},getPortalInfo:function(a){if(!b._cache[a]){var c=new e.Portal(a);b._cache[a]=d(c.signIn(),function(a){return{user:a,portal:c}})}return b._cache[a]}};return b});