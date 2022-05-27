// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../chunks/_rollupPluginBabelHelpers","../../../../core/maybe","../../../../layers/support/timeUtils"],function(c,e,f,g){let h=function(){function b(a=!0){this.enabled=a;this._time=0}var d=b.prototype;d.advance=function(a){this.enabled&&(this._time+=a)};d.stopAtTime=function(a){this._forcedTime=a};e._createClass(b,[{key:"time",get:function(){return f.isSome(this._forcedTime)?this._forcedTime:g.Milliseconds(this._time)}}]);return b}();c.AnimationTimer=h;Object.defineProperty(c,
"__esModule",{value:!0})});