//>>built
define(["dojo/_base/lang","dojo/_base/declare"],function(p,t){function u(c,g){var h={},b=c.length,a;for(a=0;a<b;a++)h[c[a]]=g[a];return h}var v=/^\s*"([\S\s]*)"\s*$/,w=/""/g,x=/"/g;return t(null,{fieldNames:null,delimiter:",",newline:"\r\n",trim:!1,parse:function(c){var g=[];c=c.split(this.newline);var h=this.fieldNames,b=0,a=[],f="",k="",l;var e=0;var d=c.length;a:for(;e<d;e++)if(p.trim(c[e])){var m=c[e].split(this.delimiter);var n=0;for(l=m.length;n<l;n++){var q=m[n];var r=-1;f+=k+q;for(k="";0<=
(r=q.indexOf('"',r+1));)b++;if(0===b%2){if(0<b)if(b=v.exec(f))a.push(b[1].replace(w,'"'));else{console.warn("Csv: discarding row with invalid value: "+f);a=[];f="";b=0;continue a}else a.push(this.trim||!h?p.trim(f):f);f="";b=0}else k=this.delimiter}0===b?(h?g.push(u(h,a)):h=this.fieldNames=a,a=[]):k=this.newline}return g},toCsv:function(c){return this.stringify(this.data,c)},stringify:function(c,g){g=g||{};var h=g.alwaysQuote,b=this.fieldNames,a=this.delimiter,f=this.newline,k="",l,e;for(l=-1;l<c.length;l++)for(-1<
l&&(k+=f),e=0;e<b.length;e++){var d=0>l?b[e]:c[l][b[e]];if(null===d||void 0===d)d="";"string"!==typeof d&&(d=d.toString());var m=h||0<=d.indexOf('"')||0<=d.indexOf(a);k+=(0<e?a:"")+(m?'"'+d.replace(x,'""')+'"':d)}g.trailingNewline&&(k+=f);return k}})});