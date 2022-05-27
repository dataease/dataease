// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./extensions/serializableProperty"],function(e,g){const f=[g.SerializablePropertyExtension];e.instanceCreated=function(a,d){const c=Object.getOwnPropertyNames(d);for(const b of f)b.instanceCreated&&b.instanceCreated(a,d,c)};e.processClassMetadatas=function(a,d){for(const c of f)if(c.processClassPropertyMetadata)for(const b in a)c.processClassPropertyMetadata(b,a[b],a,d)};e.processPrototypeMetadatas=function(a,d){for(const c of f)if(c.processPrototypePropertyMetadata)for(const b in a)c.processPrototypePropertyMetadata(b,
a[b],a,d)};Object.defineProperty(e,"__esModule",{value:!0})});