// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/tasks/datareviewer/ReviewerSession",["dojo/_base/declare","dojo/has","dojo/_base/lang","../../kernel"],function(a,b,c,e){a=a(null,{declaredClass:"esri.tasks.datareviewer.ReviewerSession",sessionId:NaN,sessionName:"",userName:"",versionName:"",constructor:function(a,b,c,d){this.sessionId=a;this.sessionName=b;this.userName=c;void 0!==d&&(this.versionName=d)},toString:function(){return isNaN(this.sessionId)?null:"Session "+this.sessionId+" : "+this.sessionName}});b("extend-esri")&&c.setObject("tasks.datareviewer.ReviewerSession",
a,e);return a});