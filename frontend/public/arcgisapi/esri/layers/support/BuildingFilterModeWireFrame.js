// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("../../chunks/_rollupPluginBabelHelpers ../../chunks/tslib.es6 ../../core/has ../../core/lang ../../core/Logger ../../core/accessorSupport/ensureType ../../core/accessorSupport/decorators/property ../../core/accessorSupport/decorators/enumeration ../../core/accessorSupport/decorators/subclass ../../core/urlUtils ../../core/uuid ../../portal/support/resourceExtension ../../symbols/edges/utils ./BuildingFilterMode".split(" "),function(g,b,a,h,q,r,k,l,m,t,u,v,n,p){var c;a=c=function(f){function d(){var e=
f.apply(this,arguments)||this;e.type="wire-frame";e.edges=null;return e}g._inheritsLoose(d,f);d.prototype.clone=function(){return new c({edges:h.clone(this.edges)})};return d}(p);b.__decorate([l.enumeration({wireFrame:"wire-frame"})],a.prototype,"type",void 0);b.__decorate([k.property(n.symbol3dEdgesProperty)],a.prototype,"edges",void 0);return a=c=b.__decorate([m.subclass("esri.layers.support.BuildingFilterModeWireFrame")],a)});