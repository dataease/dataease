// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../core/has"],function(a,c){function b(d){return!!c(`enable-feature:${d}`)}a.disableContextNavigation=()=>b("disable-context-navigation");a.enableWebStyleForceWOSR=()=>b("force-wosr");a.hasEnableFeature=b;Object.defineProperty(a,"__esModule",{value:!0})});