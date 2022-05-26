/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/when",["./Deferred","./promise/Promise"],function(d,g){return function(a,c,e,f){var b=a&&"function"===typeof a.then,h=b&&a instanceof g;if(!b)return 1<arguments.length?c?c(a):a:(new d).resolve(a);h||(b=new d(a.cancel),a.then(b.resolve,b.reject,b.progress),a=b.promise);return c||e||f?a.then(c,e,f):a}});