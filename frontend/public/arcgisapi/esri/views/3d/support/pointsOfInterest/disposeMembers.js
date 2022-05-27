// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../core/Accessor"],function(d,e){function f(a,...b){if(a instanceof e&&a.destroyed)try{throw Error("instance is already destroyed");}catch(c){console.warn(c.stack)}else for(const c of b){if(!(c in a))throw Error(`Property '${c}' does not exist and cannot be disposed`);(b=a[c])&&("function"===typeof b.destroy?b.destroy():"function"===typeof b.dispose?b.dispose():"function"===typeof b.remove&&b.remove());a instanceof e&&c in a.__accessor__.metadatas?a._set(c,null):a[c]=null}}
d.default=f;d.disposeMembers=f;Object.defineProperty(d,"__esModule",{value:!0})});