//>>built
define(["./Evented"],function(b){var a=new b;return{publish:function(c,d){return a.emit.apply(a,arguments)},subscribe:function(c,d){return a.on.apply(a,arguments)}}});