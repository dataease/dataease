// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../core/shaderModules/interfaces"],function(b,c){b.DiscardByCoverage=function(a,d){a=a.fragment;a.defines.addFloat("COVERAGE_TEST_THRESHOLD",.01);d.antialiasing?a.code.add(c.glsl`
      #define discardByCoverage(radius, coverage) { if (coverage < COVERAGE_TEST_THRESHOLD) discard; }
    `):a.code.add(c.glsl`
      #define discardByCoverage(radius, coverage) { float coverageLimit = radius <= 0.5 ? COVERAGE_TEST_THRESHOLD : 0.75; if (coverage < coverageLimit) discard; }
    `)};Object.defineProperty(b,"__esModule",{value:!0})});