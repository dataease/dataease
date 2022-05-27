// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../chunks/vec2f64"],function(a,d){let k=function(){function b(){this.offset=d.create();this.scale=0;this.tile=null}var c=b.prototype;c.init=function(e,f,g,h){this.tile=e;this.offset[0]=f;this.offset[1]=g;this.scale=h};c.dispose=function(){this.tile=null;this.offset[0]=0;this.scale=this.offset[1]=0};return b}();a.UpsampleInfo=k;Object.defineProperty(a,"__esModule",{value:!0})});