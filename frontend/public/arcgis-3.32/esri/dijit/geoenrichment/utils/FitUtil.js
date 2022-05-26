// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/utils/FitUtil",[],function(){return{fitBox:function(a,e,f){f=f||{};var k=f.vAlign||"middle";f=f.hAlign||"center";var b=a.w/e.w,c=a.h/e.h,g=1<b?1:-1,h=1<c?1:-1,b=1<b?1/b:b,c=1<c?1/c:c,d;-1===g&&-1===h?d=1/Math.max(b,c):-1===g&&1===h?d=c:1===g&&-1===h?d=b:1===g&&1===h&&(d=Math.min(b,c));a={x:0,y:0,w:a.w*d,h:a.h*d,ratio:d};switch(f){case "left":a.x=0;break;case "right":a.x=e.w-a.w;break;default:a.x=(e.w-a.w)/2}switch(k){case "top":a.y=0;break;case "bottom":a.y=e.h-a.h;
break;default:a.y=(e.h-a.h)/2}return a}}});