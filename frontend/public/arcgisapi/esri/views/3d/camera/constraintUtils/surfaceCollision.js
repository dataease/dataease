// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../chunks/vec3f64","../../../../chunks/vec3","../intersectionUtils"],function(f,l,c,m){const g=l.create();f.applySurfaceCollisionConstraint=function(d,a,h=0){var b=d.state.constraints;if(!b.collision.enabled)return!1;var e=m.surfaceElevationBelowRenderLocation(d,a.eye);const k=d.renderCoordsHelper.getAltitude(a.eye);b=e+b.collision.elevationMargin;if(k>=b)return!1;e=c.length(a.eye);c.subtract(g,a.center,a.eye);d.renderCoordsHelper.setAltitude(b,a.eye);1===h?c.add(a.center,
a.eye,g):2===h&&c.scale(a.center,a.center,(e-k+b)/e);a.markViewDirty();return!0};Object.defineProperty(f,"__esModule",{value:!0})});