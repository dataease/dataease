// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(b){b.eventMatchesMousePointerAction=function(a,c){if("touch"===a.pointerType)return!1;switch(c){case "primary":return 0===a.button;case "secondary":return 2===a.button;case "tertiary":return 1===a.button}};b.eventMatchesPointerAction=function(a,c){switch(c){case "primary":return"touch"===a.pointerType||0===a.button;case "secondary":return"touch"!==a.pointerType&&2===a.button;case "tertiary":return"touch"!==a.pointerType&&1===a.button}};Object.defineProperty(b,"__esModule",
{value:!0})});