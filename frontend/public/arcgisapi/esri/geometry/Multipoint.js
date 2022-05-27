// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("../chunks/_rollupPluginBabelHelpers ../chunks/tslib.es6 ../core/has ../core/lang ../core/Logger ../core/accessorSupport/ensureType ../core/accessorSupport/decorators/property ../core/jsonMap ../core/accessorSupport/decorators/subclass ../core/accessorSupport/decorators/writer ../core/urlUtils ../core/uuid ../portal/support/resourceExtension ./Geometry ./Point ./Extent ./support/zmUtils".split(" "),function(A,h,f,B,P,Q,n,R,K,L,S,T,U,M,C,N,D){function E(k){return(g,d)=>null==g?d:null==d?g:k(g,
d)}var p;f=p=function(k){function g(...a){a=k.call(this,...a)||this;a.points=[];a.type="multipoint";return a}A._inheritsLoose(g,k);var d=g.prototype;d.normalizeCtorArgs=function(a,b){if(!a&&!b)return null;const c={};Array.isArray(a)?(c.points=a,c.spatialReference=b):!a||"esri.geometry.SpatialReference"!==a.declaredClass&&null==a.wkid?(a.points&&(c.points=a.points),a.spatialReference&&(c.spatialReference=a.spatialReference),a.hasZ&&(c.hasZ=a.hasZ),a.hasM&&(c.hasM=a.hasM)):c.spatialReference=a;if(a=
c.points&&c.points[0])void 0===c.hasZ&&void 0===c.hasM?(c.hasZ=2<a.length,c.hasM=!1):void 0===c.hasZ?c.hasZ=3<a.length:void 0===c.hasM&&(c.hasM=3<a.length);return c};d.writePoints=function(a,b){b.points=B.clone(this.points)};d.addPoint=function(a){this.clearCache();D.updateSupportFromPoint(this,a);Array.isArray(a)?this.points.push(a):this.points.push(a.toArray());return this};d.clone=function(){const a={points:B.clone(this.points),spatialReference:this.spatialReference};this.hasZ&&(a.hasZ=!0);this.hasM&&
(a.hasM=!0);return new p(a)};d.getPoint=function(a){if(!this._validateInputs(a))return null;a=this.points[a];const b={x:a[0],y:a[1],spatialReference:this.spatialReference};let c=2;this.hasZ&&(b.z=a[2],c=3);this.hasM&&(b.m=a[c]);return new C(b)};d.removePoint=function(a){if(!this._validateInputs(a))return null;this.clearCache();return new C(this.points.splice(a,1)[0],this.spatialReference)};d.setPoint=function(a,b){if(!this._validateInputs(a))return this;this.clearCache();D.updateSupportFromPoint(this,
b);Array.isArray(b)||(b=b.toArray());this.points[a]=b;return this};d.toJSON=function(a){return this.write(null,a)};d._validateInputs=function(a){return null!=a&&0<=a&&a<this.points.length};A._createClass(g,[{key:"extent",get:function(){const a=this.points;if(!a.length)return null;const b=new N,c=this.hasZ,F=this.hasM,G=c?3:2;var e=a[0];const l=E(Math.min),m=E(Math.max);let [q,r]=e,[t,u]=e,v,w,x,y;for(let z=0,O=a.length;z<O;z++){e=a[z];const [H,I]=e;q=l(q,H);r=l(r,I);t=m(t,H);u=m(u,I);if(c&&2<e.length){const J=
e[2];v=l(v,J);x=m(x,J)}F&&e.length>G&&(e=e[G],w=l(w,e),y=m(y,e))}b.xmin=q;b.ymin=r;b.xmax=t;b.ymax=u;b.spatialReference=this.spatialReference;c?(b.zmin=v,b.zmax=x):(b.zmin=null,b.zmax=null);F?(b.mmin=w,b.mmax=y):(b.mmin=null,b.mmax=null);return b}}]);return g}(M);h.__decorate([n.property({dependsOn:["points","hasZ","hasM","spatialReference"],autoTracked:!1})],f.prototype,"cache",void 0);h.__decorate([n.property({dependsOn:["cache"],autoTracked:!1})],f.prototype,"extent",null);h.__decorate([n.property({type:[[Number]],
json:{write:{isRequired:!0}}})],f.prototype,"points",void 0);h.__decorate([L.writer("points")],f.prototype,"writePoints",null);f=p=h.__decorate([K.subclass("esri.geometry.Multipoint")],f);f.prototype.toJSON.isDefaultToJSON=!0;return f});