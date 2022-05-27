//>>built
define(["dojo","dijit","dojox"],function(n,w,u){n.provide("dojox.lang.aspect");(function(){var m=u.lang.aspect,q=Array.prototype,p=[],g,r=function(){this.next_before=this.prev_before=this.next_around=this.prev_around=this.next_afterReturning=this.prev_afterReturning=this.next_afterThrowing=this.prev_afterThrowing=this;this.counter=0};n.extend(r,{add:function(b){var c=n.isFunction(b),a={advice:b,dynamic:c};this._add(a,"before","",c,b);this._add(a,"around","",c,b);this._add(a,"after","Returning",c,
b);this._add(a,"after","Throwing",c,b);++this.counter;return a},_add:function(b,c,a,d,e){var f=c+a;if(d||e[c]||a&&e[f])c="next_"+f,f="prev_"+f,(b[f]=this[f])[c]=b,(b[c]=this)[f]=b},remove:function(b){this._remove(b,"before");this._remove(b,"around");this._remove(b,"afterReturning");this._remove(b,"afterThrowing");--this.counter},_remove:function(b,c){var a="next_"+c;c="prev_"+c;b[a]&&(b[a][c]=b[c],b[c][a]=b[a])},isEmpty:function(){return!this.counter}});var v=function(){return function(){var b=arguments.callee,
c=b.advices,a,d,e;g&&p.push(g);g={instance:this,joinPoint:b,depth:p.length,around:c.prev_around,dynAdvices:[],dynIndex:0};try{for(a=c.prev_before;a!=c;a=a.prev_before)a.dynamic?(g.dynAdvices.push(d=new a.advice(g)),(e=d.before)&&e.apply(d,arguments)):(e=a.advice,e.before.apply(e,arguments));try{var f=(c.prev_around==c?b.target:m.proceed).apply(this,arguments)}catch(k){g.dynIndex=g.dynAdvices.length;for(a=c.next_afterThrowing;a!=c;a=a.next_afterThrowing)d=a.dynamic?g.dynAdvices[--g.dynIndex]:a.advice,
(e=d.afterThrowing)&&e.call(d,k),(e=d.after)&&e.call(d);throw k;}g.dynIndex=g.dynAdvices.length;for(a=c.next_afterReturning;a!=c;a=a.next_afterReturning)d=a.dynamic?g.dynAdvices[--g.dynIndex]:a.advice,(e=d.afterReturning)&&e.call(d,f),(e=d.after)&&e.call(d);var h=b._listeners;for(a in h)a in q||h[a].apply(this,arguments)}finally{for(a=0;a<g.dynAdvices.length;++a)d=g.dynAdvices[a],d.destroy&&d.destroy();g=p.length?p.pop():null}return f}};m.advise=function(b,c,a){"object"!=typeof b&&(b=b.prototype);
var d=[];c instanceof Array||(c=[c]);for(var e=0;e<c.length;++e){var f=c[e];if(f instanceof RegExp)for(var h in b)n.isFunction(b[h])&&f.test(h)&&d.push(h);else n.isFunction(b[f])&&d.push(f)}n.isArray(a)||(a=[a]);return m.adviseRaw(b,d,a)};m.adviseRaw=function(b,c,a){if(!c.length||!a.length)return null;for(var d={},e=a.length,f=c.length-1;0<=f;--f){var h=c[f],k=b[h],t=Array(e),l=k.advices;l||(l=b[h]=v(),l.target=k.target||k,l.targetName=h,l._listeners=k._listeners||[],l.advices=new r,l=l.advices);
for(k=0;k<e;++k)t[k]=l.add(a[k]);d[h]=t}return[b,d]};m.unadvise=function(b){if(b){var c=b[0];b=b[1];for(var a in b){for(var d=c[a],e=d.advices,f=b[a],h=f.length-1;0<=h;--h)e.remove(f[h]);if(e.isEmpty()){f=!0;e=d._listeners;if(e.length)for(h in e)if(!(h in q)){f=!1;break}f?c[a]=d.target:(h=c[a]=n._listener.getDispatcher(),h.target=d.target,h._listeners=e)}}}};m.getContext=function(){return g};m.getContextStack=function(){return p};m.proceed=function(){for(var b=g.joinPoint,c=b.advices,a=g.around;a!=
c;a=g.around)if(g.around=a.prev_around,a.dynamic){a=g.dynAdvices[g.dynIndex++];var d=a.around;if(d)return d.apply(a,arguments)}else return a.advice.around.apply(a.advice,arguments);return b.target.apply(g.instance,arguments)}})()});