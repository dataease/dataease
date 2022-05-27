// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../has","../../Logger","../../deprecate"],function(a,b,c,d){a.declared=function(e,...f){if(0<f.length)throw Error("Multi-inheritance unsupported since 4.16");b("esri-deprecation-warnings")&&d.deprecated(c.getLogger("esri.core.accessorSupport.decorators"),"'extends declared(superclass)' syntax",{version:"4.16",see:"https://arcg.is/T8fr4"});return e};Object.defineProperty(a,"__esModule",{value:!0})});