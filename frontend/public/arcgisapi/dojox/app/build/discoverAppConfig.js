define([
	"build/argv",
	"build/fs",
	"build/buildControl",
	"build/messages",
	"build/process",
	"dojox/json/ref"
], function(argv, fs, bc, messages, process, json){
	var parseViews = function(mids, mainLayer, views, params){
		for(var key in views){
			// ignore naming starting with _ (jsonref adding is own stuff in there)
			if(key.indexOf("_") == 0){
				continue;
			}
			var view = views[key];
			// TODO deal with "./" shortcut & default view location (which relies on "./")
			if(view.controller && view.controller != "none"){
				var mid = view.controller.replace(/(\.js)$/, "");
				if(!bc.layers[mid] && bc.multipleAppConfigLayers){
					bc.layers[mid] = { include: [], exclude: [ "dojo/dojo", mainLayer ] };
					mids = bc.layers[mid].include;
				}
				mids.push(mid);
			}
			if(view.template){
				// we need dojo/text to load templates, let's put it in the main layer in all cases
				// as this will be shared by a lot of views
				if(!params.text){
					params.text = true;
					bc.layers[mainLayer].include.push("dojo/text");
				}
				mids.push(view.template);
			}
			if(view.nls){
				// we use nls let's add dojo/i18n to the main layer as it will be shared by a lot of views
				if(!params.nls){
					params.nls = true;
					bc.layers[mainLayer].include.push("dojo/i18n");
				}
				mids.push(view.nls);
			}
			if(view.dependencies){
				Array.prototype.splice.apply(mids, [ mids.length, 0 ].concat(view.dependencies));
			}
			if(view.views){
				parseViews(mids, mainLayer, view.views, params);
			}
		}
	};
	return function(){
		var config;
		try{
			config = json.fromJson(fs.readFileSync(bc.getSrcModuleInfo(argv.args.appConfigFile, null, false).url));
		}catch(e){
			console.log(e);
		}
		if(config){
			var mids = [], params = {};
			if(config.loaderConfig){
				require(config.loaderConfig);
			}
			// main layer
			var mainLayer;
			if(!argv.args.appConfigLayer){
				// no layer specified, take the first one
				for(var l in bc.layers){
					mainLayer = l;
					break;
				}
			}else{
				mainLayer = argv.args.appConfigLayer;
				if(!bc.layers[mainLayer]){
					bc.layers[mainLayer] = { include: [], exclude: [ "dojo/dojo"] };
				}
			}
			if(config.dependencies){
				mids = mids.concat(config.dependencies);
			}
			if(config.controllers){
				mids = mids.concat(config.controllers);
			}
			if(config.modules){
				mids = mids.concat(config.modules);
			}
			if(config.transit){
				mids.push(config.transit);
			}else{
				mids.push("dojox/css3/transit");
			}
			if(config.template){
				params.text = true;
				bc.layers[mainLayer].include.push("dojo/text");
				mids.push(config.template);
			}
			if(config.controller && config.controller != "none"){
				mids.push(config.controller.replace(/(\.js)$/, ""));
			}
			if(config.nls){
				// we use nls let's add dojo/i18n to the main layer as it will be shared by a lot of views
				params.nls = true;
				bc.layers[mainLayer].include.push("dojo/i18n");
				mids.push(config.nls);
			}
			if(config.view){
				// we use a custom view class
				mids.push(config.view);
			}else{
				// regular view
				mids.push("dojox/app/View");
			}
			// go into the view children
			if(config.views){
				parseViews(mids, mainLayer, config.views, params);
			}
			Array.prototype.splice.apply(bc.layers[mainLayer].include, [bc.layers[mainLayer].length, 0].concat(mids));
		}else{
			messages.log("pacify", argv.args.appConfigFile+" is not a valid dojox/app JSON config file");
			process.exit(-1);
		}
	};
});

