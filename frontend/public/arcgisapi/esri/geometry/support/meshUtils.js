// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["require","exports","./meshUtils/merge","./meshUtils/georeference","../Mesh"],function(f,d,h,g,k){d.createElevationSampler=async function(a,b){return(await new Promise(function(c,e){f(["./meshUtils/elevationSampler"],c,e)})).create(a,b)};d.createFromElevation=async function(a,b,c){return(await new Promise(function(e,l){f(["./meshUtils/elevation"],e,l)})).create(a,b,c)};d.georeference=function(a,b,c){return g.georeference(a,b,c)};d.merge=function(a){return new k(h.merge(a))};d.ungeoreference=
function(a,b,c){return g.ungeoreference(a,b,c)};Object.defineProperty(d,"__esModule",{value:!0})});