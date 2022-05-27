// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(c){function e(a,b,d){a.near=b;a.far=d;return a}c.create=function(a,b){return{near:a,far:b}};c.empty=function(a){return a?e(a,Infinity,-Infinity):{near:Infinity,far:-Infinity}};c.set=e;c.union=function(a,b,d=a){d.near=Math.min(a.near,b.near);d.far=Math.max(a.far,b.far);return d};c.within=function(a,b){return a.near<=b&&b<=a.far};Object.defineProperty(c,"__esModule",{value:!0})});