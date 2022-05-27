// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./Callout3D","./LineCallout3D"],function(b,e,f){function c(a){if(!a)return!1;a=a.verticalOffset;return!a||0>=a.screenLength||0>=a.maxWorldLength?!1:!0}b.calloutProperty={types:{key:"type",base:e,typeMap:{line:f}},json:{write:!0}};b.hasVisibleCallout=function(a){if(!a||!a.supportsCallout||!a.supportsCallout())return!1;const d=a.callout;return d&&d.visible?c(a)?!0:!1:!1};b.hasVisibleVerticalOffset=c;b.isCalloutSupport=function(a){return"point-3d"===a.type||"label-3d"===a.type};Object.defineProperty(b,
"__esModule",{value:!0})});