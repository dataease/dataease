// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/metadata/types/gemini/base/PortalItemTransformer",["dojo/_base/declare","dojo/_base/lang","dojo/has","../../inspire/base/PortalItemTransformer","../../../../../kernel"],function(a,c,b,d,e){a=a([d],{postCreate:function(){this.inherited(arguments)},populateTransformationInfo:function(a,c,b){this.inherited(arguments);b.url.path=a.rootElement.gxePath+"/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"}});
b("extend-esri")&&c.setObject("dijit.metadata.types.gemini.base.PortalItemTransformer",a,e);return a});