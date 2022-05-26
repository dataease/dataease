/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/on/asyncEventListener",["../on","../has","../_base/window","../dom-construct","../domReady!"],function(f,g,a,c){function h(d){var a={},b;for(b in d)a[b]=d[b];return a}var e,b=!1;if(c){a=c.create("input",{type:"button"},a.body());f.once(a,"click",function(a){e=a});a.click();try{b=void 0===e.clientX}catch(d){b=!0}finally{c.destroy(a)}}g.add("native-async-event-support",!b);return function(a){return b?function(b){a.call(this,h(b))}:a}});