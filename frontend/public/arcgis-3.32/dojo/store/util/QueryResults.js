/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/store/util/QueryResults",["../../_base/array","../../_base/lang","../../when"],function(f,c,g){var e=function(a){function b(d){a[d]=function(){var b=arguments,c=g(a,function(a){Array.prototype.unshift.call(b,a);return e(f[d].apply(f,b))});if("forEach"!==d||h)return c}}if(!a)return a;var h=!!a.then;h&&(a=c.delegate(a));b("forEach");b("filter");b("map");null==a.total&&(a.total=g(a,function(a){return a.length}));return a};c.setObject("dojo.store.util.QueryResults",e);return e});