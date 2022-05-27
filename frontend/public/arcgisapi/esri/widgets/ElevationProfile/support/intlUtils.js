// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../core/maybe"],function(b,e){b.getTranslatedLineTitle=function(c,{profiles:a}){const d=c.title;if(e.isSome(d))return d;switch(c.type){case "ground":return a.ground;case "input":return a.input;case "query":return a.query;case "view":return a.view;default:return""}};Object.defineProperty(b,"__esModule",{value:!0})});