// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../../chunks/_rollupPluginBabelHelpers"],function(f){return function(){function b(a=null,c={},d,e){this.displayId=0;this.geohashIndexed=!1;this.geohashY=this.geohashX=0;this.geometry=a;c&&(this.attributes=c);d&&(this.centroid=d);null!=e&&(this.objectId=e)}b.prototype.weakClone=function(){const a=new b(this.geometry,this.attributes,this.centroid,this.objectId);a.displayId=this.displayId;a.geohashIndexed=this.geohashIndexed;a.geohashX=this.geohashX;a.geohashY=this.geohashY;return a};f._createClass(b,
[{key:"hasGeometry",get:function(){return!(!this.geometry||!this.geometry.coords||!this.geometry.coords.length)}}]);return b}()});