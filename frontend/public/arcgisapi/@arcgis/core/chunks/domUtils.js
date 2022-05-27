/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
function e(e){return"string"==typeof e?document.getElementById(e):e}function t(e){for(;e.hasChildNodes();)e.removeChild(e.firstChild)}function n(e,t){const n=t.parentNode;n&&n.insertBefore(e,t)}function o(e,t){for(;;){const n=e.firstChild;if(!n)break;t.appendChild(n)}}function r(e){e.parentNode&&e.parentNode.removeChild(e)}const s=(()=>{if("function"==typeof Element.prototype.closest)return(e,t)=>e.closest(t);const e=Element.prototype.matches||Element.prototype.msMatchesSelector;return(t,n)=>{let o=t;do{if(e.call(o,n))return o;o=o.parentElement}while(null!==o&&1===o.nodeType);return null}})();export{r as a,e as b,s as c,t as e,n as i,o as r};
