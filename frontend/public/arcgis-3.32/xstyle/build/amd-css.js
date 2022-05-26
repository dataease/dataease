define(['dojo/json', 'build/fs', 'build/fileUtils', 'build/transforms/writeAmd', '../build'],
		function(json, fs, fileUtils, writeAmd, buildModule){
	var targetStylesheet;
	return {
		start:function(
			mid,
			referenceModule,
			bc
		){
			// mid may contain a pragma (e.g. "!strip"); remove
			mid = mid.split("!")[0];
			var cssPlugin = bc.amdResources["xstyle/css"],
				stylesheetInfo = bc.getSrcModuleInfo(mid, referenceModule, true),
				cssResource = bc.resources[stylesheetInfo.url],
				xstyleModuleInfo = bc.getSrcModuleInfo("xstyle/core/parser", referenceModule, true),
				xstyleText = fs.readFileSync(xstyleModuleInfo.url + '.js', "utf8"),
				xstyleProcess = buildModule(xstyleText);
			
			function processStylesheetLayers(){
				// build all the target stylesheets from the layers
				if(!bc.processedStylesheets){
					bc.processedStylesheets = true;
					for(var i in bc.layers){
						var layer = bc.layers[i];
						targetStylesheet = layer.targetStylesheet;
						if(targetStylesheet){
							// we want to calculate the target stylesheet relative to the layer
							var layerModule = layer.name && bc.getSrcModuleInfo(layer.name);
							var moduleInfo = bc.getSrcModuleInfo(targetStylesheet, layerModule, true);
							var targetStylesheetModule = bc.resources[moduleInfo.url];
							var targetDestStylesheetModule = bc.getDestModuleInfo(targetStylesheet, null, true);
							var targetStylesheetUrl = bc.currentTargetStylesheetUrl = moduleInfo.url;
							var targetDestStylesheetUrl = targetDestStylesheetModule.url;
							// initialize the target stylesheet
							var targetStylesheetContents = '';
							try{
								if(targetStylesheetModule){
									targetStylesheetContents = processCss(targetStylesheetModule).standardCss;
								}else{
									targetStylesheetModule = moduleInfo;
								}
								var moduleSet = writeAmd.computeLayerContents({mid:'stylesheet-process' + i}, layer.include, layer.exclude);
								var targetResource;
								for (var i in moduleSet) {
									var module = moduleSet[i];
									if(module.getCss){
										if(module.mid == targetStylesheetModule.mid) {
											// probably not necessary to assign this anymore
											targetResource = module;
											// if it is the target stylesheet, the contents are already included
										}else{
											targetStylesheetContents += module.getCss();
										}
									}
								}
								if(targetResource){
									var loadIndicator = '#' + targetResource.module.mid.replace(/\//g,'-').replace(/\..*/,'')
										+ '-loaded{display:none}';
									targetStylesheetContents += loadIndicator;
									targetResource.module.originalCss = targetResource.module.text;
									targetResource.isTargetStylesheet = true;
								}
								targetStylesheetModule.isTargetStylesheet = true;
								targetStylesheetModule.originalCss = targetStylesheetModule.text;
								targetStylesheetModule.text = targetStylesheetContents;
								
								var url = targetDestStylesheetUrl.replace(/\/x$/,'');
								
								fileUtils.ensureDirectoryByFilename(url);
								fs.writeFileSync(url, targetStylesheetContents);
							}catch(e){
								console.error('error creating css layer', e.stack || e);
							}
							bc.currentTargetStylesheetUrl = targetStylesheetUrl = null;
						}
					}
				}
			}
			if(targetStylesheet){
			}else{
				// there is no targe stylesheet, so
				// we will be directly inlining the stylesheet in the layer, so we need the createStyleSheet module
				var createStyleSheetModule = bc.getSrcModuleInfo('xstyle/core/load-css', referenceModule);
			}
			// read the stylesheet so we can process
			//var text= fs.readFileSync(stylesheetInfo.src, "utf8");

			if (!cssPlugin){
				throw new Error("text! plugin missing");
			}
			if (!cssResource){
				throw new Error("text resource (" + stylesheetInfo.url + ") missing");
			}

			var result = [cssPlugin];
			if(createStyleSheetModule){
				// if we are inlining the stylesheet, we need the functionality to insert a stylesheet from text 
				result.push(bc.amdResources['xstyle/core/load-css']);
			}
			if(bc.internStrings && !bc.internSkip(stylesheetInfo.mid, referenceModule)){
				// or inline it
				result.push({
					module:cssResource,
					pid:stylesheetInfo.pid,
					mid:stylesheetInfo.mid,
					deps:[],
					getText:function(){
						var processed = this.processed = processCss(this.module, true);//stylesheetInfo.url,  // inline resources too
						return processed.xstyleCss ?
							json.stringify({
								cssText: processed.standardCss,
								xCss: processed.xstyleCss
							}) :
							json.stringify(processed.standardCss +"");
					},
					getCss: function(){
						this.cssRetrieved = true;
						return processCss(this.module).standardCss;//, targetStylesheetUrl);
					},
					internStrings: function(){
						processStylesheetLayers();
						if(this.isTargetStylesheet){
							// a target stylesheet, this should go through the xstyle loader
							// to ensure it is actually loaded
							return ['', '0'];
						}
						if(this.cssRetrieved){
							// the CSS has been written to a stylesheet, we intern an indicator that
							// it has already been loaded
							return ['url:' + this.mid, '{}'];
						}
						// it hasn't been written to a target stylesheet, so it needs to actually be inlined
						return ['url:' + this.mid, this.getText()];
					}
				});
			}
			function processCss(module, inlineAllResource){
				var text = module.originalCss || module.getText ? module.getText() : module.text;
				if(text===undefined){
					// the module likely did not go through the read transform; therefore, just read it manually
					text= fs.readFileSync(module.src, "utf8");
				}
				try{
					var processed = xstyleProcess(text, bc.currentTargetStylesheetUrl || module.src, module.src, inlineAllResource);
					//for(var i = 0; i < processed.requiredModules.length; i++){
						// TODO: at some point, we may add an option to include the modules that
						// are required by the stylesheet, but at least by default these should 
						// probably be async lazy loaded
					//}
				}catch(e){
					console.error('Error processing CSS', e.stack || e);
					processed = {standardCss: text};
				}
				return processed;
			}
			return result;
		}
	};
});
