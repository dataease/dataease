// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/geometry/support/zmUtils",["require","exports"],function(g,f){Object.defineProperty(f,"__esModule",{value:!0});f.updateSupportFromPoint=function(d,c,e){void 0===e&&(e=!1);var a=d.hasM,b=d.hasZ;Array.isArray(c)?4!==c.length||a||b?3===c.length&&e&&!a?(b=!0,a=!1):3===c.length&&a&&b&&(b=a=!1):b=a=!0:(b=!b&&c.hasZ&&(!a||c.hasM),a=!a&&c.hasM&&(!b||c.hasZ));d.hasZ=b;d.hasM=a}});