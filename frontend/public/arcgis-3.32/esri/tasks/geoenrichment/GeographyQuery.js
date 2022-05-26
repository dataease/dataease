// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/tasks/geoenrichment/GeographyQuery",["../../declare","./GeographyQueryBase"],function(b,c){return b("esri.tasks.geoenrichment.GeographyQuery",[c],{geographyLayerIDs:null,geographyIDs:null,where:null,constructor:function(a){a&&(this.geographyLayerIDs=a.geographyLayerIDs||a.geographyLayers.split(";"),this.geographyIDs=a.geographyIDs,this.where=a.where||a.geographyQuery)},toJson:function(){var a=this.inherited(arguments);this.geographyLayerIDs&&(a.geographyLayers=this.geographyLayerIDs.join(";"));
this.geographyIDs&&(a.geographyIDs=this.geographyIDs);this.where&&(a.geographyQuery=this.where);return a}})});