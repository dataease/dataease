// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(f){function k(){return[0,0,0,1,0,0,0,0]}function l(a){return[a[0],a[1],a[2],a[3],a[4],a[5],a[6],a[7]]}function m(a,b,g,h,c,d,e,q){return[a,b,g,h,c,d,e,q]}function n(a,b,g,h,c,d,e){c*=.5;d*=.5;e*=.5;return[a,b,g,h,c*h+d*g-e*b,d*h+e*a-c*g,e*h+c*b-d*a,-c*a-d*b-e*g]}function p(a,b){return new Float64Array(a,b,8)}var r=Object.freeze({__proto__:null,create:k,clone:l,fromValues:m,fromRotationTranslationValues:n,createView:p});f.clone=l;f.create=k;f.createView=p;f.fromRotationTranslationValues=
n;f.fromValues=m;f.quat2f64=r});