/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/request/node","require ./util ./handlers ../errors/RequestTimeoutError ../node!http ../node!https ../node!url ../node!stream".split(" "),function(u,f,h,k,l,m,n,p){function g(b,a){var c=f.parseArgs(b,f.deepCreate(q,a),a&&a.data instanceof r);b=c.url;a=c.options;var d=f.deferred(c,function(a,b){b.clientRequest.abort()});b=n.parse(b);var e=c.requestOptions={hostname:b.hostname,port:b.port,socketPath:a.socketPath,method:a.method,headers:a.headers,agent:a.agent,pfx:a.pfx,key:a.key,passphrase:a.passphrase,
cert:a.cert,ca:a.ca,ciphers:a.ciphers,rejectUnauthorized:!1===a.rejectUnauthorized?!1:!0};b.path&&(e.path=b.path);if(a.user||a.password)e.auth=(a.user||"")+":"+(a.password||"");b=c.clientRequest=("https:"===b.protocol?m:l).request(e);a.socketOptions&&("timeout"in a.socketOptions&&b.setTimeout(a.socketOptions.timeout),"noDelay"in a.socketOptions&&b.setNoDelay(a.socketOptions.noDelay),"keepAlive"in a.socketOptions&&(e=a.socketOptions.keepAlive,b.setKeepAlive(0<=e,e||0)));b.on("socket",function(){c.hasSocket=
!0;d.progress(c)});b.on("response",function(a){c.clientResponse=a;c.status=a.statusCode;c.getHeader=function(b){return a.headers[b.toLowerCase()]||null};var b=[];a.on("data",function(a){b.push(a)});a.on("end",function(){g&&clearTimeout(g);c.text=b.join("");f.checkStatus(c.status)||d.reject({message:"http response code "+c.status,response:c});try{h(c),d.resolve(c)}catch(t){d.reject(t)}})});b.on("error",d.reject);a.data?"string"===typeof a.data?b.end(a.data):a.data.pipe(b):b.end();if(a.timeout)var g=
setTimeout(function(){d.cancel(new k(c))},a.timeout);return d.promise}var r=p.Stream,q={method:"GET",query:null,data:void 0,headers:{}};f.addCommonMethods(g);return g});