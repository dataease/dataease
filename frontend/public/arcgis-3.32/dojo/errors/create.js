/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/errors/create",["../_base/lang"],function(g){return function(h,e,a,k){a=a||Error;var b=function(f){if(a===Error){Error.captureStackTrace&&Error.captureStackTrace(this,b);var c=Error.call(this,f),d;for(d in c)c.hasOwnProperty(d)&&(this[d]=c[d]);this.message=f;this.stack=c.stack}else a.apply(this,arguments);e&&e.apply(this,arguments)};b.prototype=g.delegate(a.prototype,k);b.prototype.name=h;return b.prototype.constructor=b}});