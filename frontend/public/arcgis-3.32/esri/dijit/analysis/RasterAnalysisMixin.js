// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/analysis/RasterAnalysisMixin","dojo/_base/declare dojo/_base/Deferred dojo/_base/lang dojo/_base/array dojo/_base/connect dojo/_base/json dojo/store/Memory dojo/promise/all dojo/when dojo/string dojo/has dojo/dom-class dojo/dom-style dojo/dom-attr dojo/dom-construct dijit/Tooltip esri/layers/ArcGISImageServiceLayer ../../kernel ../../lang ../../request ../../symbols/SimpleFillSymbol ../../Color ../../renderers/ClassBreaksRenderer ../../tasks/AlgorithmicColorRamp ../../tasks/MultipartColorRamp ../RasterFunctionEditor/utils ./AnalysisBase ./_AnalysisOptions ./utils ./ItemTypes ./mixins/browselayers/BrowseLayerMixin ../../layers/RasterFunction ../../layers/ArcGISRenderingImageServiceLayer".split(" "),
function(p,v,d,h,w,g,A,x,y,q,B,C,m,D,l,E,F,G,H,n,I,r,J,K,L,M,N,O,k,z,P,t,Q){p=p([O,N,P],{declaredClass:"esri.dijit.analysis.RasterAnalysisMixin",map:null,outputLayerName:null,resultParameter:"outputRaster",rasterGPToolName:"GenerateRaster",analysisType:"raster",i18n:null,returnProcessInfo:null,isPPTTool:!1,isDLTool:!1,unsupportedServiceNameCharacters:/[\s~`!#$%\^&*+=\-\[\]\\';,\/{}|\\":<>\?\.]/g,_geometryService:null,_findDeepestArgsForRerun:!1,constructor:function(a,b){this._pbConnects=[];a.containerNode&&
(this.container=a.containerNode)},destroy:function(){this.inherited(arguments);h.forEach(this._pbConnects,w.disconnect);delete this._pbConnects},postMixInProperties:function(){this.inherited(arguments);d.mixin(this.i18n,this.toolNlsName)},postCreate:function(){this.inherited(arguments);C.add(this._form.domNode,"esriSimpleForm");this._outputLayerInput.set("validator",d.hitch(this,this.validateServiceName));this._buildUI()},startup:function(){},validateServiceName:function(a){return k.validateServiceName(a,
{textInput:this._outputLayerInput})},_getJobParameters:function(){},_getRasterFunction:function(){},_getRasterArguments:function(){},_getRasterObject:function(a){a=a||this.get("inputLayer");return M.getRasterJsonFromLayer(a)},_getOutputRasterLayerName:function(){},_getOutputItemProperties:function(){},_setDefaultInputs:function(){},_resetUI:function(){},_getDefaultOutputItemProperties:function(a,b,c){b=this._getDefaultRenderingRule(b);var e=this._getDefaultRenderer(),f=this._getDefaultPopupInfo();
a={visibility:!0,opacity:a||1,interpolation:c||"RSP_NearestNeighbor",popupInfo:f};b&&(a.renderingRule=b);e&&(a.layerDefinition={},a.layerDefinition.drawingInfo={},a.layerDefinition.drawingInfo.renderer=e.toJson());return a},_getDefaultRenderingRule:function(a){var b=new t;b.functionName="Stretch";b.functionArguments={StretchType:5,DRA:!1,Gamma:[1],UseGamma:!0};b.outputPixelType="U8";var c=new t;c.functionName="Colormap";c.functionArguments={colorRamp:a||"Aspect",Raster:b};return c},_getDefaultRenderer:function(){if(this.colorValues&&
this.colorValues.length&&this.classMaxValues&&this.classMaxValues.length&&this.labels&&this.labels.length){var a=this.colorValues.length;if(a===this.classMaxValues.length&&a===this.labels.length){var b=new J({field:"Value",showInAscendingOrder:!0,classificationMethod:"natural-breaks"}),c=new L,e,f,d,u;c.colorRamps=[];for(d=0;d<a;d++)u=this.colorValues[d],e&&(f=new K,f.algorithm="hsv",f.fromColor=new r(e),f.toColor=new r(u),c.colorRamps.push(f)),e=u;c&&(b.authoringInfo={},b.authoringInfo.colorRamp=
c.toJson());c=[];e=-1;for(d=0;d<a;d++)f=this.colorValues[d],f=new I("solid",null,new r({r:f[0],g:f[1],b:f[2],a:f[3]})),c.push({minValue:e,maxValue:this.classMaxValues[d],label:this.labels[d],symbol:f}),e=this.classMaxValues[d];b.infos=c;b.attributeField="Value";return b}}},_getDefaultPopupInfo:function(){return{title:this.get("outputLayerName"),description:null,fieldInfos:[{fieldName:"Raster.ServicePixelValue",label:"Service Pixel Value",isEditable:!1,isEditableOnLayer:!1,visible:!1,format:{places:2,
digitSeparator:!0}},{fieldName:"Raster.ServicePixelValue.Raw",label:"Pixel Value",isEditable:!1,isEditableOnLayer:!1,visible:!0,format:{places:2,digitSeparator:!0}}],showAttachments:!1,layerOptions:{showNoDataRecords:!0,returnTopmostRaster:!0},mediaInfos:[]}},_getReRunRFxArgs:function(a,b){var c={};this._findFunction(a,b,c);return c&&c.rasterFunctionArguments},_findFunction:function(a,b,c){var e=a&&a.rasterFunction,d=this._getRasterFunction();if(e&&c&&"object"===typeof c){if(e===d&&(c.rasterFunction=
a.rasterFunction,c.rasterFunctionArguments=a.rasterFunctionArguments,!b))return;this._findFunction(a.rasterFunctionArguments&&a.rasterFunctionArguments.Raster,b,c)}},_getSelectedLayerIndexFromUI:function(a,b){if(!a||!b)return-1;var c=-1;h.forEach(a,function(a,d){a&&a.label.toLowerCase()===b.toLowerCase()&&(c=d)});return c},_setAnalysisGpServerAttr:function(a){a&&(this.analysisGpServer=a,this.set("toolServiceUrl",this.analysisGpServer+"/"+this.rasterGPToolName))},_setInputLayersAttr:function(a){this.inputLayers=
a},_setInputLayerAttr:function(a){this.inputLayer=a},_getInputLayerAttr:function(){return this.inputLayer},_getOutputLayerNameAttr:function(){this._outputLayerInput&&(this.outputLayerName=this._outputLayerInput.get("value"));return this.outputLayerName},_setOutputLayerNameAttr:function(a){this.outputLayerName=a},_setDisableRunAnalysisAttr:function(a){this._saveBtn.set("disabled",a)},_setDisableExtentAttr:function(a){this._useExtentCheck.set("checked",!a);this._useExtentCheck.set("disabled",a)},_getDisableExtentAttr:function(){this._useExtentCheck.get("disabled")},
_setMapAttr:function(a){this.map=a},_getMapAttr:function(){return this.map},_setModelsAttr:function(a){this.models=a},_handleModeCrumbClick:function(a){a.preventDefault();this._onClose(!0)},_onClose:function(a){this._removePointLayer();a&&(this._save(),this.emit("save",{save:!0}));this.removePreviewLayerFromMap();this.emit("close",{save:!a})},_removePointLayer:function(){this.drawnPointLayer&&(this._removeLayer(this.drawnPointLayer,this.inputLayers,this._analysisSelect),this._sourceDrawBtn.deactivate())},
_removeLayer:function(a,b,c){this.map.removeLayer(a);h.forEach(b,function(e,d){e===a&&(c.removeOption({value:d+1,label:b.name}),b.splice(d,1))},this)},_save:function(){},_handleShowCreditsClick:function(a){a.preventDefault();a={};this._form.validate()&&(a.inputLayer=g.toJson(k.constructAnalysisInputLyrObj(this.get("inputLayer"))),a[this.outputName]=g.toJson({serviceProperties:{name:this.get("outputLayerName")}}),this.secondaryOutputNames&&d.mixin(a,this.updateSecondaryOutputNames()),this.showChooseExtent&&
this._useExtentCheck.get("checked")&&(a.context=g.toJson({extent:this.map.extent._normalize(!0)})),this.getCreditsEstimate(this.toolName,a).then(d.hitch(this,function(a){this._usageForm.set("content",a);this._usageDialog.show()})))},updateSecondaryOutputNames:function(){var a={};h.forEach(this.secondaryOutputNames,d.hitch(this,function(b){this.get(b)&&(a[b]=g.toJson({serviceProperties:{name:this.get(b).replace(this.unsupportedServiceNameCharacters,"_")}}))}));return a},_validateRFT:function(a){if(a){var b=
!1,c=this.analysisGpServer.replace("RasterAnalysisTools/GPServer","RasterRendering/ImageServer/validate");a={renderingRule:a,f:"json"};this._previewBtnTooltip||(this._previewBtnTooltip=new E({connectId:[this.esriAnalysisPreviewError,this.esriAnalysisPreviewWarning],label:this.i18n.previewError}));return n({url:c,content:a,load:d.hitch(this,function(a){a&&a.renderingRule&&a.renderingRule.isValid&&(b=!0);return b})})}},onValidRFT:function(){this._previewBtnTooltip.label="";this._previewBtn.checked=
!0;this.removePreviewErrors()},onInvalidRFT:function(){this._previewBtnTooltip.label="\x3cdiv class\x3d'esriAnalysisPreviewErrorToolTip'\x3e"+this.i18n.previewError+"\x3c/div\x3e";this._previewBtn.checked=!1;this.esriAnalysisPreviewWarning.style.display="none";this.esriAnalysisPreviewError.style.display="block";this.onPreviewLoad()},handleExportImageFailure:function(){this._previewBtnTooltip.label="\x3cdiv class\x3d'esriAnalysisPreviewErrorToolTip'\x3e"+this.i18n.previewWarning+"\x3c/div\x3e";this.esriAnalysisPreviewError.style.display=
"none";this.esriAnalysisPreviewWarning.style.display="block";this.onPreviewLoad()},onPreviewLoad:function(){this.esriAnalysisPreviewLoading.style.display="none"},removePreviewErrors:function(){this.esriAnalysisPreviewError.style.display="none";this.esriAnalysisPreviewWarning.style.display="none"},_handlePreviewBtnClick:function(a){this._form.validate()&&(this._preview?(this._preview=!this._preview,this.onPreviewLoad(),this.removePreviewErrors(),this.removePreviewLayerFromMap()):(this.removePreviewErrors(),
this.esriAnalysisPreviewLoading.style.display="block",this._validateRFT(this._getRenderingRule()).then(d.hitch(this,function(a){if(this._preview=a)this.onValidRFT(),this._showPreview();else this.onInvalidRFT()}))))},updatePreview:function(){this._preview&&(this.removePreviewErrors(),this._showPreview())},_showPreview:function(){var a=this._getRenderingRule();this.esriAnalysisPreviewLoading.style.display="block";this._addPreviewLayerToMap(a)},_addPreviewLayerToMap:function(a){this.previewLayer&&this.removePreviewLayerFromMap();
var b=this.analysisGpServer.replace("RasterAnalysisTools/GPServer","RasterRendering/ImageServer"),c=new Q(b,{raster:a,type:"rft"}),e=d.hitch(this,function(a){this.previewLayer=a;this.map.addLayer(this.previewLayer);this.onValidRFT()});c.loaded?e(c):(c.on("load",function(){e(c)}),c.on("error",d.hitch(this,function(){if(this.previewLayer)this.previewLayer.updating||this.handleExportImageFailure();else this.onInvalidRFT()})),c.on("update-end",d.hitch(this,function(){this.onPreviewLoad();this.onValidRFT()})),
c.on("update-start",d.hitch(this,function(){this.onValidRFT();this.esriAnalysisPreviewLoading.style.display="block"})))},removePreviewLayerFromMap:function(){this.previewLayer&&(this.map.removeLayer(this.previewLayer),this.previewLayer=void 0)},_getRenderingRule:function(){var a={},b={};this._useRFT?a=this._getRasterFunction():(a.rasterFunction=this._getRasterFunction(),a.rasterFunctionArguments=this._getRasterArguments());return b=g.toJson(a)},_handleSaveBtnClick:function(a){this._form.validate()&&
(a=this.secondaryOutputNames?this._validateSecondaryOutputNames():"done",y(a,d.hitch(this,function(){this._saveBtn.set("disabled",!0);var a=this._webLayerTypeSelect.get("value"),c={},e=this._getJobParameters();if(!H.isDefined(e)){e={};e.rasterFunction=this._getRenderingRule();var f=this._getRasterObject();f&&!this._useRFT&&(e.functionArguments=g.toJson({raster:f}))}e[this.outputName]=g.toJson({serviceProperties:{name:this.get("outputLayerName")}});this.secondaryOutputNames&&d.mixin(e,this.updateSecondaryOutputNames());
e.returnProcessInfo=this.returnProcessInfo;f={};this.showChooseExtent&&!this.get("disableExtent")&&this._useExtentCheck.get("checked")&&(f.extent=this.map.extent._normalize(!0));e.context=g.toJson(f);c.jobParams=e;if("permanentLayer"===a){c.itemParams={description:this.i18n.itemDescription,tags:q.substitute(this.i18n.itemTags,{layername:this.inputLayer&&this.inputLayer.name,fieldname:e.field||"",valuelayername:e.valuelayername||""}),snippet:this.i18n.itemSnippet};if(a=this._getOutputItemProperties())c.itemParams.text=
a;this.showSelectFolder&&(c.itemParams.folder=this.get("folderId"));c.analysisType=this.analysisType;this.execute(c)}else"dynamicLayer"===a&&this._handleSaveDynamicLayer(e)})))},_handleSaveDynamicLayer:function(a){this.get("inputLayer");this.analysisGpServer.replace("RasterAnalysisTools/GPServer","RasterRendering/ImageServer?viewId\x3d");g.fromJson(a[this.outputName]);a=new t;a.functionName=this._getRasterFunction();a.functionArguments=this._getRasterArguments()},_handleAnalysisLayerChange:function(a){if("browselayers"===
a||"browse"===a){this._isAnalysisSelect=!0;this.browseType&&(this.defaultItemTypes=[],this.set("allowedItemTypes",this.browseType));var b={geometryTypes:this.checkGeometries,tags:this.tags,customCheck:this.hasCustomCheck?{customCheckHandler:d.hitch(this,this.isValidInputLayer),customCheckFailureMessage:this.customCheckFailureMessage}:void 0};this._createBrowseItems(this.getOptionsForRasterBrowseItems(a),b,this._analysisSelect)}else this.inputLayer=this.inputLayers[a],this._updateAnalysisLayerUI(!0)},
addPointAnalysisLayer:function(){this._sourceDrawBtn&&(this._sourceDrawBtn.set("map",this.map),this._sourceDrawBtn.on("change",d.hitch(this,this._handleAnalysisPointSelectLayer)))},_handleAnalysisPointSelectLayer:function(a){this.inputLayers&&0!==this.inputLayers.length&&-1!==this.inputLayers.indexOf(a)||(this.drawnPointLayer=a,this.inputLayers.push(a),this.inputLayer=a,this._analysisSelect.removeOption(this._analysisSelect.getOptions()),k.populateAnalysisLayers(this,"inputLayer","inputLayers"),this._updateAnalysisLayerUI(!0))},
_updateAnalysisLayerUI:function(a){"ApplyRFxTemplate"===this.toolName&&(a&&(this.outputLayerName=this._getOutputRasterLayerName()),this._outputLayerInput.set("value",this.outputLayerName));this.inputLayer&&(this._interpolateToolDescription&&D.set(this._interpolateToolDescription,"innerHTML",q.substitute(this.i18n.toolDefine,{layername:this.inputLayer.name})),a&&(this.outputLayerName=this._getOutputRasterLayerName()||q.substitute(this.i18n.outputLayerName,{layername:this.inputLayer.name})),this._outputLayerInput.set("value",
this.outputLayerName));this._resetUI()},_handleBrowseItemsSelect:function(a,b){a&&a.selection&&k.addAnalysisReadyLayer({item:a.selection,layers:this.inputLayers,layersSelect:this._analysisSelect,browseDialog:this._browseLyrsdlg,widget:this},b).always(d.hitch(this,this._updateAnalysisLayerUI,!0))},_handleModelChange:function(a){this.model.itemId!==a&&(this.model.itemId=a,this._queryModelArguments(a))},_queryModelArguments:function(a){var b=this._toolServiceUrl;a={model:g.toJson({url:this.portalUrl+
"/sharing/content/items/"+a}),context:{f:"json"}};this.set("toolServiceUrl",this.analysisGpServer+"/QueryDeepLearningModelInfo");this._modelArguments.innerHTML="";m.set(this._queryModelArgsMsg,"display","table-row");this.gp.submitJob(a,d.hitch(this,function(a){this.gp.getResultData(a.jobId,"outModelInfo",d.hitch(this,function(a){this.set("toolServiceUrl",b);m.set(this._queryModelArgsMsg,"display","none");a&&a.value&&a.value.modelInfo&&(a=g.fromJson(a.value.modelInfo).ParameterInfo)&&0<a.length&&this._loadModelArguments(a)}))}))},
_loadModelArguments:function(a){var b=[],c=l.toDom("\x3ctr\x3e\x3ctd colspan\x3d'2' style\x3d'width:50%'\x3e\x3clabel\x3e"+this.i18n.nameLabel+"\x3c/label\x3e\x3c/td\x3e\x3ctd style\x3d'width:50%'\x3e\x3clabel\x3e"+this.i18n.valueLabel+"\x3c/td\x3e\x3c/tr\x3e");l.place(c,this._modelArguments);h.forEach(a,d.hitch(this,function(a){var c=a.name;0>["raster","rasters","model","device"].indexOf(c.toLowerCase())&&(a=l.toDom("\x3ctr\x3e\x3ctd colspan\x3d'2' style\x3d'width:50%'\x3e\x3clabel\x3e"+c+"\x3c/label\x3e\x3c/td\x3e\x3ctd style\x3d'width:50%'\x3e\x3cdiv\x3e\x3cinput type\x3d'text' data-dojo-type\x3d'dijit/form/NumberTextBox' data-dojo-props\x3d'intermediateChanges:true' style\x3d'width:100%;text-align:right' value\x3d'"+
a.value+"'\x3e\x3c/div\x3e\x3c/td\x3e\x3c/tr\x3e"),l.place(a,this._modelArguments),b.push(a))}));this.modelArgumentRows=b},_updateModelArguments:function(a){var b=[],c=l.toDom("\x3ctr\x3e\x3ctd colspan\x3d'2' style\x3d'width:50%'\x3e\x3clabel\x3e"+this.i18n.nameLabel+"\x3c/label\x3e\x3c/td\x3e\x3ctd style\x3d'width:50%'\x3e\x3clabel\x3e"+this.i18n.valueLabel+"\x3c/td\x3e\x3c/tr\x3e");l.place(c,this._modelArguments);for(var d in a)c=l.toDom("\x3ctr\x3e\x3ctd colspan\x3d'2' style\x3d'width:50%'\x3e\x3clabel\x3e"+
d+"\x3c/label\x3e\x3c/td\x3e\x3ctd style\x3d'width:50%'\x3e\x3cdiv\x3e\x3cinput type\x3d'text' data-dojo-type\x3d'dijit/form/NumberTextBox' data-dojo-props\x3d'intermediateChanges:true' style\x3d'width:100%;text-align:right' value\x3d'"+a[d]+"'\x3e\x3c/div\x3e\x3c/td\x3e\x3c/tr\x3e"),l.place(c,this._modelArguments),b.push(c);this.modelArgumentRows=b},_validateSecondaryOutputNames:function(a){var b=new v;this.getUserProfile().then(d.hitch(this,function(a){var c=[],f=!0,g=this.portalUrl+"/sharing/rest/portals/"+
a.orgId+"/isServiceNameAvailable";h.forEach(this.secondaryOutputNames,d.hitch(this,function(a){this.get(a)&&(a={name:this.get(a).replace(this.unsupportedServiceNameCharacters,"_"),type:"Image Service",f:"json"},c.push(n({url:g,content:a})))}));x(c).then(d.hitch(this,function(a){h.forEach(a,d.hitch(this,function(a,c){a.available||(f=!1,this.emit("job-fail",{message:this.i18n.servNameExists,type:"warning",messageCode:"AB_0002"}),b.reject())}));f&&b.resolve()}))}));return b.promise},_buildUI:function(){var a=
!0;this._loadConnections();this.isPPFTool=-1!=="ClassifyPixelsUsingDeepLearning DetectObjectsUsingDeepLearning ClassifyObjectsUsingDeepLearning CalculateSlope DeriveAspect MonitorVegetation ApplyRFxTemplate RemapValues ExtractRaster AggregateMultidimensionalRaster GenerateMultidimensionalAnomaly PredictUsingTrendRaster FindArgumentStatistics".split(" ").indexOf(this.toolName);this.isDLTool=-1!==["ClassifyPixelsUsingDeepLearning","DetectObjectsUsingDeepLearning","ClassifyObjectsUsingDeepLearning"].indexOf(this.toolName);
this.isDetectObjectTool=-1!==["DetectObjectsUsingDeepLearning","ClassifyObjectsUsingDeepLearning"].indexOf(this.toolName);this.needRFTs="AggregateMultidimensionalRaster"==this.toolName;this.isMdTool=-1!==["AggregateMultidimensionalRaster","GenerateMultidimensionalAnomaly","GenerateTrendRaster","PredictUsingTrendRaster","FindArgumentStatistics"].indexOf(this.toolName);this.signInPromise.then(d.hitch(this,function(){k.initHelpLinks(this.domNode,this.showHelp,{analysisGpServer:this.analysisGpServer,
analysisMode:"raster"});var a=this.portalUrl+"/sharing/search",b;if(this.isDLTool){var f=[];n({url:a,content:{f:"json",q:"Deep learning package"}},{usePost:!0}).then(d.hitch(this,function(a){a&&(a=a.results)&&0<a.length&&(h.forEach(a,d.hitch(this,function(a){a={value:a.id,label:a.title};f.push(a);this._modelSelect.addOption(a)})),this.models=f,this.model&&(b=this.model.itemId),this.model&&0<=f.findIndex(function(a){return a.value===b})?(this.modelArguments&&this._updateModelArguments(this.modelArguments),
this._modelSelect.set("value",this.model.itemId)):this.model={itemId:0})}))}if(this.needRFTs){var g=[];n({url:a,content:{f:"json",q:'type: "Raster function template" NOT "ArcGIS Raster Function Template"'}},{usePost:!0}).then(d.hitch(this,function(a){a&&(a=a.results)&&0<a.length&&(h.forEach(a,d.hitch(this,function(a){a={value:{itemId:a.id},label:a.title};g.push(a);this._aggregationFunctionSelect.addOption(a)})),this.aggregationFunctions=g,this.aggregationFunction&&(b=this.aggregationFunction.value),
this.aggregationFunction&&0<=g.findIndex(function(a){return a.value===b})||(this.aggregationFunction=g[0]),this._aggregationFunctionSelect.set("value",this.aggregationFunction))}))}}));if(this.rasterFunction){var b=this._getReRunRFxArgs(this.rasterFunction,this._findDeepestArgsForRerun);b&&d.mixin(this,b)}this.functionArguments&&this.functionArguments.Raster&&this.set("inputLayer",this.functionArguments.Raster);this.get("showSelectAnalysisLayer")&&(this.inputLayers&&this.inputLayer&&!k.isLayerInLayers(this.inputLayer,
this.inputLayers)&&this.inputLayers.push(this.inputLayer),this.get("inputLayer")||!this.get("inputLayers")||this.rerun||this.set("inputLayer",this.inputLayers[0]),k.populateAnalysisLayers(this,"inputLayer","inputLayers"));this.addAnalysisBrowseOption();this.outputLayerName&&(this._outputLayerInput.set("value",this.outputLayerName),a=!1);(this.inputLayer||"ApplyRFxTemplate"===this.toolName)&&this._updateAnalysisLayerUI(a);m.set(this._chooseFolderRow,"display",!0===this.showSelectFolder?"block":"none");
this.showSelectFolder&&this.getFolderStore().then(d.hitch(this,function(a){this.folderStore=a;k.setupFoldersUI({folderStore:this.folderStore,folderId:this.folderId,folderName:this.folderName,folderSelect:this._webMapFolderSelect,username:this.portalUser?this.portalUser.username:""})}));this._chooseLayerTypeRow&&(m.set(this._chooseLayerTypeRow,"display",!0===this.showSelectLayerType?"block":"none"),a=new A({data:[{name:this.i18n.permanentLayer,id:"permanentLayer"},{name:this.i18n.dynamicLayer,id:"dynamicLayer"}]}),
this._webLayerTypeSelect.set("store",a),this._webLayerTypeSelect.set("value","permanentLayer"));m.set(this._chooseExtentDiv,"display",!0===this.showChooseExtent?"inline-block":"none");m.set(this._showCreditsLink,"display",!0===this.showCredits?"block":"none");this.inputLayer&&this.inputLayer.drawnLayer&&this._sourceDrawBtn&&this._sourceDrawBtn.set("pointFeatureLayer",this.inputLayer);this._setDefaultInputs()},addAnalysisBrowseOption:function(){this.browseType&&1===this.browseType.length&&-1<this.browseType.indexOf(z.IS)?
k.addReadyToUseLayerOption(this,[{select:this._analysisSelect,disableLAAL:!0}]):k.addReadyToUseLayerOption(this,[this._analysisSelect]);this.addBrowseOption()},getOptionsForRasterBrowseItems:function(a){return-1<this.browseType.indexOf(z.FS)?{browseValue:a}:{browseValue:a,disableLAAL:!0,disableBoundary:!0}},addBrowseOption:function(){},isImageServiceLayer:function(a){return a&&(a instanceof F||"esri.layers.ArcGISImageServiceLayer"===a.declaredClass)},validateTrendInputLayers:function(a){a=this._validateTrendRasterLayers(a);
y(a,function(a){this.inputLayers=a})},_validateTrendRasterLayers:function(a){var b=[],c=new v,e,f,g=0;h.filter(a,d.hitch(this,function(a){e=n({url:a.url+"/keyProperties",content:{f:"json"}});b.push(e)}));x(b).then(d.hitch(this,function(b){h.forEach(b,d.hitch(this,function(b,c){f=b&&b.TrendParameters&&b.TrendParameters.DimensionName;f||(a.splice(c-g,1),this._analysisSelect.removeOption(c-g),g++)}));c.resolve(a)}));return c.promise},_loadConnections:function(){this.on("start",d.hitch(this,"_onClose",
!1));this._connect(this._closeBtn,"onclick",d.hitch(this,"_onClose",!0))},_connect:function(a,b,c){this._pbConnects.push(w.connect(a,b,c))}});B("extend-esri")&&d.setObject("dijit.analysis.RasterAnalysisMixin",p,G);return p});