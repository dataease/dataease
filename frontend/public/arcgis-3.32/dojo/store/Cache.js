/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/store/Cache",["../_base/lang","../when"],function(h,f){var k=function(e,d,g){g=g||{};return h.delegate(e,{query:function(a,b){a=e.query(a,b);a.forEach(function(a){g.isLoaded&&!g.isLoaded(a)||d.put(a)});return a},queryEngine:e.queryEngine||d.queryEngine,get:function(a,b){return f(d.get(a),function(c){return c||f(e.get(a,b),function(b){b&&d.put(b,{id:a});return b})})},add:function(a,b){return f(e.add(a,b),function(c){d.add(c&&"object"==typeof c?c:a,b);return c})},put:function(a,b){d.remove(b&&
b.id||this.getIdentity(a));return f(e.put(a,b),function(c){d.put(c&&"object"==typeof c?c:a,b);return c})},remove:function(a,b){return f(e.remove(a,b),function(c){return d.remove(a,b)})},evict:function(a){return d.remove(a)}})};h.setObject("dojo.store.Cache",k);return k});