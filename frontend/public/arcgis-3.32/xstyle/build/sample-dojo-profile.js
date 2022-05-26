dependencies = {
	action: 'release',
	plugins: {
		"xstyle/css": "xstyle/build/amd-css"
	},
	layers: [
	{
		name: "../xstyle/moduleWithCss.js",
		targetStylesheet: "./some-stylesheet.css",
		dependencies: [
			"xstyle.moduleWithCss"
		]
	}],
	
	prefixes: [
		[ "xstyle", "C:/packages/xstyle" ],
		[ "dojo", "C:/packages/dojo" ],
		[ "put-selector", "C:/packages/put-selector" ]
		
	]
};