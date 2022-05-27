// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(e){const g={divisor:0},h={u8:5121,u16:5123,u32:5125,i8:5120,i16:5122,i32:5124,f32:5126};e.glLayout=function(c,d={}){d={...g,...d};const k=c.stride;return c.fieldNames.filter(a=>{a=c.fields.get(a).optional;return!(a&&a.glPadding)}).map(a=>{const b=c.fields.get(a),l=b.constructor.ElementCount;var f=h[b.constructor.ElementType];if(!f)throw Error("BufferType not supported in WebGL");return{name:a,stride:k,count:l,type:f,offset:b.offset,normalized:!(!b.optional||!b.optional.glNormalized),
divisor:d.divisor}})};Object.defineProperty(e,"__esModule",{value:!0})});