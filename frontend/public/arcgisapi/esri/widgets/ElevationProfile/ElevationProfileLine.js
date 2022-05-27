// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../chunks/_rollupPluginBabelHelpers ../../chunks/tslib.es6 ../../core/has ../../core/maybe ../../core/Logger ../../core/accessorSupport/ensureType ../../core/handleUtils ../../core/accessorSupport/decorators/property ../../core/jsonMap ../../core/accessorSupport/decorators/subclass ../../core/urlUtils ../../core/uuid ../../portal/support/resourceExtension ../../core/arrayUtils ../../geometry/Point ../../core/Evented ../../Color ../../core/watchUtils ./support/unitUtils".split(" "),
function(b,l,d,D,f,E,F,u,e,G,v,H,w,I,x,y,z,m,k,n){b.ElevationProfileLine=function(p){function g(c){var a=p.call(this,c)||this;a.type=null;a.id=w.generateUUID();a.title=null;a.color=new m("#000000");a.visible=!0;a.result=null;a.effectiveUnits=null;a.hoveredChartPosition=null;a.numSamplesForPreview=30;a.numSamplesPerChunk=200;a.showFill=!0;a._onChange=()=>{a.emit("change")};return a}l._inheritsLoose(g,p);var h=g.prototype;h.queryElevation=function(){throw Error("not implemented");};h.attach=function(c){this._viewModel=
c;return u.handlesGroup([k.init(c,"effectiveUnits",()=>{this.effectiveUnits=c.effectiveUnits}),k.init(c,"hoveredChartPosition",()=>{this.hoveredChartPosition=c.hoveredChartPosition}),k.init(c,["input","minDemResolution","queue"],this._onChange)])};h.toggleVisibility=function(c){this.visible=void 0===c?!this.visible:c};h._getPoint=function(c){const {samples:a,result:q}=this;if(f.isNone(a)||f.isNone(q))return null;const r=a.length;if(0===r)return null;const {x:A,y:B,z:t}=x.binaryFindClosest(a,c*a[r-
1].distance,C=>C.distance);return f.isNone(t)?null:new y({x:A,y:B,z:t,spatialReference:q.spatialReference})};l._createClass(g,[{key:"progress",get:function(){return f.isSome(this.result)&&this.visible?this.result.progress:0}},{key:"samples",get:function(){return n.convertSamples(this.result,this.effectiveUnits)}},{key:"statistics",get:function(){return n.convertStatistics(this.result,this.effectiveUnits)}},{key:"hoveredPoint",get:function(){return f.applySome(this.hoveredChartPosition,c=>this._getPoint(c))}},
{key:"minDemResolution",get:function(){return null}}]);return g}(z.EventedAccessor);d.__decorate([e.property({nonNullable:!0})],b.ElevationProfileLine.prototype,"id",void 0);d.__decorate([e.property({nonNullable:!0})],b.ElevationProfileLine.prototype,"title",void 0);d.__decorate([e.property({type:m,nonNullable:!0})],b.ElevationProfileLine.prototype,"color",void 0);d.__decorate([e.property({nonNullable:!0})],b.ElevationProfileLine.prototype,"visible",void 0);d.__decorate([e.property({readOnly:!0})],
b.ElevationProfileLine.prototype,"progress",null);d.__decorate([e.property({readOnly:!0})],b.ElevationProfileLine.prototype,"samples",null);d.__decorate([e.property({readOnly:!0})],b.ElevationProfileLine.prototype,"statistics",null);d.__decorate([e.property()],b.ElevationProfileLine.prototype,"result",void 0);d.__decorate([e.property()],b.ElevationProfileLine.prototype,"effectiveUnits",void 0);d.__decorate([e.property()],b.ElevationProfileLine.prototype,"hoveredChartPosition",void 0);d.__decorate([e.property({readOnly:!0})],
b.ElevationProfileLine.prototype,"minDemResolution",null);d.__decorate([e.property()],b.ElevationProfileLine.prototype,"_viewModel",void 0);b.ElevationProfileLine=d.__decorate([v.subclass("esri.widgets.ElevationProfile.ElevationProfileLine")],b.ElevationProfileLine);b.default=b.ElevationProfileLine;Object.defineProperty(b,"__esModule",{value:!0})});