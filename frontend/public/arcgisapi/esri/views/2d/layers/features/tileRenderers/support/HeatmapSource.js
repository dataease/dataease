// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../../../renderers/support/heatmapUtils"],function(a,c){let e=function(){function b(){this.gradient=null;this.width=this.height=512}b.prototype.render=function(d){c.drawHeatmap(d,512,this.intensities,this.gradient,this.minPixelIntensity,this.maxPixelIntensity)};return b}();a.HeatmapSource=e;Object.defineProperty(a,"__esModule",{value:!0})});