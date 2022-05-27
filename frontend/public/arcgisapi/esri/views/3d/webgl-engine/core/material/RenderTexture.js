// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../../core/maybe"],function(e,c){let l=function(){function f(a,b){this._textureRep=a;this._textureId=b;this._textureRef=c.applySome(this._textureId,d=>this._textureRep.acquire(d))}var g=f.prototype;g.dispose=function(){this._textureRef=c.applySome(this._textureId,a=>{this._textureRep.release(a)})};g.bind=function(a,b,d,h,k){c.isSome(this._textureRef)&&(b.setUniform1i(d,h),a.bindTexture(this._textureRef.glTexture,h),k&&(a=this._textureRef.glTexture,b.setUniform2f(k,a.descriptor.width,
a.descriptor.height)))};return f}();e.RenderTexture=l;Object.defineProperty(e,"__esModule",{value:!0})});