// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(function(){return function(){function a(c){this._pos=0;this._buffer=c;this._i32View=new Int32Array(this._buffer);this._f32View=new Float32Array(this._buffer)}var b=a.prototype;b.readInt32=function(){return this._i32View[this._pos++]};b.readF32=function(){return this._f32View[this._pos++]};return a}()});