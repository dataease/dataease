/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/DeferredList",["./_base/kernel","./_base/Deferred","./_base/array"],function(b,c,d){b.DeferredList=function(a,b,k,l,m){var f=[];c.call(this);var e=this;0!==a.length||b||this.resolve([0,[]]);var g=0;d.forEach(a,function(c,d){function h(b,c){f[d]=[b,c];g++;g===a.length&&e.resolve(f)}c.then(function(a){b?e.resolve([d,a]):h(!0,a)},function(a){k?e.reject(a):h(!1,a);if(l)return null;throw a;})})};b.DeferredList.prototype=new c;b.DeferredList.prototype.gatherResults=function(a){a=new b.DeferredList(a,
!1,!0,!1);a.addCallback(function(a){var b=[];d.forEach(a,function(a){b.push(a[1])});return b});return a};return b.DeferredList});