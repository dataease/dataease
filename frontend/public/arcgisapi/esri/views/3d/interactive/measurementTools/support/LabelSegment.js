// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../../../../../chunks/vec3f64","../../../../../chunks/vec3","./viewUtils"],function(b,a,f){return function(){function g(){this.origin=b.create();this.start=b.create();this.end=b.create()}g.prototype.update=function(c,d,e){a.copy(this.start,c);a.copy(this.end,d);if(e)switch(e){case "start":a.copy(this.origin,this.start);break;case "center":f.midpoint([c,d],this.origin);break;case "end":a.copy(this.origin,this.end);break;default:a.copy(this.origin,e)}else f.midpoint([c,d],this.origin)};return g}()});