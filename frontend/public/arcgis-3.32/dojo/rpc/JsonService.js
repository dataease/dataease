/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/rpc/JsonService","../_base/declare ../_base/Deferred ../_base/json ../_base/lang ../_base/xhr ./RpcService".split(" "),function(d,e,f,g,h,k){return d("dojo.rpc.JsonService",k,{bustCache:!1,contentType:"application/json-rpc",lastSubmissionId:0,callRemote:function(a,b){var c=new e;this.bind(a,b,c);return c},bind:function(a,b,c,d){h.post({url:d||this.serviceUrl,postData:this.createRequest(a,b),contentType:this.contentType,timeout:this.timeout,handleAs:"json-comment-optional"}).addCallbacks(this.resultCallback(c),
this.errorCallback(c))},createRequest:function(a,b){a={params:b,method:a,id:++this.lastSubmissionId};return f.toJson(a)},parseResults:function(a){if(g.isObject(a)){if("result"in a)return a.result;if("Result"in a)return a.Result;if("ResultSet"in a)return a.ResultSet}return a}})});