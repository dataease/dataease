// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../core/maybe ../request ../geometry/support/normalizeUtils ./utils ../tasks/support/ImageServiceIdentifyResult".split(" "),function(e,g,h,k,c,l){e.imageServiceIdentify=async function(m,d,n){const f=c.parseUrl(m);return k.normalizeCentralMeridian(d.geometry?[d.geometry]:[]).then(a=>{var b=d.toJSON();a=a&&a[0];g.isSome(a)&&(b.geometry=JSON.stringify(a.toJSON()));b=c.encode({...f.query,f:"json",...b});b=c.asValidOptions(b,n);return h(f.path+"/identify",b)}).then(a=>l.fromJSON(a.data))};
Object.defineProperty(e,"__esModule",{value:!0})});