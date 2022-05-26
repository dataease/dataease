/*
 * This module can be executed standalone in node, to build CSS files, inlining imports, and 
 * pre-processing extensions for faster run-time execution. This module is also
 * used by the AMD build process.
 */
var operatorMatch = {
	'{': '}',
	'[': ']',
	'(': ')'
};
var nextId = 1;

document = {
	createElement: function(){
		return {style:
			{// TODO: may want to import static has features to determine if some of these exist
			}
		};
	},
	getElementsByTagName: function(){
		return [];
	},
	addEventListener: function(){}
};
navigator = {
	userAgent: "build"
};

var pseudoDefine = function(id, deps, factory){
		parse = factory({
				isBuild: true,
				isTagSupported: function(){
					return true;					
				}
		});
	};
var requiredModules = [], base64Module;

if(typeof define == 'undefined'){
	var fs = require('fs'),
		pathModule = require('path'),
		parse;
	define = pseudoDefine;
	require('./core/parser');
}else{
	define(['build/fs', 'build/fileUtils', './build/base64'], function(fsModule, fileUtils, base64){
		fs = fsModule;
		base64Module = base64;
		// must create our own path module for Rhino :/
		pathModule = {
			resolve: function(base, target){
				return fileUtils.compactPath(base.replace(/[^\/]+$/, '') + target);
			},
			dirname: function(path){
				return path.replace(/[\/\\][^\/\\]*$/, '');
			},
			relative: function(basePath, path){
				basePath = this.dirname(basePath);
				var baseParts = basePath.split(/[\/\\]/);
				var parts = path.split(/[\/\\]/);
				var l = parts.length;
				for (var i = 0; i < l; i++){
					if (parts[i] !== baseParts[i]){
						break;
					}
				}
				parts.splice(0, i);
				for (; i < baseParts.length; i++){
					parts.unshift('..');
				}
				return parts.join('/');
			},
			join: function(base, target){
				return fileUtils.compactPath((base[base.length - 1] == '/' ? base : (base + '/')) + target);
			}
		};
		return function(xstyleText){
			var define = pseudoDefine;
			var vm;
			try {
				require(['dojo/node!vm']);
				vm = require('dojo/node!vm');
			} catch (e) {
			}
			vm ?
				vm.runInNewContext(xstyleText, {
					define: define,
					console: console
				}, 'xstyle/core/parser.js') :
				eval(xstyleText);
			return processCss;
		};
	});
}
function main(source, target){
	var imported = {};
	var basePath = source.replace(/[^\/]*$/, '');
	var cssText = fs.readFileSync(source).toString("utf-8");
	var processed = processCss(cssText, basePath, basePath);
	var output = processed.standardCss;
	if(processed.xstyleCss){
		output += 'x-xstyle{content:"' + 
				processed.xstyleCss.replace(/["\\\n\r]/g, '\\$&') + 
					'";}';
	}
	if(target){
		fs.writeFileSync(target, output);
	}else{
		console.log(output);
	}
}
function minify(cssText){
	return cssText.
			replace(/\/\*([^\*]|\*[^\/])*\*\//g, ' ').
			replace(/\s*("(\\\\|[^\"])*"|'(\\\\|[^\'])*'|[;}{:])\s*/g,"$1").
			replace(/[^\x00-\x7F]([0-9a-fA-F])?/g, function(character, hexComesNext){
				// escape non-ascii characters to be safe
				return '\\' + character.charCodeAt(0).toString(16) + (hexComesNext ? ' ' + hexComesNext : '');
			});
}
var mimeTypes = {
	eot: "application/vnd.ms-fontobject",
	woff: "application/font-woff",
	gif: "image/gif",
	jpg: "image/jpeg",
	jpeg: "image/jpeg",
	png: "image/png",
	svg:"image/svg+xml"
}
function processCss(cssText, basePath, cssPath, inlineAllResources){
	function XRule(){
	}
	var ruleCount = 0;
	XRule.prototype = {
		newCall: function(name){
			var call = new Call(name);
			call.parent = this;
			return call;
		},
		childRules: 0,
		newRule: function(name){
			var rule = (this.rules || (this.rules = {}))[name] = new XRule();
			rule.ruleIndex = this.childRules++;
			rule.parent = this;
			return rule;
		},
		getDefinition: function(name, includeRules){
			var parentRule = this;
			do{
				var target = parentRule.definitions && parentRule.definitions[name]
					|| (includeRules && parentRule.rules && parentRule.rules[name]);
				parentRule = parentRule.parent;
			}while(!target && parentRule);
			if(target){
				target.name = name;
			}
			return target;
		},
		declareDefinition: function(name, value, conditional){
			// TODO: access staticHasFeatures to check conditional
			var valueString = {
				toString: function(){
					return value.toString(2);
				}
			};
			(this.xstyleCss = this.xstyleCss || []).push(name + '=', valueString, ';');
			var definitions = (this.definitions || (this.definitions = {}));
			definitions[name] = value || new XRule();
		},
		setValue: function(name, value){
			var target = this.getDefinition(name);
			var browserCss = this.browserCss = this.browserCss || [];
			if(!this.ruleStarted && !this.root){
				this.ruleStarted = true;
				browserCss.push(this.selector, '{');
			}
			if(!target){
				browserCss.push(name, ':', value, ';');
			}
			if(target || typeof value == 'object'){
				if(!this.xstyleStarted){
					this.xstyleStarted = true;
					this.xstyleCss = this.xstyleCss || [];
				}
				this.xstyleCss.push(name + ':' + value + ';');
			}
		},
		onArguments: function(){},
		setMediaSelector: function(selector){
			this.selector = '';
			browserCss.push(selector, '{');
			this.isMediaBlock = true;
		},
		toString: function(mode){
			var str = '';
			str += this.xstyleCss ? this.xstyleCss.join('') : '';
			for(var i in this.rules){
				var rule = this.rules[i];
				if(rule.ref && !rule.creating){
					str += rule.toString(1);
				}
			}
			if(!this.root && !this.isMediaBlock && this.xstyleStarted && (str || mode != 1)){
				str = ((mode == 2 && this.bases) || this.tagName || '') + '{' + this.ref + str.replace(/\s+/g, ' ') + '}';
			}
			return str;
		},
		onRule: function(){
			if(this.browserCss){
				this.browserCss.push('}');
				this.ref = '/' + ruleCount;
				ruleCount++;
				browserCss.push(this.browserCss.join(''));
			}
			if(this.isMediaBlock){
				browserCss.push('}');
			}
		},
		extend: function(derivative, fullExtension){
			var definitions = this.definitions;
			if(definitions && fullExtension){
				// TODO: need to mixin this in, if it already exists
				derivative.definitions = Object.create(definitions);
			}
			(derivative.bases = derivative.bases || []).push(this.name);
		}
	};
	// a class representing function calls
	function Call(value){
		// we store the caller and the arguments
		this.caller = value;
		this.args = [];
	}
	var CallPrototype = Call.prototype = new XRule;
	CallPrototype.declareProperty = CallPrototype.setValue = function(name, value){
		// handle these both as addition of arguments
		this.args.push(value);
	};
	CallPrototype.toString = function(){
		var operator = this.operator;
		return operator + this.args + operatorMatch[operator]; 
	};
	
	function insertRule(cssText){
		//browserCss.push(cssText);
	}
	function correctUrls(cssText, path){
		// correct all the URLs in the stylesheets
		// determine the directory path
		path = pathModule.dirname(path) + '/';
		//console.log("starting path", basePath , path);
		// compute the relative path from where we are to the base path where the stylesheet will go
		var relativePath = pathModule.relative(basePath, path);
		return cssText.replace(/url\s*\(\s*['"]?([^'"\)]*)['"]?\s*\)/g, function(t, url){
			if(url.match(/^data:/)){
				return url;
			}
			if(/^#/.test(url)){ //do not replace urls which have only fragment (e.g. VML behavior property: "behavior: url(#default#VML);"")
				return url;
			}
			if(inlineAllResources || /#inline$/.test(url)){
				// we can inline the resource
				suffix = url.match(/\.(\w+)(#|\?|$)/);
				suffix = suffix && suffix[1];
				url = url.replace(/[\?#].*/,'');
				
				if(typeof java !== 'undefined'){
					// reading binary is hard in rhino
					var file = new java.io.File(pathModule.join(path, url));
					var length = file.length();
					var fis = new java.io.FileInputStream(file);
					var bytes = java.lang.reflect.Array.newInstance(java.lang.Byte.TYPE, length); 
					fis.read(bytes, 0, length);
					var jsBytes = new Array(length);
					for(var i = 0; i < bytes.length;i++){
						var singleByte = bytes[i];
						if(singleByte < 0){
							singleByte = 256 + singleByte;
						}
						jsBytes[i] = singleByte;
					}
					var moduleText =base64Module.encode(jsBytes);
				}else{
					// in node base64 encoding is easy
					var moduleText = fs.readFileSync(pathModule.join(path, url)).toString("base64");
				}
				return 'url("data:' + (mimeTypes[suffix] || 'application/octet-stream') + 
							';base64,' + moduleText + '")';
			}
			// or we adjust the URL
			var adjustedPath = pathModule.resolve(relativePath, url);
			return 'url("' + adjustedPath + '")';
		});
	};
	parse.getStyleSheet = function(importRule, sequence, styleSheet){
		var importValue = sequence[1];
		var path;
		try{
			if(importValue.operator === '('){
				// a url() call
				var firstArg = importValue.args[0];
				// extract  from a literal string, or just grab the raw text
				// note that the correctUrls should have already resolved this path
				path = firstArg.length? firstArg[0].value : firstArg.toString();
			}else{
				// assume it is a string
				path  = pathModule.resolve(styleSheet.href, importValue.value);
			}
		}catch(error){
			console.error('Can not parse import value of ', error, importValue, firstArg);
		}
		var localSource = '';
		try{
			localSource = fs.readFileSync(path).toString('utf-8');
		}catch(e){
			console.error(e.stack || e);
		}
		localSource = correctUrls(localSource, path);
		return {
			localSource: localSource,
			href: path || '.',
			insertRule: insertRule,
			cssRules: []
		};
	};
	var browserCss = [];//[correctUrls(cssText, basePath + "placeholder.css")];
	var rootRule = new XRule;
	rootRule.root = true;
	parse(rootRule, correctUrls(cssText, cssPath),
			{href: cssPath || '.', cssRules:[], insertRule: insertRule});
	rootRule.definitions = {
		Math:1,
		require:1,
		item: 1,
		'native': 1,
		prefixed: 1
	}
	function visit(parent){
		//browserCss.push(parent.selector + '{' + parent.cssText + '}'); 
		/*for(var i in parent.variables){
			if(!intrinsicVariables.hasOwnProperty(i)){
				xstyleCss.push(i,'=',parent.variables[i]);
			}
		}*/
	}
	visit(rootRule);
	return {
		standardCss: minify(browserCss.join('')),
		xstyleCss: rootRule.toString(),
		requiredModules: requiredModules
	};
}
if(typeof module != 'undefined' && require.main == module){
	main.apply(this, process.argv.slice(2));
}
