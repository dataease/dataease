/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
const h={width:600,height:400};function t(t,i){i=i||h;let{width:e,height:d}=i;const g=e/d;return g<1.5?d=e/1.5:g>1.5&&(e=1.5*d),e>t.width&&(d*=t.width/e,e=t.width),d>t.height&&(e*=t.height/d,d=t.height),{width:Math.round(e),height:Math.round(d)}}export{t as g};
