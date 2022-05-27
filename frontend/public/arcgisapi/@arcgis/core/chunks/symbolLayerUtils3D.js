/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import"./object.js";import{f as e}from"./vec3f64.js";import{u as t}from"./aaBoundingBox.js";function n(t,{isPrimitive:n,width:r,depth:o,height:s}){const c=n?10:1;if(null==r&&null==s&&null==o)return[c*t[0],c*t[1],c*t[2]];const a=e(r,o,s);let i;for(let e=0;e<3;e++){const n=a[e];if(null!=n){i=n/t[e];break}}for(let e=0;e<3;e++)null==a[e]&&(a[e]=t[e]*i);return a}const r=t(-.5,-.5,-.5,.5,.5,.5),o=t(-.5,-.5,0,.5,.5,1),s=t(-.5,-.5,0,.5,.5,.5);function c(e){switch(e){case"sphere":case"cube":case"diamond":return r;case"cylinder":case"cone":case"inverted-cone":return o;case"tetrahedron":return s;default:return}}export{n as a,c as o};
