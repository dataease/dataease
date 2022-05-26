/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/behavior","./_base/kernel ./_base/lang ./_base/array ./_base/connect ./query ./domReady".split(" "),function(d,f,l,g,m,k){d.deprecated("dojo.behavior","Use dojo/on with event delegation (on.selector())");d.behavior=new function(){function d(a,b){a[b]||(a[b]=[]);return a[b]}function h(a,b,c){var n={},e;for(e in a)"undefined"==typeof n[e]&&(c?c.call(b,a[e],e):b(a[e],e))}var k=0;this._behaviors={};this.add=function(a){h(a,this,function(b,a){a=d(this._behaviors,a);"number"!=typeof a.id&&
(a.id=k++);var c=[];a.push(c);if(f.isString(b)||f.isFunction(b))b={found:b};h(b,function(a,b){d(c,b).push(a)})})};var p=function(a,b,c){f.isString(b)?"found"==c?g.publish(b,[a]):g.connect(a,c,function(){g.publish(b,arguments)}):f.isFunction(b)&&("found"==c?b(a):g.connect(a,c,b))};this.apply=function(){h(this._behaviors,function(a,b){m(b).forEach(function(b){var c=0,e="_dj_behavior_"+a.id;if("number"==typeof b[e]&&(c=b[e],c==a.length))return;for(var d;d=a[c];c++)h(d,function(a,c){f.isArray(a)&&l.forEach(a,
function(a){p(b,a,c)})});b[e]=a.length})})}};k(function(){d.behavior.apply()});return d.behavior});