/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as e}from"../chunks/tslib.es6.js";import"../chunks/ArrayPool.js";import"../chunks/object.js";import"../chunks/deprecate.js";import"../core/lang.js";import"../config.js";import"../chunks/Logger.js";import"../chunks/string.js";import"../chunks/metadata.js";import{property as t}from"../core/accessorSupport/decorators/property.js";import"../core/Accessor.js";import"../chunks/PropertyOrigin.js";import{e as i}from"../core/scheduling.js";import{o as s}from"../core/promiseUtils.js";import"../chunks/Message.js";import r from"../core/Error.js";import{n as o}from"../chunks/compilerUtils.js";import"../chunks/ensureType.js";import{subclass as a}from"../core/accessorSupport/decorators/subclass.js";import"../chunks/Evented.js";import n from"../core/Collection.js";import"../chunks/JSONSupport.js";import"../chunks/Promise.js";import"../core/urlUtils.js";import{aliasOf as l}from"../core/accessorSupport/decorators/aliasOf.js";import"../core/accessorSupport/decorators/cast.js";import"../chunks/jsonMap.js";import"../chunks/enumeration.js";import"../chunks/reader.js";import"../chunks/writer.js";import"../chunks/resourceExtension.js";import"../geometry/SpatialReference.js";import"../chunks/locale.js";import{a as m,c as u}from"../chunks/number.js";import"../intl.js";import"../kernel.js";import"../request.js";import"../chunks/assets.js";import"../geometry/Geometry.js";import"../geometry/Point.js";import"../chunks/Ellipsoid.js";import"../geometry/support/webMercatorUtils.js";import"../geometry/Extent.js";import{c}from"../chunks/mathUtils2.js";import"../chunks/zmUtils.js";import"../geometry/Multipoint.js";import"../geometry/Polygon.js";import"../chunks/extentUtils.js";import"../geometry/Polyline.js";import"../chunks/typeUtils.js";import"../geometry/support/jsonUtils.js";import"../geometry.js";import"../layers/support/CodedValueDomain.js";import"../layers/support/Domain.js";import"../layers/support/InheritedDomain.js";import"../layers/support/RangeDomain.js";import"../chunks/domains.js";import"../chunks/arcadeOnDemand.js";import"../layers/support/fieldUtils.js";import"../core/Handles.js";import"../chunks/CollectionFlattener.js";import{t as d,o as p}from"../chunks/timeUtils.js";import"../TimeExtent.js";import v from"../TimeInterval.js";import{init as h}from"../core/watchUtils.js";import"../chunks/Widgets.js";import"../core/HandleOwner.js";import"../chunks/domUtils.js";import"../chunks/widgetUtils.js";import"../chunks/projector.js";import{a as y}from"../chunks/accessibleHandler.js";import{m as w}from"../chunks/messageBundle.js";import{r as f}from"../chunks/renderable.js";import{j}from"../chunks/index.js";import k from"./Widget.js";import"../layers/support/TimeInfo.js";import"../chunks/TemporalLayer.js";import{t as b}from"../chunks/throttle.js";import"./Slider/SliderViewModel.js";import g from"./Slider.js";import _ from"./TimeSlider/TimeSliderViewModel.js";const x="esri-icon-time-clock",T="esri-widget",M="esri-widget--button",S="esri-button--disabled",C="esri-disabled",E="esri-time-slider",F="esri-time-slider__mode--",U="esri-time-slider__layout--",D="esri-time-slider__row",L="esri-time-slider__animation",O="esri-time-slider__animation-button",V="esri-icon-play",P="esri-icon-pause",A="esri-time-slider__time-extent",R="esri-time-slider__time-extent-group",W="esri-time-slider__time-extent-date",I="esri-time-slider__time-extent-time",$="esri-time-slider__time-extent-separator",z="esri-time-slider__min",H="esri-time-slider__min-date",N="esri-time-slider__slider",q="majorTick",B="minorTick",G="esri-time-slider__max",J="esri-time-slider__max-date",K="esri-time-slider__previous",Q="esri-time-slider__previous-button",X="esri-icon-reverse",Y="esri-time-slider__next",Z="esri-time-slider__next-button",ee="esri-icon-forward",te=new n([{minor:new v({value:100,unit:"milliseconds"}),major:new v({value:1,unit:"seconds"}),format:{second:"numeric"}},{minor:new v({value:500,unit:"milliseconds"}),major:new v({value:5,unit:"seconds"}),format:{second:"numeric"}},{minor:new v({value:1,unit:"seconds"}),major:new v({value:20,unit:"seconds"}),format:{minute:"numeric",second:"numeric"}},{minor:new v({value:2,unit:"seconds"}),major:new v({value:30,unit:"seconds"}),format:{minute:"numeric",second:"numeric"}},{minor:new v({value:10,unit:"seconds"}),major:new v({value:1,unit:"minutes"}),format:{minute:"numeric"}},{minor:new v({value:15,unit:"seconds"}),major:new v({value:5,unit:"minutes"}),format:{hour:"numeric",minute:"numeric"}},{minor:new v({value:1,unit:"minutes"}),major:new v({value:20,unit:"minutes"}),format:{hour:"numeric",minute:"numeric"}},{minor:new v({value:5,unit:"minutes"}),major:new v({value:2,unit:"hours"}),format:{hour:"numeric",minute:"numeric"}},{minor:new v({value:15,unit:"minutes"}),major:new v({value:6,unit:"hours"}),format:{hour:"numeric",minute:"numeric"}},{minor:new v({value:1,unit:"hours"}),major:new v({value:1,unit:"days"}),format:{day:"numeric",month:"short"}},{minor:new v({value:6,unit:"hours"}),major:new v({value:1,unit:"weeks"}),format:{day:"numeric",month:"short"}},{minor:new v({value:1,unit:"days"}),major:new v({value:1,unit:"months"}),format:{month:"long"}},{minor:new v({value:2,unit:"days"}),major:new v({value:1,unit:"months"}),format:{month:"short"}},{minor:new v({value:3,unit:"days"}),major:new v({value:1,unit:"months"}),format:{month:"short"}},{minor:new v({value:4,unit:"days"}),major:new v({value:3,unit:"months"}),format:{month:"short",year:"numeric"}},{minor:new v({value:1,unit:"weeks"}),major:new v({value:1,unit:"years"}),format:{year:"numeric"}},{minor:new v({value:1,unit:"months"}),major:new v({value:1,unit:"years"}),format:{year:"numeric"}},{minor:new v({value:2,unit:"months"}),major:new v({value:2,unit:"years"}),format:{year:"numeric"}},{minor:new v({value:1,unit:"years"}),major:new v({value:1,unit:"decades"}),format:{year:"numeric"}},{minor:new v({value:2,unit:"years"}),major:new v({value:5,unit:"decades"}),format:{year:"numeric"}},{minor:new v({value:5,unit:"decades"}),major:new v({value:10,unit:"centuries"}),format:{era:"short",year:"numeric"}},{minor:new v({value:1,unit:"centuries"}),major:new v({value:10,unit:"centuries"}),format:{era:"short",year:"numeric"}},{minor:new v({value:2,unit:"centuries"}),major:new v({value:20,unit:"centuries"}),format:{era:"short",year:"numeric"}},{minor:new v({value:5,unit:"centuries"}),major:new v({value:50,unit:"centuries"}),format:{era:"short",year:"numeric"}},{minor:new v({value:10,unit:"centuries"}),major:new v({value:100,unit:"centuries"}),format:{era:"short",year:"numeric"}},{minor:new v({value:20,unit:"centuries"}),major:new v({value:200,unit:"centuries"}),format:{era:"short",year:"numeric"}},{minor:new v({value:50,unit:"centuries"}),major:new v({value:500,unit:"centuries"}),format:{era:"short",year:"numeric"}},{minor:new v({value:100,unit:"centuries"}),major:new v({value:1e3,unit:"centuries"}),format:{era:"short",year:"numeric"}},{minor:new v({value:200,unit:"centuries"}),major:new v({value:1e3,unit:"centuries"}),format:{era:"short",year:"numeric"}},{minor:new v({value:500,unit:"centuries"}),major:new v({value:5e3,unit:"centuries"}),format:{era:"short",year:"numeric"}},{minor:new v({value:1e3,unit:"centuries"}),major:new v({value:1e4,unit:"centuries"}),format:{era:"short",year:"numeric"}}]);let ie=class extends k{constructor(e,t){super(e,t),this._slider=new g({precision:0,visibleElements:{rangeLabels:!1},rangeLabelInputsEnabled:!1}),this._tickFormat=null,this.disabled=!1,this.effectiveStops=null,this.fullTimeExtent=null,this.iconClass=x,this.label=void 0,this.labelFormatFunction=null,this.loop=!1,this.messages=null,this.messagesCommon=null,this.mode="time-window",this.playRate=1e3,this.stops={count:10},this.tickConfigs=null,this.timeExtent=null,this.timeVisible=!1,this.values=null,this.view=null,this.viewModel=new _}initialize(){this.own([this._slider.watch("values",(e=>{var t;const s=null==(t=this.values)?void 0:t.map((e=>e.getTime()));i(e,s)||this.set("values",e.map((e=>new Date(e))))})),s(window,"resize",b((()=>this.scheduleRender()),100)),h(this,"effectiveStops",(()=>this._updateSliderSteps()))]),this._validateTimeExtent()}destroy(){this._slider.destroy(),this._tickFormat=null}get interactive(){return!this.disabled&&this.viewModel&&"disabled"!==this.viewModel.state}set layout(e){-1===["auto","compact","wide"].indexOf(e)&&(e="auto"),this._set("layout",e)}next(){}play(){}previous(){}stop(){return null}render(){var e;const{_slider:t,domNode:s,effectiveStops:r,fullTimeExtent:o,interactive:a,messagesCommon:n,mode:l,tickConfigs:u,timeVisible:c,values:d,viewModel:p}=this;if(null!=o){const{start:e,end:i}=o;if(null!=e&&null!=i){const s=e.getTime(),r=i.getTime(),o=t.min!==s||t.max!==r;if(o&&(t.min=s,t.max=r),null!=u)t.tickConfigs!==u&&(t.tickConfigs=u);else{var v,h;const e=(r-s)/(null!=(v=null==(h=t.trackElement)?void 0:h.offsetWidth)?v:400),i=te.find((t=>t.minor.toMilliseconds()>3*e)),a=this._tickFormat!==i&&null!=i;if(a&&(this._tickFormat=i),o||a){const e={mode:"position",values:this._getTickPositions(i.minor),labelsVisible:!1,tickCreatedFunction:(e,t)=>{t.classList.add(B)}},s={mode:"position",values:this._getTickPositions(i.major),labelsVisible:!0,tickCreatedFunction:(e,t)=>{t.classList.add(q)},labelFormatFunction:e=>m(e,i.format)};t.tickConfigs=[e,s]}}}}const y=null==d?void 0:d.map((e=>e.getTime()));i(t.values,y)||(t.values=y),t.disabled=!a;const w=null==o?void 0:o.start,f=null==o?void 0:o.end,k=null!=(e=null==d?void 0:d.length)?e:0,b=k&&this._formatDate(d[0]),g=k&&c&&this._formatTime(d[0]),_=k>1&&"time-window"===l&&this._formatDate(d[1]),x=k>1&&"time-window"===l&&c&&this._formatTime(d[1]),{state:ie}=p,se="ready"===ie,re="playing"===ie,oe=!a||0===r.length,ae="auto"===this.layout?s.clientWidth<858?"compact":"wide":this.layout,ne=j("div",{class:L},j("button",{"aria-disabled":oe?"true":"false","aria-label":re?n.control.stop:n.control.play,bind:this,class:this.classes(M,O,oe&&S),disabled:oe,title:re?n.control.stop:n.control.play,onclick:this._playOrStopClick,type:"button"},j("div",{class:this.classes((se||oe)&&V,re&&P)}))),le=this.labelFormatFunction?j("div",{key:"extent",bind:this,class:W,"data-type":"extent","data-layout":ae,"data-date":d,afterCreate:this._createLabel,afterUpdate:this._createLabel}):[b&&j("div",{key:"start-date-group",class:R},j("div",{key:"start-date",class:W},b),g&&j("div",{key:"start-time",class:I},g)),_&&j("div",{key:"separator",class:$},"–"),b&&j("div",{key:"end-date-group",class:R},j("div",{key:"end-date",class:W},_),x&&j("div",{key:"end-time",class:I},x))],me=j("div",{class:this.classes(A,!a&&S)},[le]),ue=this.labelFormatFunction?j("div",{key:"min-date",bind:this,class:H,"data-date":w,"data-type":"min","data-layout":ae,afterCreate:this._createLabel,afterUpdate:this._createLabel}):[w&&j("div",{key:"min-date",class:H},this._formatDate(w)),w&&c&&j("div",{key:"min-time"},this._formatTime(w))],ce=j("div",{class:this.classes(z,!a&&S)},[ue]),de=j("div",{class:N},t.render()),pe=this.labelFormatFunction?j("div",{key:"max-date",bind:this,class:J,"data-date":f,"data-type":"max","data-layout":ae,afterCreate:this._createLabel,afterUpdate:this._createLabel}):[f&&j("div",{key:"max-date",class:J},this._formatDate(f)),f&&c&&j("div",{key:"max-time"},this._formatTime(f))],ve=j("div",{class:this.classes(G,!a&&S)},[pe]),he=j("div",{class:K},j("button",{"aria-disabled":oe?"true":"false","aria-label":n.pagination.previous,bind:this,class:this.classes(M,Q,(re||oe)&&S),disabled:oe,title:n.pagination.previous,onclick:this._previousClick,type:"button"},j("div",{class:X}))),ye=j("div",{class:Y},j("button",{"aria-disabled":oe?"true":"false","aria-label":n.pagination.next,bind:this,class:this.classes(M,Z,(re||oe)&&S),disabled:oe,title:n.pagination.next,onclick:this._nextClick,type:"button"},j("div",{class:ee})));return j("div",{class:this.classes(E,T,`${F}${l}`,`${U}${ae}`,!a&&C)},"wide"===ae&&j("div",{class:D},[ne,me,ce,de,ve,he,ye]),"compact"===ae&&[j("div",{key:"time-slider-row-1",class:D},[me]),j("div",{key:"time-slider-row-2",class:D},[de]),j("div",{key:"time-slider-row-3",class:D},[ce,he,ne,ye,ve])])}updateWebDocument(e){var t;null==(t=this.viewModel)||t.updateWebDocument(e)}_createLabel(e){const t=e.getAttribute("data-type"),i=e.getAttribute("data-layout"),s=e["data-date"];this.labelFormatFunction(s,t,e,i)}_getTickPositions(e){const{fullTimeExtent:t}=this,{start:i,end:s}=t,r=[],{value:o,unit:a}=e;let n=d(i,a);for(;n.getTime()<=s.getTime();)n.getTime()>=i.getTime()&&r.push(n.getTime()),n=p(n,o,a);return r}_formatDate(e){return m(e,u("short-date"))}_formatTime(e){return m(e,u("long-time"))}_updateSliderSteps(){this._slider.steps=this.effectiveStops&&this.effectiveStops.length>0?this.effectiveStops.map((e=>e.getTime())):null}_validateTimeExtent(){if(this.fullTimeExtent&&this.mode&&this.values){if(!Array.isArray(this.values))throw new r("time-slider:invalid-values","Values must be an array of dates.");if(0===this.values.length||this.values.some((e=>!(e instanceof Date))))throw new r("time-slider:invalid-values","Values must be an array of dates.");switch(this.values=this.values.map((e=>{const t=e.getTime(),i=c(t,this.fullTimeExtent.start.getTime(),this.fullTimeExtent.end.getTime());return new Date(i)})),this.mode){case"instant":case"cumulative-from-start":case"cumulative-from-end":this.values.length>1&&this.values.splice(1);break;case"time-window":1===this.values.length?this.values.push(this.fullTimeExtent.end):this.values.length>2&&this.values.splice(2),this.values.sort(((e,t)=>e.getTime()-t.getTime()));break;default:o(this.mode)}}}_playOrStopClick(){switch(this.viewModel.state){case"ready":this.viewModel.play();break;case"playing":this.viewModel.stop();break;case"disabled":break;default:o(this.viewModel.state)}}_previousClick(){this.viewModel.previous()}_nextClick(){this.viewModel.next()}};e([t()],ie.prototype,"disabled",void 0),e([l("viewModel.effectiveStops"),f()],ie.prototype,"effectiveStops",void 0),e([l("viewModel.fullTimeExtent"),f()],ie.prototype,"fullTimeExtent",void 0),e([t()],ie.prototype,"iconClass",void 0),e([t({dependsOn:["disabled","viewModel.state"],readOnly:!0}),f()],ie.prototype,"interactive",null),e([t({aliasOf:{source:"messages.widgetLabel",overridable:!0}})],ie.prototype,"label",void 0),e([t(),f()],ie.prototype,"labelFormatFunction",void 0),e([t({value:"auto"}),f()],ie.prototype,"layout",null),e([l("viewModel.loop")],ie.prototype,"loop",void 0),e([t(),f(),w("esri/widgets/TimeSlider/t9n/TimeSlider")],ie.prototype,"messages",void 0),e([t(),f(),w("esri/t9n/common")],ie.prototype,"messagesCommon",void 0),e([l("viewModel.mode"),f()],ie.prototype,"mode",void 0),e([l("viewModel.playRate")],ie.prototype,"playRate",void 0),e([l("viewModel.stops")],ie.prototype,"stops",void 0),e([t(),f()],ie.prototype,"tickConfigs",void 0),e([l("viewModel.timeExtent")],ie.prototype,"timeExtent",void 0),e([t({nonNullable:!0}),f()],ie.prototype,"timeVisible",void 0),e([l("viewModel.values"),f()],ie.prototype,"values",void 0),e([l("viewModel.view")],ie.prototype,"view",void 0),e([t({type:_}),f(["viewModel.state"])],ie.prototype,"viewModel",void 0),e([l("viewModel.next")],ie.prototype,"next",null),e([l("viewModel.play")],ie.prototype,"play",null),e([l("viewModel.previous")],ie.prototype,"previous",null),e([l("viewModel.stop")],ie.prototype,"stop",null),e([y()],ie.prototype,"_playOrStopClick",null),e([y()],ie.prototype,"_previousClick",null),e([y()],ie.prototype,"_nextClick",null),ie=e([a("esri.widgets.TimeSlider")],ie);var se=ie;export default se;
