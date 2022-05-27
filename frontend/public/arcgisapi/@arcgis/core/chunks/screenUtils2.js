/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import"./object.js";import{c as e}from"./screenUtils.js";function c(c){return e(c.x,c.y)}function t(c,t){const n=(c instanceof HTMLElement?c:c.surface).getBoundingClientRect();return e(t.clientX-n.left,t.clientY-n.top)}function n(e,n){return n instanceof Event?t(e,n):c(n)}function a(e){if(e instanceof Event)return!0;if("object"==typeof e&&"type"in e){switch(e.type){case"click":case"double-click":case"pointer-down":case"pointer-drag":case"pointer-enter":case"pointer-leave":case"pointer-up":case"pointer-move":case"immediate-click":case"immediate-double-click":case"hold":case"drag":case"mouse-wheel":return!0;case"key-down":case"key-up":case"gamepad":case"focus":case"blur":default:return!1}}return!1}export{n as a,t as b,c,a as i};
