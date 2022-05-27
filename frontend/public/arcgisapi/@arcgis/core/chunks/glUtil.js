/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
const t={divisor:0};function o(o,n={}){n={...t,...n};const r=o.stride;return o.fieldNames.filter((t=>{const e=o.fields.get(t).optional;return!(e&&e.glPadding)})).map((t=>{const i=o.fields.get(t),s=i.constructor.ElementCount,u=function(t){const o=e[t];if(o)return o;throw new Error("BufferType not supported in WebGL")}(i.constructor.ElementType),f=i.offset,c=!(!i.optional||!i.optional.glNormalized);return{name:t,stride:r,count:s,type:u,offset:f,normalized:c,divisor:n.divisor}}))}const e={u8:5121,u16:5123,u32:5125,i8:5120,i16:5122,i32:5124,f32:5126};export{o as g};
