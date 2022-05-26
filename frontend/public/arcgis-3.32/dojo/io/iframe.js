/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/io/iframe","../_base/config ../_base/json ../_base/kernel ../_base/lang ../_base/xhr ../sniff ../_base/window ../dom ../dom-construct ../query require ../aspect ../request/iframe".split(" "),function(m,n,h,g,l,b,p,q,u,v,w,r,e){h.deprecated("dojo/io/iframe","Use dojo/request/iframe.","2.0");b=e._iframeName;b=b.substring(0,b.lastIndexOf("_"));var f=g.delegate(e,{create:function(){return f._frame=e.create.apply(e,arguments)},get:null,post:null,send:function(a){var d,c=l._ioSetArgs(a,function(a){d&&
d.cancel()},function(a){var b=null;a=a.ioArgs;try{var c=a.handleAs;"xml"===c||"html"===c?b=d.response.data:(b=d.response.text,"json"===c?b=n.fromJson(b):"javascript"===c&&(b=h.eval(b)))}catch(t){b=t}return b},function(a,b){b.ioArgs._hasError=!0;return a}),b=c.ioArgs,f="GET",k=q.byId(a.form);a.method&&"POST"===a.method.toUpperCase()&&k&&(f="POST");a={method:f,handleAs:"json"===a.handleAs||"javascript"===a.handleAs?"text":a.handleAs,form:a.form,query:k?null:a.content,data:k?a.content:null,timeout:a.timeout,
ioArgs:b};a.method&&(a.method=a.method.toUpperCase());if(m.ioPublish&&h.publish&&!1!==b.args.ioPublish)var g=r.after(e,"_notifyStart",function(a){a.options.ioArgs===b&&(g.remove(),l._ioNotifyStart(c))},!0);d=e(b.url,a,!0);b._callNext=d._callNext;d.then(function(){c.resolve(c)}).otherwise(function(a){c.ioArgs.error=a;c.reject(a)});return c},_iframeOnload:p.global[b+"_onload"]});g.setObject("dojo.io.iframe",f);return f});