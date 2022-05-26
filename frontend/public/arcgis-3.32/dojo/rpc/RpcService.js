/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/rpc/RpcService","../_base/array ../_base/declare ../_base/Deferred ../_base/kernel ../_base/lang ../_base/url ../_base/xhr".split(" "),function(f,g,h,k,d,e,l){return g("dojo.rpc.RpcService",null,{constructor:function(a){if(a)if(d.isString(a)||a instanceof e){var b=l.get({url:a instanceof e?a+"":a,handleAs:"json-comment-optional",sync:!0});b.addCallback(this,"processSmd");b.addErrback(function(){throw Error("Unable to load SMD from "+a);})}else a.smdStr?this.processSmd(k.eval("("+a.smdStr+
")")):(a.serviceUrl&&(this.serviceUrl=a.serviceUrl),this.timeout=a.timeout||0,"strictArgChecks"in a&&(this.strictArgChecks=a.strictArgChecks),this.processSmd(a))},strictArgChecks:!0,serviceUrl:"",parseResults:function(a){return a},errorCallback:function(a){return function(b){a.errback(b)}},resultCallback:function(a){return d.hitch(this,function(b){if(null!=b.error){var c;"object"==typeof b.error?(c=Error(b.error.message),c.code=b.error.code,c.error=b.error.error):c=Error(b.error);c.id=b.id;c.errorObject=
b;a.errback(c)}else a.callback(this.parseResults(b))})},generateMethod:function(a,b,c){return d.hitch(this,function(){var e=new h;if(this.strictArgChecks&&null!=b&&arguments.length!=b.length)throw Error("Invalid number of parameters for remote method.");this.bind(a,d._toArray(arguments),e,c);return e})},processSmd:function(a){a.methods&&f.forEach(a.methods,function(a){if(a&&a.name&&(this[a.name]=this.generateMethod(a.name,a.parameters,a.url||a.serviceUrl||a.serviceURL),!d.isFunction(this[a.name])))throw Error("RpcService: Failed to create"+
a.name+"()");},this);this.serviceUrl=a.serviceUrl||a.serviceURL;this.required=a.required;this.smd=a}})});