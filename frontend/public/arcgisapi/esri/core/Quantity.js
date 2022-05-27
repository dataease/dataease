// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../chunks/_rollupPluginBabelHelpers","./unitUtils"],function(f,a){return function(){function b(c,d){this.measure=a.measureForUnit(d);this.value=c;this.unit=d}var e=b.prototype;e.toUnit=function(c){return new b(a.convertUnit(this.value,this.unit,c),c)};e.toBaseUnit=function(){return this.toUnit(a.baseUnitForUnit(this.unit))};f._createClass(b,[{key:"isBaseUnit",get:function(){return a.isBaseUnit(this.unit)}}]);return b}()});