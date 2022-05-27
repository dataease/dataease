// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./support/spatialReferenceUtils","./SpatialReference","./support/Ellipsoid"],function(c,d,k,b){function g(a){return new k({wkt:`GEOCCS["Spherical geocentric",
    DATUM["Not specified",
      SPHEROID["Sphere",${a.radius},0]],
    PRIMEM["Greenwich",0.0,
      AUTHORITY["EPSG","8901"]],
    UNIT["m",1.0],
    AXIS["Geocentric X",OTHER],
    AXIS["Geocentric Y",EAST],
    AXIS["Geocentric Z",NORTH]
  ]`})}const h=g(b.earth),e=g(b.mars),f=g(b.moon),l=new k({wkt:`GEOCCS["WGS 84",
  DATUM["WGS_1984",
    SPHEROID["WGS 84",${b.earth.radius},298.257223563,
      AUTHORITY["EPSG","7030"]],
    AUTHORITY["EPSG","6326"]],
  PRIMEM["Greenwich",0,
    AUTHORITY["EPSG","8901"]],
  UNIT["m",1.0,
    AUTHORITY["EPSG","9001"]],
  AXIS["Geocentric X",OTHER],
  AXIS["Geocentric Y",OTHER],
  AXIS["Geocentric Z",NORTH],
  AUTHORITY["EPSG","4978"]
]`});c.SphericalECEFSpatialReference=h;c.SphericalPCPFMars=e;c.SphericalPCPFMoon=f;c.WGS84ECEFSpatialReference=l;c.createSphericalPCPF=g;c.getReferenceEllipsoid=function(a){return a&&(d.isMars(a)||a===e)?b.mars:a&&(d.isMoon(a)||a===f)?b.moon:b.earth};c.getReferenceEllipsoidFromWKID=function(a){return d.isWKIDFromMars(a)?b.mars:d.isWKIDFromMoon(a)?b.moon:b.earth};c.getSphericalPCPF=function(a){return a&&(d.isMars(a)||a===e)?e:a&&(d.isMoon(a)||a===f)?f:h};c.getSphericalPCPFForEllipsoid=function(a){return a&&
a===b.mars?e:a&&a===b.moon?f:h};Object.defineProperty(c,"__esModule",{value:!0})});