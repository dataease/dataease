// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/dataProvider/commands/mapToImage/_MapReplacer",["dojo/dom-construct","dojo/dom-style"],function(c,b){return{replaceMapWithImage:function(a){for(var e=[];a.children.length;)e.push(a.firstChild),a.removeChild(a.firstChild);var f=b.get(a,"position"),d=c.create("img",{"class":"esriGEAbsoluteStretched"},a);b.set(d,{width:b.get(a,"width")+"px",height:b.get(a,"height")+"px"});b.set(a,"position","relative");return{mapImage:d,undo:function(){c.destroy(d);b.set(a,
"position",f);e.forEach(function(b){c.place(b,a)})}}}}});