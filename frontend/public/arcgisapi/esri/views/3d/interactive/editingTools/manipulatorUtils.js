// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../core/maybe","../../../../support/elevationInfoUtils"],function(d,e,g){d.canMoveZ=function(a,c=g.getGraphicEffectiveElevationInfo(a)){return"on-the-ground"===c.mode||e.isNone(a.geometry)||!a.geometry.hasZ?!1:!0};d.disableDisplayOnGrab=function(a,c){let b=null;const h=a.events.on("grab-changed",f=>{e.isSome(b)&&(b.remove(),b=null);"start"===f.action&&(b=a.disableDisplay());c&&c(f)});return{remove(){e.isSome(b)&&b.remove();h.remove()}}};Object.defineProperty(d,"__esModule",
{value:!0})});