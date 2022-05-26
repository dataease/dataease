// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/config",["esri/urlUtils"],function(a){a=a.getProtocolForWebResource();return{portalUrl:a+"//arcgis.com",server:a+"//geoenrich.arcgis.com/arcgis/rest/services/World/GeoenrichmentServer",levels:["Admin3","Admin2"],highestLevel:"Admin1",locatorUrl:a+"//geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer",addressFormat:"${Address}, ${City}, ${Region} ${Postal}"}});