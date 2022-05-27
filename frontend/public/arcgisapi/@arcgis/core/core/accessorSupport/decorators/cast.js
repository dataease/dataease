/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import"../../../chunks/object.js";import"../../lang.js";import"../../../config.js";import"../../../chunks/Logger.js";import"../../../chunks/string.js";import{a as t}from"../../../chunks/metadata.js";import{e as n}from"../../../chunks/ensureType.js";const o=Object.prototype.toString;function r(t){const o="__accessorMetadata__"in t?n(t):t;return function(...t){if(t.push(o),"number"==typeof t[2])throw new Error("Using @cast has parameter decorator is not supported since 4.16");return s.apply(this,t)}}function s(n,o,r,s){t(n,o).cast=s}function e(...n){var s;if(3!==n.length||"string"!=typeof n[1])return 1===n.length&&"[object Function]"===o.call(n[0])?r(n[0]):1===n.length&&"string"==typeof n[0]?(s=n[0],function(n,o){t(n,s).cast=n[o]}):void 0}export{e as cast};
