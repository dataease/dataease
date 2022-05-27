/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{J as t}from"./jsonMap.js";const o=new t({esriGeometryPoint:"point",esriGeometryMultipoint:"multipoint",esriGeometryPolyline:"polyline",esriGeometryPolygon:"polygon",esriGeometryMultiPatch:"multipatch",mesh:"mesh"});function e(t){return o.toJSON(t)}function n(t,o,e){const n=[],i=[];let s=0,r=0;for(const l of t){const t=r;let m=l[0][0],p=l[0][1];n[r++]=m,n[r++]=p;let c=0;for(let t=1;t<l.length;++t){const o=m,e=p;m=l[t][0],p=l[t][1],c+=p*o-m*e,n[r++]=m,n[r++]=p}o(c/2),c>0?(t-s>0&&(e(s,t,n,i),s=t),i.length=0):c<0&&t-s>0?i.push(.5*(t-s)):r=t}r-s>0&&e(s,r,n,i)}export{n as a,e as t};
