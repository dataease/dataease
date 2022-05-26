// TODO: this file needs to be converted to the v1.7 loader

// module:
//		configSpidermonkey
// summary:
//		 SpiderMonkey host environment

if(dojo.config["baseUrl"]){
	dojo.baseUrl = dojo.config["baseUrl"];
}else{
	dojo.baseUrl = "./";
}

dojo._name = 'spidermonkey';



dojo.isSpidermonkey = true;
dojo.exit = function(exitcode){
	quit(exitcode);
};

if(typeof print == "function"){
	console.debug = print;
}

if(typeof line2pc == 'undefined'){
	throw new Error("attempt to use SpiderMonkey host environment when no 'line2pc' global");
}

dojo._spidermonkeyCurrentFile = function(depth){
	//
	//	This is a hack that determines the current script file by parsing a
	//	generated stack trace (relying on the non-standard "stack" member variable
	//	of the SpiderMonkey Error object).
	//
	//	If param depth is passed in, it'll return the script file which is that far down
	//	the stack, but that does require that you know how deep your stack is when you are
	//	calling.
	//
	var s = '';
	try{
		throw Error("whatever");
	}catch(e){
		s = e.stack;
	}
	// lines are like: bu_getCurrentScriptURI_spidermonkey("ScriptLoader.js")@burst/Runtime.js:101
	var matches = s.match(/[^@]*\.js/gi);
	if(!matches){
		throw Error("could not parse stack string: '" + s + "'");
	}
	var fname = (typeof depth != 'undefined' && depth) ? matches[depth + 1] : matches[matches.length - 1];
	if(!fname){
		throw Error("could not find file name in stack string '" + s + "'");
	}
	//print("SpiderMonkeyRuntime got fname '" + fname + "' from stack string '" + s + "'");
	return fname;
};

// print(dojo._spidermonkeyCurrentFile(0));

dojo._loadUri = function(uri){
	// spidermonkey load() evaluates the contents into the global scope (which
	// is what we want).
	// TODO: sigh, load() does not return a useful value.
	// Perhaps it is returning the value of the last thing evaluated?
	// var ok =
	load(uri);
	// console.log("spidermonkey load(", uri, ") returned ", ok);
	return 1;
};

//Register any module paths set up in djConfig. Need to do this
//in the hostenvs since hostenv_browser can read djConfig from a
//script tag's attribute.
if(dojo.config["modulePaths"]){
	for(var param in dojo.config["modulePaths"]){
		dojo.registerModulePath(param, dojo.config["modulePaths"][param]);
	}
}
