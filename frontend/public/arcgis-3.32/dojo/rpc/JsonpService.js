/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/rpc/JsonpService",["../_base/array","../_base/declare","../_base/lang","./RpcService","../io/script"],function(d,e,b,f,g){return e("dojo.rpc.JsonpService",f,{constructor:function(a,c){this.required&&(c&&b.mixin(this.required,c),d.forEach(this.required,function(a){if(""==a||void 0==a)throw Error("Required Service Argument not found: "+a);}))},strictArgChecks:!1,bind:function(a,c,b,d){g.get({url:d||this.serviceUrl,callbackParamName:this.callbackParamName||"callback",content:this.createRequest(c),
timeout:this.timeout,handleAs:"json",preventCache:!0}).addCallbacks(this.resultCallback(b),this.errorCallback(b))},createRequest:function(a){a=b.isArrayLike(a)&&1==a.length?a[0]:{};b.mixin(a,this.required);return a}})});