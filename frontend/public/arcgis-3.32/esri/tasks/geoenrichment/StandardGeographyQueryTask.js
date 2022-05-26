// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/tasks/geoenrichment/StandardGeographyQueryTask","../../declare ../../urlUtils dojo/_base/array dojo/_base/lang ./taskHelper ../FeatureSet ./GeographyQueryBase ./GeographyQuery ./BatchGeographyQuery ./SubGeographyQuery".split(" "),function(d,e,m,f,b,g,h,k,c,l){return d("esri.tasks.geoenrichment.StandardGeographyQueryTask",null,{constructor:function(a){this.url=a||e.getProtocolForWebResource()+"//geoenrich.arcgis.com/arcgis/rest/services/World/GeoenrichmentServer"},execute:function(a){a instanceof
h||(a=a.returnSubGeographyLayer?new l(a):a.geographyQueries||f.isArray(a.where)?new c(a):new k(a));return b.invokeMethod(this,a instanceof c?"/StandardGeographiesBatchQuery/execute":"/StandardGeographyQuery/execute",function(){return b.jsonToRest(a.toJson())},function(a){(!a.results||1>a.results.length||!a.results[0].value)&&b.throwEmptyResponse();return{featureSet:new g(a.results[0].value),messages:a.messages}},"onExecuteComplete","onError")},onExecuteComplete:function(a){},onError:function(a){}})});