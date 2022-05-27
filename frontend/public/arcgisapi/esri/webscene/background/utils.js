// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./Background","./ColorBackground"],function(e,a,g){a={base:a,key:"type",typeMap:{color:g}};a={types:a,json:{read:function(d){return(c,b,h)=>{if(!c)return c;for(const f in d.typeMap)if(c.type===f)return b=new d.typeMap[f],b.read(c,h),b}}(a),write:{overridePolicy:(d,c,b)=>({enabled:!b||!b.isPresentation})}}};e.backgroundProperty=a;Object.defineProperty(e,"__esModule",{value:!0})});