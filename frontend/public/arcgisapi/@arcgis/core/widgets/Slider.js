/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as t}from"../chunks/tslib.es6.js";import"../chunks/ArrayPool.js";import"../chunks/object.js";import{a as e}from"../chunks/deprecate.js";import"../core/lang.js";import"../config.js";import{L as i,i as n}from"../chunks/Logger.js";import"../chunks/string.js";import"../chunks/metadata.js";import{property as s}from"../core/accessorSupport/decorators/property.js";import"../core/Accessor.js";import"../chunks/PropertyOrigin.js";import"../core/scheduling.js";import{e as o}from"../core/promiseUtils.js";import"../chunks/Message.js";import"../core/Error.js";import"../chunks/ensureType.js";import{subclass as r}from"../core/accessorSupport/decorators/subclass.js";import"../chunks/Evented.js";import"../core/Collection.js";import"../chunks/Promise.js";import"../core/urlUtils.js";import{aliasOf as a}from"../core/accessorSupport/decorators/aliasOf.js";import{cast as l}from"../core/accessorSupport/decorators/cast.js";import"../chunks/jsonMap.js";import"../chunks/resourceExtension.js";import"../chunks/locale.js";import"../chunks/number.js";import{substitute as h}from"../intl.js";import"../kernel.js";import"../request.js";import"../chunks/assets.js";import"../core/Handles.js";import"../core/watchUtils.js";import"../chunks/domUtils.js";import{s as u}from"../chunks/widgetUtils.js";import"../chunks/projector.js";import{m as d}from"../chunks/messageBundle.js";import{r as c}from"../chunks/renderable.js";import{j as m}from"../chunks/index.js";import _ from"./Widget.js";import p from"./Slider/SliderViewModel.js";const v="esri-slider",g="esri-slider--reversed",b="esri-slider--horizontal",f="esri-slider--vertical",x="esri-slider__content",E="esri-slider__extra-content",P="esri-slider__track",y="esri-slider__ticks",k="esri-slider__tick",I="esri-slider__tick-label",A="esri-slider__max",L="esri-slider__min",M="esri-slider__max--interactive",S="esri-slider__min--interactive",w="esri-slider__range-input",C="esri-slider__anchor",F="esri-slider__anchor--moving",D="esri-slider__anchor--moved",V="esri-slider__anchor-",T="esri-slider__segment",j="esri-slider__segment-",O="esri-slider__segment--interactive",$="esri-slider__thumb",z="esri-slider__label",H="esri-slider__label--interactive",U="esri-slider__label-input",N="esri-widget",R="esri-disabled",B="esri-hidden",W={showInput:"Enter",hideInput1:"Enter",hideInput2:"Escape",hideInput3:"Tab",moveAnchorUp:"ArrowUp",moveAnchorDown:"ArrowDown",moveAnchorLeft:"ArrowLeft",moveAnchorRight:"ArrowRight",moveAnchorToMax:"End",moveAnchorToMin:"Home"},K=i.getLogger("esri.widgets.Slider"),X={labels:!1,rangeLabels:!1};let Y=class extends _{constructor(t,e){super(t,e),this._activeLabelInputIndex=null,this._anchorElements=[],this._baseNode=null,this._dragged=!1,this._dragStartInfo=null,this._focusedAnchorIndex=null,this._isMinInputActive=!1,this._isMaxInputActive=!1,this._labelElements=[],this._lastMovedHandleIndex=null,this._observer=null,this._positionPrecision=5,this._segmentDragStartInfo=null,this._segmentElements=[],this._thumbElements=[],this._tickElements=[],this._trackHeight=null,this._trackWidth=null,this._zIndices=null,this._zIndexOffset=3,this.disabled=!1,this.extraNodes=[],this.draggableSegmentsEnabled=!0,this.inputCreatedFunction=null,this.inputFormatFunction=null,this.inputParseFunction=null,this.label=void 0,this.labelInputsEnabled=!1,this.labelFormatFunction=null,this.labels=null,this.max=null,this.messages=null,this.min=null,this.precision=4,this.rangeLabelInputsEnabled=!1,this.snapOnClickEnabled=!0,this.steps=null,this.thumbsConstrained=!0,this.thumbCreatedFunction=null,this.tickConfigs=null,this.trackElement=null,this.values=null,this.viewModel=new p,this.visibleElements={...X},this._observer=new ResizeObserver((()=>this.scheduleRender())),this._onAnchorPointerDown=this._onAnchorPointerDown.bind(this),this._onAnchorPointerMove=this._onAnchorPointerMove.bind(this),this._onAnchorPointerUp=this._onAnchorPointerUp.bind(this),this._onLabelPointerDown=this._onLabelPointerDown.bind(this),this._onLabelPointerUp=this._onLabelPointerUp.bind(this),this._onSegmentPointerDown=this._onSegmentPointerDown.bind(this),this._onSegmentPointerMove=this._onSegmentPointerMove.bind(this),this._onSegmentPointerUp=this._onSegmentPointerUp.bind(this),this._onTrackPointerDown=this._onTrackPointerDown.bind(this)}destroy(){document.removeEventListener("pointerup",this._onLabelPointerUp),document.removeEventListener("pointermove",this._onLabelPointerMove),document.removeEventListener("pointerup",this._onAnchorPointerUp),document.removeEventListener("pointermove",this._onAnchorPointerMove)}set labelsVisible(t){e(K,"labelsVisible",{replacement:"visibleElements.labels",version:"4.15"}),this.visibleElements={...this.visibleElements,labels:t}}set layout(t){-1===["vertical","vertical-reversed","horizontal","horizontal-reversed"].indexOf(t)&&(t="horizontal"),this._set("layout",t)}set rangeLabelsVisible(t){e(K,"rangeLabelsVisible",{replacement:"visibleElements.rangeLabels",version:"4.15"}),this.visibleElements={...this.visibleElements,rangeLabels:t}}get state(){const{_activeLabelInputIndex:t,_isMaxInputActive:e,_isMinInputActive:i,_dragStartInfo:s,_segmentDragStartInfo:o,viewModel:r}=this,a=n(s)||n(o);return!(null===t&&!e&&!i)?"editing":a?"dragging":r.state}castVisibleElements(t){return{...X,...t}}render(){const{label:t}=this,e=this.classes(v,N,this._isHorizontalLayout()?b:f,this._isReversedLayout()?g:null,this._isDisabled()?R:null);return this._storeTrackDimensions(),m("div",{afterCreate:this._afterBaseNodeCreate,bind:this,"aria-label":t,class:e,"touch-action":"none"},this.renderContent())}toNextStep(t){this._toStep(t,1)}toPreviousStep(t){this._toStep(t,-1)}renderContent(){const{max:t,min:e}=this;if(n(e)&&n(t)&&!(e>t))return[this.renderMin(),this.renderSliderContainer(),this.renderMax()]}renderSliderContainer(){return m("div",{key:"slider-container",bind:this,class:x},this.renderTrackElement(),this.renderTicksContainer(),this.renderExtraContentElements())}renderTrackElement(){return m("div",{afterCreate:this._afterTrackCreate,bind:this,class:P,"data-node-ref":"trackElement","touch-action":"none"},this.renderSegmentElements(),this.renderAnchorElements())}renderSegmentElements(){if(!this.trackElement||!this.values||!this.values.length)return;const t=this.values.length+1,e=[];for(let i=0;i<t;i++)e.push(this.renderSegmentElement(i));return e}renderSegmentElement(t){const{_trackHeight:e,_trackWidth:i,draggableSegmentsEnabled:s,id:o,state:r,values:a}=this,l=this._isHorizontalLayout(),h=l?i:e,u=t===a.length?null:t,d=0===t?null:t-1,c=n(u),_=n(d);let p,v;const g=[...a].sort(((t,e)=>t-e));this._isReversedLayout()?(p=_?this._positionFromValue(g[d]):l?h:0,v=c?this._positionFromValue(g[u]):l?0:h):(p=c?this._positionFromValue(g[u]):l?h:0,v=_?this._positionFromValue(g[d]):l?0:h);const b=this._applyPrecisionToPosition(100*v/h),f=(p-v)/h,x=l?`transform: translate(${b}%, 0px) scale(${f}, 1);`:`transform: translate(0px, ${b}%) scale(1, ${f});`,E=this.classes(T,j+t,s&&c&&_&&"disabled"!==r?O:null);return m("div",{afterCreate:this._afterSegmentCreate,afterRemoved:this._afterSegmentRemoved,bind:this,class:E,"data-max-thumb-index":u,"data-min-thumb-index":d,"data-segment-index":t,key:`${o}-segment-${t}`,style:x,"touch-action":"none"})}renderAnchorElements(){const{trackElement:t,values:e}=this;if(e&&e.length)return this._zIndices=e.map(((t,i)=>{const n=this._positionFromValue(t),s=this._positionToPercent(n),o=(this._isHorizontalLayout()?s>50:s<50)?-1:1;return this._zIndexOffset+(e.length+o*i)})),t&&e&&e.length?e.map(((t,e)=>this.renderAnchorElement(t,e))):null}renderAnchorElement(t,e){const i=this._positionFromValue(t),s=this._valueFromPosition(i);if(!n(s)||isNaN(s))return;const{_dragStartInfo:o,_lastMovedHandleIndex:r,id:a,layout:l,values:u,visibleElements:{labels:d}}=this,c=o&&o.index===e,_=r===e,p=this.classes(C,V+e,c?F:null,_?D:null),v=this.labels.values[e],g=this._getStyleForAnchor(t,e,c||_),{min:b,max:f}=this.viewModel.getBoundsForValueAtIndex(e),{messages:x}=this,E=2===u.length?h(0===e?x.rangeMinimum:x.rangeMaximum,{value:t}):v,P=1===u.length?null:0===e?`${a}-handle-${e+1}`:e===u.length-1?`${a}-handle-${e-1}`:`${a}-handle-${e-1} ${a}-handle-${e+1}`;return m("div",{afterCreate:this._afterAnchorCreate,afterUpdate:this._afterAnchorUpdate,afterRemoved:this._afterAnchorRemoved,"aria-controls":P,"aria-label":x.sliderValue,"aria-labelledby":d?`${a}-label-${e}`:null,"aria-orientation":l,"aria-valuemax":f.toString(),"aria-valuemin":b.toString(),"aria-valuenow":t.toString(),"aria-valuetext":E,bind:this,class:p,"data-thumb-index":e,"data-value":t,id:`${a}-handle-${e}`,key:`${a}-handle-${e}`,onkeydown:this._onAnchorKeyDown,"touch-action":"none",role:"slider",style:g,tabIndex:0},m("span",{afterCreate:this._afterThumbCreate,afterRemoved:this._afterThumbRemoved,bind:this,class:$,"data-thumb-index":e,"touch-action":"none"}),this.renderThumbLabel(e))}renderThumbLabel(t){const{id:e,labels:i,labelInputsEnabled:n,state:s}=this,o=this.visibleElements.labels,r=i.values[t],a=this.classes(z,o?null:B,n&&"disabled"!==s?H:null);return m("span",{afterCreate:this._afterLabelCreate,afterRemoved:this._afterLabelRemoved,"aria-hidden":!o,bind:this,class:a,"data-thumb-index":t,key:`${e}-label-${t}`,id:`${e}-label-${t}`,role:n?"button":null,"touch-action":"none"},this._activeLabelInputIndex===t?this.renderValueInput(t):r)}renderValueInput(t){const e=this.values[t];return m("input",{afterCreate:this._afterInputCreate,"aria-label":this.messages.sliderValue,bind:this,class:U,"data-input-type":"thumb","data-input-index":t,required:!0,tabIndex:0,type:"text",value:this._formatInputValue(e,"value",t),onblur:this._onLabelInputBlur,onkeydown:this._onInputKeyDown})}renderMax(){const{_isMaxInputActive:t,labels:e,rangeLabelInputsEnabled:i,state:n}=this,s=this.visibleElements.rangeLabels,o=this.classes(A,s?null:B,i&&"disabled"!==n?M:null);return m("div",{"aria-hidden":!s,bind:this,class:o,onclick:this._onMaxLabelClick,onkeydown:this._onMaxLabelKeyDown,role:i?"button":null,tabIndex:i?0:null},t?this.renderMaxInput():e.max)}renderMin(){const{_isMinInputActive:t,labels:e,rangeLabelInputsEnabled:i,state:n}=this,s=this.visibleElements.rangeLabels,o=this.classes(L,s?null:B,i&&"disabled"!==n?S:null);return m("div",{"aria-hidden":!s,bind:this,class:o,onclick:this._onMinLabelClick,onkeydown:this._onMinLabelKeyDown,role:i?"button":null,tabIndex:i?0:null},t?this.renderMinInput():e.min)}renderMaxInput(){return m("input",{afterCreate:this._afterInputCreate,"aria-label":this.messages.maximumValue,bind:this,class:w,"data-input-type":"max",required:!0,tabIndex:0,type:"text",value:this._formatInputValue(this.max,"max"),onblur:this._onMaxInputBlur,onkeydown:this._onInputKeyDown})}renderMinInput(){return m("input",{afterCreate:this._afterInputCreate,"aria-label":this.messages.minimumValue,bind:this,class:w,"data-input-type":"min",required:!0,tabIndex:0,type:"text",value:this._formatInputValue(this.min,"min"),onblur:this._onMinInputBlur,onkeydown:this._onInputKeyDown})}renderExtraContentElements(){return m("div",{bind:this,class:E},this.extraNodes)}renderTicksContainer(){if(this.tickConfigs&&this.trackElement&&(0!==this._trackHeight||0!==this._trackWidth))return this.tickConfigs.map(((t,e)=>m("div",{key:"ticks-container",class:this.classes(y)},this.renderTicks(t,e))))}renderTicks(t,e){const{mode:i,values:n}=t;if(this._tickElements[e]=[],"position"===i){const i=Array.isArray(n)?n:[n];return this._calculateTickPositions(i).map(((i,n)=>this.renderTickGroup(t,n,e,i)))}if("percent"===i&&Array.isArray(n)){const{max:i,min:s}=this,o=i-s,r=n.map((t=>this._applyPrecisionToPosition(t/100*o+s)));return this._calculateTickPositions(r).map(((i,n)=>this.renderTickGroup(t,n,e,i)))}const s=Array.isArray(n)&&n.length?n[0]:isNaN(n)?null:n,o=this._getTickCounts(s,t);return this._calculateEquidistantTickPositions(o).map(((i,n)=>this.renderTickGroup(t,n,e,i)))}renderTickGroup(t,e,i,s){const o="position"===t.mode?Array.isArray(t.values)?t.values[e]:t.values:this._valueFromPosition(s);if(n(o)&&!isNaN(o))return m("div",{afterCreate:this._afterTickGroupCreate,bind:this,"data-config":t,"data-position":s,"data-tick-config-index":i,"data-tick-group-index":e,"data-value":o,key:`tick-group-${e}`},this.renderTickLine(t,e,i,o),t.labelsVisible?this.renderTickLabel(t,e,i,o):null)}renderTickLine(t,e,i,n){return m("div",{afterCreate:this._afterTickLineCreate,"aria-valuenow":n.toString(),bind:this,class:k,"data-tick-config-index":i,"data-tick-group-index":e,"data-value":n,key:`tick-label-${e}`,style:this._getPositionStyleForElement(n)})}renderTickLabel(t,e,i,n){const s=t.labelFormatFunction?t.labelFormatFunction(n,"tick",e):this.viewModel.getLabelForValue(n,"tick",e);return m("div",{afterCreate:this._afterTickLabelCreate,"aria-label":s,"aria-valuenow":n.toString(),"aria-valuetext":s,bind:this,class:I,"data-tick-config-index":i,"data-tick-group-index":e,"data-value":n,key:`tick-label-${e}`,style:`transform: translate(-50%); ${this._getPositionStyleForElement(n)}`},s)}_afterBaseNodeCreate(t){this._baseNode&&this._observer.unobserve(this._baseNode),this._baseNode=t,this._observer.observe(this._baseNode)}_afterTrackCreate(t){u.call(this,t),t.addEventListener("pointerdown",this._onTrackPointerDown),this.scheduleRender()}_afterSegmentCreate(t){this._segmentElements.push(t),t.addEventListener("pointerdown",this._onSegmentPointerDown)}_afterSegmentRemoved(t){const e=this._segmentElements.indexOf(t,0);e>-1&&this._segmentElements.splice(e,1),t.removeEventListener("pointerdown",this._onSegmentPointerDown)}_afterAnchorCreate(t){if(this._anchorElements.push(t),t.addEventListener("pointerdown",this._onAnchorPointerDown),this.thumbCreatedFunction){const e=t["data-thumb-index"],i=t["data-value"],n=this._thumbElements[e]||null,s=this._labelElements[e]||null;this.thumbCreatedFunction(e,i,n,s)}}_afterAnchorUpdate(t){if(n(this._focusedAnchorIndex)){t["data-thumb-index"]===this._focusedAnchorIndex&&(t.focus(),this._focusedAnchorIndex=null)}}_afterAnchorRemoved(t){const e=this._anchorElements.indexOf(t,0);e>-1&&this._anchorElements.splice(e,1),t.removeEventListener("pointerdown",this._onAnchorPointerDown)}_afterThumbCreate(t){this._thumbElements.push(t)}_afterThumbRemoved(t){const e=this._thumbElements.indexOf(t,0);e>-1&&this._thumbElements.splice(e,1)}_afterLabelCreate(t){this._labelElements.push(t),t.addEventListener("pointerdown",this._onLabelPointerDown),t.addEventListener("pointerup",this._onLabelPointerUp)}_afterLabelRemoved(t){const e=this._labelElements.indexOf(t,0);e>-1&&this._labelElements.splice(e,1),t.removeEventListener("pointerdown",this._onLabelPointerDown),t.removeEventListener("pointerup",this._onLabelPointerUp)}_afterInputCreate(t){if(t.focus(),t.select(),this.inputCreatedFunction){const e=t.getAttribute("data-input-type"),i="thumb"===e?t["data-input-index"]:null;this.inputCreatedFunction(t,e,i)}}_afterTickLineCreate(t){const e=t["data-tick-config-index"],i=t["data-tick-group-index"],n=this._tickElements[e];n[i]?n[i].line=t:n[i]={line:t,label:null}}_afterTickLabelCreate(t){const e=t["data-tick-config-index"],i=t["data-tick-group-index"],n=this._tickElements[e];n[i]?n[i].label=t:n[i]={label:t,line:null}}_afterTickGroupCreate(t){const e=t["data-config"];if(e&&e.tickCreatedFunction){const i=t["data-tick-config-index"],n=t["data-tick-group-index"],s=t["data-value"],o=this._tickElements[i][n];if(o){const t=o.line||null,i=o.label||null;e.tickCreatedFunction(s,t,i)}}}_onAnchorKeyDown(t){if(this._isDisabled()||"editing"===this.state)return;const{target:e}=t,i=o(t),{_anchorElements:s,values:r}=this,a=e["data-thumb-index"],l=s[a],h=r[a],u=this._isHorizontalLayout(),d=[W.moveAnchorUp,W.moveAnchorDown,W.moveAnchorLeft,W.moveAnchorRight];if(i===W.showInput&&this.labelInputsEnabled)this._activeLabelInputIndex=a,this.notifyChange("state");else if(d.indexOf(i)>-1){t.preventDefault();const{steps:e}=this,s=i===W.moveAnchorUp||i===W.moveAnchorRight?1:-1;if(n(e))this._toStep(a,this._isReversedLayout()?-1*s:s);else{const{precision:t}=this,e=this._getPositionOfElement(l),i=this._valueFromPosition(e),n=this._isHorizontalLayout()?s:-1*s;let o;o=0===t?this._positionFromValue(i+n):1===t?this._positionFromValue(i+.1*n):e+n,this._toPosition(a,o)}const o=this.values[a];h!==o&&this._emitThumbChangeEvent({index:a,oldValue:h,value:o})}else if(i===W.moveAnchorToMax||i===W.moveAnchorToMin){t.preventDefault();const{min:e,max:n}=this._getAnchorBoundsInPixels(a),s=u?i===W.moveAnchorToMax?n:e:i===W.moveAnchorToMin?n:e;this._toPosition(a,s);const o=this.values[a];h!==o&&this._emitThumbChangeEvent({index:a,oldValue:h,value:o})}}_onAnchorPointerDown(t){if(this._isDisabled())return;const{target:e,clientX:i,clientY:n}=t,s=e["data-thumb-index"];void 0!==s&&(t.preventDefault(),this._anchorElements[s]&&this._anchorElements[s].focus(),this._storeTrackDimensions(),this._dragStartInfo={clientX:i,clientY:n,index:s,position:this._getPositionOfElement(this._anchorElements[s])},this.notifyChange("state"),document.addEventListener("pointerup",this._onAnchorPointerUp),document.addEventListener("pointermove",this._onAnchorPointerMove))}_onAnchorPointerMove(t){if("editing"===this.state||!this._dragStartInfo)return;t.preventDefault();const{values:e,_anchorElements:i,_dragged:n,_dragStartInfo:s,_dragStartInfo:{index:o,position:r}}=this,{clientX:a,clientY:l}=t,h=n?"drag":"start",u=i[o],d=this._getPositionOfElement(u),c=this._applyPrecisionToPosition(this._isHorizontalLayout()?r+a-s.clientX:r+l-s.clientY);if(d===c)return;const m=e[o];this._dragged=!0,this._toPosition(o,c);const _=this.values[o];n?m!==_&&this._emitThumbDragEvent({index:o,state:h,value:_}):this._emitThumbDragEvent({index:o,state:h,value:m})}_onAnchorPointerUp(t){if(document.removeEventListener("pointerup",this._onAnchorPointerUp),document.removeEventListener("pointermove",this._onAnchorPointerMove),!this._dragStartInfo)return;t.preventDefault();const{index:e}=this._dragStartInfo,i=this._dragged;this._dragged=!1,this._dragStartInfo=null,this._lastMovedHandleIndex=e,i?(this.notifyChange("state"),this._emitThumbDragEvent({index:e,state:"stop",value:this.values[e]})):this.scheduleRender()}_onTrackPointerDown(t){const{_dragStartInfo:e,snapOnClickEnabled:i,state:s,values:o}=this;if(this._isDisabled()||"editing"===s||e||!i||!o.length)return;const{steps:r}=this,{clientX:a,clientY:l}=t,h=this._getCursorPositionFromEvent(t),u=this._valueFromPosition(h),d=this._getIndexOfNearestValue(u);if(!n(d))return;const c=o[d],m=n(r)?this._calculateNearestStepPosition(h):h;this._toPosition(d,m),this._dragged=!0,this._dragStartInfo={clientX:a,clientY:l,index:d,position:m},this._focusedAnchorIndex=d,this.notifyChange("state"),this._emitThumbDragEvent({index:d,state:"start",value:c});const _=this.values[d];c!==_&&this._emitThumbDragEvent({index:d,state:"drag",value:_}),document.addEventListener("pointerup",this._onAnchorPointerUp),document.addEventListener("pointermove",this._onAnchorPointerMove)}_onSegmentPointerDown(t){t.preventDefault();const{draggableSegmentsEnabled:e}=this,i=t.target,s=i["data-segment-index"],o=i["data-min-thumb-index"],r=i["data-max-thumb-index"];!this._isDisabled()&&e&&n(o)&&n(r)&&(t.stopPropagation(),this._storeTrackDimensions(),this._segmentDragStartInfo={cursorPosition:this._getCursorPositionFromEvent(t),index:s,details:this._normalizeSegmentDetails({min:this._getAnchorDetails(o),max:this._getAnchorDetails(r)})},this.notifyChange("state"),this._emitSegmentDragEvent({index:s,state:"start",thumbIndices:[o,r]}),document.addEventListener("pointerup",this._onSegmentPointerUp),document.addEventListener("pointermove",this._onSegmentPointerMove))}_onSegmentPointerMove(t){if(!this._segmentDragStartInfo)return;t.preventDefault();const{_trackHeight:e,_trackWidth:i,_segmentDragStartInfo:{index:n,cursorPosition:s,details:{min:o,max:r}}}=this,{index:a,position:l,value:h}=o,{index:u,position:d,value:c}=r;this._dragged=!0;const m=this._getCursorPositionFromEvent(t);if(m===s)return;const _=this._positionToPercent(s),p=this._positionToPercent(m)-_,v=this._positionToPercent(l)+p,g=this._positionToPercent(d)+p,{min:b}=this._getAnchorBoundsAsPercents(a),{max:f}=this._getAnchorBoundsAsPercents(u);let x=!1,E=!1;if(v<b?x=!0:g>f&&(E=!0),x){const{min:t,max:e}=this.viewModel.getBoundsForValueAtIndex(a),i=this._isPositionInverted()?e:t,n=i,s=c+(i-h);return void this._updateAnchorValues([a,u],[n,s])}if(E){const{min:t,max:e}=this.viewModel.getBoundsForValueAtIndex(u),i=this._isPositionInverted()?t:e,n=i,s=h+(i-c);return void this._updateAnchorValues([a,u],[s,n])}const P=this._isHorizontalLayout()?i:e,y=g/100*P,k=v/100*P,I=this.values,A=[I[a],I[u]],L=this._getValueForAnchorAtPosition(a,k),M=this._getValueForAnchorAtPosition(u,y);this._updateAnchorValues([a,u],[L,M]);[this.values[a],this.values[u]].every(((t,e)=>t===A[e]))||this._emitSegmentDragEvent({index:n,state:"drag",thumbIndices:[a,u]})}_onSegmentPointerUp(t){if(t.preventDefault(),document.removeEventListener("pointerup",this._onSegmentPointerUp),document.removeEventListener("pointermove",this._onSegmentPointerMove),!this._segmentDragStartInfo)return;const{max:e,min:i,values:n}=this,{index:s,details:{min:{index:o},max:{index:r}}}=this._segmentDragStartInfo,a=e-i,l=n[o],h=n[r];this._lastMovedHandleIndex=l===h?l>a/2?o:r:null,this._dragged=!1,this._segmentDragStartInfo=null,this.notifyChange("state"),this._emitSegmentDragEvent({index:s,state:"stop",thumbIndices:[o,r]})}_storeTrackDimensions(){if(this.trackElement){const t=this._getDimensions(this.trackElement);this._trackHeight=t.height,this._trackWidth=t.width}}_onLabelPointerDown(){this._isDisabled()||(this._dragged=!1,document.addEventListener("pointerup",this._onAnchorPointerUp),document.addEventListener("pointermove",this._onAnchorPointerMove))}_onLabelPointerMove(){this._isDisabled()||(this._dragged=!0)}_onLabelPointerUp(t){if(this._isDisabled())return;const e=t.target["data-thumb-index"];this.labelInputsEnabled&&!this._dragged&&n(e)&&(this._activeLabelInputIndex=e),this._dragged=!1,this.notifyChange("state"),document.removeEventListener("pointerup",this._onLabelPointerUp),document.removeEventListener("pointermove",this._onLabelPointerMove)}_onLabelInputBlur(t){const{_activeLabelInputIndex:e,values:i,viewModel:n}=this,s=t.target.value;if(this._activeLabelInputIndex=null,this.notifyChange("state"),!s)return;const o=this._parseInputValue(s,"value",e),r=i[e],{min:a,max:l}=this.viewModel.getBoundsForValueAtIndex(e);if(o<a||o>l)return;n.setValue(e,o);const h=this.values[e];r!==h&&this._emitThumbChangeEvent({index:e,oldValue:r,value:h})}_onInputKeyDown(t){if(this._isDisabled())return;const{target:e}=t,i=o(t),{hideInput1:s,hideInput2:r,hideInput3:a}=W,{_activeLabelInputIndex:l,_anchorElements:h}=this,u=e;if(i===s||i===r||i===a){t.stopPropagation();const e=l;u.blur(),n(e)?h[e].focus():u.parentElement.focus()}}_onMaxLabelClick(){!this._isDisabled()&&this.rangeLabelInputsEnabled&&(this._isMaxInputActive=!0,this.notifyChange("state"))}_onMaxLabelKeyDown(t){this._isDisabled()||o(t)!==W.showInput||(this._isMaxInputActive=!0,this.notifyChange("state"))}_onMaxInputBlur(t){const e=t.target.value;if(this._isMaxInputActive=!1,this.notifyChange("state"),!e)return;const i=this.max,n=this._parseInputValue(e,"max");n<=this.min||(this.viewModel.set("max",n),this.max!==i&&this._emitMaxChangeEvent({oldValue:i,value:this.max}))}_onMinLabelClick(){!this._isDisabled()&&this.rangeLabelInputsEnabled&&(this._isMinInputActive=!0,this.notifyChange("state"))}_onMinLabelKeyDown(t){this._isDisabled()||o(t)!==W.showInput||(this._isMinInputActive=!0,this.notifyChange("state"))}_onMinInputBlur(){const t=event.target.value;if(this._isMinInputActive=!1,this.notifyChange("state"),!t)return;const e=this.min,i=this._parseInputValue(t,"min");i>=this.max||(this.viewModel.set("min",i),this.min!==e&&this._emitMinChangeEvent({oldValue:e,value:this.min}))}_isDisabled(){return this.disabled||"disabled"===this.state}_positionFromValue(t){const{max:e,min:i}=this,n=e-i;if(0===n)return 0;const{_trackHeight:s,_trackWidth:o}=this,r=this._isHorizontalLayout();let a=r?parseFloat((o*(t-i)/n).toFixed(2)):parseFloat((s*(e-t)/n).toFixed(2));return this._isReversedLayout()&&(a=r?o-a:s-a),a}_valueFromPosition(t){const{_trackHeight:e,_trackWidth:i,max:n,min:s,precision:o}=this,r=n-s;let a=this._isHorizontalLayout()?t*r/i+s:r*(1e3-t/e*1e3)/1e3+s;return this._isReversedLayout()&&(a=n+s-a),parseFloat(a.toFixed(o))}_positionToPercent(t){const{_trackHeight:e,_trackWidth:i}=this,n=100*t/(this._isHorizontalLayout()?i:e);return this._applyPrecisionToPosition(n)}_applyPrecisionToPosition(t){return parseFloat(t.toFixed(this._positionPrecision))}_isPositionInverted(){const{layout:t}=this;return"horizontal-reversed"===t||"vertical"===t}_isHorizontalLayout(){return this.layout.indexOf("horizontal")>-1}_isReversedLayout(){return this.layout.indexOf("reversed")>-1}_normalizeSegmentDetails(t){if(this._isPositionInverted()){const{min:e,max:i}=t;return{min:i,max:e}}return t}_parseInputValue(t,e,i){return this.inputParseFunction?this.inputParseFunction(t,e,i):this.viewModel.defaultInputParseFunction(t)}_formatInputValue(t,e,i){return this.inputFormatFunction?this.inputFormatFunction(t,e,i):this.viewModel.defaultInputFormatFunction(t)}_getAnchorDetails(t){return{index:t,position:this._getPositionOfElement(this._anchorElements[t]),value:this.values[t]}}_updateAnchorStyle(t,e){const i=this._anchorElements[t];i&&(this._isHorizontalLayout()?i.style.left=`${e}`:i.style.top=`${e}`)}_getStyleForAnchor(t,e,i){const n=this._getPositionStyleForElement(t);if(1===this.values.length)return`${n}`;const s=this._zIndices[e];return`${n}; z-index: ${i?this._zIndexOffset+s:s}`}_getPositionStyleForElement(t){const e=this._positionFromValue(t),i=this._positionToPercent(e);return`${this._isHorizontalLayout()?"left":"top"}: ${i+"%"}`}_getPositionOfElement(t){const e=this._getDimensions(t.offsetParent),i=this._getDimensions(t);return this._isHorizontalLayout()?this._applyPrecisionToPosition(i.left-e.left):this._applyPrecisionToPosition(i.top-e.top)}_updateAnchorValues(t,e){t.forEach(((t,i)=>this._toValue(t,e[i])))}_toValue(t,e){if(n(this.steps)){e=this._getStepValues()[this._getIndexOfNearestStepValue(e)]}this._updateAnchorStyle(t,this._getPositionStyleForElement(e)),this.viewModel.setValue(t,e)}_toPosition(t,e){const i=n(this.steps)?this._getStepValueForAnchorAtPosition(t,e):this._getValueForAnchorAtPosition(t,e);this._updateAnchorStyle(t,this._getPositionStyleForElement(i)),this.viewModel.setValue(t,i)}_getValueForAnchorAtPosition(t,e){const{min:i,max:n}=this._getAnchorBoundsInPixels(t),{min:s,max:o}=this.viewModel.getBoundsForValueAtIndex(t);let r,a,l=null;return this._isPositionInverted()?(r=s,a=o):(r=o,a=s),l=e>n?r:e<i?a:this._valueFromPosition(e),l>o?l=o:l<s&&(l=s),l}_getStepValueForAnchorAtPosition(t,e){const i=this._getStepValues(),n=this._calculateNearestStepPosition(e),s=this._getValueForAnchorAtPosition(t,n);return i[this._getIndexOfNearestStepValue(s)]}_getAnchorBoundsAsPercents(t){const{min:e,max:i}=this._getAnchorBoundsInPixels(t);return{min:this._positionToPercent(e),max:this._positionToPercent(i)}}_getAnchorBoundsInPixels(t){const{_anchorElements:e,_trackHeight:i,_trackWidth:n,thumbsConstrained:s}=this,o=e[t-1],r=e[t+1],a=this._isHorizontalLayout()?n:i;return s?this._isPositionInverted()?{max:o?this._getPositionOfElement(o):a,min:r?this._getPositionOfElement(r):0}:{max:r?this._getPositionOfElement(r):a,min:o?this._getPositionOfElement(o):0}:{max:a,min:0}}_getIndexOfNearestValue(t){return this.values.indexOf(this.values.reduce(((e,i)=>Math.abs(i-t)<Math.abs(e-t)?i:e)))}_getCursorPositionFromEvent(t){const e=this._getDimensions(this.trackElement);return this._isHorizontalLayout()?t.clientX-e.left:t.clientY-e.top}_getStepValues(){const{steps:t}=this;if(Array.isArray(t))return t;const{max:e,min:i}=this,n=Math.ceil((e-i)/t),s=[];for(let o=0;o<=n;o++){const n=i+t*o;s.push(n>e?e:n)}return s}_toStep(t,e){const{values:i,viewModel:n}=this,s=i[t],o=this._getStepValues(),r=o.indexOf(s);let a=null;if(r>-1){const i=o[r+e],n=this._positionFromValue(i);a=this._getStepValueForAnchorAtPosition(t,n)}else{a=o[this._getIndexOfNearestStepValue(s)+e]}n.setValue(t,a)}_getIndexOfNearestStepValue(t){const{steps:e}=this;if(!n(e))return null;const i=this._getStepValues(),s=i.reduce(((e,i)=>Math.abs(i-t)<Math.abs(e-t)?i:e));return i.indexOf(s)}_calculateNearestStepPosition(t){const e=this._valueFromPosition(t),i=this._getIndexOfNearestStepValue(e),n=this._getStepValues();return this._positionFromValue(n[i])}_getTickCounts(t,e){const{mode:i}=e;return"count"===i||"position"===i?t||0:"percent"===i&&100/t||0}_calculateTickPositions(t){return t.map((t=>this._positionFromValue(t)))}_calculateEquidistantTickPositions(t){const{_trackWidth:e,_trackHeight:i}=this,n=this._isHorizontalLayout()?e:i,s=n/(t-1),o=[];if(1===t)return[n/2];for(let e=0;e<t;e++){const t=e*s;t<=n&&o.push(t)}return o}_getDimensions(t){try{return t.getBoundingClientRect()}catch(t){if("object"==typeof t&&null!==t)return{top:0,bottom:0,left:0,width:0,height:0,right:0};throw t}}_emitMaxChangeEvent(t){this.emit("max-change",{...t,type:"max-change"})}_emitMinChangeEvent(t){this.emit("min-change",{...t,type:"min-change"})}_emitThumbChangeEvent(t){this.emit("thumb-change",{...t,type:"thumb-change"})}_emitThumbDragEvent(t){this.emit("thumb-drag",{...t,type:"thumb-drag"})}_emitSegmentDragEvent(t){this.emit("segment-drag",{...t,type:"segment-drag"})}};t([s(),c()],Y.prototype,"disabled",void 0),t([s()],Y.prototype,"extraNodes",void 0),t([s(),c()],Y.prototype,"draggableSegmentsEnabled",void 0),t([s()],Y.prototype,"inputCreatedFunction",void 0),t([a("viewModel.inputFormatFunction")],Y.prototype,"inputFormatFunction",void 0),t([a("viewModel.inputParseFunction")],Y.prototype,"inputParseFunction",void 0),t([s({aliasOf:{source:"messages.widgetLabel",overridable:!0}})],Y.prototype,"label",void 0),t([s()],Y.prototype,"labelInputsEnabled",void 0),t([a("viewModel.labelFormatFunction")],Y.prototype,"labelFormatFunction",void 0),t([a("viewModel.labels")],Y.prototype,"labels",void 0),t([s(),c()],Y.prototype,"labelsVisible",null),t([s({value:"horizontal"}),c()],Y.prototype,"layout",null),t([a("viewModel.max")],Y.prototype,"max",void 0),t([s(),c(),d("esri/widgets/Slider/t9n/Slider")],Y.prototype,"messages",void 0),t([a("viewModel.min")],Y.prototype,"min",void 0),t([a("viewModel.precision")],Y.prototype,"precision",void 0),t([s()],Y.prototype,"rangeLabelInputsEnabled",void 0),t([s(),c()],Y.prototype,"rangeLabelsVisible",null),t([s()],Y.prototype,"snapOnClickEnabled",void 0),t([s({dependsOn:["viewModel.state"],readOnly:!0}),c()],Y.prototype,"state",null),t([s(),c()],Y.prototype,"steps",void 0),t([a("viewModel.thumbsConstrained")],Y.prototype,"thumbsConstrained",void 0),t([s()],Y.prototype,"thumbCreatedFunction",void 0),t([s(),c()],Y.prototype,"tickConfigs",void 0),t([s()],Y.prototype,"trackElement",void 0),t([a("viewModel.values")],Y.prototype,"values",void 0),t([s(),c(["viewModel.thumbsConstrained","viewModel.max","viewModel.min","viewModel.precision","viewModel.labelFormatFunction","viewModel.labels","viewModel.values"])],Y.prototype,"viewModel",void 0),t([s(),c()],Y.prototype,"visibleElements",void 0),t([l("visibleElements")],Y.prototype,"castVisibleElements",null),Y=t([r("esri.widgets.Slider")],Y);var q=Y;export default q;
