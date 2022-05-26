// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/annotations/utils/AnnotationsUtil",["dojo/dom-style"],function(d){return{alignAnnotationContainer:function(b,a){var c=a&&a.horizontalAlign||"left",e=a&&a.verticalAlign||"top";a={left:"auto",right:"auto",top:"auto",bottom:"auto"};d.set(b.content,a);switch(c){case "left":a.left="0px";break;case "center":var c=b.getWidth(),f=d.get(b.content,"width");a.left=(c-f)/2+"px";break;case "right":a.right="0px"}switch(e){case "top":a.top="0px";break;case "middle":e=
b.getHeight();c=d.get(b.content,"height");a.top=(e-c)/2+"px";break;case "bottom":a.bottom="0px"}d.set(b.content,a)}}});