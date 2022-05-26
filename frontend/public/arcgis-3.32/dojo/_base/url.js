/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/_base/url",["./kernel"],function(g){var h=/^(([^:/?#]+):)?(\/\/([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?$/,k=/^((([^\[:]+):)?([^@]+)@)?(\[([^\]]+)\]|([^\[:]*))(:([0-9]+))?$/,f=function(){for(var c=arguments,a=[c[0]],e=1;e<c.length;e++)if(c[e]){var b=new f(c[e]+""),a=new f(a[0]+"");if(""==b.path&&!b.scheme&&!b.authority&&!b.query)null!=b.fragment&&(a.fragment=b.fragment),b=a;else if(!b.scheme&&(b.scheme=a.scheme,!b.authority&&(b.authority=a.authority,"/"!=b.path.charAt(0)))){for(var a=(a.path.substring(0,
a.path.lastIndexOf("/")+1)+b.path).split("/"),d=0;d<a.length;d++)"."==a[d]?d==a.length-1?a[d]="":(a.splice(d,1),d--):0<d&&(1!=d||""!=a[0])&&".."==a[d]&&".."!=a[d-1]&&(d==a.length-1?(a.splice(d,1),a[d-1]=""):(a.splice(d-1,2),d-=2));b.path=a.join("/")}a=[];b.scheme&&a.push(b.scheme,":");b.authority&&a.push("//",b.authority);a.push(b.path);b.query&&a.push("?",b.query);b.fragment&&a.push("#",b.fragment)}this.uri=a.join("");c=this.uri.match(h);this.scheme=c[2]||(c[1]?"":null);this.authority=c[4]||(c[3]?
"":null);this.path=c[5];this.query=c[7]||(c[6]?"":null);this.fragment=c[9]||(c[8]?"":null);null!=this.authority&&(c=this.authority.match(k),this.user=c[3]||null,this.password=c[4]||null,this.host=c[6]||c[7],this.port=c[9]||null)};f.prototype.toString=function(){return this.uri};return g._Url=f});