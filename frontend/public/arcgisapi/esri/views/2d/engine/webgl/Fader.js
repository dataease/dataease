// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../../../../chunks/_rollupPluginBabelHelpers"],function(d){return function(){function b(a=400){this.duration=a;this._value=this._elapsed=this._lastTime=0;this._finished=!1}var c=b.prototype;c.reset=function(){this._value=this._elapsed=this._lastTime=0};c.step=function(){const a=performance.now();if(0===this._lastTime)return this._lastTime=a,this._value=0,!0;if(this._elapsed>=this.duration)return!0;this._elapsed+=a-this._lastTime;this._lastTime=a;this._value=Math.min(this._elapsed/this.duration,
1);return!1};d._createClass(b,[{key:"value",get:function(){return this._value}}]);return b}()});