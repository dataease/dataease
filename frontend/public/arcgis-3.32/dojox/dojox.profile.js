var profile = (function(){
	var testResourceRe = /\/tests\//,
	nodeModulesRe = /\/node_modules\//,

		copyOnly = function(filename, mid){
			var list = {
				"dojox/dojox.profile":1,
				"dojox/package.json":1,
				"dojox/mobile/themes/utils/compile":1,
				"dojox/mobile/themes/utils/cleanup":1,
				"dojox/app/tests/layoutApp/build.profile": 1,
				"dojox/app/tests/globalizedApp/build.profile": 1
			};
			return (mid in list) || /^dojox\/resources\//.test(mid) ||
				/(png|jpg|jpeg|gif|tiff)$/.test(filename) ||
				/dojox\/app\/build\//.test(mid) ||
				nodeModulesRe.test(mid);
		},

		excludes = [
			"secure",
			"data/(demos|ItemExplorer|StoreExplorer|restListener)",
			"drawing/plugins/drawing/Silverlight",
			"embed/(IE)",
			"flash/_base",
			"help",
			"image/(Gallery|SlideShow|ThumbnailPicker)",
			"jq",
			"lang/(aspect|async|docs|observable|oo|typed|functional/(binrec|linrec|listcomp|multirec|numrec|tailrec|util))",
			"layout/(BorderContainer|dnd|ext-dijit)",
			"mobile/app/",
			"rails",
			"robot",
			"sql/",
			"storage/(_common|AirDBStorageProvider|AirEncryptedLocalStorageProvider|AirFileStorageProvider|BehaviorStorageProvider|CookieStorageProvider|FlashStorageProvider|GearsStorageProvider|WhatWGStorageProvider)",
			"widget/(AnalogGauge|BarGauge|DataPresentation|DocTester|DynamicTooltip|FeedPortlet|FilePicker|gauge|Iterator|Loader|RollingList|SortList)",
			"wire/",
			"xmpp"
		],

		excludesRe = new RegExp(("^dojox/(" + excludes.join("|") + ")").replace(/\//, "\\/")),

		usesDojoProvideEtAl = function(mid){
			return excludesRe.test(mid);
		};

	return {
		resourceTags:{
			test: function(filename, mid){
				return testResourceRe.test(mid);
			},

			copyOnly: function(filename, mid){
				return copyOnly(filename, mid);
			},

			amd: function(filename, mid){
				return !testResourceRe.test(mid) && !copyOnly(filename, mid) && !usesDojoProvideEtAl(mid) && /\.js$/.test(filename);
			},

			miniExclude: function(filename, mid){
				return /\/demos\//.test(mid) || nodeModulesRe.test(mid);
			}
		}
	};
})();
