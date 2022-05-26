/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/store/api/Store",["../../_base/declare"],function(d){var c=d(null,{idProperty:"id",queryEngine:null,get:function(a){},getIdentity:function(a){},put:function(a,b){},add:function(a,b){},remove:function(a){delete this.index[a];for(var b=this.data,d=this.idProperty,c=0,e=b.length;c<e;c++)if(b[c][d]==a){b.splice(c,1);break}},query:function(a,b){},transaction:function(){},getChildren:function(a,b){},getMetadata:function(a){}});c.PutDirectives=d(null,{});c.SortInformation=d(null,{});c.QueryOptions=
d(null,{});c.QueryResults=d(null,{forEach:function(a,b){},filter:function(a,b){},map:function(a,b){},then:function(a,b){},observe:function(a,b){},total:0});c.Transaction=d(null,{commit:function(){},abort:function(a,b){}});return c});