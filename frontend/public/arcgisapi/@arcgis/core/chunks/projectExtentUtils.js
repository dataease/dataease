/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{resolve as e}from"../core/promiseUtils.js";import{canProject as t,project as r}from"../geometry/support/webMercatorUtils.js";import{projectGeometry as o}from"./geometryServiceUtils.js";function l(l){const s=l.view.spatialReference,i=l.layer.fullExtent,a=i&&i.spatialReference;return a?a.equals(s)?e(i.clone()):t(a,s)?e(r(i,s)):l.view.state.isLocal?o(i,s,l.layer.portalItem).then((e=>!l.destroyed&&e?e:void 0)).catch((()=>null)):e(null):e(null)}export{l as t};
