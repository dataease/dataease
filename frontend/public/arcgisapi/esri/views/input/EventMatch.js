// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(c){let e=function(){function d(a,b=[]){this.eventType=a;this.keyModifiers=b}d.prototype.matches=function(a){if(a.type!==this.eventType)return!1;if(0===this.keyModifiers.length)return!0;a=a.modifiers;for(const b of this.keyModifiers)if(!a.has(b))return!1;return!0};return d}();c.EventMatch=e;Object.defineProperty(c,"__esModule",{value:!0})});