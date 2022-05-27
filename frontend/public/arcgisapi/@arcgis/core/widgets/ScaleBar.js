/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as e}from"../chunks/tslib.es6.js";import"../chunks/ArrayPool.js";import"../chunks/object.js";import"../chunks/deprecate.js";import"../core/lang.js";import"../config.js";import"../chunks/Logger.js";import"../chunks/string.js";import"../chunks/metadata.js";import{property as s}from"../core/accessorSupport/decorators/property.js";import"../core/Accessor.js";import"../chunks/PropertyOrigin.js";import"../core/scheduling.js";import"../core/promiseUtils.js";import"../chunks/Message.js";import"../core/Error.js";import"../chunks/ensureType.js";import{subclass as t}from"../core/accessorSupport/decorators/subclass.js";import"../chunks/Evented.js";import"../core/Collection.js";import"../chunks/JSONSupport.js";import"../chunks/Promise.js";import"../core/urlUtils.js";import{aliasOf as r}from"../core/accessorSupport/decorators/aliasOf.js";import{cast as i}from"../core/accessorSupport/decorators/cast.js";import"../chunks/jsonMap.js";import"../chunks/reader.js";import"../chunks/writer.js";import"../chunks/resourceExtension.js";import"../geometry/SpatialReference.js";import"../chunks/locale.js";import"../chunks/number.js";import"../intl.js";import"../kernel.js";import"../request.js";import"../chunks/assets.js";import"../geometry/Geometry.js";import"../geometry/Point.js";import"../chunks/Ellipsoid.js";import"../geometry/support/webMercatorUtils.js";import"../geometry/Extent.js";import"../chunks/zmUtils.js";import"../geometry/Multipoint.js";import"../geometry/Polygon.js";import"../chunks/extentUtils.js";import"../geometry/Polyline.js";import"../chunks/typeUtils.js";import"../geometry/support/jsonUtils.js";import"../geometry.js";import{c as o}from"../chunks/screenUtils.js";import"../core/Handles.js";import"../chunks/unitUtils.js";import"../geometry/support/normalizeUtils.js";import{whenTrue as a,watch as l}from"../core/watchUtils.js";import"../chunks/geodesicConstants.js";import"../geometry/support/geodesicUtils.js";import"../chunks/domUtils.js";import"../chunks/widgetUtils.js";import"../chunks/projector.js";import{m as n}from"../chunks/messageBundle.js";import{r as c}from"../chunks/renderable.js";import{j as p}from"../chunks/index.js";import m from"./Widget.js";import u from"./ScaleBar/ScaleBarViewModel.js";const d="esri-scale-bar esri-widget",h="esri-scale-bar__label-container",j="esri-scale-bar__label-container--ruler",b="esri-scale-bar__label-container--line",_="esri-scale-bar__label-container--top",g="esri-scale-bar__label-container--bottom",y="esri-scale-bar__label",k="esri-scale-bar__line",v="esri-scale-bar__line--top",w="esri-scale-bar__line--bottom",f="esri-scale-bar__ruler",U="esri-scale-bar__ruler-block",S="esri-scale-bar__bar-container",M="esri-scale-bar__bar-container--ruler",O="esri-scale-bar__bar-container--line",R="esri-disabled";function B(e){return 2*e}let x=class extends m{constructor(e,s){super(e,s),this.label=void 0,this.messages=null,this.unit="non-metric",this.view=null,this.viewModel=new u}initialize(){this.own([a(this,"view.stationary",(()=>this.scheduleRender())),l(this,["view.center","view.scale","view.zoom"],(()=>{this.view.stationary&&this.scheduleRender()}))])}set style(e){const s="dual"===this.unit?"line":e;this._set("style",s)}castStyle(e){return"line"===e?e:"ruler"}castUnit(e){return"metric"===e||"dual"===e?e:"non-metric"}render(){const e="disabled"===this.get("viewModel.state"),s={[R]:e};let t,r;if(!e){const{unit:e,style:s}=this,i="metric"===e||"dual"===e,o=50;if("non-metric"===e||"dual"===e){const e=this.viewModel.getScaleBarProperties(o,"non-metric");e&&(r="ruler"===s?this._renderRuler(e):this._renderLine(e,"bottom"))}if(i){const e=this.viewModel.getScaleBarProperties(o,"metric");e&&(t="ruler"===s?this._renderRuler(e):this._renderLine(e,"top"))}}return p("div",{afterCreate:this._handleRootCreateOrUpdate,afterUpdate:this._handleRootCreateOrUpdate,bind:this,class:this.classes(d,s)},t,r)}_renderRuler(e){const s=B(Math.round(e.length)),{messages:t}=this,r=t[e.unit]||t.unknownUnit,i=`${B(e.value)} ${r}`;return p("div",{class:this.classes(S,M),key:"esri-scale-bar__ruler"},p("div",{class:f,styles:{width:`${s}px`}},p("div",{class:U}),p("div",{class:U}),p("div",{class:U}),p("div",{class:U})),p("div",{class:this.classes(h,j)},p("div",{class:y},"0"),p("div",{class:y},i)))}_renderLine(e,s){const{messages:t}=this,r=t[e.unit]||t.unknownUnit,i=`${B(e.value)} ${r}`,o={[_]:"top"===s,[g]:"bottom"===s},a=p("div",{class:this.classes(h,b,o),key:"esri-scale-bar__label"},p("div",{class:y},i)),l={[v]:"top"===s,[w]:"bottom"===s},n=B(Math.round(e.length)),c=p("div",{class:this.classes(k,l),key:"esri-scale-bar__line",styles:{width:`${n}px`}});return p("div",{class:this.classes(S,O),key:"esri-scale-bar__line-container"},[c,a])}_handleRootCreateOrUpdate(e){const s=this.viewModel;if(s){const t=e.getBoundingClientRect(),r=t.left+window.pageXOffset,i=t.top+window.pageYOffset;s.scaleComputedFrom=o(r,i)}}};e([s({aliasOf:{source:"messages.widgetLabel",overridable:!0}})],x.prototype,"label",void 0),e([s(),c(),n("esri/widgets/ScaleBar/t9n/ScaleBar")],x.prototype,"messages",void 0),e([s({dependsOn:["unit"]}),c()],x.prototype,"style",null),e([i("style")],x.prototype,"castStyle",null),e([s(),c()],x.prototype,"unit",void 0),e([i("unit")],x.prototype,"castUnit",null),e([r("viewModel.view"),c()],x.prototype,"view",void 0),e([s(),c(["viewModel.state"])],x.prototype,"viewModel",void 0),x=e([t("esri.widgets.ScaleBar")],x);var C=x;export default C;
