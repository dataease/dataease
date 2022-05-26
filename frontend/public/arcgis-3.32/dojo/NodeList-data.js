/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/NodeList-data",["./_base/kernel","./query","./_base/lang","./_base/array","./dom-attr"],function(d,h,k,m,l){var e=h.NodeList,c={},n=0,g=function(a){var b=l.get(a,"data-dojo-dataid");b||(b="pid"+n++,l.set(a,"data-dojo-dataid",b));return b},p=d._nodeData=function(a,b,e){var f=g(a),d;c[f]||(c[f]={});if(1==arguments.length)return c[f];"string"==typeof b?2<arguments.length?c[f][b]=e:d=c[f][b]:d=k.mixin(c[f],b);return d},q=d._removeNodeData=function(a,b){a=g(a);c[a]&&(b?delete c[a][b]:delete c[a])};
e._gcNodeData=d._gcNodeData=function(){var a=h("[data-dojo-dataid]").map(g),b;for(b in c)0>m.indexOf(a,b)&&delete c[b]};k.extend(e,{data:e._adaptWithCondition(p,function(a){return 0===a.length||1==a.length&&"string"==typeof a[0]}),removeData:e._adaptAsForEach(q)});return e});