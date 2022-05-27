/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{g as e}from"./object.js";const t=/\{([^\}]+)\}/g;function n(e){return null==e?"":e}function r(r,o){return r.replace(t,"object"==typeof o?(t,r)=>n(e(r,o)):(e,t)=>n(o(t)))}function o(e,t){return e.replace(/([\.$?*|{}\(\)\[\]\\\/\+\-^])/g,(e=>t&&-1!==t.indexOf(e)?e:`\\${e}`))}function c(e){let t=0;for(let n=0;n<e.length;n++)t=(t<<5)-t+e.charCodeAt(n),t|=0;return t}export{o as e,c as n,r};
