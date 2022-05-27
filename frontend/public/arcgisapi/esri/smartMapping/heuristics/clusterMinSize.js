// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../../core/screenUtils","../../views/2d/support/engineHelpers"],function(c,d){return async function(a,b){await b.when();a=a.clone();a.text="9";b=await d.getTextBounds(a,b);return 4*c.px2pt(b.width)}});