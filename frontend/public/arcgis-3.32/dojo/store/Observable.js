/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/store/Observable",["../_base/kernel","../_base/lang","../when","../_base/array"],function(c,h,p,u){c=function(b){function c(a,l){var d=b[a];d&&(b[a]=function(q){var f;"put"===a&&(f=b.getIdentity(q));if(r)return d.apply(this,arguments);r=!0;try{var c=d.apply(this,arguments);p(c,function(b){l("object"==typeof b&&b||q,f)});return c}finally{r=!1}})}var m=[],v=0;b=h.delegate(b);b.notify=function(b,l){v++;for(var d=m.slice(),a=0,f=d.length;a<f;a++)d[a](b,l)};var y=b.query;b.query=function(a,
l){l=l||{};var d=y.apply(this,arguments);if(d&&d.forEach){var c=h.mixin({},l);delete c.start;delete c.count;var f=b.queryEngine&&b.queryEngine(a,c),r=v,t=[],x;d.observe=function(a,c){1==t.push(a)&&m.push(x=function(a,h){p(d,function(e){var d=e.length!=l.count,g,m;if(++r!=v)throw Error("Query is out of date, you must observe() the query prior to any data modifications");var p,n=-1,k=-1;if(void 0!==h){var q=[].concat(e);f&&!a&&(q=f(e));g=0;for(m=e.length;g<m;g++){var w=e[g];if(b.getIdentity(w)==h&&
!(0>q.indexOf(w))){p=w;n=g;!f&&a||e.splice(g,1);break}}}f?a&&(f.matches?f.matches(a):f([a]).length)&&(g=-1<n?n:e.length,e.splice(g,0,a),k=u.indexOf(f(e),a),e.splice(g,1),l.start&&0==k||!d&&k==e.length?k=-1:e.splice(k,0,a)):a&&(void 0!==h?k=n:l.start||(k=b.defaultIndex||0,e.splice(k,0,a)));if((-1<n||-1<k)&&(c||!f||n!=k))for(d=t.slice(),g=0;e=d[g];g++)e(a||p,n,k)})});var h={};h.remove=h.cancel=function(){var b=u.indexOf(t,a);-1<b&&(t.splice(b,1),t.length||m.splice(u.indexOf(m,x),1))};return h}}return d};
var r;c("put",function(a,c){b.notify(a,c)});c("add",function(a){b.notify(a)});c("remove",function(a){b.notify(void 0,a)});return b};h.setObject("dojo.store.Observable",c);return c});