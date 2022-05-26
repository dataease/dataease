// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/editing/EditOperationBase",["dojo/_base/array","dojo/_base/declare","dojo/has","../../kernel","../../OperationBase"],function(d,a,b,c,e){a=a(e,{declaredClass:"esri.EditOperationBase",updateIds:function(a,b,c,e){d.forEach(b,function(b,g){var f=b[a.objectIdField];d.forEach(c,function(c,d){f===c&&(b[a.objectIdField]=e[d])})})}});b("extend-esri")&&(c.EditOperationBase=a);return a});