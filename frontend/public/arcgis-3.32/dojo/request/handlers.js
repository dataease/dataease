/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/request/handlers",["../json","../_base/kernel","../_base/array","../has","../selector/_loader"],function(l,m,n,d){function g(a){var b=h[a.options.handleAs];a.data=b?b(a):a.data||a.text;return a}d.add("activex","undefined"!==typeof ActiveXObject);d.add("dom-parser",function(a){return"DOMParser"in a});var k;if(d("activex")){var p=["Msxml2.DOMDocument.6.0","Msxml2.DOMDocument.4.0","MSXML2.DOMDocument.3.0","MSXML.DOMDocument"],f;k=function(a){function b(a){try{var b=new ActiveXObject(a);
b.async=!1;b.loadXML(e);c=b;f=a}catch(q){return!1}return!0}var c=a.data,e=a.text;c&&d("dom-qsa2.1")&&!c.querySelectorAll&&d("dom-parser")&&(c=(new DOMParser).parseFromString(e,"application/xml"));c&&c.documentElement||f&&b(f)||n.some(p,b);return c}}var e=function(a){return d("native-xhr2-blob")||"blob"!==a.options.handleAs||"undefined"===typeof Blob?a.xhr.response:new Blob([a.xhr.response],{type:a.xhr.getResponseHeader("Content-Type")})},h={javascript:function(a){return m.eval(a.text||"")},json:function(a){return l.parse(a.text||
null)},xml:k,blob:e,arraybuffer:e,document:e};g.register=function(a,b){h[a]=b};return g});