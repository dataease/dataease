// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../../../../chunks/_rollupPluginBabelHelpers ../../../../../core/has ../../../../../core/maybe ../../../../../core/handleUtils ../../../../../core/Handles ./config ./Manipulation ./MoveXYAxisManipulation ./MoveXYDiscManipulation ./MoveZManipulation".split(" "),function(l,m,t,n,p,u,q,g,v,w,x){g=function(r){function f(a){var b=r.call(this)||this;b._handles=new u;b._angleDeferred=null;b._interactive=!0;const {tool:d,view:c,snapToScene:h,radius:k}=a;b._view=c;b.xyManipulation=new w.MoveXYDiscManipulation({tool:d,
view:c,snapToScene:h,radius:k});b.xyAxisManipulation=new v.MoveXYAxisManipulation({tool:d,view:c,radius:k});b.zManipulation=new x.MoveZManipulation({tool:d,view:c,radius:k});b.xyManipulation.available=a.xyAvailable;b.xyAxisManipulation.available=a.xyAxisAvailable;b.zManipulation.available=a.zAvailable;b.autoHideXYAxis();b.forEachManipulator(y=>{b._handles.add(y.events.on("grab-changed",()=>b.updateManipulatorInteractivity()))});return b}m._inheritsLoose(f,r);var e=f.prototype;e.destroy=function(){this._handles.destroy();
this.xyManipulation.destroy();this.xyAxisManipulation.destroy();this.zManipulation.destroy()};e.createGraphicDragPipeline=function(a,b){return p.handlesGroup([this.xyManipulation.createGraphicDragPipeline(a,b),this.xyAxisManipulation.createGraphicDragPipeline(a,b),this.zManipulation.createGraphicDragPipeline(a,b)])};e.createDragPipeline=function(a,b,d){return p.handlesGroup([this.xyManipulation.createDragPipeline(a,b,d),this.xyAxisManipulation.createDragPipeline(a,b,d),this.zManipulation.createDragPipeline(a,
d)])};e.forEachManipulator=function(a){this.xyManipulation.forEachManipulator(b=>a(b,1));this.xyAxisManipulation.forEachManipulator(b=>a(b,1));this.zManipulation.forEachManipulator(b=>a(b,0))};e.autoHideXYAxis=function(){const a=this.xyAxisManipulation;var b=this.xyManipulation;if(!t("esri-mobile")){var d=[];b.forEachManipulator(c=>d.push(c));a.forEachManipulator(c=>d.push(c));b=()=>{const c=[];this.xyAxisVisible?n.isSome(this._angleDeferred)&&(this.angle=this._angleDeferred()):a.forEachManipulator(h=>
c.push(h.disableDisplay()));this._handles.remove("disable-xy-axis-display");this._handles.add(c,"disable-xy-axis-display")};for(const c of d)this._handles.add(c.events.on("focus-changed",b));this._view.inputManager&&this._handles.add(this._view.inputManager.watch("latestPointerType",b));b()}};e.updateManipulatorInteractivity=function(){const a=this.grabbing;this.forEachManipulator(b=>{b.interactive=!a&&this._interactive||b.grabbing})};f.radiusForSymbol=function(a){return(a=n.isSome(a)&&"point-3d"===
a.type&&a.symbolLayers)&&0<a.length&&a.some(b=>"icon"===b.type)?q.DISC_RADIUS_SMALL:q.DISC_RADIUS};m._createClass(f,[{key:"snapToScene",set:function(a){this.xyManipulation.snapToScene=a}},{key:"angle",set:function(a){this._angleDeferred=null;this.xyAxisManipulation.angle=a}},{key:"angleDeferred",set:function(a){this.xyAxisVisible?this.angle=a():this._angleDeferred=a}},{key:"interactive",set:function(a){this._interactive!==a&&(this._interactive=a,this.updateManipulatorInteractivity())}},{key:"radius",
set:function(a){this.xyAxisManipulation.radius=a;this.xyManipulation.radius=a;this.zManipulation.radius=a}},{key:"displayScale",set:function(a){this.xyManipulation.displayScale=a;this.xyAxisManipulation.displayScale=a}},{key:"xyAxisVisible",get:function(){const a=this.xyManipulation.someManipulator(b=>b.focused)||this.xyAxisManipulation.someManipulator(b=>b.focused);return this._view.inputManager&&"touch"===this._view.inputManager.latestPointerType||a}}]);return f}(g.Manipulation);l.MoveManipulation=
g;Object.defineProperty(l,"__esModule",{value:!0})});