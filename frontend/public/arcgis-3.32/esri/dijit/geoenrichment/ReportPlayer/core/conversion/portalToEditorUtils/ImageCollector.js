// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/conversion/portalToEditorUtils/ImageCollector",["esri/dijit/geoenrichment/when","esri/dijit/geoenrichment/promise/all","esri/dijit/geoenrichment/utils/DataUtil","esri/dijit/geoenrichment/utils/ImageUtil","esri/dijit/geoenrichment/utils/ImageInfoUtil"],function(d,e,f,g,h){return{collectImageResources:function(a,c,k){function l(b){return d(h.getImageInfo(c[b],b),null,function(){delete c[b]})}a&&a.files&&a.files.forEach(function(b){var a=f.getContentType(b.name);
a&&"image/"===a.substr(0,6)&&(c[k(b.name)]=g.base64DataToDataURL(b.data,a))});a=[];for(var m in c)a.push(l(m));return e(a)}}});