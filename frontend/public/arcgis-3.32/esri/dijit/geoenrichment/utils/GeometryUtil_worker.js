// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
(function(a){a.addEventListener&&a.addEventListener("message",function(b){var c=b.data.rings;c.forEach(function(a){(new GeometryUtil_base.RingInfo(a)).generalize(b.data.maxAllowableOffset,.8)});a.postMessage&&a.postMessage({msgId:b.data.msgId,rings:c})},!1)})(this);