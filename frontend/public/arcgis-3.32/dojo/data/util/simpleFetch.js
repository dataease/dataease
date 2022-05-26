/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/data/util/simpleFetch",["../../_base/lang","../../_base/kernel","./sorter"],function(d,h,l){var c={};d.setObject("dojo.data.util.simpleFetch",c);c.errorHandler=function(b,a){a.onError&&a.onError.call(a.scope||h.global,b,a)};c.fetchHandler=function(b,a){var c=a.abort||null,d=!1,f=a.start?a.start:0,k=a.count&&Infinity!==a.count?f+a.count:b.length;a.abort=function(){d=!0;c&&c.call(a)};var g=a.scope||h.global;a.store||(a.store=this);a.onBegin&&a.onBegin.call(g,b.length,a);a.sort&&b.sort(l.createSortFunction(a.sort,
this));if(a.onItem)for(var e=f;e<b.length&&e<k;++e){var m=b[e];d||a.onItem.call(g,m,a)}a.onComplete&&!d&&(e=null,a.onItem||(e=b.slice(f,k)),a.onComplete.call(g,e,a))};c.fetch=function(b){b=b||{};b.store||(b.store=this);this._fetchItems(b,d.hitch(this,"fetchHandler"),d.hitch(this,"errorHandler"));return b};return c});