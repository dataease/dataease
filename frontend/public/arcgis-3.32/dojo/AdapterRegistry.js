/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/AdapterRegistry",["./_base/kernel","./_base/lang"],function(c,d){c=c.AdapterRegistry=function(b){this.pairs=[];this.returnWrappers=b||!1};d.extend(c,{register:function(b,a,c,d,e){this.pairs[e?"unshift":"push"]([b,a,c,d])},match:function(){for(var b=0;b<this.pairs.length;b++){var a=this.pairs[b];if(a[1].apply(this,arguments))return a[3]||this.returnWrappers?a[2]:a[2].apply(this,arguments)}throw Error("No match found");},unregister:function(b){for(var a=0;a<this.pairs.length;a++)if(this.pairs[a][0]==
b)return this.pairs.splice(a,1),!0;return!1}});return c});