// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/charts/utils/plots/ClusteredColumns",["dojo/_base/declare","./_ClusteredColumnsBase"],function(k,l){return k(l,{_drawColumn:function(f,a,b,c,g,d,h,e){this._drawColumnBackground(f,a,b,c,g,d,h,e);return{shape:this.createRect(h,f,b).setFill(c.series.fill).setStroke(c.series.stroke),rect:b}},_drawColumnBackground:function(f,a,b,c,g,d,h,e){c.series.showColumnBarBackground&&(a=a[a.valueProp],this.createRect(h,f,{x:b.x,y:0<a?d.t:g.height-d.b-e,width:b.width,
height:0<a?g.height-d.t-d.b-e:e}).setFill(c.series.columnBarBackgroundColor))}})});