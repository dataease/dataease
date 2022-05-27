// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../core/urlUtils","../Portal"],function(b,c,d){b.createForItem=function(a){return{origin:"portal-item",url:c.urlToObject(a.itemUrl),portal:a.portal||d.getDefault(),portalItem:a,readResourcePaths:[]}};Object.defineProperty(b,"__esModule",{value:!0})});