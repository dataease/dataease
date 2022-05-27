// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("../../chunks/_rollupPluginBabelHelpers ../../chunks/tslib.es6 ../../core/has ../../core/Logger ../../core/accessorSupport/decorators/property ../../core/accessorSupport/decorators/aliasOf ../../core/accessorSupport/decorators/cast ../../core/jsonMap ../../core/accessorSupport/decorators/subclass ../../core/urlUtils ../../core/uuid ../../portal/support/resourceExtension ../../Color ../support/widgetUtils ../support/decorators/messageBundle ../support/decorators/renderable ../../chunks/index ./SmartMappingSliderBase ./OpacitySlider/OpacitySliderViewModel".split(" "),
function(x,d,c,E,l,u,y,F,z,G,H,I,A,J,B,p,g,C,D){var q;const v={trackFillColor:new A([0,121,193])};c=q=function(w){function m(a,b){a=w.call(this,a,b)||this;a._bgFillId=null;a._rampFillId=null;a.label=void 0;a.messages=null;a.stops=null;a.style={...v};a.viewModel=new D;a.zoomOptions=null;a._rampFillId=`${a.id}-ramp-fill`;a._bgFillId=`${a.id}-bg-fill`;return a}x._inheritsLoose(m,w);var n=m.prototype;n.castStyle=function(a){return{...v,...a}};m.fromVisualVariableResult=function(a,b){const {visualVariable:{stops:e},
statistics:f}=a,{avg:h,max:k,min:r,stddev:t}=f;return new q({max:k,min:r,stops:e,histogramConfig:{average:h,standardDeviation:t,bins:b?b.bins:[]}})};n.updateFromVisualVariableResult=function(a,b){const {visualVariable:{stops:e},statistics:f}=a,{avg:h,max:k,min:r,stddev:t}=f;this.set({max:k,min:r,stops:e,histogramConfig:{average:h,standardDeviation:t,bins:b?b.bins:[]}})};n.render=function(){const {state:a,label:b}=this,e="disabled"===a,f=this.classes("esri-opacity-slider","esri-widget","esri-widget--panel",
{["esri-disabled"]:e});return g.jsx("div",{"aria-label":b,class:f},e?null:this.renderContent(this.renderRamp(),"esri-opacity-slider__slider-container","esri-opacity-slider__histogram-container"))};n.renderRamp=function(){const {_bgFillId:a,_rampFillId:b,style:{trackFillColor:e},viewModel:f,zoomOptions:h}=this,k=f.getStopInfo(e);return g.jsx("div",{class:"esri-opacity-slider__ramp"},g.jsx("svg",{xmlns:"http://www.w3.org/2000/svg"},g.jsx("defs",null,this.renderRampFillDefinition(b,k),this.renderBackgroundFillDefinition(a)),
g.jsx("rect",{x:"0",y:"0",fill:`url(#${a})`,height:"100%",width:"100%"}),g.jsx("rect",{x:"0",y:"0",fill:`url(#${b})`,height:"100%",width:"100%"})),h?this.renderZoomCaps():null)};return m}(C.SmartMappingSliderBase);d.__decorate([l.property({aliasOf:{source:"messages.widgetLabel",overridable:!0}})],c.prototype,"label",void 0);d.__decorate([l.property(),p.renderable(),B.messageBundle("esri/widgets/smartMapping/OpacitySlider/t9n/OpacitySlider")],c.prototype,"messages",void 0);d.__decorate([u.aliasOf("viewModel.stops")],
c.prototype,"stops",void 0);d.__decorate([l.property(),p.renderable()],c.prototype,"style",void 0);d.__decorate([y.cast("style")],c.prototype,"castStyle",null);d.__decorate([l.property(),p.renderable("viewModel.hasTimeData viewModel.inputFormatFunction viewModel.inputParseFunction viewModel.labelFormatFunction viewModel.max viewModel.min viewModel.stops viewModel.values viewModel.zoomOptions".split(" "))],c.prototype,"viewModel",void 0);d.__decorate([u.aliasOf("viewModel.zoomOptions")],c.prototype,
"zoomOptions",void 0);return c=q=d.__decorate([z.subclass("esri.widgets.smartMapping.OpacitySlider")],c)});