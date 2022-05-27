// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(a,b){a.IsNaN=function(c){const d=b.glsl`
    bool isNaN( float val )
    {
      return ( val < 0.0 || 0.0 < val || val == 0.0 ) ? false : true;
      // important: some nVidias failed to cope with version below.
      // Probably wrong optimization.
      /*return ( val <= 0.0 || 0.0 <= val ) ? false : true;*/
    }
  `;c.code.add(d)};Object.defineProperty(a,"__esModule",{value:!0})});