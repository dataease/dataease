/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
function a(a,h,s=!1){let{hasM:t,hasZ:e}=a;Array.isArray(h)?4!==h.length||t||e?3===h.length&&s&&!t?(e=!0,t=!1):3===h.length&&t&&e&&(t=!1,e=!1):(t=!0,e=!0):(e=!e&&h.hasZ&&(!t||h.hasM),t=!t&&h.hasM&&(!e||h.hasZ)),a.hasZ=e,a.hasM=t}export{a as u};
