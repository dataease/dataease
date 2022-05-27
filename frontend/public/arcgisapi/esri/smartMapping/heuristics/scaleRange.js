// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../../core/maybe","../../core/Error","../../geometry/support/scaleUtils","../support/adapters/support/layerUtils","../statistics/spatialStatistics"],function(q,k,m,n,r){async function t(a){const {view:c,sampleSize:b}=a;if(!(a&&c&&a.layer))throw new k("scale-range:missing-parameters","'view' and 'layer' parameters are required");var e=[0,2,3,1];const {layer:f,...l}=a;a=n.createLayerAdapter(f,e);const g={layerAdapter:a,...l};g.sampleSize=b||500;if(!a)throw new k("scale-range:invalid-parameters",
"'layer' must be one of these types: "+n.getLayerTypeLabels(e).join(", "));await c.when();e=q.isSome(g.signal)?{signal:g.signal}:null;await a.load(e);return g}function p(a,c,b=!0){if(a.constraints&&"effectiveLODs"in a.constraints){a=a.constraints.effectiveLODs;a=b?a:a.slice(0).reverse();let e=null;for(const f of a)if(!(b?f.scale>c:f.scale<c)){e=f;break}return e}}return async function(a){var c,b,e=await t(a);const {view:f,sampleSize:l,layerAdapter:g,signal:u}=e;a=await g.getSampleFeatures({view:f,
sampleSize:l,returnGeometry:!0,signal:u});a=await r({features:a,geometryType:g.geometryType});{let h=c=null;var d=b=null;switch(g.geometryType){case "point":case "multipoint":c=a.avgMinDistance;h=12;b=a.minDistance;d=320;break;case "polyline":c=a.avgLength;h=30;b=a.minLength;d=320;break;case "polygon":c=a.avgSize,h=15,b=a.minSize,d=640}c=0<c?c/h:null;b=0<b?b/d:null}d=m.getScaleForResolution(c,f.spatialReference);b=m.getScaleForResolution(b,f.spatialReference);b={minScale:d,maxScale:b};const {minScale:v,
maxScale:w}=b;{b=v;d=w;const {view:h,snapToLOD:x,layerAdapter:y}=e;x&&(e=p(h,b),c=p(h,d,!1),b=e?e.scale:b,d=c?c.scale:d);if(b<d)throw new k("scale-range:invalid","calculated minScale is less than maxScale.");d>b/2&&(d=Math.floor(d/2));1E8<b&&(b=0);"polygon"!==y.geometryType&&(d=0);a={minScale:Math.ceil(b),maxScale:Math.floor(d),spatialStatistics:a}}return a}});