/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import"./object.js";import{b as s}from"./Logger.js";import o from"../symbols/SimpleLineSymbol.js";import r from"../symbols/SimpleFillSymbol.js";import m from"../symbols/SimpleMarkerSymbol.js";import e from"../symbols/TextSymbol.js";import{d as t,a,b as l,c as i,e as n,f,g as p}from"./defaultsJSON.js";const S=m.fromJSON(t),c=o.fromJSON(a),b=r.fromJSON(l),u=e.fromJSON(i);function y(o){if(s(o))return null;switch(o.type){case"mesh":return null;case"point":case"multipoint":return S;case"polyline":return c;case"polygon":case"extent":return b}return null}const j=m.fromJSON(n),J=o.fromJSON(f),N=r.fromJSON(p);export{N as a,j as b,S as c,u as d,J as e,c as f,y as g,b as h};
