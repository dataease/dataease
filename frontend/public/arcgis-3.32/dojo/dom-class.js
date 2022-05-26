/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/dom-class",["./_base/lang","./_base/array","./dom"],function(p,q,g){function k(a){if("string"==typeof a||a instanceof String){if(a&&!m.test(a))return n[0]=a,n;a=a.split(m);a.length&&!a[0]&&a.shift();a.length&&!a[a.length-1]&&a.pop();return a}return a?q.filter(a,function(a){return a}):[]}var e,m=/\s+/,n=[""],d={};return e={contains:function(a,c){return 0<=(" "+g.byId(a).className+" ").indexOf(" "+c+" ")},add:function(a,c){a=g.byId(a);c=k(c);var b=a.className,f,b=b?" "+b+" ":" ";f=b.length;
for(var h=0,e=c.length,d;h<e;++h)(d=c[h])&&0>b.indexOf(" "+d+" ")&&(b+=d+" ");f<b.length&&(a.className=b.substr(1,b.length-2))},remove:function(a,c){a=g.byId(a);var b;if(void 0!==c){c=k(c);b=" "+a.className+" ";for(var f=0,d=c.length;f<d;++f)b=b.replace(" "+c[f]+" "," ");b=p.trim(b)}else b="";a.className!=b&&(a.className=b)},replace:function(a,c,b){a=g.byId(a);d.className=a.className;e.remove(d,b);e.add(d,c);a.className!==d.className&&(a.className=d.className)},toggle:function(a,c,b){a=g.byId(a);
if(void 0===b){c=k(c);for(var d=0,h=c.length,l;d<h;++d)l=c[d],e[e.contains(a,l)?"remove":"add"](a,l)}else e[b?"add":"remove"](a,c);return b}}});