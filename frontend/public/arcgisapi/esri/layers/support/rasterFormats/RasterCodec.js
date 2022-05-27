// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../../core/Error ../PixelBlock ../../../chunks/_commonjsHelpers ../../../chunks/Zlib ./ImageCanvasDecoder ./JpgPlus ../../../chunks/LercCodec ./Raw ./utils ./TiffDecoder".split(" "),function(Q,J,K,X,Y,Z,aa,ba,ca,S,T){function da(n,m){if(!S.isPlatformLittleEndian)throw new J("rasterCoded:decode","lerc decoder is not supported on big endian platform");const {width:t,height:v,pixelType:b}=m;var a=U(b);const e=a.pixelTypeCtor;m=null==m.noDataValue?a.noDataValue:m.noDataValue;a=0;let d,
c=0,f;const k=n.byteLength-10;for(;c<k;){var g=ba.decode(n,{inputOffset:c,encodedMaskData:d,returnMask:0===a?!0:!1,returnEncodedMask:0===a?!0:!1,returnFileInfo:!0,pixelType:e,noDataValue:m});if(t&&v&&(g.width!==t||g.height!==v))throw new J("rasterCoded:decode","lerc decoded result has width or height different from specified in options");c=g.fileInfo.eofOffset;0===a&&(d=g.encodedMaskData,f=new K({width:g.width,height:g.height,pixels:[],pixelType:b,mask:g.maskData,statistics:[]}));a++;f.addData({pixels:g.pixelData,
statistics:{minValue:g.minValue,maxValue:g.maxValue,noDataValue:g.noDataValue}});10<k-c&&(g=String.fromCharCode.apply(null,new Uint8Array(n,c,10)),c=-1<c+g.indexOf("CntZ")?g.indexOf("CntZ"):0)}return f}function ea(n,m){if(!S.isPlatformLittleEndian)throw new J("rasterCoded:decode","lerc decoder is not supported on big endian platform");var t=0,v=0,b=0;let a,e;const d=n.byteLength-10,c=[],{width:f,height:k}=m;for(;v<d;){var g;m=fa.decode(n,{inputOffset:v,maskData:a,returnFileInfo:!0});if(f&&k&&(m.width!==
f||m.height!==k))throw new J("rasterCoded:decode","lerc2 decoded result has width or height different from what's specified in options");v=m.fileInfo.eofOffset;0===t&&(b=m.fileInfo.numValidPixel,a=m.maskData,e=new K({width:m.width,height:m.height,pixels:[],pixelType:m.fileInfo.pixelType,mask:m.maskData,statistics:[]}));m.fileInfo.mask&&0<m.fileInfo.mask.numBytes&&c.push(m.maskData);if(1<m.dimCount&&(null==(g=m.pixelData)?void 0:g.length)===m.width*m.height*m.dimCount){var h,q,r,l;m.pixelData=m.pixelData.slice(-m.width*
m.height);const p=null==(h=m.dimStats)?void 0:null==(q=h.minValues)?void 0:q[m.dimCount-1],u=null==(r=m.dimStats)?void 0:null==(l=r.maxValues)?void 0:l[m.dimCount-1];null!=p&&null!=u&&(m.minValue=p,m.maxValue=u)}t++;e.addData({pixels:m.pixelData,statistics:{minValue:m.minValue,maxValue:m.maxValue}});10<d-v&&(m=String.fromCharCode.apply(null,new Uint8Array(n,v,10)),v+=-1<m.indexOf("Lerc")?m.indexOf("Lerc"):0)}v=t=n=0;if(1<c.length){v=e.width*e.height;a=new Uint8Array(v);a.set(c[0]);for(n=1;n<c.length;n++)for(b=
c[n],t=0;t<v;t++)a[t]&=b[t];for(t=b=0;t<v;t++)b+=a[t];e.mask=a}e.validPixelCount=b;return e}function ha(n){n=T.decode(n);n=new K({width:n.width,height:n.height,pixels:n.pixels,pixelType:n.pixelType.toLowerCase(),mask:n.mask,statistics:null});n.updateStatistics();return n}function ia(n){n=aa.decode(n);n=new K({width:n.width,height:n.height,pixels:n.pixels,pixelType:"U8",mask:n.mask,statistics:null});n.updateStatistics();return n}function ja(n,m){n=new Uint8Array(n);n=new ka(n);const {width:t,height:v}=
m;m=t*v;n=n.decode();let b=0;var a=0;let e;a=new Uint8Array(m);for(b=0;b<m;b++)a[b]=n[4*b+3];const d=new K({width:t,height:v,pixels:[],pixelType:"U8",mask:a,statistics:[]});for(b=0;3>b;b++){e=new Uint8Array(m);for(a=0;a<m;a++)e[a]=n[4*a+b];d.addData({pixels:e})}d.updateStatistics();return d}async function la(n,m,t,v){n=await (new Z).decode(n,{applyJpegMask:!1,format:m,...t},v);n=new K(n);n.updateStatistics();return n}function V(n){if(null==n)throw new J("rasterCodec:decode","parameter encodeddata is required.");
n=new Uint8Array(n,0,10);let m="";255===n[0]&&216===n[1]?m="jpg":137===n[0]&&80===n[1]&&78===n[2]&&71===n[3]?m="png":67===n[0]&&110===n[1]&&116===n[2]&&90===n[3]&&73===n[4]&&109===n[5]&&97===n[6]&&103===n[7]&&101===n[8]&&32===n[9]?m="lerc":76===n[0]&&101===n[1]&&114===n[2]&&99===n[3]&&50===n[4]&&32===n[5]?m="lerc2":73===n[0]&&73===n[1]&&42===n[2]&&0===n[3]||77===n[0]&&77===n[1]&&0===n[2]&&42===n[3]?m="tiff":71===n[0]&&73===n[1]&&70===n[2]?m="gif":66===n[0]&&77===n[1]?m="bmp":-1<String.fromCharCode.apply(null,
n).toLowerCase().indexOf("error")&&(m="error");return m}function ma(n){let m=null;switch(n){case "lerc":m=da;break;case "lerc2":m=ea;break;case "jpg":m=ia;break;case "png":m=ja;break;case "bsq":case "bip":m=(t,v)=>{{const {pixelTypeCtor:b}=U(v.pixelType),a=ca.decode;t=a(t,{width:v.width,height:v.height,pixelType:b,format:n});v=new K({width:v.width,height:v.height,pixels:t.pixels,pixelType:v.pixelType,mask:t.mask,statistics:null});v.updateStatistics()}return v};break;case "tiff":m=ha;break;case "error":m=
()=>{throw new J("rasterCodec:decode","input data contains error");};break;default:m=()=>{throw new J("rasterCodec:decode","unsupported raster format");}}return m}function U(n){let m=null,t=null;switch(n?n.toLowerCase():"f32"){case "u1":case "u2":case "u4":case "u8":t=Math.pow(2,8)-1;m=Uint8Array;break;case "u16":t=t||Math.pow(2,16)-1;m=Uint16Array;break;case "u32":t=t||Math.pow(2,32)-1;m=Uint32Array;break;case "s8":t=t||0-Math.pow(2,7);m=Int8Array;break;case "s16":t=t||0-Math.pow(2,15);m=Int16Array;
break;case "s32":t=t||0-Math.pow(2,31);m=Int32Array;break;default:m=Float32Array}return{pixelTypeCtor:m,noDataValue:t}}function na(n,m=1){if(n){var {pixels:t,width:v,height:b,mask:a}=n;if(t&&0!==t.length){var e=t.length,d=v-1,c=b-1,f=[],k,g,h=K.getPixelArrayConstructor(n.pixelType);if(0===m){for(m=0;m<e;m++){var q=t[m];var r=new h(d*c);for(k=0;k<c;k++){var l=k*v;for(g=0;g<d;g++)r[k*d+g]=q[l+g]}f.push(r)}if(a){var p=new Uint8Array(d*c);for(k=0;k<c;k++)for(l=k*v,g=0;g<d;g++)p[k*d+g]=a[l+g]}}else{for(m=
0;m<e;m++){q=t[m];r=new h(d*c);for(k=0;k<c;k++)for(l=k*v,g=0;g<d;g++)r[k*d+g]=(q[l+g]+q[l+g+1]+q[l+v+g]+q[l+v+g+1])/4;f.push(r)}if(a)for(p=new Uint8Array(d*c),k=0;k<c;k++)for(l=k*v,g=0;g<d;g++)p[k*d+g]=Math.min.apply(null,[a[l+g],a[l+g+1],a[l+v+g],a[l+v+g+1]])}n.width=d;n.height=c;n.mask=p;n.pixels=f}}}var fa=X.createCommonjsModule(function(n){(function(m){m=m();void 0!==m&&(n.exports=m)})(function(){var m={unstuff:function(b,a,e,d,c,f,k,g){var h=(1<<e)-1,q=0,r,l=0;b[b.length-1]<<=8*(4*b.length-Math.ceil(e*
d/8));if(c)for(r=0;r<d;r++){if(0===l){var p=b[q++];l=32}if(l>=e){var u=p>>>l-e&h;l-=e}else l=e-l,u=(p&h)<<l&h,p=b[q++],l=32-l,u+=p>>>l;a[r]=c[u]}else for(c=Math.ceil((g-f)/k),r=0;r<d;r++)0===l&&(p=b[q++],l=32),l>=e?(u=p>>>l-e&h,l-=e):(l=e-l,u=(p&h)<<l&h,p=b[q++],l=32-l,u+=p>>>l),a[r]=u<c?f+u*k:g},unstuffLUT:function(b,a,e,d,c,f){var k=(1<<a)-1,g=0,h=0,q=0,r=q=0,l=[];b[b.length-1]<<=8*(4*b.length-Math.ceil(a*e/8));var p=Math.ceil((f-d)/c);for(h=0;h<e;h++){if(0===q){var u=b[g++];q=32}q>=a?(r=u>>>q-
a&k,q-=a):(q=a-q,r=(u&k)<<q&k,u=b[g++],q=32-q,r+=u>>>q);l[h]=r<p?d+r*c:f}l.unshift(d);return l},unstuff2:function(b,a,e,d,c,f,k,g){var h=(1<<e)-1,q=0,r,l=0,p=0;if(c)for(r=0;r<d;r++){if(0===l){var u=b[q++];l=32;p=0}if(l>=e){var y=u>>>p&h;l-=e;p+=e}else{var w=e-l;y=u>>>p&h;u=b[q++];l=32-w;y|=(u&(1<<w)-1)<<e-w;p=w}a[r]=c[y]}else for(c=Math.ceil((g-f)/k),r=0;r<d;r++)0===l&&(u=b[q++],l=32,p=0),l>=e?(y=u>>>p&h,l-=e,p+=e):(w=e-l,y=u>>>p&h,u=b[q++],l=32-w,y|=(u&(1<<w)-1)<<e-w,p=w),a[r]=y<c?f+y*k:g;return a},
unstuffLUT2:function(b,a,e,d,c,f){var k=(1<<a)-1,g=0,h=0,q=0,r=0,l=0,p=0,u=[],y=Math.ceil((f-d)/c);for(h=0;h<e;h++){if(0===r){var w=b[g++];r=32;p=0}r>=a?(l=w>>>p&k,r-=a,p+=a):(q=a-r,l=w>>>p&k,w=b[g++],r=32-q,l|=(w&(1<<q)-1)<<a-q,p=q);u[h]=l<y?d+l*c:f}u.unshift(d);return u},originalUnstuff:function(b,a,e,d){var c=(1<<e)-1,f=0,k,g=0;b[b.length-1]<<=8*(4*b.length-Math.ceil(e*d/8));for(k=0;k<d;k++){if(0===g){var h=b[f++];g=32}if(g>=e){var q=h>>>g-e&c;g-=e}else g=e-g,q=(h&c)<<g&c,h=b[f++],g=32-g,q+=h>>>
g;a[k]=q}return a},originalUnstuff2:function(b,a,e,d){var c=(1<<e)-1,f=0,k,g=0,h=0;for(k=0;k<d;k++){if(0===g){var q=b[f++];g=32;h=0}if(g>=e){var r=q>>>h&c;g-=e;h+=e}else{var l=e-g;r=q>>>h&c;q=b[f++];g=32-l;r|=(q&(1<<l)-1)<<e-l;h=l}a[k]=r}return a}},t={HUFFMAN_LUT_BITS_MAX:12,computeChecksumFletcher32:function(b){for(var a=65535,e=65535,d=b.length,c=Math.floor(d/2),f=0;c;){var k=359<=c?359:c;c-=k;do a+=b[f++]<<8,e+=a+=b[f++];while(--k);a=(a&65535)+(a>>>16);e=(e&65535)+(e>>>16)}d&1&&(e+=a+=b[f]<<8);
return((e&65535)+(e>>>16)<<16|(a&65535)+(a>>>16))>>>0},readHeaderInfo:function(b,a){var e=a.ptr,d=new Uint8Array(b,e,6),c={};c.fileIdentifierString=String.fromCharCode.apply(null,d);if(0!==c.fileIdentifierString.lastIndexOf("Lerc2",0))throw"Unexpected file identifier string (expect Lerc2 ): "+c.fileIdentifierString;e+=6;d=new DataView(b,e,8);var f=d.getInt32(0,!0);c.fileVersion=f;e+=4;3<=f&&(c.checksum=d.getUint32(4,!0),e+=4);d=new DataView(b,e,12);c.height=d.getUint32(0,!0);c.width=d.getUint32(4,
!0);e+=8;4<=f?(c.numDims=d.getUint32(8,!0),e+=4):c.numDims=1;d=new DataView(b,e,40);c.numValidPixel=d.getUint32(0,!0);c.microBlockSize=d.getInt32(4,!0);c.blobSize=d.getInt32(8,!0);c.imageType=d.getInt32(12,!0);c.maxZError=d.getFloat64(16,!0);c.zMin=d.getFloat64(24,!0);c.zMax=d.getFloat64(32,!0);e+=40;a.headerInfo=c;a.ptr=e;if(3<=f&&(b=this.computeChecksumFletcher32(new Uint8Array(b,e-(4<=f?52:48),c.blobSize-14)),b!==c.checksum))throw"Checksum failed.";return!0},checkMinMaxRanges:function(b,a){var e=
a.headerInfo,d=this.getDataTypeArray(e.imageType),c=e.numDims*this.getDataTypeSize(e.imageType),f=this.readSubArray(b,a.ptr,d,c);b=this.readSubArray(b,a.ptr+c,d,c);a.ptr+=2*c;c=!0;for(a=0;a<e.numDims;a++)if(f[a]!==b[a]){c=!1;break}e.minValues=f;e.maxValues=b;return c},readSubArray:function(b,a,e,d){if(e===Uint8Array)b=new Uint8Array(b,a,d);else{var c=new ArrayBuffer(d);(new Uint8Array(c)).set(new Uint8Array(b,a,d));b=new e(c)}return b},readMask:function(b,a){var e=a.ptr,d=a.headerInfo,c=d.width*d.height,
f=d.numValidPixel,k=new DataView(b,e,4);d={};d.numBytes=k.getUint32(0,!0);e+=4;if((0===f||c===f)&&0!==d.numBytes)throw"invalid mask";if(0===f)f=new Uint8Array(Math.ceil(c/8)),d.bitset=f,k=new Uint8Array(c),a.pixels.resultMask=k,e+=d.numBytes;else if(0<d.numBytes){f=new Uint8Array(Math.ceil(c/8));k=new DataView(b,e,d.numBytes);b=k.getInt16(0,!0);var g=2,h=0,q=0;do{if(0<b)for(;b--;)f[h++]=k.getUint8(g++);else for(q=k.getUint8(g++),b=-b;b--;)f[h++]=q;b=k.getInt16(g,!0);g+=2}while(g<d.numBytes);if(-32768!==
b||h<f.length)throw"Unexpected end of mask RLE encoding";k=new Uint8Array(c);for(g=g=b=0;g<c;g++)g&7?(b=f[g>>3],b<<=g&7):b=f[g>>3],b&128&&(k[g]=1);a.pixels.resultMask=k;d.bitset=f;e+=d.numBytes}a.ptr=e;a.mask=d;return!0},readDataOneSweep:function(b,a,e){var d=a.ptr,c=a.headerInfo,f=c.numDims,k=c.width*c.height;c=c.numValidPixel*t.getDataTypeSize(c.imageType)*f;var g=a.pixels.resultMask;if(e===Uint8Array)b=new Uint8Array(b,d,c);else{var h=new ArrayBuffer(c);(new Uint8Array(h)).set(new Uint8Array(b,
d,c));b=new e(h)}if(b.length===k*f)a.pixels.resultPixels=b;else{a.pixels.resultPixels=new e(k*f);var q=h=e=0,r=0;if(1<f)for(q=0;q<f;q++)for(r=q*k,h=0;h<k;h++)g[h]&&(a.pixels.resultPixels[r+h]=b[e++]);else for(h=0;h<k;h++)g[h]&&(a.pixels.resultPixels[h]=b[e++])}a.ptr=d+c;return!0},readHuffmanTree:function(b,a){var e=this.HUFFMAN_LUT_BITS_MAX,d=new DataView(b,a.ptr,16);a.ptr+=16;if(2>d.getInt32(0,!0))throw"unsupported Huffman version";var c=d.getInt32(4,!0),f=d.getInt32(8,!0);d=d.getInt32(12,!0);if(f>=
d)return!1;var k=new Uint32Array(d-f);t.decodeBits(b,a,k);var g=[],h;for(h=f;h<d;h++){var q=h-(h<c?0:c);g[q]={first:k[h-f],second:null}}h=b.byteLength-a.ptr;k=new ArrayBuffer(4*Math.ceil(h/4));(new Uint8Array(k)).set(new Uint8Array(b,a.ptr,h));b=new Uint32Array(k);a=0;k=0;var r=b[0];for(h=f;h<d;h++){q=h-(h<c?0:c);var l=g[q].first;0<l&&(g[q].second=r<<a>>>32-l,32-a>=l?(a+=l,32===a&&(a=0,k++,r=b[k])):(a+=l-32,k++,r=b[k],g[q].second|=r>>>32-a))}var p=r=0,u=new v;for(h=0;h<g.length;h++)void 0!==g[h]&&
(r=Math.max(r,g[h].first));p=r>=e?e:r;e=[];var y;for(h=f;h<d;h++)if(q=h-(h<c?0:c),l=g[q].first,0<l)if(f=[l,q],l<=p){q=g[q].second<<p-l;var w=1<<p-l;for(l=0;l<w;l++)e[q|l]=f}else for(q=g[q].second,w=u,--l;0<=l;l--)(y=q>>>l&1)?(w.right||(w.right=new v),w=w.right):(w.left||(w.left=new v),w=w.left),0!==l||w.val||(w.val=f[1]);return{decodeLut:e,numBitsLUTQick:p,numBitsLUT:r,tree:u,stuffedData:b,srcPtr:k,bitPos:a}},readHuffman:function(b,a,e){var d=a.headerInfo,c=d.numDims,f=a.headerInfo.height,k=a.headerInfo.width,
g=k*f,h=this.readHuffmanTree(b,a);b=h.decodeLut;var q=h.tree,r=h.stuffedData,l=h.srcPtr,p=h.bitPos,u=h.numBitsLUTQick;h=h.numBitsLUT;var y=0===a.headerInfo.imageType?128:0,w=a.pixels.resultMask,A,B,F,H,E,x,G=0;0<p&&(l++,p=0);var C=r[l],N=1===a.encodeMode,P=new e(g*c),L=P,O;for(O=0;O<d.numDims;O++){1<c&&(L=new e(P.buffer,g*O,g),G=0);if(a.headerInfo.numValidPixel===k*f)for(F=E=0;F<f;F++)for(H=0;H<k;H++,E++){var D=0;var z=A=C<<p>>>32-u;32-p<u&&(z=A|=r[l+1]>>>64-p-u);if(b[z])D=b[z][1],p+=b[z][0];else for(z=
A=C<<p>>>32-h,32-p<h&&(z=A|=r[l+1]>>>64-p-h),z=q,x=0;x<h;x++)if(z=(B=A>>>h-x-1&1)?z.right:z.left,!z.left&&!z.right){D=z.val;p=p+x+1;break}32<=p&&(p-=32,l++,C=r[l]);D-=y;N?(D=0<H?D+G:0<F?D+L[E-k]:D+G,D&=255,G=L[E]=D):L[E]=D}else for(F=E=0;F<f;F++)for(H=0;H<k;H++,E++)if(w[E]){D=0;z=A=C<<p>>>32-u;32-p<u&&(z=A|=r[l+1]>>>64-p-u);if(b[z])D=b[z][1],p+=b[z][0];else for(z=A=C<<p>>>32-h,32-p<h&&(z=A|=r[l+1]>>>64-p-h),z=q,x=0;x<h;x++)if(z=(B=A>>>h-x-1&1)?z.right:z.left,!z.left&&!z.right){D=z.val;p=p+x+1;break}32<=
p&&(p-=32,l++,C=r[l]);D-=y;N?(D=0<H&&w[E-1]?D+G:0<F&&w[E-k]?D+L[E-k]:D+G,D&=255,G=L[E]=D):L[E]=D}a.ptr=a.ptr+4*(l+1)+(0<p?4:0)}a.pixels.resultPixels=P},decodeBits:function(b,a,e,d,c){var f=a.headerInfo,k=f.fileVersion,g=0,h=new DataView(b,a.ptr,5<=b.byteLength-a.ptr?5:b.byteLength-a.ptr),q=h.getUint8(0);g++;var r=q>>6,l=0===r?4:3-r,p=0<(q&32)?!0:!1;r=q&31;q=0;if(1===l)q=h.getUint8(g),g++;else if(2===l)q=h.getUint16(g,!0),g+=2;else if(4===l)q=h.getUint32(g,!0),g+=4;else throw"Invalid valid pixel count type";
l=2*f.maxZError;c=1<f.numDims?f.maxValues[c]:f.zMax;if(p){a.counter.lut++;p=h.getUint8(g);g++;f=Math.ceil((p-1)*r/8);var u=Math.ceil(f/4);u=new ArrayBuffer(4*u);var y=new Uint8Array(u);a.ptr+=g;y.set(new Uint8Array(b,a.ptr,f));g=new Uint32Array(u);a.ptr+=f;for(h=0;p-1>>>h;)h++;f=Math.ceil(q*h/8);u=Math.ceil(f/4);u=new ArrayBuffer(4*u);y=new Uint8Array(u);y.set(new Uint8Array(b,a.ptr,f));b=new Uint32Array(u);a.ptr+=f;a=3<=k?m.unstuffLUT2(g,r,p-1,d,l,c):m.unstuffLUT(g,r,p-1,d,l,c);3<=k?m.unstuff2(b,
e,h,q,a):m.unstuff(b,e,h,q,a)}else a.counter.bitstuffer++,h=r,a.ptr+=g,0<h&&(f=Math.ceil(q*h/8),u=Math.ceil(f/4),u=new ArrayBuffer(4*u),y=new Uint8Array(u),y.set(new Uint8Array(b,a.ptr,f)),b=new Uint32Array(u),a.ptr+=f,3<=k?null==d?m.originalUnstuff2(b,e,h,q):m.unstuff2(b,e,h,q,!1,d,l,c):null==d?m.originalUnstuff(b,e,h,q):m.unstuff(b,e,h,q,!1,d,l,c))},readTiles:function(b,a,e){var d=a.headerInfo,c=d.width,f=d.height,k=d.microBlockSize,g=d.imageType,h=t.getDataTypeSize(g),q=Math.ceil(c/k),r=Math.ceil(f/
k);a.pixels.numBlocksY=r;a.pixels.numBlocksX=q;var l=a.pixels.ptr=0,p=0,u=0,y=0,w=0,A=0,B=0,F=0,H=0,E=0,x=0,G=0,C=0;B=C=B=0;var N=new e(k*k),P=f%k||k,L=c%k||k,O=d.numDims,D,z=a.pixels.resultMask,I=a.pixels.resultPixels,W=5<=d.fileVersion?14:15,R=d.zMax;for(u=0;u<r;u++)for(w=u!==r-1?k:P,y=0;y<q;y++)for(A=y!==q-1?k:L,x=u*c*k+y*k,G=c-A,D=0;D<O;D++){1<O?(C=I,x=u*c*k+y*k,I=new e(a.pixels.resultPixels.buffer,c*f*D*h,c*f),R=d.maxValues[D]):C=null;B=b.byteLength-a.ptr;l=new DataView(b,a.ptr,Math.min(10,B));
p={};B=0;F=l.getUint8(0);B++;var M=5<=d.fileVersion?F&4:0;H=F>>6&255;E=F>>2&W;if(E!==(y*k>>3&W))throw"integrity issue";if(M&&0===D)throw"integrity issue";F&=3;if(3<F)throw a.ptr+=B,"Invalid block encoding ("+F+")";if(2===F){if(M)if(z)for(l=0;l<w;l++)for(p=0;p<A;p++)z[x]&&(I[x]=C[x]),x++;else for(l=0;l<w;l++)for(p=0;p<A;p++)I[x]=C[x],x++;a.counter.constant++;a.ptr+=B}else if(0===F){if(M)throw"integrity issue";a.counter.uncompressed++;a.ptr+=B;C=w*A*h;B=b.byteLength-a.ptr;C=C<B?C:B;B=new ArrayBuffer(0===
C%h?C:C+h-C%h);M=new Uint8Array(B);M.set(new Uint8Array(b,a.ptr,C));B=new e(B);C=0;if(z)for(l=0;l<w;l++){for(p=0;p<A;p++)z[x]&&(I[x]=B[C++]),x++;x+=G}else for(l=0;l<w;l++){for(p=0;p<A;p++)I[x++]=B[C++];x+=G}a.ptr+=C*h}else if(H=t.getDataTypeUsed(M&&6>g?4:g,H),E=t.getOnePixel(p,B,H,l),B+=t.getDataTypeSize(H),3===F)if(a.ptr+=B,a.counter.constantoffset++,z)for(l=0;l<w;l++){for(p=0;p<A;p++)z[x]&&(I[x]=M?Math.min(R,C[x]+E):E),x++;x+=G}else for(l=0;l<w;l++){for(p=0;p<A;p++)I[x]=M?Math.min(R,C[x]+E):E,x++;
x+=G}else if(a.ptr+=B,t.decodeBits(b,a,N,E,D),B=0,M)if(z)for(l=0;l<w;l++){for(p=0;p<A;p++)z[x]&&(I[x]=N[B++]+C[x]),x++;x+=G}else for(l=0;l<w;l++){for(p=0;p<A;p++)I[x]=N[B++]+C[x],x++;x+=G}else if(z)for(l=0;l<w;l++){for(p=0;p<A;p++)z[x]&&(I[x]=N[B++]),x++;x+=G}else for(l=0;l<w;l++){for(p=0;p<A;p++)I[x++]=N[B++];x+=G}}},formatFileInfo:function(b){return{fileIdentifierString:b.headerInfo.fileIdentifierString,fileVersion:b.headerInfo.fileVersion,imageType:b.headerInfo.imageType,height:b.headerInfo.height,
width:b.headerInfo.width,numValidPixel:b.headerInfo.numValidPixel,microBlockSize:b.headerInfo.microBlockSize,blobSize:b.headerInfo.blobSize,maxZError:b.headerInfo.maxZError,pixelType:t.getPixelType(b.headerInfo.imageType),eofOffset:b.eofOffset,mask:b.mask?{numBytes:b.mask.numBytes}:null,pixels:{numBlocksX:b.pixels.numBlocksX,numBlocksY:b.pixels.numBlocksY,maxValue:b.headerInfo.zMax,minValue:b.headerInfo.zMin,noDataValue:b.noDataValue}}},constructConstantSurface:function(b){var a=b.headerInfo.zMax,
e=b.headerInfo.numDims,d=b.headerInfo.height*b.headerInfo.width,c=0,f=0,k=0,g=b.pixels.resultMask,h=b.pixels.resultPixels;if(g)if(1<e)for(c=0;c<e;c++)for(k=c*d,a=b.headerInfo.maxValues[c],f=0;f<d;f++)g[f]&&(h[k+f]=a);else for(f=0;f<d;f++)g[f]&&(h[f]=a);else if(1<e)for(c=0;c<e;c++)for(k=c*d,a=b.headerInfo.maxValues[c],f=0;f<d;f++)h[k+f]=a;else for(f=0;f<d;f++)h[f]=a},getDataTypeArray:function(b){switch(b){case 0:b=Int8Array;break;case 1:b=Uint8Array;break;case 2:b=Int16Array;break;case 3:b=Uint16Array;
break;case 4:b=Int32Array;break;case 5:b=Uint32Array;break;case 6:b=Float32Array;break;case 7:b=Float64Array;break;default:b=Float32Array}return b},getPixelType:function(b){switch(b){case 0:b="S8";break;case 1:b="U8";break;case 2:b="S16";break;case 3:b="U16";break;case 4:b="S32";break;case 5:b="U32";break;case 6:b="F32";break;case 7:b="F64";break;default:b="F32"}return b},isValidPixelValue:function(b,a){if(null==a)return!1;switch(b){case 0:b=-128<=a&&127>=a;break;case 1:b=0<=a&&255>=a;break;case 2:b=
-32768<=a&&32767>=a;break;case 3:b=0<=a&&65536>=a;break;case 4:b=-2147483648<=a&&2147483647>=a;break;case 5:b=0<=a&&4294967296>=a;break;case 6:b=-3.4027999387901484E38<=a&&3.4027999387901484E38>=a;break;case 7:b=4.9E-324<=a&&1.7976931348623157E308>=a;break;default:b=!1}return b},getDataTypeSize:function(b){var a=0;switch(b){case 0:case 1:a=1;break;case 2:case 3:a=2;break;case 4:case 5:case 6:a=4;break;case 7:a=8;break;default:a=b}return a},getDataTypeUsed:function(b,a){var e=b;switch(b){case 2:case 4:e=
b-a;break;case 3:case 5:e=b-2*a;break;case 6:e=0===a?b:1===a?2:1;break;case 7:e=0===a?b:b-2*a+1;break;default:e=b}return e},getOnePixel:function(b,a,e,d){b=0;switch(e){case 0:b=d.getInt8(a);break;case 1:b=d.getUint8(a);break;case 2:b=d.getInt16(a,!0);break;case 3:b=d.getUint16(a,!0);break;case 4:b=d.getInt32(a,!0);break;case 5:b=d.getUInt32(a,!0);break;case 6:b=d.getFloat32(a,!0);break;case 7:b=d.getFloat64(a,!0);break;default:throw"the decoder does not understand this pixel type";}return b}},v=function(b,
a,e){this.val=b;this.left=a;this.right=e};return{decode:function(b,a){a=a||{};var e=a.noDataValue,d=0,c={};c.ptr=a.inputOffset||0;c.pixels={};if(t.readHeaderInfo(b,c)){d=c.headerInfo;var f=d.fileVersion,k=t.getDataTypeArray(d.imageType);if(5<f)throw"unsupported lerc version 2."+f;t.readMask(b,c);d.numValidPixel===d.width*d.height||c.pixels.resultMask||(c.pixels.resultMask=a.maskData);var g=d.width*d.height;c.pixels.resultPixels=new k(g*d.numDims);c.counter={onesweep:0,uncompressed:0,lut:0,bitstuffer:0,
constant:0,constantoffset:0};if(0!==d.numValidPixel)if(d.zMax===d.zMin)t.constructConstantSurface(c);else if(4<=f&&t.checkMinMaxRanges(b,c))t.constructConstantSurface(c);else{var h=new DataView(b,c.ptr,2),q=h.getUint8(0);c.ptr++;if(q)t.readDataOneSweep(b,c,k);else if(1<f&&1>=d.imageType&&1E-5>Math.abs(d.maxZError-.5)){h=h.getUint8(1);c.ptr++;c.encodeMode=h;if(2<h||4>f&&1<h)throw"Invalid Huffman flag "+h;h?t.readHuffman(b,c,k):t.readTiles(b,c,k)}else t.readTiles(b,c,k)}c.eofOffset=c.ptr;a.inputOffset?
(b=c.headerInfo.blobSize+a.inputOffset-c.ptr,1<=Math.abs(b)&&(c.eofOffset=a.inputOffset+c.headerInfo.blobSize)):(b=c.headerInfo.blobSize-c.ptr,1<=Math.abs(b)&&(c.eofOffset=c.headerInfo.blobSize));b={width:d.width,height:d.height,pixelData:c.pixels.resultPixels,minValue:d.zMin,maxValue:d.zMax,validPixelCount:d.numValidPixel,dimCount:d.numDims,dimStats:{minValues:d.minValues,maxValues:d.maxValues},maskData:c.pixels.resultMask};if(c.pixels.resultMask&&t.isValidPixelValue(d.imageType,e)){f=c.pixels.resultMask;
for(d=0;d<g;d++)f[d]||(b.pixelData[d]=e);b.noDataValue=e}c.noDataValue=e;a.returnFileInfo&&(b.fileInfo=t.formatFileInfo(c));return b}},getBandCount:function(b){for(var a=0,e=0,d={ptr:0,pixels:{}};e<b.byteLength-58;)t.readHeaderInfo(b,d),e+=d.headerInfo.blobSize,a++,d.ptr=e;return a}}})}),ka=function(n){function m(a){var e,d;this.data=a;this.pos=8;this.palette=[];this.imgData=[];this.transparency={};this.animation=null;this.text={};for(d=null;;){var c=this.readUInt32();var f=a=void 0;a=[];for(f=0;4>
f;++f)a.push(String.fromCharCode(this.data[this.pos++]));a=a.join("");switch(a){case "IHDR":this.width=this.readUInt32();this.height=this.readUInt32();this.bits=this.data[this.pos++];this.colorType=this.data[this.pos++];this.compressionMethod=this.data[this.pos++];this.filterMethod=this.data[this.pos++];this.interlaceMethod=this.data[this.pos++];break;case "acTL":this.animation={numFrames:this.readUInt32(),numPlays:this.readUInt32()||Infinity,frames:[]};break;case "PLTE":this.palette=this.read(c);
break;case "fcTL":d&&this.animation.frames.push(d);this.pos+=4;d={width:this.readUInt32(),height:this.readUInt32(),xOffset:this.readUInt32(),yOffset:this.readUInt32()};a=this.readUInt16();c=this.readUInt16()||100;d.delay=1E3*a/c;d.disposeOp=this.data[this.pos++];d.blendOp=this.data[this.pos++];d.data=[];break;case "IDAT":case "fdAT":"fdAT"===a&&(this.pos+=4,c-=4);a=(null!=d?d.data:void 0)||this.imgData;for(f=0;0<=c?f<c:f>c;0<=c?++f:--f)a.push(this.data[this.pos++]);break;case "tRNS":this.transparency=
{};switch(this.colorType){case 3:this.transparency.indexed=this.read(c);c=255-this.transparency.indexed.length;if(0<c)for(a=0;0<=c?a<c:a>c;0<=c?++a:--a)this.transparency.indexed.push(255);break;case 0:this.transparency.grayscale=this.read(c)[0];break;case 2:this.transparency.rgb=this.read(c)}break;case "tEXt":f=this.read(c);c=f.indexOf(0);a=String.fromCharCode.apply(String,f.slice(0,c));this.text[a]=String.fromCharCode.apply(String,f.slice(c+1));break;case "IEND":d&&this.animation.frames.push(d);
a:{switch(this.colorType){case 0:case 3:case 4:d=1;break a;case 2:case 6:d=3;break a}d=void 0}this.colors=d;this.hasAlphaChannel=4===(e=this.colorType)||6===e;e=this.colors+(this.hasAlphaChannel?1:0);this.pixelBitlength=this.bits*e;a:{switch(this.colors){case 1:e="DeviceGray";break a;case 3:e="DeviceRGB";break a}e=void 0}this.colorSpace=e;this.imgData=new Uint8Array(this.imgData);return;default:this.pos+=c}this.pos+=4;if(this.pos>this.data.length)throw Error("Incomplete or corrupt PNG file");}}var t;
m.load=function(a,e,d){"function"===typeof e&&(d=e);var c=new XMLHttpRequest;c.open("GET",a,!0);c.responseType="arraybuffer";c.onload=function(){var f=new Uint8Array(c.response||c.mozResponseArrayBuffer);f=new m(f);"function"===typeof(null!=e?e.getContext:void 0)&&f.render(e);return"function"===typeof d?d(f):void 0};return c.send(null)};m.prototype.read=function(a){var e;var d=[];for(e=0;0<=a?e<a:e>a;0<=a?++e:--e)d.push(this.data[this.pos++]);return d};m.prototype.readUInt32=function(){var a=this.data[this.pos++]<<
24;var e=this.data[this.pos++]<<16;var d=this.data[this.pos++]<<8;var c=this.data[this.pos++];return a|e|d|c};m.prototype.readUInt16=function(){var a=this.data[this.pos++]<<8;var e=this.data[this.pos++];return a|e};m.prototype.decodePixels=function(a){var e,d,c,f,k,g,h,q;null==a&&(a=this.imgData);if(0===a.length)return new Uint8Array(0);a=new Y.Zlib(a);a=a.getBytes();var r=this.pixelBitlength/8;var l=r*this.width;var p=new Uint8Array(l*this.height);var u=a.length;for(d=f=k=0;f<u;){switch(a[f++]){case 0:for(c=
e=0;e<l;c=e+=1)p[d++]=a[f++];break;case 1:for(c=g=0;g<l;c=g+=1){e=a[f++];var y=c<r?0:p[d-r];p[d++]=(e+y)%256}break;case 2:for(c=y=0;y<l;c=y+=1){e=a[f++];var w=(c-c%r)/r;g=k&&p[(k-1)*l+w*r+c%r];p[d++]=(g+e)%256}break;case 3:for(c=q=0;q<l;c=q+=1)e=a[f++],w=(c-c%r)/r,y=c<r?0:p[d-r],g=k&&p[(k-1)*l+w*r+c%r],p[d++]=(e+Math.floor((y+g)/2))%256;break;case 4:for(c=q=0;q<l;c=q+=1){e=a[f++];w=(c-c%r)/r;y=c<r?0:p[d-r];0===k?g=h=0:(g=p[(k-1)*l+w*r+c%r],h=w&&p[(k-1)*l+(w-1)*r+c%r]);var A=y+g-h;c=Math.abs(A-y);
w=Math.abs(A-g);A=Math.abs(A-h);y=c<=w&&c<=A?y:w<=A?g:h;p[d++]=(e+y)%256}break;default:throw Error("Invalid filter algorithm: "+a[f-1]);}k++}return p};m.prototype.decodePalette=function(){var a,e,d,c,f;var k=this.palette;var g=this.transparency.indexed||[];var h=new Uint8Array((g.length||0)+k.length);var q=d=a=e=0;for(c=k.length;d<c;q=d+=3)h[e++]=k[q],h[e++]=k[q+1],h[e++]=k[q+2],h[e++]=null!=(f=g[a++])?f:255;return h};m.prototype.copyToImageData=function(a,e){var d,c;var f=this.colors;var k=null;
var g=this.hasAlphaChannel;this.palette.length&&(k=null!=(d=this._decodedPalette)?d:this._decodedPalette=this.decodePalette(),f=4,g=!0);a=a.data||a;var h=a.length;var q=k||e;d=c=0;if(1===f)for(;d<h;)f=k?4*e[d/4]:c,c=q[f++],a[d++]=c,a[d++]=c,a[d++]=c,a[d++]=g?q[f++]:this.transparency.grayscale&&this.transparency.grayscale===c?0:255,c=f;else for(;d<h;)f=k?4*e[d/4]:c,a[d++]=q[f++],a[d++]=q[f++],a[d++]=q[f++],a[d++]=g?q[f++]:this.transparency.rgb&&this.transparency.rgb[1]===q[f-3]&&this.transparency.rgb[3]===
q[f-2]&&this.transparency.rgb[5]===q[f-1]?0:255,c=f};m.prototype.decode=function(){var a=new Uint8Array(this.width*this.height*4);this.copyToImageData(a,this.decodePixels());return a};var v=(t=n.document&&n.document.createElement("canvas"))&&t.getContext("2d");var b=function(a){v.width=a.width;v.height=a.height;v.clearRect(0,0,a.width,a.height);v.putImageData(a,0,0);a=new Image;a.src=t.toDataURL();return a};m.prototype.decodeFrames=function(a){var e,d;if(this.animation){var c=this.animation.frames;
var f=[];var k=e=0;for(d=c.length;e<d;k=++e){k=c[k];var g=a.createImageData(k.width,k.height);var h=this.decodePixels(new Uint8Array(k.data));this.copyToImageData(g,h);k.imageData=g;f.push(k.image=b(g))}return f}};m.prototype.renderFrame=function(a,e){var d=this.animation.frames;var c=d[e];d=d[e-1];0===e&&a.clearRect(0,0,this.width,this.height);1===(null!=d?d.disposeOp:void 0)?a.clearRect(d.xOffset,d.yOffset,d.width,d.height):2===(null!=d?d.disposeOp:void 0)&&a.putImageData(d.imageData,d.xOffset,
d.yOffset);0===c.blendOp&&a.clearRect(c.xOffset,c.yOffset,c.width,c.height);return a.drawImage(c.image,c.xOffset,c.yOffset)};m.prototype.animate=function(a){var e,d=this;var c=0;var f=this.animation;var k=f.numFrames;var g=f.frames;var h=f.numPlays;return(e=function(){var q=c++%k;var r=g[q];d.renderFrame(a,q);if(1<k&&c/k<h)return d.animation._timeout=setTimeout(e,r.delay)})()};m.prototype.stopAnimation=function(){var a;return clearTimeout(null!=(a=this.animation)?a._timeout:void 0)};m.prototype.render=
function(a){a._png&&a._png.stopAnimation();a._png=this;a.width=this.width;a.height=this.height;a=a.getContext("2d");if(this.animation)return this.decodeFrames(a),this.animate(a);var e=a.createImageData(this.width,this.height);this.copyToImageData(e,this.decodePixels());return a.putImageData(e,0,0)};return m}(self);const oa=new Set(["jpg","png","bmp","gif"]);Q.decode=async function(n,m,t){if(null==n)throw new J("rasterCodec:decode","missing encodeddata parameter.");if(null==m||null==m.width||null==
m.height)throw new J("rasterCodec:decode","requires width and height in options parameter.");let v=m.format&&m.format.toLowerCase();if("tiff"===v&&m.customOptions)return m=T.decodeTileOrStrip(n,m.customOptions),m=new K({width:m.width,height:m.height,pixels:m.pixels,pixelType:m.pixelType.toLowerCase(),mask:m.mask,statistics:null}),m.updateStatistics(),m;if(!v||"bsq"!==v&&"bip"!==v)v=V(n);m.useCanvas&&oa.has(v)?n=await la(n,v,m,t):(t=ma(v),m.isPoint&&(m={...m},m.width++,m.height++),n=t(n,m),m.isPoint&&
na(n));return n};Q.getFormat=function(n){n=V(n);"lerc2"===n?n="lerc":"error"===n&&(n="");return n};Object.defineProperty(Q,"__esModule",{value:!0})});