// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../../../../chunks/_rollupPluginBabelHelpers","../../support/geometryUtils"],function(d,e){const a=e.boundedPlane.create();return function(){function b(){this._worldPlane=a}d._createClass(b,[{key:"isEnabled",get:function(){return this.plane!==a}},{key:"plane",get:function(){return this._worldPlane},set:function(c){this._worldPlane=c?c:a}}]);return b}()});