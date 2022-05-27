// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(f){function*d(c,a){if(!(c>=a)){var b=c+Math.floor((a-c)/2);yield b;c=d(c,b);for(a=d(b+1,a);;){b=c.next();const e=a.next();if(b.done&&e.done)break;b.done||(yield b.value);e.done||(yield e.value)}}}f.getIndices=function(c,a,b=[]){if(c>=a)return b;b.push(c);if(2>a-c)return b;--a;b.push(a);for(c=d(c+1,a);;){a=c.next();if(a.done)break;b.push(a.value)}return b};Object.defineProperty(f,"__esModule",{value:!0})});