/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
class t{constructor(t,s={ignoreUnknown:!1}){this.jsonToAPI=t,this.options=s,this.apiValues=[],this.jsonValues=[],this.apiToJSON=this.invertMap(t),this.apiValues=this.getKeysSorted(this.apiToJSON),this.jsonValues=this.getKeysSorted(this.jsonToAPI),this.read=t=>this.fromJSON(t),this.write=(t,s,o)=>{const i=this.toJSON(t);void 0!==i&&(s[o]=i)},this.write.isJSONMapWriter=!0}toJSON(t){return this.apiToJSON.hasOwnProperty(t)?this.apiToJSON[t]:this.options.ignoreUnknown?void 0:t}fromJSON(t){return this.jsonToAPI.hasOwnProperty(t)?this.jsonToAPI[t]:this.options.ignoreUnknown?void 0:t}invertMap(t){const s={};for(const o in t)s[t[o]]=o;return s}getKeysSorted(t){const s=[];for(const o in t)s.push(o);return s.sort(),s}}function s(){return function(s){return new t(s,{ignoreUnknown:!0})}}export{t as J,s};
