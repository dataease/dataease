// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../multiOriginJSONSupportUtils"],function(c,e){c.updateOrigins=function(b){b&&b.writtenProperties&&b.writtenProperties.forEach(a=>{const d=a.target;a.newOrigin&&a.oldOrigin!==a.newOrigin&&e.isMultiOriginJSONMixin(d)&&d.updateOrigin(a.propName,a.newOrigin)})};Object.defineProperty(c,"__esModule",{value:!0})});