var profile = (function(){
	var testResourceRe = /^dijit\/tests\//,
	nodeModulesRe = /\/node_modules\//,

		copyOnly = function(filename, mid){
			var list = {
				"dijit/dijit.profile":1,
				"dijit/package.json":1,
				"dijit/themes/claro/compile":1
			};
			return (mid in list) ||
				(/^dijit\/resources\//.test(mid) && !/\.css$/.test(filename)) ||
				/(png|jpg|jpeg|gif|tiff)$/.test(filename) ||
				nodeModulesRe.test(mid);
		};

	return {
		resourceTags:{
			test: function(filename, mid){
				return testResourceRe.test(mid) || mid=="dijit/robot" || mid=="dijit/robotx";
			},

			copyOnly: function(filename, mid){
				return copyOnly(filename, mid);
			},

			amd: function(filename, mid){
				return !testResourceRe.test(mid) && !copyOnly(filename, mid) && /\.js$/.test(filename);
			},

			miniExclude: function(filename, mid){
				return /^dijit\/bench\//.test(mid) ||
					/^dijit\/themes\/themeTest/.test(mid) ||
					nodeModulesRe.test(mid);
			}
		}
	};
})();
