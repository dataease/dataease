// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../core/promiseUtils","../../../../geometry/support/webMercatorUtils","../../../../portal/support/geometryServiceUtils"],function(f,c,g,k){f.toViewIfLocal=function(a){const d=a.view.spatialReference,b=a.layer.fullExtent,e=b&&b.spatialReference;return e?e.equals(d)?c.resolve(b.clone()):g.canProject(e,d)?c.resolve(g.project(b,d)):a.view.state.isLocal?k.projectGeometry(b,d,a.layer.portalItem).then(h=>!a.destroyed&&h?h:void 0).catch(()=>null):c.resolve(null):c.resolve(null)};
Object.defineProperty(f,"__esModule",{value:!0})});