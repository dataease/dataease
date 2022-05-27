// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../core/maybe"],function(b,e){b.extractSafeScaleBounds=function(a){let {minScale:c,maxScale:d}=a;return{minScale:c||0,maxScale:d||0}};b.highlightsSupported=function(a){return a&&"function"===typeof a.highlight};b.occludeesSupported=function(a){return a&&"function"===typeof a.maskOccludee};b.scaleBoundsPredicate=function(a,c,d){return e.isNone(a)||a>d&&(0===c||a<c)};Object.defineProperty(b,"__esModule",{value:!0})});