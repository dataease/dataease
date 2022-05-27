// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../chunks/_rollupPluginBabelHelpers ../chunks/tslib.es6 ../core/has ../core/Logger ../core/accessorSupport/ensureType ../core/accessorSupport/decorators/property ../core/jsonMap ../core/accessorSupport/decorators/subclass ../core/urlUtils ../core/uuid ../portal/support/resourceExtension ../core/ArrayPool ../core/Handles ../core/watchUtils".split(" "),function(u,v,h,A,B,C,l,D,x,E,F,G,m,y,z){function p(a,d){return d?n[a].valueToClassName[d].split(" "):[]}const n={widthBreakpoint:{getValue(a){const d=
a.viewSize[0];a=a.breakpoints;const e=this.values;return d<=a.xsmall?e.xsmall:d<=a.small?e.small:d<=a.medium?e.medium:d<=a.large?e.large:e.xlarge},values:{xsmall:"xsmall",small:"small",medium:"medium",large:"large",xlarge:"xlarge"},valueToClassName:{xsmall:"esri-view-width-xsmall esri-view-width-less-than-small esri-view-width-less-than-medium esri-view-width-less-than-large esri-view-width-less-than-xlarge",small:"esri-view-width-small esri-view-width-greater-than-xsmall esri-view-width-less-than-medium esri-view-width-less-than-large esri-view-width-less-than-xlarge",
medium:"esri-view-width-medium esri-view-width-greater-than-xsmall esri-view-width-greater-than-small esri-view-width-less-than-large esri-view-width-less-than-xlarge",large:"esri-view-width-large esri-view-width-greater-than-xsmall esri-view-width-greater-than-small esri-view-width-greater-than-medium esri-view-width-less-than-xlarge",xlarge:"esri-view-width-xlarge esri-view-width-greater-than-xsmall esri-view-width-greater-than-small esri-view-width-greater-than-medium esri-view-width-greater-than-large"}},
heightBreakpoint:{getValue(a){const d=a.viewSize[1];a=a.breakpoints;const e=this.values;return d<=a.xsmall?e.xsmall:d<=a.small?e.small:d<=a.medium?e.medium:d<=a.large?e.large:e.xlarge},values:{xsmall:"xsmall",small:"small",medium:"medium",large:"large",xlarge:"xlarge"},valueToClassName:{xsmall:"esri-view-height-xsmall esri-view-height-less-than-small esri-view-height-less-than-medium esri-view-height-less-than-large esri-view-height-less-than-xlarge",small:"esri-view-height-small esri-view-height-greater-than-xsmall esri-view-height-less-than-medium esri-view-height-less-than-large esri-view-height-less-than-xlarge",
medium:"esri-view-height-medium esri-view-height-greater-than-xsmall esri-view-height-greater-than-small esri-view-height-less-than-large esri-view-height-less-than-xlarge",large:"esri-view-height-large esri-view-height-greater-than-xsmall esri-view-height-greater-than-small esri-view-height-greater-than-medium esri-view-height-less-than-xlarge",xlarge:"esri-view-height-xlarge esri-view-height-greater-than-xsmall esri-view-height-greater-than-small esri-view-height-greater-than-medium esri-view-height-greater-than-large"}},
orientation:{getValue(a){a=a.viewSize;const d=this.values;return a[1]>=a[0]?d.portrait:d.landscape},values:{portrait:"portrait",landscape:"landscape"},valueToClassName:{portrait:"esri-view-orientation-portrait",landscape:"esri-view-orientation-landscape"}}},q={xsmall:544,small:768,medium:992,large:1200};u.BreakpointsOwner=a=>{a=function(d){function e(...b){b=d.call(this,...b)||this;b._breakpointsHandles=new y;b.orientation=null;b.widthBreakpoint=null;b.heightBreakpoint=null;b.breakpoints=q;return b}
v._inheritsLoose(e,d);var k=e.prototype;k.initialize=function(){this._breakpointsHandles.add(z.init(this,["breakpoints","size"],this._updateClassNames))};k.destroy=function(){this.destroyed||(this._removeActiveClassNames(),this._breakpointsHandles.destroy(),this._breakpointsHandles=null)};k._updateClassNames=function(){if(this.container){var b=m.acquire(),c=m.acquire(),f=!1,g;for(g in n){const w=this[g],r=n[g].getValue({viewSize:this.size,breakpoints:this.breakpoints});w!==r&&(f=!0,this[g]=r,p(g,
w).forEach(t=>c.push(t)),p(g,r).forEach(t=>b.push(t)))}f&&(this._applyClassNameChanges(b,c),m.release(b),m.release(c))}};k._applyClassNameChanges=function(b,c){const f=this.container;f&&(c.forEach(g=>f.classList.remove(g)),b.forEach(g=>f.classList.add(g)))};k._removeActiveClassNames=function(){const b=this.container;if(b)for(var c in n)p(c,this[c]).forEach(f=>b.classList.remove(f))};v._createClass(e,[{key:"breakpoints",set:function(b){var c=this._get("breakpoints");if(b!==c){c=(c=b)&&c.xsmall<c.small&&
c.small<c.medium&&c.medium<c.large;if(!c){const f=JSON.stringify(q,null,2);console.warn("provided breakpoints are not valid, using defaults:"+f)}b=c?b:q;this._set("breakpoints",{...b})}}}]);return e}(a);h.__decorate([l.property()],a.prototype,"breakpoints",null);h.__decorate([l.property()],a.prototype,"orientation",void 0);h.__decorate([l.property()],a.prototype,"widthBreakpoint",void 0);h.__decorate([l.property()],a.prototype,"heightBreakpoint",void 0);return a=h.__decorate([x.subclass("esri.views.BreakpointsOwner")],
a)};Object.defineProperty(u,"__esModule",{value:!0})});