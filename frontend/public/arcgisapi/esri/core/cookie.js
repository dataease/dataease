// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./maybe"],function(f,g){f.writeCookie=function(b,e,c={}){let d=g.assumeNonNull(c.expires);if("number"===typeof d){const a=new Date;a.setTime(a.getTime()+864E5*d);d=c.expires=a}"string"!==typeof d&&(c.expires=d.toUTCString());b=b+"\x3d"+encodeURIComponent(e);for(const a in c)b+="; "+a,e=c[a],!0!==e&&(b+="\x3d"+e);document.cookie=b};Object.defineProperty(f,"__esModule",{value:!0})});