/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/store/util/SimpleQueryEngine",["../../_base/array"],function(l){return function(a,b){function h(c){c=l.filter(c,a);var f=b&&b.sort;f&&c.sort("function"==typeof f?f:function(c,a){for(var b,g=0;b=f[g];g++){var d=c[b.attribute],e=a[b.attribute],d=null!=d?d.valueOf():d,e=null!=e?e.valueOf():e;if(d!=e)return!!b.descending==(null==d||d>e)?-1:1}return 0});if(b&&(b.start||b.count)){var g=c.length;c=c.slice(b.start||0,(b.start||0)+(b.count||Infinity));c.total=g}return c}switch(typeof a){default:throw Error("Can not query with a "+
typeof a);case "object":case "undefined":var k=a;a=function(c){for(var b in k){var a=k[b];if(a&&a.test){if(!a.test(c[b],c))return!1}else if(a!=c[b])return!1}return!0};break;case "string":if(!this[a])throw Error("No filter function "+a+" was found in store");a=this[a];case "function":}h.matches=a;return h}});