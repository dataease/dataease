/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/dom-form",["./_base/lang","./dom","./io-query","./json"],function(p,n,q,r){var g={fieldToObject:function(a){var c=null;if(a=n.byId(a)){var b=a.name,f=(a.type||"").toLowerCase();if(b&&f&&!a.disabled)if("radio"==f||"checkbox"==f)a.checked&&(c=a.value);else if(a.multiple)for(c=[],a=[a.firstChild];a.length;)for(b=a.pop();b;b=b.nextSibling)if(1==b.nodeType&&"option"==b.tagName.toLowerCase())b.selected&&c.push(b.value);else{b.nextSibling&&a.push(b.nextSibling);b.firstChild&&a.push(b.firstChild);
break}else c=a.value}return c},toObject:function(a){var c={};a=n.byId(a).elements;for(var b=0,f=a.length;b<f;++b){var d=a[b],e=d.name,k=(d.type||"").toLowerCase();if(e&&k&&0>"file|submit|image|reset|button".indexOf(k)&&!d.disabled){var l=c,m=e,d=g.fieldToObject(d);if(null!==d){var h=l[m];"string"==typeof h?l[m]=[h,d]:p.isArray(h)?h.push(d):l[m]=d}"image"==k&&(c[e+".x"]=c[e+".y"]=c[e].x=c[e].y=0)}}return c},toQuery:function(a){return q.objectToQuery(g.toObject(a))},toJson:function(a,c){return r.stringify(g.toObject(a),
null,c?4:0)}};return g});