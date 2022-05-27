// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../../../core/compilerUtils ../../../../core/urlUtils ../../../../core/promiseUtils ../../../../core/Version ../../../../chunks/mat4 ../../../../chunks/mat4f64 ../../../../chunks/quatf64 ../../support/buffer/BufferView ../../../../chunks/quat ./BinaryStreamReader ./fillDefaults ./pathUtils ../../../../chunks/scalar".split(" "),function(w,D,p,x,y,m,t,E,k,F,z,u,G,v){function H(e){switch(e.componentType){case 5120:return new k.BufferViewVec2i8(e.raw,e.byteOffset,e.byteStride,e.byteOffset+
e.byteStride*e.entryCount);case 5121:return new k.BufferViewVec2u8(e.raw,e.byteOffset,e.byteStride,e.byteOffset+e.byteStride*e.entryCount);case 5122:return new k.BufferViewVec2i16(e.raw,e.byteOffset,e.byteStride,e.byteOffset+e.byteStride*e.entryCount);case 5123:return new k.BufferViewVec2u16(e.raw,e.byteOffset,e.byteStride,e.byteOffset+e.byteStride*e.entryCount);case 5125:return new k.BufferViewVec2u32(e.raw,e.byteOffset,e.byteStride,e.byteOffset+e.byteStride*e.entryCount);case 5126:return new k.BufferViewVec2f(e.raw,
e.byteOffset,e.byteStride,e.byteOffset+e.byteStride*e.entryCount);default:D.neverReached(e.componentType)}}async function I(e){return x.create((g,a)=>{const c=new Blob([e]),b=new FileReader;b.onload=()=>{g(JSON.parse(b.result))};b.onerror=d=>{a(d)};b.readAsText(c)})}async function J(e,g){return x.create((a,c)=>{const b=new Blob([e],{type:g}),d=URL.createObjectURL(b),f=new Image;f.addEventListener("load",()=>{URL.revokeObjectURL(d);"decode"in f?f.decode().then(()=>a(f),()=>a(f)):a(f)});f.addEventListener("error",
h=>{URL.revokeObjectURL(d);c(h)});f.src=d})}let O=function(){function e(a,c,b,d,f){this.context=a;this.errorContext=c;this.uri=b;this.json=d;this.glbBuffer=f;this.bufferCache=new Map;this.textureCache=new Map;this.materialCache=new Map;this.nodeParentMap=new Map;this.nodeTransformCache=new Map;this.baseUri=G.splitURI(this.uri).dirPart;this.checkVersionSupported();this.checkRequiredExtensionsSupported();c.errorUnsupportedIf(null==d.scenes,"Scenes must be defined.");c.errorUnsupportedIf(null==d.meshes,
"Meshes must be defined");c.errorUnsupportedIf(null==d.nodes,"Nodes must be defined.");this.computeNodeParents()}e.load=async function(a,c,b,d){if(p.isDataProtocol(b)){var f=p.dataComponents(b);if("model/gltf-binary"!==f.mediaType)try{const h=JSON.parse(f.isBase64?atob(f.data):f.data);return new e(a,c,b,h)}catch{}f=p.dataToArrayBuffer(b);if(e.isGLBData(f))return this.fromGLBData(a,c,b,f)}if(b.endsWith(".gltf"))return d=await a.loadJSON(b,d),new e(a,c,b,d);f=await a.loadBinary(b,d);if(e.isGLBData(f))return this.fromGLBData(a,
c,b,f);d=await a.loadJSON(b,d);return new e(a,c,b,d)};e.isGLBData=function(a){a=new z.BinaryStreamReader(a);return 4<=a.remainingBytes()&&1179937895===a.readUint32()};e.fromGLBData=async function(a,c,b,d){d=await e.parseGLBData(c,d);return new e(a,c,b,d.json,d.binaryData)};e.parseGLBData=async function(a,c){const b=new z.BinaryStreamReader(c);a.assert(12<=b.remainingBytes(),"GLB binary data is insufficiently large.");var d=b.readUint32(),f=b.readUint32();const h=b.readUint32();a.assert(1179937895===
d,"Magic first 4 bytes do not fit to expected GLB value.");a.assert(c.byteLength>=h,"GLB binary data is smaller than header specifies.");a.errorUnsupportedIf(2!==f,"An unsupported GLB container version was detected. Only version 2 is supported.");c=0;let l,n;for(;8<=b.remainingBytes();)d=b.readUint32(),f=b.readUint32(),0===c?(a.assert(1313821514===f,"First GLB chunk must be JSON."),a.assert(0<=d,"No JSON data found."),l=await I(b.readUint8Array(d))):1===c?(a.errorUnsupportedIf(5130562!==f,"Second GLB chunk expected to be BIN."),
n=b.readUint8Array(d)):a.warnUnsupported("More than 2 GLB chunks detected. Skipping."),c+=1;l||a.error("No GLB JSON chunk detected.");return{json:l,binaryData:n}};var g=e.prototype;g.getBuffer=async function(a,c){const b=this.json.buffers[a],d=this.errorContext;if(null==b.uri)return d.assert(null!=this.glbBuffer,"GLB buffer not present"),this.glbBuffer;let f=this.bufferCache.get(a);f||(c=await this.context.loadBinary(this.resolveUri(b.uri),c),f=new Uint8Array(c),this.bufferCache.set(a,f),d.assert(f.byteLength===
b.byteLength,"Buffer byte lengths should match."));return f};g.getAccessor=async function(a,c){a=this.json.accessors[a];var b=this.errorContext;b.errorUnsupportedIf(null==a.bufferView,"Some accessor does not specify a bufferView.");b.errorUnsupportedIf(a.type in["MAT2","MAT3","MAT4"],`AttributeType ${a.type} is not supported`);b=this.json.bufferViews[a.bufferView];c=await this.getBuffer(b.buffer,c);const d=K[a.type],f=L[a.componentType],h=d*f,l=b.byteStride||h;return{raw:c.buffer,byteStride:l,byteOffset:c.byteOffset+
(b.byteOffset||0)+(a.byteOffset||0),entryCount:a.count,isDenselyPacked:l===h,componentCount:d,componentByteSize:f,componentType:a.componentType,min:a.min,max:a.max,normalized:!!a.normalized}};g.getIndexData=async function(a,c){if(null==a.indices)return null;a=await this.getAccessor(a.indices,c);if(a.isDenselyPacked)switch(a.componentType){case 5121:return new Uint8Array(a.raw,a.byteOffset,a.entryCount);case 5123:return new Uint16Array(a.raw,a.byteOffset,a.entryCount);case 5125:return new Uint32Array(a.raw,
a.byteOffset,a.entryCount)}else switch(a.componentType){case 5121:return v.makeDense(this.wrapAccessor(k.BufferViewUint8,a));case 5123:return v.makeDense(this.wrapAccessor(k.BufferViewUint16,a));case 5125:return v.makeDense(this.wrapAccessor(k.BufferViewUint32,a))}};g.getPositionData=async function(a,c){const b=this.errorContext;b.errorUnsupportedIf(null==a.attributes.POSITION,"No POSITION vertex data found.");a=await this.getAccessor(a.attributes.POSITION,c);b.errorUnsupportedIf(5126!==a.componentType,
"Expected type FLOAT for POSITION vertex attribute, but found "+q[a.componentType]);b.errorUnsupportedIf(3!==a.componentCount,"POSITION vertex attribute must have 3 components, but found "+a.componentCount.toFixed());return this.wrapAccessor(k.BufferViewVec3f,a)};g.getNormalData=async function(a,c){const b=this.errorContext;b.assert(null!=a.attributes.NORMAL,"No NORMAL vertex data found.");a=await this.getAccessor(a.attributes.NORMAL,c);b.errorUnsupportedIf(5126!==a.componentType,"Expected type FLOAT for NORMAL vertex attribute, but found "+
q[a.componentType]);b.errorUnsupportedIf(3!==a.componentCount,"NORMAL vertex attribute must have 3 components, but found "+a.componentCount.toFixed());return this.wrapAccessor(k.BufferViewVec3f,a)};g.getTangentData=async function(a,c){const b=this.errorContext;b.assert(null!=a.attributes.TANGENT,"No TANGENT vertex data found.");a=await this.getAccessor(a.attributes.TANGENT,c);b.errorUnsupportedIf(5126!==a.componentType,"Expected type FLOAT for TANGENT vertex attribute, but found "+q[a.componentType]);
b.errorUnsupportedIf(4!==a.componentCount,"TANGENT vertex attribute must have 4 components, but found "+a.componentCount.toFixed());return new k.BufferViewVec4f(a.raw,a.byteOffset,a.byteStride,a.byteOffset+a.byteStride*a.entryCount)};g.getTextureCoordinates=async function(a,c){const b=this.errorContext;b.assert(null!=a.attributes.TEXCOORD_0,"No TEXCOORD_0 vertex data found.");a=await this.getAccessor(a.attributes.TEXCOORD_0,c);b.errorUnsupportedIf(2!==a.componentCount,"TEXCOORD_0 vertex attribute must have 2 components, but found "+
a.componentCount.toFixed());if(5126===a.componentType)return this.wrapAccessor(k.BufferViewVec2f,a);b.errorUnsupportedIf(!a.normalized,"Integer component types are only supported for a normalized accessor for TEXCOORD_0.");return H(a)};g.getVertexColors=async function(a,c){const b=this.errorContext;b.assert(null!=a.attributes.COLOR_0,"No COLOR_0 vertex data found.");a=await this.getAccessor(a.attributes.COLOR_0,c);b.errorUnsupportedIf(4!==a.componentCount&&3!==a.componentCount,"COLOR_0 attribute must have 3 or 4 components, but found "+
a.componentCount.toFixed());if(4===a.componentCount){if(5126===a.componentType)return this.wrapAccessor(k.BufferViewVec4f,a);if(5121===a.componentType)return this.wrapAccessor(k.BufferViewVec4u8,a);if(5123===a.componentType)return this.wrapAccessor(k.BufferViewVec4u16,a)}else if(3===a.componentCount){if(5126===a.componentType)return this.wrapAccessor(k.BufferViewVec3f,a);if(5121===a.componentType)return this.wrapAccessor(k.BufferViewVec3u8,a);if(5123===a.componentType)return this.wrapAccessor(k.BufferViewVec3u16,
a)}b.errorUnsupported("Unsupported component type for COLOR_0 attribute: "+q[a.componentType])};g.hasPositions=function(a){return void 0!==a.attributes.POSITION};g.hasNormals=function(a){return void 0!==a.attributes.NORMAL};g.hasVertexColors=function(a){return void 0!==a.attributes.COLOR_0};g.hasTextureCoordinates=function(a){return void 0!==a.attributes.TEXCOORD_0};g.hasTangents=function(a){return void 0!==a.attributes.TANGENT};g.getMaterial=async function(a,c){const b=this.errorContext;var d=this.materialCache.get(a.material);
if(!d){d=null!=a.material?u.material(this.json.materials[a.material]):u.material();const f=d.pbrMetallicRoughness,h=this.hasVertexColors(a);let l;f.baseColorTexture&&(b.errorUnsupportedIf(0!==(f.baseColorTexture.texCoord||0),"Only TEXCOORD with index 0 is supported."),l=await this.getTexture(f.baseColorTexture.index,c));let n;d.normalTexture&&(0!==(d.normalTexture.texCoord||0)?b.warnUnsupported("Only TEXCOORD with index 0 is supported for the normal map texture."):n=await this.getTexture(d.normalTexture.index,
c));let A;d.occlusionTexture&&(0!==(d.occlusionTexture.texCoord||0)?b.warnUnsupported("Only TEXCOORD with index 0 is supported for the occlusion texture."):A=await this.getTexture(d.occlusionTexture.index,c));let B;d.emissiveTexture&&(0!==(d.emissiveTexture.texCoord||0)?b.warnUnsupported("Only TEXCOORD with index 0 is supported for the emissive texture."):B=await this.getTexture(d.emissiveTexture.index,c));let C;f.metallicRoughnessTexture&&(0!==(f.metallicRoughnessTexture.texCoord||0)?b.warnUnsupported("Only TEXCOORD with index 0 is supported for the metallicRoughness texture."):
C=await this.getTexture(f.metallicRoughnessTexture.index,c));d={alphaMode:d.alphaMode,alphaCutoff:d.alphaCutoff,color:f.baseColorFactor,doubleSided:!!d.doubleSided,colorTexture:l,normalTexture:n,name:d.name,id:null!=a.material?a.material:-1,occlusionTexture:A,emissiveTexture:B,emissiveFactor:d.emissiveFactor,metallicFactor:f.metallicFactor,roughnessFactor:f.roughnessFactor,metallicRoughnessTexture:C,vertexColors:h,ESRI_externalColorMixMode:d.extras.ESRI_externalColorMixMode}}return d};g.getTexture=
async function(a,c){var b=this.errorContext,d=this.json.textures[a];const f=u.textureSampler(null!=d.sampler?this.json.samplers[d.sampler]:{});b.errorUnsupportedIf(null==d.source,"Source is expected to be defined for a texture.");d=this.json.images[d.source];var h=this.textureCache.get(a);h||(d.uri?b=await this.context.loadImage(this.resolveUri(d.uri),c):(b.errorUnsupportedIf(null==d.bufferView,"Image bufferView must be defined."),b.errorUnsupportedIf(null==d.mimeType,"Image mimeType must be defined."),
h=this.json.bufferViews[d.bufferView],c=await this.getBuffer(h.buffer,c),b.errorUnsupportedIf(null!=h.byteStride,"byteStride not supported for image buffer"),b=await J(new Uint8Array(c.buffer,c.byteOffset+(h.byteOffset||0),h.byteLength),d.mimeType)),h={data:b,wrapS:f.wrapS,wrapT:f.wrapT,minFilter:f.minFilter,name:d.name,id:a},this.textureCache.set(a,h));return h};g.getNodeTransform=function(a){if(void 0===a)return M;var c=this.nodeTransformCache.get(a);if(!c){c=this.getNodeTransform(this.getNodeParent(a));
const b=this.json.nodes[a];if(b.matrix)c=m.multiply(t.create(),c,b.matrix);else if(b.translation||b.rotation||b.scale)c=t.clone(c),b.translation&&m.translate(c,c,b.translation),b.rotation&&(r[3]=F.getAxisAngle(r,b.rotation),m.rotate(c,c,r[3],r)),b.scale&&m.scale(c,c,b.scale);this.nodeTransformCache.set(a,c)}return c};g.wrapAccessor=function(a,c){return new a(c.raw,c.byteOffset,c.byteStride,c.byteOffset+c.byteStride*(c.entryCount-1)+c.componentByteSize*c.componentCount)};g.resolveUri=function(a){return p.makeAbsolute(a,
this.baseUri)};g.getNodeParent=function(a){return this.nodeParentMap.get(a)};g.checkVersionSupported=function(){const a=y.Version.parse(this.json.asset.version,"glTF");N.validate(a)};g.checkRequiredExtensionsSupported=function(){const a=this.json,c=this.errorContext;a.extensionsRequired&&0!==a.extensionsRequired.length&&c.errorUnsupported("gltf loader was not able to load unsupported feature. Required extensions: "+a.extensionsRequired.join(", "))};g.computeNodeParents=function(){this.json.nodes.forEach((a,
c)=>{a.children&&a.children.forEach(b=>{this.nodeParentMap.set(b,c)})})};return e}();const N=new y.Version(2,0,"glTF"),M=m.fromXRotation(t.create(),Math.PI/2),r=E.create(),K={SCALAR:1,VEC2:2,VEC3:3,VEC4:4},L={[5120]:1,[5121]:1,[5122]:2,[5123]:2,[5126]:4,[5125]:4},q={5120:"BYTE",5121:"UNSIGNED_BYTE",5122:"SHORT",5123:"UNSIGNED_SHORT",5125:"UNSIGNED_INT",5126:"FLOAT"};w.Resource=O;Object.defineProperty(w,"__esModule",{value:!0})});