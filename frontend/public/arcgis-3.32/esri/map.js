// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/map","require dojo/_base/kernel dojo/_base/declare dojo/_base/connect dojo/_base/lang dojo/_base/array dojo/_base/event dojo/on dojo/aspect dojo/dom dojo/dom-attr dojo/dom-class dojo/dom-construct dojo/dom-geometry dojo/dom-style dijit/a11yclick dijit/registry ./kernel ./config ./sniff ./lang ./_coremap ./MapNavigationManager dojo/i18n!./nls/jsapi".split(" "),function(w,q,K,z,l,d,A,B,L,C,D,f,r,M,N,t,O,u,P,e,p,Q,R,E){var x={up:"panUp",right:"panRight",down:"panDown",left:"panLeft"},F=
{upperRight:"panUpperRight",lowerRight:"panLowerRight",lowerLeft:"panLowerLeft",upperLeft:"panUpperLeft"},h=z.connect,G=z.disconnect,k=r.create,m=N.set,H=l.hitch,v=M.getMarginBox,I=q.deprecated,y=l.mixin,J=0;q=K(Q,{declaredClass:"esri.Map",constructor:function(a,b){b=b||{};y(this,{_slider:null,_navDiv:null,_mapParams:y({attributionWidth:.45,slider:!0,nav:!1,logo:!0,sliderStyle:"small",sliderPosition:"top-left",sliderOrientation:"vertical",autoResize:!0},b)});y(this,{isMapNavigation:null!=b.isMapNavigation?
b.isMapNavigation:!0,isDoubleClickZoom:null!=b.isDoubleClickZoom?b.isDoubleClickZoom:!0,isClickRecenter:null!=b.isClickRecenter?b.isClickRecenter:!0,isPan:null!=b.isPan?b.isPan:!0,isRubberBandZoom:null!=b.isRubberBandZoom?b.isRubberBandZoom:!0,isPinchZoom:null!=b.isPinchZoom?b.isPinchZoom:!0,isKeyboardNavigation:null!=b.isKeyboardNavigation?b.isKeyboardNavigation:!0,isScrollWheel:null!=b.isScrollWheel?b.isScrollWheel:!0,isShiftDoubleClickZoom:!1,isScrollWheelZoom:!1,isPanArrows:!1,isZoomSlider:!1});
l.isFunction(u._css)&&(u._css=u._css(this._mapParams.force3DTransforms),this.force3DTransforms=this._mapParams.force3DTransforms);a=e("esri-transforms")&&e("esri-transitions");this.navigationMode=this._mapParams.navigationMode||a&&"css-transforms"||"classic";"css-transforms"!==this.navigationMode||a||(this.navigationMode="classic");this.fadeOnZoom=p.isDefined(this._mapParams.fadeOnZoom)?this._mapParams.fadeOnZoom:"css-transforms"===this.navigationMode;"css-transforms"!==this.navigationMode&&(this.fadeOnZoom=
!1);this.setMapCursor("default");this.smartNavigation=b&&b.smartNavigation;if(!(p.isDefined(this.smartNavigation)||!e("mac")||e("esri-touch")||e("esri-pointer")||3.5>=e("ff"))){var c=navigator.userAgent.match(/Mac\s+OS\s+X\s+([\d]+)(\.|\_)([\d]+)\D/i);c&&p.isDefined(c[1])&&p.isDefined(c[3])&&(a=parseInt(c[1],10),c=parseInt(c[3],10),this.smartNavigation=10<a||10===a&&6<=c)}a=!0;a=!1;this.showAttribution=p.isDefined(this._mapParams.showAttribution)?this._mapParams.showAttribution:a;this._onLoadHandler_connect=
h(this,"onLoad",this,"_onLoadInitNavsHandler");var n=k("div",{class:"esriControlsBR"+(this._mapParams.nav?" withPanArrows":"")},this.root);if(this.showAttribution)if(a=l.getObject("esri.dijit.Attribution",!1))this._initAttribution(a,n);else{var g=J++,f=this;this._rids&&this._rids.push(g);w(["./dijit/Attribution"],function(a){var b=f._rids?d.indexOf(f._rids,g):-1;-1!==b&&(f._rids.splice(b,1),f._initAttribution(a,n))})}this._mapParams.logo&&(a={},6===e("ie")&&(a.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled\x3d'true', sizingMethod\x3d'crop', src\x3d'"+
w.toUrl("./images/map/logo-med.png")+"')"),this._ogol=k("div",{style:a,tabIndex:"0",title:"Esri"},n),this._setLogoSize(),this._onMapResizeLogo_connect=h(this,"onResize",this,"_setLogoSize"),this._ogol_connect=h(this._ogol,t,this,"_openLogoLink"));this.navigationManager=new R(this);b&&b.basemap&&(this._onLoadFix=!0,this.setBasemap(b.basemap),this._onLoadFix=!1);if(this.autoResize=this._mapParams.autoResize)b=this._getEnclosingResizableWidget(this.container)||window,a=this.resize,this._rszSignal=B.pausable(b,
"resize",a),this._oriSignal=B.pausable(window,"orientationchange",a),L.after(b,"resize",a,!0),this._startResizeTimer()},_startResizeTimer:function(){clearTimeout(this._persistentTimer);this._persistentTimer=setTimeout(this._timedResize,2*this.resizeDelay)},_getEnclosingResizableWidget:function(a){var b=O.getEnclosingWidget(a);return b?b.resize?b:this._getEnclosingResizableWidget(a.parentNode):b},_setLogoSize:function(){this._ogol&&(25E4>this.root.clientWidth*this.root.clientHeight?(f.remove(this._ogol,
"logo-med"),f.add(this._ogol,"logo-sm")):(f.remove(this._ogol,"logo-sm"),f.add(this._ogol,"logo-med")))},_initAttribution:function(a,b){b=k("span",{class:"esriAttribution"},b,"first");m(b,"maxWidth",Math.floor(this.width*this._mapParams.attributionWidth)+"px");this._connects.push(h(b,t,function(){f.contains(this,"esriAttributionOpen")?f.remove(this,"esriAttributionOpen"):this.scrollWidth>this.clientWidth&&f.add(this,"esriAttributionOpen")}));this.attribution=new a({map:this},b)},_cleanUp:function(){this.disableMapNavigation();
this.navigationManager.destroy();var a=this._slider;a&&a.destroy&&!a._destroyed&&a.destroy();var a=this._navDiv,b=this.attribution;a&&r.destroy(a);b&&b.destroy();this._connects.push(this._slider_connect,this._ogol_connect,this._rszSignal,this._oriSignal);d.forEach(this._connects,G);clearInterval(this._persistentTimer);this.attribution=this.navigationManager=this._rids=this._connects=this._slider_connect=this._ogol_connect=this._rszSignal=this._oriSignal=this._persistentTimer=null;this.inherited("_cleanUp",
arguments)},_isPanningOrZooming:function(){return this.__panning||this.__zooming},_canZoom:function(a){var b=this.getLevel();return!this.__tileInfo||!(b===this.getMinZoom()&&0>a||b===this.getMaxZoom()&&0<a)},_onLoadInitNavsHandler:function(){this._evalMapNavigation();this._createNav();if("small"===this._mapParams.sliderStyle||!this._createSlider)this._createSimpleSlider();else if(this._mapParams.slider){var a=-1!==this._getSliderClass(!0).indexOf("Horizontal"),a=[a?"dijit.form.HorizontalSlider":"dijit.form.VerticalSlider",
a?"dijit.form.HorizontalRule":"dijit.form.VerticalRule",a?"dijit.form.HorizontalRuleLabels":"dijit.form.VerticalRuleLabels"];if(d.some(a,function(a){return!l.getObject(a,!1)})){var a=d.map(a,function(a){return a.replace(/\./g,"/")}),b=J++,c=this;this._rids&&this._rids.push(b);w(a,function(){var a=c._rids?d.indexOf(c._rids,b):-1;-1!==a&&(c._rids.splice(a,1),c._createSlider.apply(c,arguments))})}else a=d.map(a,function(a){return l.getObject(a,!1)}),this._createSlider.apply(this,a)}G(this._onLoadHandler_connect)},
_createNav:function(){if(this._mapParams.nav){var a,b,c,n=f.add,g=this.id;this._navDiv=k("div",{id:g+"_navdiv"},this.root);n(this._navDiv,"navDiv");var e=this.width/2,l=this.height/2,d;for(c in x)b=x[c],a=k("div",{id:g+"_pan_"+c},this._navDiv),n(a,"fixedPan "+b),"up"===c||"down"===c?(d=parseInt(v(a).w,10)/2,m(a,{left:e-d+"px",zIndex:30})):(d=parseInt(v(a).h,10)/2,m(a,{top:l-d+"px",zIndex:30})),this._connects.push(h(a,"onclick",H(this,this[b])));this._onMapResizeNavHandler_connect=h(this,"onResize",
this,"_onMapResizeNavHandler");for(c in F)b=F[c],a=k("div",{id:g+"_pan_"+c,style:{zIndex:30}},this._navDiv),n(a,"fixedPan "+b),this._connects.push(h(a,"onclick",H(this,this[b])));this.isPanArrows=!0}},_onMapResizeNavHandler:function(a,b,c){a=this.id;b/=2;c/=2;var f=C.byId,g,e,d;for(g in x)e=f(a+"_pan_"+g),"up"===g||"down"===g?(d=parseInt(v(e).w,10)/2,m(e,"left",b-d+"px")):(d=parseInt(v(e).h,10)/2,m(e,"top",c-d+"px"))},_createSimpleSlider:function(){if(this._mapParams.slider){var a=this._slider=k("div",
{id:this.id+"_zoom_slider",class:this._getSliderClass(),style:{zIndex:30}}),b=k("div",{class:"esriSimpleSliderIncrementButton",tabIndex:"0",role:"button"},a),c=k("div",{class:"esriSimpleSliderDecrementButton",tabIndex:"0",role:"button"},a);this._addZoomButtonTooltips(b,c);this._incButton=b;this._decButton=c;this._simpleSliderZoomHandler(null,null,null,this.getLevel());var d=E.widgets.zoomSlider;this._addZoomButtonIcon(b,"+",d.zoomIn);this._addZoomButtonIcon(c,"\x26minus;",d.zoomOut);8>e("ie")&&f.add(c,
"dj_ie67Fix");this._connects.push(h(b,t,this,this._simpleSliderChangeHandler));this._connects.push(h(c,t,this,this._simpleSliderChangeHandler));(-1<this.getMaxZoom()||-1<this.getMinZoom())&&this._connects.push(h(this,"onZoomEnd",this,this._simpleSliderZoomHandler));10>e("ie")&&C.setSelectable(a,!1);this.root.appendChild(a);this.isZoomSlider=!0}},_simpleSliderChangeHandler:function(a){A.stop(a);a=-1!==a.currentTarget.className.indexOf("IncrementButton")?!0:!1;this._extentUtil({numLevels:a?1:-1})},
_simpleSliderZoomHandler:function(a,b,c,d){var e;a=this._incButton;b=this._decButton;-1<d&&d===this.getMaxZoom()?e=a:-1<d&&d===this.getMinZoom()&&(e=b);e?(f.add(e,"esriSimpleSliderDisabledButton"),f.remove(e===a?b:a,"esriSimpleSliderDisabledButton")):(f.remove(a,"esriSimpleSliderDisabledButton"),f.remove(b,"esriSimpleSliderDisabledButton"))},_getSliderClass:function(a){a=a?"Large":"Simple";var b=this._mapParams.sliderOrientation,c=this._mapParams.sliderPosition||"",b=b&&"horizontal"===b.toLowerCase()?
"esri"+a+"SliderHorizontal":"esri"+a+"SliderVertical";if(c)switch(c.toLowerCase()){case "top-left":c="esri"+a+"SliderTL";break;case "top-right":c="esri"+a+"SliderTR";break;case "bottom-left":c="esri"+a+"SliderBL";break;case "bottom-right":c="esri"+a+"SliderBR"}return"esri"+a+"Slider "+b+" "+c},_addZoomButtonIcon:function(a,b,c){r.create("span",{"aria-hidden":"true",role:"presentation",innerHTML:b},a);r.create("span",{class:"esriIconFallbackText",innerHTML:c},a)},_addZoomButtonTooltips:function(a,
b){var c=E.widgets.zoomSlider;D.set(a,"title",c.zoomIn);D.set(b,"title",c.zoomOut)},_openLogoLink:function(a){window.open(P.defaults.map.logoLink,"_blank");A.stop(a)},enableMapNavigation:function(){this.isMapNavigation||(this.isMapNavigation=!0,this._evalMapNavigation())},disableMapNavigation:function(){this.isMapNavigation&&(this.isMapNavigation=!1,this._evalMapNavigation())},_evalMapNavigation:function(){this.isMapNavigation?this.navigationManager.enableNavigation():this.navigationManager.disableNavigation()},
_evalNavigationFeature:function(a){if(this.isMapNavigation&&this["is"+a])this.navigationManager["enable"+a]();else this.navigationManager["disable"+a]()},enableDoubleClickZoom:function(){this.isDoubleClickZoom||(this.isDoubleClickZoom=!0,this._evalNavigationFeature("DoubleClickZoom"))},disableDoubleClickZoom:function(){this.isDoubleClickZoom&&(this.isDoubleClickZoom=!1,this._evalNavigationFeature("DoubleClickZoom"))},enableShiftDoubleClickZoom:function(){this.isShiftDoubleClickZoom||(I(this.declaredClass+
": Map.(enable/disable)ShiftDoubleClickZoom deprecated. Shift-Double-Click zoom behavior will not be supported.",null,"v2.0"),this.navigationManager.enableShiftDoubleClickZoom(),this.isShiftDoubleClickZoom=!0)},disableShiftDoubleClickZoom:function(){this.isShiftDoubleClickZoom&&(I(this.declaredClass+": Map.(enable/disable)ShiftDoubleClickZoom deprecated. Shift-Double-Click zoom behavior will not be supported.",null,"v2.0"),this.navigationManager.disableShiftDoubleClickZoom(),this.isShiftDoubleClickZoom=
!1)},enableClickRecenter:function(){this.isClickRecenter||(this.isClickRecenter=!0,this._evalNavigationFeature("ClickRecenter"))},disableClickRecenter:function(){this.isClickRecenter&&(this.isClickRecenter=!1,this._evalNavigationFeature("ClickRecenter"))},enablePan:function(){this.isPan||(this.isPan=!0,this._evalNavigationFeature("Pan"))},disablePan:function(){this.isPan&&(this.isPan=!1,this._evalNavigationFeature("Pan"))},enableRubberBandZoom:function(){this.isRubberBandZoom||(this.isRubberBandZoom=
!0,this._evalNavigationFeature("RubberBandZoom"))},disableRubberBandZoom:function(){this.isRubberBandZoom&&(this.isRubberBandZoom=!1,this._evalNavigationFeature("RubberBandZoom"))},enablePinchZoom:function(){this.isPinchZoom||(this.isPinchZoom=!0,this._evalNavigationFeature("PinchZoom"))},disablePinchZoom:function(){this.isPinchZoom&&(this.isPinchZoom=!1,this._evalNavigationFeature("PinchZoom"))},enableKeyboardNavigation:function(){this.isKeyboardNavigation||(this.isKeyboardNavigation=!0,this._evalNavigationFeature("KeyboardNavigation"))},
disableKeyboardNavigation:function(){this.isKeyboardNavigation&&(this.isKeyboardNavigation=!1,this._evalNavigationFeature("KeyboardNavigation"))},enableScrollWheel:function(){this.isScrollWheel||(this.isScrollWheel=!0,this._evalNavigationFeature("ScrollWheel"))},disableScrollWheel:function(){this.isScrollWheel&&(this.isScrollWheel=!1,this._evalNavigationFeature("ScrollWheel"))},enableScrollWheelZoom:function(){this.isScrollWheelZoom||(this.navigationManager.enableScrollWheelZoom(),this.isScrollWheelZoom=
!0)},disableScrollWheelZoom:function(){this.isScrollWheelZoom&&(this.navigationManager.disableScrollWheelZoom(),this.isScrollWheelZoom=!1)},enableScrollWheelPan:function(){this.isScrollWheelPan||this.navigationManager.enableScrollWheelPan()},disableScrollWheelPan:function(){this.isScrollWheelPan&&this.navigationManager.disableScrollWheelPan()},showPanArrows:function(){this._navDiv&&(this._navDiv.style.display="block",this.isPanArrows=!0)},hidePanArrows:function(){this._navDiv&&(this._navDiv.style.display=
"none",this.isPanArrows=!1)},showZoomSlider:function(){this._slider&&(m(this._slider.domNode||this._slider,"visibility","inherit"),this.isZoomSlider=!0)},hideZoomSlider:function(){this._slider&&(m(this._slider.domNode||this._slider,"visibility","hidden"),this.isZoomSlider=!1)},onClick:function(a){a.graphic||(a.graphic=this.syncHitTestForWebGL(a))}});e("extend-esri")&&(u.Map=q);return q});