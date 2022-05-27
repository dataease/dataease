// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../../../core/Logger"],function(c){return function(b,a,d){b.referencesGeometry();a=a.readArcadeFeature();try{return b.evaluate({...d,$feature:a})}catch(e){return c.getLogger("esri.views.2d.support.arcadeOnDemand").warn("Feature arcade evaluation failed:",e),null}}});