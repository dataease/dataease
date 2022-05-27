// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../../core/has","../../../../../core/maybe","../../../../../geometry/Circle","../../../../../support/elevationInfoUtils"],function(c,g,e,f,d){c.isSupportedGraphic=function(b){var a;if("graphics"!==(null==(a=b.layer)?void 0:a.type))return 1;a=b.geometry;if(e.isNone(a))return 2;switch(a.type){case "polygon":if(a instanceof f)return 3;break;case "polyline":break;case "point":case "multipoint":case "extent":case "mesh":return 3;default:return 3}return"on-the-ground"!==d.getGraphicEffectiveElevationMode(b)&&
d.hasGraphicFeatureExpressionInfo(b)?4:0};Object.defineProperty(c,"__esModule",{value:!0})});