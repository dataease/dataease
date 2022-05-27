// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../../../chunks/Zlib","../../../chunks/Jpg"],function(n,p){return function(){function l(){}l.decode=function(c){var a=new Uint8Array(c);c=new p.Jpg;c.parse(a);const {width:f,height:g,numComponents:q,eof:m}=c;c=c.getData(f,g,!0);let h;const k=f*g;let b=0;var e=0,d=0;if(m<a.length-1)for(a=(new n.Zlib(a.subarray(m))).getBytes(),h=new Uint8Array(k),b=e=0;b<a.length;b++)for(d=7;0<=d;d--)h[e++]=a[b]>>d&1;a=null;if(1===q)a=[c,c,c];else{a=[];for(b=0;3>b;b++)d=new Uint8Array(k),a.push(d);for(e=d=
0;e<k;e++)for(b=0;3>b;b++)a[b][e]=c[d++]}return{width:f,height:g,pixels:a,mask:h}};return l}()});