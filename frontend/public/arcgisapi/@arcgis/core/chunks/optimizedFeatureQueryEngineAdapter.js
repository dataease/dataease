/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{a as t,b as e}from"./OptimizedGeometry.js";import{g as o}from"./centroid.js";const r={getObjectId:t=>t.objectId,getAttributes:t=>t.attributes,getAttribute:(t,e)=>t.attributes[e],cloneWithGeometry:(e,o)=>new t(o,e.attributes,null,e.objectId),getGeometry:t=>t.geometry,getCentroid:(t,r)=>(t.centroid||(t.centroid=o(new e,t.geometry,r.hasZ,r.hasM)),t.centroid)};export{r as o};
