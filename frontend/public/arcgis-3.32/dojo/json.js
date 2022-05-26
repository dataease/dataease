/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/json",["./has"],function(f){var q="undefined"!=typeof JSON;f.add("json-parse",q);f.add("json-stringify",q&&'{"a":1}'==JSON.stringify({a:0},function(c,e){return e||1}));if(f("json-stringify"))return JSON;var r=function(c){return('"'+c.replace(/(["\\])/g,"\\$1")+'"').replace(/[\f]/g,"\\f").replace(/[\b]/g,"\\b").replace(/[\n]/g,"\\n").replace(/[\t]/g,"\\t").replace(/[\r]/g,"\\r")};return{parse:f("json-parse")?JSON.parse:function(c,e){if(e&&!/^([\s\[\{]*(?:"(?:\\.|[^"])*"|-?\d[\d\.]*(?:[Ee][+-]?\d+)?|null|true|false|)[\s\]\}]*(?:,|:|$))+$/.test(c))throw new SyntaxError("Invalid characters in JSON");
return eval("("+c+")")},stringify:function(c,e,g){function h(a,c,b){e&&(a=e(b,a));var d;d=typeof a;if("number"==d)return isFinite(a)?a+"":"null";if("boolean"==d)return a+"";if(null===a)return"null";if("string"==typeof a)return r(a);if("function"==d||"undefined"==d)return f;if("function"==typeof a.toJSON)return h(a.toJSON(b),c,b);if(a instanceof Date)return'"{FullYear}-{Month+}-{Date}T{Hours}:{Minutes}:{Seconds}Z"'.replace(/\{(\w+)(\+)?\}/g,function(b,c,d){b=a["getUTC"+c]()+(d?1:0);return 10>b?"0"+
b:b});if(a.valueOf()!==a)return h(a.valueOf(),c,b);var l=g?c+g:"",n=g?" ":"",m=g?"\n":"";if(a instanceof Array){var n=a.length,k=[];for(b=0;b<n;b++)d=h(a[b],l,b),"string"!=typeof d&&(d="null"),k.push(m+l+d);return"["+k.join(",")+m+c+"]"}k=[];for(b in a){var p;if(a.hasOwnProperty(b)){if("number"==typeof b)p='"'+b+'"';else if("string"==typeof b)p=r(b);else continue;d=h(a[b],l,b);"string"==typeof d&&k.push(m+l+p+":"+n+d)}}return"{"+k.join(",")+m+c+"}"}var f;"string"==typeof e&&(g=e,e=null);return h(c,
"","")}}});