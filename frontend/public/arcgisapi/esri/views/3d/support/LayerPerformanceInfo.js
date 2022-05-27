// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../layers/support/PerformanceInfoLayerView","../terrain/terrainUtils"],function(b,c){return function(a,d){this.layer=null;this.displayedNumberOfFeatures=this.memory=0;this.totalNumberOfFeatures=this.maximumNumberOfFeatures=null;this.layer=a.layer;this.memory=c.isSurfaceLayerView(a)?d.basemapTerrain.getUsedMemoryForLayerView(a):a.getUsedMemory();b.isPerformanceInfoLayerView(a)&&(a=a.performanceInfo,this.displayedNumberOfFeatures=a.displayedNumberOfFeatures,this.maximumNumberOfFeatures=a.maximumNumberOfFeatures,
this.totalNumberOfFeatures=a.totalNumberOfFeatures)}});