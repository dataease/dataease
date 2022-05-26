/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/domReady",["./global","./has"],function(k,t){function d(b){c.push(b);e&&p()}function p(){if(!l){for(l=!0;c.length;)try{c.shift()(a)}catch(b){console.error(b,"in domReady callback",b.stack)}l=!1;d._onQEmpty()}}var a=document,m={loaded:1,complete:1},n="string"!=typeof a.readyState,e=!!m[a.readyState],c=[],l;d.load=function(b,a,c){d(c)};d._Q=c;d._onQEmpty=function(){};n&&(a.readyState="loading");if(!e){var f=[],g=function(b){b=b||k.event;e||"readystatechange"==b.type&&!m[a.readyState]||
(n&&(a.readyState="complete"),e=1,p())},h=function(b,a){b.addEventListener(a,g,!1);c.push(function(){b.removeEventListener(a,g,!1)})};if(!t("dom-addeventlistener")){var h=function(b,a){a="on"+a;b.attachEvent(a,g);c.push(function(){b.detachEvent(a,g)})},q=a.createElement("div");try{q.doScroll&&null===k.frameElement&&f.push(function(){try{return q.doScroll("left"),1}catch(b){}})}catch(b){}}h(a,"DOMContentLoaded");h(k,"load");"onreadystatechange"in a?h(a,"readystatechange"):n||f.push(function(){return m[a.readyState]});
if(f.length){var r=function(){if(!e){for(var a=f.length;a--;)if(f[a]()){g("poller");return}setTimeout(r,30)}};r()}}return d});