// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../chunks/_rollupPluginBabelHelpers ../../chunks/tslib.es6 ../../core/has ../../core/Logger ../../core/accessorSupport/ensureType ../../core/accessorSupport/decorators/property ../../core/jsonMap ../../core/accessorSupport/decorators/subclass ../../core/urlUtils ../../core/uuid ../../portal/support/resourceExtension".split(" "),function(d,g,e,l,m,n,h,p,k,q,r,t){d.RefreshableLayer=b=>{b=function(a){function c(){var f=a.apply(this,arguments)||this;f.refreshInterval=0;return f}g._inheritsLoose(c,
a);c.prototype.refresh=function(){this.emit("refresh")};return c}(b);e.__decorate([h.property({type:Number,cast:a=>.1<=a?a:0>=a?0:.1,json:{write:!0,origins:{"web-document":{write:!0}}}})],b.prototype,"refreshInterval",void 0);return b=e.__decorate([k.subclass("esri.layers.mixins.RefreshableLayer")],b)};Object.defineProperty(d,"__esModule",{value:!0})});