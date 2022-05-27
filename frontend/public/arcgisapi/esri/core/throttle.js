// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(d){function h(n,a,k,e){let f=null,c=1E3;"number"===typeof a?(c=a,e=k):(f=null!=a?a:null,c=k);let b=0,l;const g=()=>{b=0;n.apply(e,l)};a=(...m)=>{f&&f.apply(e,m);l=m;c?b||(b=setTimeout(g,c)):g()};a.remove=()=>{b&&(clearTimeout(b),b=0)};a.forceUpdate=()=>{b&&(clearTimeout(b),g())};a.hasPendingUpdates=()=>!!b;return a}d.default=h;d.throttle=h;Object.defineProperty(d,"__esModule",{value:!0})});