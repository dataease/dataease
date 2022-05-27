/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{b as n}from"./Logger.js";function e(n){return n&&"function"==typeof n.highlight}function t(n){return n&&"function"==typeof n.maskOccludee}function o(e,t,o){return n(e)||e>o&&(0===t||e<t)}function a(n){let{minScale:e,maxScale:t}=n;return e=e||0,t=t||0,{minScale:e,maxScale:t}}export{a as e,e as h,t as o,o as s};
