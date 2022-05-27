// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../enums","../visualVariablesUtils"],function(e,d,f){e.getVVFlags=function(c){if(!c)return d.WGLVVFlag.NONE;let a=0;for(const b of c)"size"===b.type?(c=f.getTypeOfSizeVisualVariable(b),a|=c,"outline"===b.target&&(a|=c<<4)):"color"===b.type?a|=d.WGLVVFlag.COLOR:"opacity"===b.type?a|=d.WGLVVFlag.OPACITY:"rotation"===b.type&&(a|=d.WGLVVFlag.ROTATION);return a};Object.defineProperty(e,"__esModule",{value:!0})});