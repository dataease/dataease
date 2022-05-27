// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../has","../Logger","./get"],function(e,k,g,h){function b(c,a,f){if(c&&a)if("object"===typeof a)for(var d of Object.getOwnPropertyNames(a))b(c,d,a[d]);else-1!==a.indexOf(".")?(a=a.split("."),d=a.splice(a.length-1,1)[0],b(h.get(c,a),d,f)):c[a]=f}g.getLogger("esri.core.accessorSupport.set");e.default=b;e.set=b;Object.defineProperty(e,"__esModule",{value:!0})});