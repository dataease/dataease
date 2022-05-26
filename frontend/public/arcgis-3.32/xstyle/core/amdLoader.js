/*
 * This is a very simple AMD module loader so that xstyle can be used standalone
 */

(function(){
	var doc = document;
	// find a script to go off of
	var scripts = doc.scripts;
	var baseScript = scripts[scripts.length-1];
	var baseUrl = baseScript.src.replace(/[^\/]+\/xstyle[^\/]*js/,'');
	// a very simple AMD loader
	define = function(id, deps, factory){
		if(!factory){
			factory = deps;
			deps = id;
			id = 'put-selector/put';
		}
		var waiting = 1;
		for(var i = 0;i < deps.length; i++){
			var dep = deps[i];
			var module = modules[dep];
			if(!module){
				// inject script tag
				module = modules[dep] = {callbacks: []};
				var node = doc.createElement('script');
				node.src = baseUrl + dep + '.js';
				baseScript.parentNode.insertBefore(node, baseScript);
			}
			if(module.callbacks){
				// add a callback for this waiting module
				waiting++;
				module.callbacks.push((function(i){
					return function(value){
						deps[i] = value;
						loaded();
					};
				})(i));
			}else{
				deps[i] = module.result;
			}
		}
		module = modules[id] || (modules[id] = {callbacks: []});
		loaded();
		function loaded(){
			if(--waiting < 1){
				// done loading, run the factory
				var result = module.result = factory && factory.apply(this, deps);
				var callbacks = module.callbacks;
				for(var i = 0 ; i < callbacks.length; i++){
					callbacks[i](result);
				}
				module.callbacks = 0;
			}
		}
	};
	
	require = function(deps, factory){
		define('', deps, factory);
	};
	
	var modules = {require: {result: require}};
})();
