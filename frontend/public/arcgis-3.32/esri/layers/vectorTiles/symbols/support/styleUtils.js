// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/symbols/support/styleUtils",["require","exports","../../core/promiseUtils","../../core/Error"],function(b,a,c,d){Object.defineProperty(a,"__esModule",{value:!0});a.fetchStyle=function(a,b){return c.reject(new d("symbolstyleutils:style-url-and-name-missing","Either styleUrl or styleName is required to resolve a style"))}});