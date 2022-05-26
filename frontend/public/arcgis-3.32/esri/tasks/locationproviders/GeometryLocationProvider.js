// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/tasks/locationproviders/GeometryLocationProvider",["../../declare","dojo/json","../../geometry/jsonUtils","./LocationProviderClientBase"],function(e,f,d,g){return e("esri.tasks.locationproviders.GeometryLocationProvider",g,{geometryField:null,getGeometry:function(a){if(a=a.attributes[this.geometryField])try{"string"===typeof a&&(a=f.parse(a));var c;a.spatialReference||(c=this.inSpatialReference);var b=d.fromJson(a);if(b&&d.getJsonType(b)===this.geometryType)return c&&b.setSpatialReference(c),
b}catch(h){}}})});