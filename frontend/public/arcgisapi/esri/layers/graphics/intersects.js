// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./contains"],function(a,h){a.polygonIntersectsMultipoint=function(b,c,d,e,f,k){f=f?k?4:3:k?3:2;const {coords:l,lengths:n}=e;for(let m=0,g=0;m<n.length;m++,g+=f)if(h.polygonContainsCoords(b,c,d,l[g],l[g+1]))return!0;return!1};a.polygonIntersectsPoint=function(b,c,d,e){return h.polygonContainsPoint(b,c,d,e)};Object.defineProperty(a,"__esModule",{value:!0})});