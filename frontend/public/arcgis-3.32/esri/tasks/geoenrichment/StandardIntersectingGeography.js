// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/tasks/geoenrichment/StandardIntersectingGeography",["../../declare","./IntersectingGeography"],function(b,c){return b("esri.tasks.geoenrichment.StandardIntersectingGeography",[c],{geographyLayerID:null,constructor:function(a){a&&(this.geographyLayerID=a.geographyLayer||a.geographyLayerID||null)},toJson:function(){var a=this.inherited(arguments);a.geographyType="standard";a.geographyLayer=this.geographyLayerID;return a}})});