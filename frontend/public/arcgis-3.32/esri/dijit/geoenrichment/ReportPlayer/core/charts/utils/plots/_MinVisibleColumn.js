// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/charts/utils/plots/_MinVisibleColumn",["dojo/_base/declare","dojo/_base/lang"],function(b,d){return b(null,{minVisibleHeight:2,createRect:function(a,b,c){a=d.mixin({},c);a.height<this.minVisibleHeight&&(a.y-=this.minVisibleHeight-c.height,a.height=this.minVisibleHeight);return b.createRect(a)}})});