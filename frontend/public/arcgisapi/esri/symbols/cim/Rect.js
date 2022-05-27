// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../../chunks/_rollupPluginBabelHelpers"],function(c){return function(){function b(a=0,d=0,e=0,f=0){this.x=a;this.y=d;this.width=e;this.height=f}b.prototype.union=function(a){this.x=Math.min(this.x,a.x);this.y=Math.min(this.y,a.y);this.width=Math.max(this.width,a.width);this.height=Math.max(this.height,a.height)};c._createClass(b,[{key:"isEmpty",get:function(){return 0>=this.width||0>=this.height}}]);return b}()});