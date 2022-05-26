/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/store/Memory",["../_base/declare","./util/QueryResults","./util/SimpleQueryEngine"],function(f,g,h){return f("dojo.store.Memory",null,{constructor:function(a){for(var b in a)this[b]=a[b];this.setData(this.data||[])},data:null,idProperty:"id",index:null,queryEngine:h,get:function(a){return this.data[this.index[a]]},getIdentity:function(a){return a[this.idProperty]},put:function(a,b){var d=this.data,e=this.index,c=this.idProperty,c=a[c]=b&&"id"in b?b.id:c in a?a[c]:Math.random();if(c in
e){if(b&&!1===b.overwrite)throw Error("Object already exists");d[e[c]]=a}else e[c]=d.push(a)-1;return c},add:function(a,b){(b=b||{}).overwrite=!1;return this.put(a,b)},remove:function(a){var b=this.index,d=this.data;if(a in b)return d.splice(b[a],1),this.setData(d),!0},query:function(a,b){return g(this.queryEngine(a,b)(this.data))},setData:function(a){a.items?(this.idProperty=a.identifier||this.idProperty,a=this.data=a.items):this.data=a;this.index={};for(var b=0,d=a.length;b<d;b++)this.index[a[b][this.idProperty]]=
b}})});