// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/tasks/geoenrichment/AddressStudyArea",["../../declare","./StudyArea"],function(b,c){return b("esri.tasks.geoenrichment.AddressStudyArea",[c],{address:null,constructor:function(a){a&&a.address&&(this.address=a.address)},toJson:function(){var a=this.inherited(arguments);a.address=this.address;return a},getGeomType:function(){return"point"}})});