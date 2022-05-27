/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{v as e}from"./unitUtils.js";const t={dependsOn:["view.map.portalItem?.portal.units","view.map.portalItem?.portal.user.units","view.spatialReference"],readOnly:!0,get(){const t="metric",{view:r}=this;if(!r)return t;const i=r.get("map.portalItem.portal");if(i){switch(i.get("user.units")||i.units){case t:return t;case"english":return"imperial"}}return e(r.spatialReference)||t}};export{t as d};
