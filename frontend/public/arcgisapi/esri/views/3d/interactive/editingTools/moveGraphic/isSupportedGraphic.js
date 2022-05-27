// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../../core/has","../../../../../core/maybe","../../../../../support/elevationInfoUtils"],function(b,f,e,c){b.isSupportedGraphic=function(a){var d;if("graphics"!==(null==(d=a.layer)?void 0:d.type))return 1;if(e.isNone(a.geometry))return 2;switch(a.geometry.type){case "polygon":case "point":case "polyline":break;case "multipoint":case "extent":case "mesh":return 3;default:return 3}return"on-the-ground"!==c.getGraphicEffectiveElevationMode(a)&&c.hasGraphicFeatureExpressionInfo(a)?
4:0};Object.defineProperty(b,"__esModule",{value:!0})});