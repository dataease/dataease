// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(function(){return function(){function c(){this.vertexData=new Map;this.vertexCount=0;this.indexData=[]}var d=c.prototype;d.clear=function(){this.vertexData.clear();this.vertexCount=0;this.indexData=[]};d.update=function(b,e,f){for(const a in b)this.vertexData.set(a,b[a]);for(const a in this.vertexData)null===b[a]&&this.vertexData.delete(a);this.vertexCount=e;this.indexData=f};return c}()});