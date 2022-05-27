/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import"./object.js";import e from"../config.js";import{n as t}from"./string.js";const r=null;function n(e){return null!=e}function o(e){return null==e}function i(e){return void 0===e}function s(e,t){return n(e)?t(e):null}function l(e){return e}function u(e,t){return n(e)?e:"function"==typeof t?t():t}function c(e){return n(e)&&e.destroy(),null}function a(e){return n(e)&&e.dispose(),null}function h(e){return n(e)&&e.remove(),null}function g(e){return null}function f(e,t){const r=new Array;return e.forEach((e=>{const o=t(e);n(o)&&r.push(o)})),r}function _(e,t){const r=new Array;for(const n of e)r.push(m(n,null,t));return r}function d(e,t){for(const r of e)m(r,null,t)}function m(e,t,r){return n(e)?r(e):t}function w(e,t){return n(e)?t(e):null}function p(e,...t){let r=e,n=0;for(;n<t.length&&r;)r=r[t[n++]];return r}function k(e){return e}const y={info:0,warn:1,error:2,none:3};class v{constructor(e){this.level=null,this._module="",this._parent=null,this.writer=null,this._loggedMessages={error:new Map,warn:new Map,info:new Map},null!=e.level&&(this.level=e.level),null!=e.writer&&(this.writer=e.writer),this._module=e.module,v._loggers[this.module]=this;const t=this.module.lastIndexOf(".");-1!==t&&(this._parent=v.getLogger(this.module.slice(0,t)))}get module(){return this._module}get parent(){return this._parent}error(...e){this._log("error","always",...e)}warn(...e){this._log("warn","always",...e)}info(...e){this._log("info","always",...e)}errorOnce(...e){this._log("error","once",...e)}warnOnce(...e){this._log("warn","once",...e)}infoOnce(...e){this._log("info","once",...e)}errorOncePerTick(...e){this._log("error","oncePerTick",...e)}warnOncePerTick(...e){this._log("warn","oncePerTick",...e)}infoOncePerTick(...e){this._log("info","oncePerTick",...e)}static get test(){return{resetLoggers(e={}){const t=v._loggers;return v._loggers=e,t},set throttlingDisabled(e){v._throttlingDisabled=e}}}static getLogger(e){let t=v._loggers[e];return t||(t=new v({module:e})),t}_log(t,r,...n){if(!this._matchLevel(t))return;if("always"!==r&&!v._throttlingDisabled){const e=this._argsToKey(n),o=this._loggedMessages[t].get(e);if("once"===r&&null!=o||"oncePerTick"===r&&o&&o>=v._tickCounter)return;this._loggedMessages[t].set(e,v._tickCounter),v._scheduleTickCounterIncrement()}for(const r of e.log.interceptors)if(r(t,this.module,...n))return;this._inheritedWriter()(t,this.module,...n)}_parentWithMember(e,t){let r=this;for(;n(r);){const t=r[e];if(n(t))return t;r=r.parent}return t}_inheritedWriter(){return this._parentWithMember("writer",this._consoleWriter)}_consoleWriter(e,t,...r){console[e](`[${t}]`,...r)}_matchLevel(t){const r=e.log.level?e.log.level:"warn";return y[this._parentWithMember("level",r)]<=y[t]}_argsToKey(...e){return t(JSON.stringify(e,((e,t)=>"object"!=typeof t||Array.isArray(t)?t:"[Object]")))}static _scheduleTickCounterIncrement(){v._tickCounterScheduled||(v._tickCounterScheduled=!0,Promise.resolve().then((()=>{v._tickCounter++,v._tickCounterScheduled=!1})))}}v._loggers={},v._tickCounter=0,v._tickCounterScheduled=!1,v._throttlingDisabled=!1;export{v as L,k as a,o as b,u as c,s as d,c as e,r as f,p as g,a as h,n as i,i as j,w as k,_ as l,m,g as n,d as o,f as p,h as r,l as u};
