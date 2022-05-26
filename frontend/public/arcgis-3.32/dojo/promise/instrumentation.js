/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/promise/instrumentation",["./tracer","../has","../_base/lang","../_base/array"],function(g,l,h,m){function n(c,a,b){if(!c||!1!==c.log){var d="";c&&c.stack&&(d+=c.stack);a&&a.stack&&(d+="\n    ----------------------------------------\n    rejected"+a.stack.split("\n").slice(1).join("\n").replace(/^\s+/," "));b&&b.stack&&(d+="\n    ----------------------------------------\n"+b.stack);console.error(c,d)}}function q(c,a,b,d){a||n(c,b,d)}function r(c,a,b,d){m.some(e,function(b){if(b.error===
c)return a&&(b.handled=!0),!0})||e.push({error:c,rejection:b,handled:a,deferred:d,timestamp:(new Date).getTime()});k||(k=setTimeout(p,f))}function p(){var c=(new Date).getTime(),a=c-f;e=m.filter(e,function(b){return b.timestamp<a?(b.handled||n(b.error,b.rejection,b.deferred),!1):!0});k=e.length?setTimeout(p,e[0].timestamp+f-c):!1}l.add("config-useDeferredInstrumentation","report-unhandled-rejections");var e=[],k=!1,f=1E3;return function(c){var a=l("config-useDeferredInstrumentation");if(a){g.on("resolved",
h.hitch(console,"log","resolved"));g.on("rejected",h.hitch(console,"log","rejected"));g.on("progress",h.hitch(console,"log","progress"));var b=[];"string"===typeof a&&(b=a.split(","),a=b.shift());if("report-rejections"===a)c.instrumentRejected=q;else if("report-unhandled-rejections"===a||!0===a||1===a)c.instrumentRejected=r,f=parseInt(b[0],10)||f;else throw Error("Unsupported instrumentation usage \x3c"+a+"\x3e");}}});