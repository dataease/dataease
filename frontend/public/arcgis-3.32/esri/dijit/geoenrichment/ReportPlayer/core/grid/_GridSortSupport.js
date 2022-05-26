// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/grid/_GridSortSupport",["dojo/_base/declare","./coreUtils/sorting/GridSortUtil"],function(b,a){return b(null,{presetSorting:null,ignorePresetSorting:!1,allowSorting:!1,canSortCellFunc:null,_applySortingToStoreData:function(){!this.ignorePresetSorting&&a.applySortingToStoreData(this)},_buildSortControls:function(){a.buildSortControls(this)},getSorting:function(){return a.getSorting(this)},setSorting:function(c,b){return a.setSorting(this,c,b)},getSortRowIndexMapping:function(){return a.getSortRowIndexMapping(this)},
onSortingChanged:function(a){}})});