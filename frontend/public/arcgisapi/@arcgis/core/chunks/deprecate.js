/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{h as n}from"./object.js";const e=new Set;function o(e,o,t={}){n("esri-deprecation-warnings")&&r(e,`Module: ${o}`,t)}function t(e,o,t={}){if(n("esri-deprecation-warnings")){const{moduleName:n}=t;r(e,`Function: ${(n?n+"::":"")+o+"()"}`,t)}}function i(e,o,t={}){if(n("esri-deprecation-warnings")){const{moduleName:n}=t;r(e,`Property: ${(n?n+"::":"")+o}`,t)}}function r(o,t,i={}){if(n("esri-deprecation-warnings")){const{replacement:n,version:r,see:a,warnOnce:s}=i;let c=t;n&&(c+=`\n\t🛠️ Replacement: ${n}`),r&&(c+=`\n\t⚙️ Version: ${r}`),a&&(c+=`\n\t🔗 See ${a} for more details.`),function(n,o,t=!1){t&&e.has(o)||(t&&e.add(o),n.warn(`🛑 DEPRECATED - ${o}`))}(o,c,s)}}export{i as a,r as b,o as c,t as d};
