// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(function(){return function(){function b(a,d){this.layerExtent=4096;this._features=[];this.layer=a;this.zoom=d;this._filter=a.getFeatureFilter()}var c=b.prototype;c.pushFeature=function(a){this._filter&&!this._filter.filter(a,this.zoom)||this._features.push(a)};c.hasFeatures=function(){return 0<this._features.length};return b}()});