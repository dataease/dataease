/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import"./object.js";import"../core/lang.js";import"../config.js";import"./Logger.js";import"./string.js";import{resolve as r}from"../core/promiseUtils.js";import"./Message.js";import"../core/Error.js";import{d as o}from"./LercCodec.js";class t{_decode(t){const e=o(t.buffer,t.options);return r({result:e,transferList:[e.pixelData.buffer]})}}function e(){return new t}export default e;
