/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{I as r}from"./ensureType.js";import t from"../Color.js";import{t as e}from"./screenUtils.js";import{t as s,o as a}from"./opacityUtils.js";const o={type:t,json:{type:[r],default:null,read:{source:["color","transparency"],reader:function(r,e){const a=null!=e.transparency?s(e.transparency):1,o=e.color;return o&&Array.isArray(o)?new t([o[0]||0,o[1]||0,o[2]||0,a]):null}},write:{target:{color:{type:[r]},transparency:{type:r}},writer:function(r,t){t.color=r.toJSON().slice(0,3);const e=a(r.a);0!==e&&(t.transparency=e)}}}},n={type:Number,cast:e,json:{write:!0}},c={type:[Number],cast:r=>null!=r?r:Array.isArray(r)?r.map(e):null,json:{read:!1,write:!1}};export{c as a,o as c,n as s};
