// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(e){e.updateSupportFromPoint=function(d,c,f=!1){let {hasM:a,hasZ:b}=d;Array.isArray(c)?4!==c.length||a||b?3===c.length&&f&&!a?(b=!0,a=!1):3===c.length&&a&&b&&(b=a=!1):b=a=!0:(b=!b&&c.hasZ&&(!a||c.hasM),a=!a&&c.hasM&&(!b||c.hasZ));d.hasZ=b;d.hasM=a};Object.defineProperty(e,"__esModule",{value:!0})});