// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(function(){return function(){function c(a){this.materials={};this.stage=a}var b=c.prototype;b.getMaterial=function(a){return this.materials[a]};b.addMaterial=function(a,d){this.materials[a]=d;this.stage.add(3,d)};b.dispose=function(){for(const a in this.materials)this.stage.remove(3,this.materials[a].id);this.materials={}};return c}()});