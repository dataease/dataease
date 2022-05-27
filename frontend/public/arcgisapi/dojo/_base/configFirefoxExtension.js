// TODO: this file needs to be converted to the v1.7 loader

// a host environment specifically built for Mozilla extensions, but derived
// from the browser host environment
if(typeof window != 'undefined'){
	dojo.isBrowser = true;
	dojo._name = "browser";


	// FIXME: PORTME
	//	http://developer.mozilla.org/en/mozIJSSubScriptLoader


	// attempt to figure out the path to dojo if it isn't set in the config
	(function(){
		// this is a scope protection closure. We set browser versions and grab
		// the URL we were loaded from here.

		// FIXME: need to probably use a different reference to "document" to get the hosting XUL environment

		dojo.baseUrl = dojo.config.baseUrl;

		// fill in the rendering support information in dojo.render.*
		var n = navigator;
		var dua = n.userAgent;
		var dav = n.appVersion;
		var tv = parseFloat(dav);

		dojo.isMozilla = dojo.isMoz = tv;
		if(dojo.isMoz){
			dojo.isFF = parseFloat(dua.split("Firefox/")[1]) || undefined;
		}

		// FIXME
		dojo.isQuirks = document.compatMode == "BackCompat";

		// FIXME
		// TODO: is the HTML LANG attribute relevant?
		dojo.locale = dojo.config.locale || n.language.toLowerCase();

		dojo._xhrObj = function(){
			return new XMLHttpRequest();
		};

		// monkey-patch _loadUri to handle file://, chrome://, and resource:// url's
		var oldLoadUri = dojo._loadUri;
		dojo._loadUri = function(uri, cb){
			var handleLocal = ["file:", "chrome:", "resource:"].some(function(prefix){
				return String(uri).indexOf(prefix) == 0;
			});
			if(handleLocal){
				// see:
				//		http://developer.mozilla.org/en/mozIJSSubScriptLoader
				var l = Components.classes["@mozilla.org/moz/jssubscript-loader;1"]
					.getService(Components.interfaces.mozIJSSubScriptLoader);
				var value = l.loadSubScript(uri, dojo.global);
				if(cb){ cb(value); }
				return true;
			}else{
				// otherwise, call the pre-existing version
				return oldLoadUri.apply(dojo, arguments);
			}
		};

		// FIXME: PORTME
		dojo._isDocumentOk = function(http){
			var stat = http.status || 0;
			return (stat >= 200 && stat < 300) || 	// Boolean
				stat == 304 || 						// allow any 2XX response code
				stat == 1223 || 						// get it out of the cache
				(!stat && (location.protocol == "file:" || location.protocol == "chrome:") );
		};

		// FIXME: PORTME
		// var owloc = window.location+"";
		// var base = document.getElementsByTagName("base");
		// var hasBase = (base && base.length > 0);
		var hasBase = false;

		dojo._getText = function(/*URI*/ uri, /*Boolean*/ fail_ok){
			// summary:
			//		Read the contents of the specified uri and return those contents.
			// uri:
			//		A relative or absolute uri. If absolute, it still must be in
			//		the same "domain" as we are.
			// fail_ok:
			//		Default false. If fail_ok and loading fails, return null
			//		instead of throwing.
			// returns:
			//		The response text. null is returned when there is a
			//		failure and failure is okay (an exception otherwise)

			// alert("_getText: " + uri);

			// NOTE: must be declared before scope switches ie. this._xhrObj()
			var http = dojo._xhrObj();

			if(!hasBase && dojo._Url){
				uri = (new dojo._Url(uri)).toString();
			}
			if(dojo.config.cacheBust){
				//Make sure we have a string before string methods are used on uri
				uri += "";
				uri += (uri.indexOf("?") == -1 ? "?" : "&") + String(dojo.config.cacheBust).replace(/\W+/g, "");
			}
			var handleLocal = ["file:", "chrome:", "resource:"].some(function(prefix){
				return String(uri).indexOf(prefix) == 0;
			});
			if(handleLocal){
				// see:
				//		http://forums.mozillazine.org/viewtopic.php?p=921150#921150
				var ioService = Components.classes["@mozilla.org/network/io-service;1"]
					.getService(Components.interfaces.nsIIOService);
				var scriptableStream = Components
					.classes["@mozilla.org/scriptableinputstream;1"]
					.getService(Components.interfaces.nsIScriptableInputStream);

				var channel = ioService.newChannel(uri, null, null);
				var input = channel.open();
				scriptableStream.init(input);
				var str = scriptableStream.read(input.available());
				scriptableStream.close();
				input.close();
				return str;
			}else{
				http.open('GET', uri, false);
				try{
					http.send(null);
					// alert(http);
					if(!dojo._isDocumentOk(http)){
						var err = Error("Unable to load " + uri + " status:" + http.status);
						err.status = http.status;
						err.responseText = http.responseText;
						throw err;
					}
				}catch(e){
					if(fail_ok){
						return null;
					} // null
					// rethrow the exception
					throw e;
				}
				return http.responseText; // String
			}
		};

		dojo._windowUnloaders = [];

		// FIXME: PORTME
		dojo.windowUnloaded = function(){
			// summary:
			//		signal fired by impending window destruction. You may use
			//		dojo.addOnWIndowUnload() or dojo.connect() to this method to perform
			//		page/application cleanup methods. See dojo.addOnWindowUnload for more info.
			var mll = dojo._windowUnloaders;
			while(mll.length){
				(mll.pop())();
			}
		};

		// FIXME: PORTME
		dojo.addOnWindowUnload = function(/*Object?*/obj, /*String|Function?*/functionName){
			// summary:
			//		registers a function to be triggered when window.onunload fires.
			//		Be careful trying to modify the DOM or access JavaScript properties
			//		during this phase of page unloading: they may not always be available.
			//		Consider dojo.addOnUnload() if you need to modify the DOM or do heavy
			//		JavaScript work.
			// example:
			//	|	dojo.addOnWindowUnload(functionPointer)
			//	|	dojo.addOnWindowUnload(object, "functionName")
			//	|	dojo.addOnWindowUnload(object, function(){ /* ... */});

			dojo._onto(dojo._windowUnloaders, obj, functionName);
		};

		// XUL specific APIs
		var contexts = [];
		var current = null;
		dojo._defaultContext = [ window, document ];

		dojo.pushContext = function(/*Object|String?*/g, /*MDocumentElement?*/d){
			// summary:
			//		causes subsequent calls to Dojo methods to assume the
			//		passed object and, optionally, document as the default
			//		scopes to use. A 2-element array of the previous global and
			//		document are returned.
			// description:
			//		dojo.pushContext treats contexts as a stack. The
			//		auto-detected contexts which are initially provided using
			//		dojo.setContext() require authors to keep state in order to
			//		"return" to a previous context, whereas the
			//		dojo.pushContext and dojo.popContext methods provide a more
			//		natural way to augment blocks of code to ensure that they
			//		execute in a different window or frame without issue. If
			//		called without any arguments, the default context (the
			//		context when Dojo is first loaded) is instead pushed into
			//		the stack. If only a single string is passed, a node in the
			//		intitial context's document is looked up and its
			//		contextWindow and contextDocument properties are used as
			//		the context to push. This means that iframes can be given
			//		an ID and code can be executed in the scope of the iframe's
			//		document in subsequent calls easily.
			// g:
			//		The global context. If a string, the id of the frame to
			//		search for a context and document.
			// d:
			//		The document element to execute subsequent code with.
			var old = [dojo.global, dojo.doc];
			contexts.push(old);
			var n;
			if(!g && !d){
				n = dojo._defaultContext;
			}else{
				n = [ g, d ];
				if(!d && dojo.isString(g)){
					var t = document.getElementById(g);
					if(t.contentDocument){
						n = [t.contentWindow, t.contentDocument];
					}
				}
			}
			current = n;
			dojo.setContext.apply(dojo, n);
			return old; // Array
		};

		dojo.popContext = function(){
			// summary:
			//		If the context stack contains elements, ensure that
			//		subsequent code executes in the *previous* context to the
			//		current context. The current context set ([global,
			//		document]) is returned.
			var oc = current;
			if(!contexts.length){
				return oc;
			}
			dojo.setContext.apply(dojo, contexts.pop());
			return oc;
		};

		// FIXME:
		//		don't really like the current arguments and order to
		//		_inContext, so don't make it public until it's right!
		dojo._inContext = function(g, d, f){
			var a = dojo._toArray(arguments);
			f = a.pop();
			if(a.length == 1){
				d = null;
			}
			dojo.pushContext(g, d);
			var r = f();
			dojo.popContext();
			return r;
		};

	})();

	dojo._initFired = false;
	//	BEGIN DOMContentLoaded, from Dean Edwards (http://dean.edwards.name/weblog/2006/06/again/)
	dojo._loadInit = function(e){
		dojo._initFired = true;
		// allow multiple calls, only first one will take effect
		// A bug in khtml calls events callbacks for document for event which isnt supported
		// for example a created contextmenu event calls DOMContentLoaded, workaround
		var type = (e && e.type) ? e.type.toLowerCase() : "load";
		if(arguments.callee.initialized || (type != "domcontentloaded" && type != "load")){ return; }
		arguments.callee.initialized = true;
		if(dojo._inFlightCount == 0){
			dojo._modulesLoaded();
		}
	};

	/*
	(function(){
		var _w = window;
		var _handleNodeEvent = function(evtName, fp){
			// summary:
			//		non-destructively adds the specified function to the node's
			//		evtName handler.
			// evtName: should be in the form "onclick" for "onclick" handlers.
			// Make sure you pass in the "on" part.
			var oldHandler = _w[evtName] || function(){};
			_w[evtName] = function(){
				fp.apply(_w, arguments);
				oldHandler.apply(_w, arguments);
			};
		};
		// FIXME: PORT
		// FIXME: dojo.unloaded requires dojo scope, so using anon function wrapper.
		_handleNodeEvent("onbeforeunload", function(){ dojo.unloaded(); });
		_handleNodeEvent("onunload", function(){ dojo.windowUnloaded(); });
	})();
	*/


	//	FIXME: PORTME
	//		this event fires a lot, namely for all plugin XUL overlays and for
	//		all iframes (in addition to window navigations). We only want
	//		Dojo's to fire once..but we might care if pages navigate. We'll
	//		probably need an extension-specific API
	if(!dojo.config.afterOnLoad){
		window.addEventListener("DOMContentLoaded", function(e){
			dojo._loadInit(e);
			// console.log("DOM content loaded", e);
		}, false);
	}

} //if (typeof window != 'undefined')

//Register any module paths set up in djConfig. Need to do this
//in the hostenvs since hostenv_browser can read djConfig from a
//script tag's attribute.
(function(){
	var mp = dojo.config["modulePaths"];
	if(mp){
		for(var param in mp){
			dojo.registerModulePath(param, mp[param]);
		}
	}
})();

//Load debug code if necessary.
if(dojo.config.isDebug){
	// logging stub for extension logging
	console.log = function(m){
		var s = Components.classes["@mozilla.org/consoleservice;1"].getService(
			Components.interfaces.nsIConsoleService
			);
		s.logStringMessage(m);
	};
	console.debug = function(){
		console.log(dojo._toArray(arguments).join(" "));
	};
	// FIXME: what about the rest of the console.* methods? And is there any way to reach into firebug and log into it directly?
}
