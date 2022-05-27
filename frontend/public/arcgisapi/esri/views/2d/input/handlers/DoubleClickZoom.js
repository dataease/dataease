// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../chunks/_rollupPluginBabelHelpers","../../../input/InputHandler","../../../input/handlers/support"],function(f,h,d,k){d=function(g){function e(a,b){var c=g.call(this,!0)||this;c.view=a;c.registerIncoming("double-click",b,l=>c._handleDoubleClick(l,b));return c}h._inheritsLoose(e,g);e.prototype._handleDoubleClick=function(a,b){k.eventMatchesPointerAction(a.data,"primary")&&(a.stopPropagation(),b?this.view.mapViewNavigation.zoomOut([a.data.x,a.data.y]):this.view.mapViewNavigation.zoomIn([a.data.x,
a.data.y]))};return e}(d.InputHandler);f.DoubleClickZoom=d;Object.defineProperty(f,"__esModule",{value:!0})});