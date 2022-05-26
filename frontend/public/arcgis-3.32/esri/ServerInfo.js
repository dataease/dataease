// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/ServerInfo",["dojo/_base/declare","dojo/_base/lang","dojo/has","./kernel","./lang"],function(a,b,c,d,e){a=a(null,{declaredClass:"esri.ServerInfo",constructor:function(a){b.mixin(this,a)},toJson:function(){return e.fixJson({server:this.server,tokenServiceUrl:this.tokenServiceUrl,adminTokenServiceUrl:this.adminTokenServiceUrl,shortLivedTokenValidity:this.shortLivedTokenValidity,owningSystemUrl:this.owningSystemUrl,owningTenant:this.owningTenant,currentVersion:this.currentVersion,hasPortal:this.hasPortal,
hasServer:this.hasServer,webTierAuth:this.webTierAuth})}});c("extend-esri")&&(d.ServerInfo=a);return a});