//>>built
define(["dojo","dijit","dojox","dojo/require!dojox/lang/functional/lambda,dojox/lang/functional/util"],function(d,n,g){d.provide("dojox.lang.functional.numrec");d.require("dojox.lang.functional.lambda");d.require("dojox.lang.functional.util");(function(){var b=g.lang.functional,h=b.inlineLambda,k=["_r","_i"];b.numrec=function(l,a){var f={},c=function(m){f[m]=1};if("string"==typeof a)a=h(a,k,c);else{var e=b.lambda(a);a="_a.call(this, _r, _i)"}c=b.keys(f);a=new Function(["_x"],"var _t\x3darguments.callee,_r\x3d_t.t,_i".concat(c.length?
","+c.join(","):"",e?",_a\x3d_t.a":"",";for(_i\x3d1;_i\x3c\x3d_x;++_i){_r\x3d",a,"}return _r"));a.t=l;e&&(a.a=e);return a}})()});