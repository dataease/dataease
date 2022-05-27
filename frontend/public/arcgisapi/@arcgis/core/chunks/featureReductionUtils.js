/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{s as e}from"./object.js";import t from"../core/Error.js";import r,{F as s}from"../layers/support/FeatureReductionSelection.js";import o from"../layers/support/FeatureReductionCluster.js";const n={key:"type",base:s,typeMap:{selection:r}};function u(e,t){const s=(t=t.layerDefinition||t).featureReduction;if(s)switch(s.type){case"selection":return r.fromJSON(s);case"cluster":return o.fromJSON(s)}return null}function i(r,s,o,n){const u=function(e,r,s){if(!e)return null;if("selection"!==e.type)return s.messages&&s.messages.push(new t("featureReduction:unsupported",`FeatureReduction of type '${e.declaredClass}' are not supported in scenes.`,{featureReduction:e,context:s})),null;return e.write(r,s)}(r,{},n);u&&e(o,u,s)}export{n as a,u as r,i as w};
