// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
require({cache:{"url:esri/dijit/RasterFunctionEditor/templates/RFxRasterInfoCellSizeEditor.html":'\x3cdiv\x3e\r\n    \x3ctable class\x3d"esri-rfx-args-editor__table"\x3e\r\n        \x3ctr class\x3d"esri-rfx-args-editor__tr--arg-name esri-rfx-rasterInfo-cellSize-editor"\x3e\r\n            \x3ctd data-dojo-attach-point\x3d"cellSizeLabel"\x3e${_i18n.cellSizeName}\x3c/td\x3e\r\n        \x3c/tr\x3e\r\n        \x3ctr class\x3d"esri-rfx-args-editor__tr--arg-widget esri-rfx-rasterInfo-cellSize-editor"\x3e\r\n            \x3ctd\x3e\r\n                \x3cdiv data-dojo-type\x3d"esri/dijit/RasterFunctionEditor/RFxCellSizeInput" data-dojo-attach-point\x3d"rasterInfoPixelSize"\x3e\x3c/div\x3e\r\n            \x3c/td\x3e\r\n        \x3c/tr\x3e\r\n    \x3c/table\x3e\r\n\x3c/div\x3e'}});
define("esri/dijit/RasterFunctionEditor/RFxRasterInfoCellSizeEditor","dojo/_base/declare ../../lang dijit/_WidgetBase dijit/_TemplatedMixin dijit/_WidgetsInTemplateMixin dojo/_base/lang dojo/i18n!../../nls/jsapi dojo/text!./templates/RFxRasterInfoCellSizeEditor.html".split(" "),function(c,d,e,f,g,h,k,l){return c("RFxRasterInfoCellSizeEditor",[e,f,g],{templateString:l,defaultRasterInfo:{blockHeight:256,blockWidth:2048,compressionQuality:0,compressionType:"",firstPyramidLevel:1,format:"",maximumPyramidLevel:30,
packetSize:4,pixelType:-1,pyramidResamplingType:-1,type:"RasterInfo"},value:this.defaultRasterInfo,constructor:function(a){this.inherited(arguments);this._i18n=k.rasterFunctions.rfxArgs},postCreate:function(){this.inherited(arguments);this._setLabels();this._readValues();this.rasterInfoPixelSize.on("change",h.hitch(this,function(a){this._onPixelSizeChange(a.x)}))},_readValues:function(){var a=this.value||this.inputArgs.RasterInfo.value;a?this.rasterInfoPixelSize.cellSize.set("value",a.pixelSizeX||
a.pixelSizeY?a.pixelSizeX:0):this.rasterInfoPixelSize.cellSize.set("value",0)},_onPixelSizeChange:function(a){if(!d.isDefined(a)||!isNaN(a)){if(0===a)this.value=this.defaultRasterInfo;else{var b=this.value||this.defaultRasterInfo;b.pixelSizeX=a;b.pixelSizeY=a}this.inputArgs.RasterInfo.value=this.value}},_setLabels:function(){this.inputArgs&&this.inputArgs.RasterInfo&&(this.cellSizeLabel.innerHTML=this.inputArgs.RasterInfo.displayName)}})});