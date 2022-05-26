// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/core/Error",["require","exports","./tsSupport/extendsHelper","./lang","./Message"],function(a,l,g,h,k){a=function(a){function c(b,d,e){var f=a.call(this,b,d,e)||this;return f instanceof c?f:new c(b,d,e)}g(c,a);c.prototype.toJSON=function(){return{name:this.name,message:this.message,details:h.clone(this.details),dojoType:this.dojoType}};c.fromJSON=function(b){var a=new c(b.name,b.message,b.details);null!=b.dojoType&&(a.dojoType=b.dojoType);return a};return c}(k);a.prototype.type=
"error";return a});