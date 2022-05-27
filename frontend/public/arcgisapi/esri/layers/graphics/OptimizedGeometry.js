// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../../chunks/_rollupPluginBabelHelpers"],function(h){return function(){function d(b=[],a=[],c=!1){this.lengths=null!=b?b:[];this.coords=null!=a?a:[];this.hasIndeterminateRingOrder=c}var f=d.prototype;f.forEachVertex=function(b){let a=0;this.lengths.length||b(this.coords[0],this.coords[1]);for(let c=0;c<this.lengths.length;c++){const g=this.lengths[c];for(let e=0;e<g;e++)b(this.coords[2*(e+a)],this.coords[2*(e+a)+1]);a+=g}};f.clone=function(){return new d(this.lengths.slice(),this.coords.slice(),
this.hasIndeterminateRingOrder)};h._createClass(d,[{key:"isPoint",get:function(){return 0===this.lengths.length}}]);return d}()});