// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/kernel",["dojo/_base/kernel","dojo/_base/config","dojo/has"],function(e,f,g){var d=function(){return this}(),a=d.location,b=a.pathname,c=a.protocol,a={version:"3.32",_appBaseUrl:c+"//"+a.host+b.substring(0,b.lastIndexOf(b.split("/")[b.split("/").length-1]))};f.noGlobals||(d.esri=a);e.isAsync||g.add("extend-esri",1);(a.dijit=a.dijit||{})._arcgisUrl=("http:"===c||"https:"===c?c:"http:")+"//www.arcgis.com/sharing/rest";return a});