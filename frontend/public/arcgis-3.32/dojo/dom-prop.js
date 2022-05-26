/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/dom-prop","exports ./_base/kernel ./sniff ./_base/lang ./dom ./dom-style ./dom-construct ./_base/connect".split(" "),function(f,q,h,r,m,t,l,n){function g(a){var c="";a=a.childNodes;for(var d=0,b;b=a[d];d++)8!=b.nodeType&&(c=1==b.nodeType?c+g(b):c+b.nodeValue);return c}var k={},u=1,p=q._scopeName+"attrid";h.add("dom-textContent",function(a,c,d){return"textContent"in d});f.names={"class":"className","for":"htmlFor",tabindex:"tabIndex",readonly:"readOnly",colspan:"colSpan",frameborder:"frameBorder",
rowspan:"rowSpan",textcontent:"textContent",valuetype:"valueType"};f.get=function(a,c){a=m.byId(a);var d=c.toLowerCase();c=f.names[d]||c;return"textContent"!=c||h("dom-textContent")?a[c]:g(a)};f.set=function(a,c,d){a=m.byId(a);if(2==arguments.length&&"string"!=typeof c){for(var b in c)f.set(a,b,c[b]);return a}b=c.toLowerCase();b=f.names[b]||c;if("style"==b&&"string"!=typeof d)return t.set(a,d),a;if("innerHTML"==b)return h("ie")&&a.tagName.toLowerCase()in{col:1,colgroup:1,table:1,tbody:1,tfoot:1,thead:1,
tr:1,title:1}?(l.empty(a),a.appendChild(l.toDom(d,a.ownerDocument))):a[b]=d,a;if("textContent"==b&&!h("dom-textContent"))return l.empty(a),a.appendChild(a.ownerDocument.createTextNode(d)),a;if(r.isFunction(d)){var e=a[p];e||(e=u++,a[p]=e);k[e]||(k[e]={});var g=k[e][b];if(g)n.disconnect(g);else try{delete a[b]}catch(v){}d?k[e][b]=n.connect(a,b,d):a[b]=null;return a}a[b]=d;return a}});