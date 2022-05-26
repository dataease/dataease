function rhinoDojoConfig(config, baseUrl, rhinoArgs){
	// summary:
	//		This module provides bootstrap configuration for running dojo in rhino.

	// TODO: v1.6 tries to set dojo.doc and dojo.body in rhino; why?

	// get a minimal console up
	var log = function(hint, args){
		print((hint ? hint + ":" : "") + args[0]);
		for(var i = 1; i < args.length; i++){
			print(", " + args[i]);
		}
	};
	// intentionally define console in the global namespace
	console= {
		log: function(){ log(0, arguments); },
		error: function(){ log("ERROR", arguments); },
		warn: function(){ log("WARN", arguments); }
	};

	// any command line arguments with the load flag are pushed into deps
	for(var deps = [], i = 0; i < rhinoArgs.length; i++){
		var arg = (rhinoArgs[i] + "").split("=");
		if(arg[0] == "load"){
			deps.push(arg[1]);
		}else if(arg[0] == "mapPackage") {
			var parts = arg[1].split(":"),
				name = parts[0],
				location=parts[1],
				isPrexisting = false;

			for (var j = 0; j < config.packages.length; j++) {
				var pkg = config.packages[j];
				if (pkg.name === name) {
					pkg.location = location;
					isPrexisting = true;
					break;
				}
			}

			if (!isPrexisting) {
				config.packages.push({
					name: name,
					location: location
				});
			}
		}
	}

	// provides timed callbacks using Java threads
	if(typeof setTimeout == "undefined" || typeof clearTimeout == "undefined"){
		var timeouts = [];
		clearTimeout = function(idx){
			if(!timeouts[idx]){ return; }
			timeouts[idx].stop();
		};

		setTimeout = function(func, delay){
			var def = {
				sleepTime:delay,
				hasSlept:false,

				run:function(){
					if(!this.hasSlept){
						this.hasSlept = true;
						java.lang.Thread.currentThread().sleep(this.sleepTime);
					}
					try{
						func();
					}catch(e){
						console.debug("Error running setTimeout thread:" + e);
					}
				}
			};

			var runnable = new java.lang.Runnable(def);
			var thread = new java.lang.Thread(runnable);
			thread.start();
			return timeouts.push(thread) - 1;
		};
	}

	var isLocal = function(url){
		return (new java.io.File(url)).exists();
	};

	// reset the has cache with node-appropriate values;
	var hasCache = {
		"host-rhino":1,
		"host-browser":0,
		"dom":0,
		"dojo-has-api":1,
		"dojo-xhr-factory":0,
		"dojo-inject-api":1,
		"dojo-timeout-api":0,
		"dojo-trace-api":1,
		"dojo-loader-catches":1,
		"dojo-dom-ready-api":0,
		"dojo-publish-privates":1,
		"dojo-sniff":0,
		"dojo-loader":1,
		"dojo-test-xd":0,
		"dojo-test-sniff":0
	};
	for(var p in hasCache){
		config.hasCache[p] = hasCache[p];
	}

	// reset some configuration switches with rhino-appropriate values
	var rhinoConfig = {
		baseUrl:baseUrl,
		commandLineArgs:rhinoArgs,
		deps:deps,
		timeout:0,
		locale:String(java.util.Locale.getDefault().toString().replace('_', '-').toLowerCase()),

		loaderPatch:{
			injectUrl: function(url, callback){
				try{
					if(isLocal(url)){
						load(url);
					}else{
						require.eval(readUrl(url, "UTF-8"));
					}
					callback();
				}catch(e){
					console.log("failed to load resource (" + url + ")");
					console.log(e);
				}
			},

			getText: function(url, sync, onLoad){
				// TODO: test https://bugzilla.mozilla.org/show_bug.cgi?id=471005; see v1.6 hostenv_rhino
				// note: async mode not supported in rhino
				onLoad(isLocal(url) ? readFile(url, "UTF-8") : readUrl(url, "UTF-8"));
			}
		}
	};
	for(p in rhinoConfig){
		config[p] = rhinoConfig[p];
	}
}
