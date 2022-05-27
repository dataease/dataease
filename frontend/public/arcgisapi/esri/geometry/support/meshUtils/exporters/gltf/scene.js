// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(b){let e=function(){function c(){this.name="";this.nodes=[]}var d=c.prototype;d.addNode=function(a){if(0<=this.nodes.indexOf(a))throw Error("Node already added");this.nodes.push(a)};d.forEachNode=function(a){this.nodes.forEach(a)};return c}();b.Scene=e;Object.defineProperty(b,"__esModule",{value:!0})});