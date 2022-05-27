// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../../chunks/_rollupPluginBabelHelpers","../../../../../core/Logger"],function(f,k,g){const r=g.getLogger("esri.views.3d.webgl-engine.core.shaderModules.shaderBuilder");g=function(){function d(){this.includedModules=new Map}d.prototype.include=function(b,a){this.includedModules.has(b)?this.includedModules.get(b)!==a&&r.error("Trying to include shader module multiple times with different sets of options."):(this.includedModules.set(b,a),b(this.builder,a))};return d}();let x=
function(d){function b(){var a=d.apply(this,arguments)||this;a.vertex=new m;a.fragment=new m;a.attributes=new t;a.varyings=new u;a.extensions=new n;a.defines=new p;return a}k._inheritsLoose(b,d);b.prototype.generateSource=function(a){const c=this.extensions.generateSource(a),e=this.attributes.generateSource(a),l=this.varyings.generateSource();var h="vertex"===a?this.vertex:this.fragment;const v=h.uniforms.generateSource(),w=h.code.generateSource();a="vertex"===a?"precision highp float;\nprecision highp sampler2D;":
"#ifdef GL_FRAGMENT_PRECISION_HIGH\n  precision highp float;\n  precision highp sampler2D;\n#else\n  precision mediump float;\n  precision mediump sampler2D;\n#endif";h=this.defines.generateSource().concat(h.defines.generateSource());return`
${c.join("\n")}

${h.join("\n")}

${a}

${v.join("\n")}

${e.join("\n")}

${l.join("\n")}

${w.join("\n")}`};k._createClass(b,[{key:"builder",get:function(){return this}}]);return b}(g),y=function(){function d(){this._entries=[];this._set=new Set}var b=d.prototype;b.add=function(a,c,e){const l=`${a}_${c}_${e}`;this._set.has(l)||(this._entries.push([a,c,e]),this._set.add(l));return this};b.generateSource=function(){return this._entries.map(a=>{var c=a[2];return`uniform ${a[1]} ${a[0]}${c?`[${c}]`:""};`})};return d}(),q=function(){function d(){this._entries=[]}var b=d.prototype;b.add=function(a){this._entries.push(a)};
b.generateSource=function(){return this._entries};return d}(),m=function(d){function b(){var a=d.apply(this,arguments)||this;a.uniforms=new y;a.code=new q;a.defines=new p;return a}k._inheritsLoose(b,d);k._createClass(b,[{key:"builder",get:function(){return this}}]);return b}(g),t=function(){function d(){this._entries=[]}var b=d.prototype;b.add=function(a,c){this._entries.push([a,c])};b.generateSource=function(a){return"fragment"===a?[]:this._entries.map(c=>`attribute ${c[1]} ${c[0]};`)};return d}(),
u=function(){function d(){this._entries=[]}var b=d.prototype;b.add=function(a,c){this._entries.push([a,c])};b.generateSource=function(){return this._entries.map(a=>`varying ${a[1]} ${a[0]};`)};return d}(),n=function(){function d(){this._entries=new Set}var b=d.prototype;b.add=function(a){this._entries.add(a)};b.generateSource=function(a){const c="vertex"===a?d.ALLOWLIST_VERTEX:d.ALLOWLIST_FRAGMENT;return Array.from(this._entries).filter(e=>c.includes(e)).map(e=>`#extension ${e} : enable`)};return d}();
n.ALLOWLIST_FRAGMENT=["GL_EXT_shader_texture_lod","GL_OES_standard_derivatives"];n.ALLOWLIST_VERTEX=[];let p=function(){function d(){this._entries=new Map}var b=d.prototype;b.addInt=function(a,c){c=0===c%1?c.toFixed(0):c.toString();this._entries.set(a,c)};b.addFloat=function(a,c){c=0===c%1?c.toFixed(1):c.toString();this._entries.set(a,c)};b.generateSource=function(){return Array.from(this._entries,([a,c])=>`#define ${a} ${c}`)};return d}();f.Code=q;f.Includes=g;f.ShaderBuilder=x;f.Stage=m;Object.defineProperty(f,
"__esModule",{value:!0})});