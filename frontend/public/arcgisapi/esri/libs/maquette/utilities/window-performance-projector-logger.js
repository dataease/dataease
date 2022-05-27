// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(b){if(window.performance&&window.performance.measure){const a=window.performance;let d;b.windowPerformanceProjectorLogger=c=>{a.mark(c);switch(c){case "domEventProcessed":a.measure("eventHandler","domEvent","domEventProcessed");break;case "renderDone":a.measure("renderCycle","renderStart","renderDone");break;case "rendered":a.measure("render",d,"rendered");break;case "patched":a.measure("diff+patch","rendered","patched")}d=c}}else b.windowPerformanceProjectorLogger=()=>
{};Object.defineProperty(b,"__esModule",{value:!0})});