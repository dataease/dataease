/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{i as e,b as r,c as t}from"./Logger.js";import n from"../geometry/Point.js";import{s as o}from"./mathUtils2.js";import{c as s}from"./vec3f64.js";import{g as a,l as c,c as l,d as i,s as u,a as d,e as m}from"./vec3.js";import"../geometry.js";import{s as p,h as f,b as S}from"./screenUtils.js";import{projectPoint as g}from"../geometry/projection.js";import{r as y,O as E,p as j,A as x}from"./geometryUtils.js";import{T as w}from"./intersectorUtils.js";import{I}from"./Intersector.js";import{d as b,g as v}from"./elevationInfoUtils.js";import{E as R}from"./InteractiveToolBase.js";function C(t,n){const o=s(),a=s();let c=!1;return s=>{if("start"===s.action){const r=p(s.screenStart,f(E.get())),a=y.fromScreen(t.state.camera,r,k);e(a)&&(c=j.intersectRay(n,a,o))}if(!c)return null;const l=p(s.screenEnd,f(E.get())),i=y.fromScreen(t.state.camera,l,k);return r(i)?null:j.intersectRay(n,i,a)?{...s,renderStart:o,renderEnd:a,plane:n,ray:i}:null}}function H(t,n,o,s=null){return function(t,n,o,s=null){let a=null;return c=>{if("start"===c.action&&(a=t.sceneIntersectionHelper.intersectElevationFromScreen(S(c.screenStart.x,c.screenStart.y),n,o),e(a)&&e(s)&&!g(a,a,s)))return null;if(r(a))return null;const l=t.sceneIntersectionHelper.intersectElevationFromScreen(S(c.screenEnd.x,c.screenEnd.y),n,o);return e(l)?e(s)&&!g(l,l,s)?null:{...c,mapStart:a,mapEnd:l}:null}}(t,o,v(n,t,o),s)}function P(e,r,t,n=null){return H(e,t,b(r),n)}function U(r,t,n,o){const s=t.toMap(r.screenStart,{include:[n]});return e(s)?P(t,n,s,o):null}function T(r,t,p){let f=null;const S=new R;return S.next(C(r,function(e,r){const t=F,n=M,o=j.create();e.renderCoordsHelper.worldUpAtPosition(r,t);const s=m(o,t,l(n,r,e.state.camera.eye));return m(s,s,t),j.fromPositionAndNormal(r,s,o)}(r,t))).next(function(e,r){const t=s(),n=c(r);e.renderCoordsHelper.worldUpAtPosition(r,t);const m=s(),p=s(),f=s=>{if(l(s,s,r),x.projectPoint(t,s,s),"global"===e.viewingMode){c(s)*o(i(t,s))<.001-n&&l(s,u(s,t,.001),r)}return d(s,s,r),s};return e=>(e.renderStart=f(a(m,e.renderStart)),e.renderEnd=f(a(p,e.renderEnd)),e)}(r,t)).next(function(r,t){const o=r.renderCoordsHelper;return r=>{const s=o.fromRenderCoords(r.renderStart,new n,t),a=o.fromRenderCoords(r.renderEnd,new n,t);return e(s)&&e(a)?{...r,mapStart:s,mapEnd:a}:null}}(r,p)).next((e=>{e.mapEnd.x=e.mapStart.x,e.mapEnd.y=e.mapStart.y,f=e})),e=>(f=null,S.execute(e),f)}function h(r){let t=null;return n=>{switch(n.action){case"start":t=r.disableDisplay();break;case"end":case"cancel":e(t)&&(t.remove(),t=null)}return n}}function A(o,a=null){let c=null;const l=new I(o.state.mode);l.options.selectionMode=!0,l.options.store=0;const i=S(),u={requiresGroundFeedback:!0,enableDraped:!0,exclude:new Set},d=s(),m=t(a,o.spatialReference),f=e=>{o.map.ground&&o.map.ground.opacity<1?u.exclude.add(w):u.exclude.delete(w),o.sceneIntersectionHelper.intersectIntersectorScreen(p(e,i),l,u);const r=l.results.min;let t;if(r.getIntersectionPoint(d))t="TerrainRenderer"===r.intersector?0:1;else{if(!l.results.ground.getIntersectionPoint(d))return null;t=0}const s=new n({spatialReference:m});return o.renderCoordsHelper.fromRenderCoords(d,s),{location:s,surfaceType:t}};return t=>{if("start"===t.action){const r=f(t.screenStart);c=e(r)?r.location:null}if(r(c))return null;const n=f(t.screenEnd);return e(n)?{...t,mapStart:c,mapEnd:n.location,surfaceType:n.surfaceType}:null}}const F=s(),M=s(),k=y.create();export{C as a,H as b,T as c,U as d,P as e,h,A as s};
