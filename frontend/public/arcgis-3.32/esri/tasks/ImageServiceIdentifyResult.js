// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/tasks/ImageServiceIdentifyResult","dojo/_base/declare dojo/_base/lang dojo/has ../kernel ../geometry/jsonUtils ./FeatureSet".split(" "),function(b,c,d,e,f,g){b=b(null,{declaredClass:"esri.tasks.ImageServiceIdentifyResult",constructor:function(a){a.catalogItems&&(this.catalogItems=new g(a.catalogItems));a.location&&(this.location=f.fromJson(a.location));this.catalogItemVisibilities=a.catalogItemVisibilities;this.name=a.name;this.objectId=a.objectId;this.value=a.value;this.processedValues=
a.processedValues;this.properties=a.properties}});d("extend-esri")&&c.setObject("tasks.ImageServiceIdentifyResult",b,e);return b});