// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../PortalItem","./portalItemUtils"],function(c,e,f){async function d(a,b,g,h){return!a.layerType||"ArcGISFeatureLayer"!==a.layerType||a.url?!1:a.featureCollectionType&&a.featureCollectionType===g?!0:a.itemId?(a=new e({id:a.itemId,portal:b}),await a.load(),"Feature Collection"===a.type&&f.hasTypeKeyword(a,h)):!1}c.isMapNotesLayer=function(a,b){return d(a,b,"notes","Map Notes")};c.isRouteLayer=function(a,b){return d(a,b,"route","Route Layer")};Object.defineProperty(c,"__esModule",
{value:!0})});