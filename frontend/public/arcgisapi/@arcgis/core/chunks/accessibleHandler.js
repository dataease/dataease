/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
function n(){return function(n,e){if(!n[e])throw new TypeError(`Cannot auto bind undefined function '${e}'`);return{value:(t=n[e],function(n,...e){!function(n){const{type:e}=n;return n instanceof KeyboardEvent||"keyup"===e||"keydown"===e||"keypress"===e}(n)?t.call(this,n,...e):"Enter"!==n.key&&" "!==n.key||(n.preventDefault(),n.stopPropagation(),n.target.click())})};var t}}export{n as a};
