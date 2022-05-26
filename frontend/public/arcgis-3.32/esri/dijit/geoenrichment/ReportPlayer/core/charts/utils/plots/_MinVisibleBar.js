// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/charts/utils/plots/_MinVisibleBar",["dojo/_base/declare","dojo/_base/lang"],function(b,d){return b(null,{minVisibleWidth:2,createRect:function(a,b,c){a=d.mixin({},c);a.width<this.minVisibleWidth&&(a.y-=this.minVisibleWidth-c.width,a.width=this.minVisibleWidth);return b.createRect(a)}})});