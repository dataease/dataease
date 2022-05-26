/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/cookie",["./_base/kernel","./regexp"],function(c,h){c.cookie=function(c,f,b){var a=document.cookie,e;if(1==arguments.length)e=(e=a.match(new RegExp("(?:^|; )"+h.escapeString(c)+"\x3d([^;]*)")))?decodeURIComponent(e[1]):void 0;else{b=b||{};a=b.expires;if("number"==typeof a){var d=new Date;d.setTime(d.getTime()+864E5*a);a=b.expires=d}a&&a.toUTCString&&(b.expires=a.toUTCString());f=encodeURIComponent(f);var a=c+"\x3d"+f,g;for(g in b)a+="; "+g,d=b[g],!0!==d&&(a+="\x3d"+d);document.cookie=
a}return e};c.cookie.isSupported=function(){"cookieEnabled"in navigator||(this("__djCookieTest__","CookiesAllowed"),navigator.cookieEnabled="CookiesAllowed"==this("__djCookieTest__"),navigator.cookieEnabled&&this("__djCookieTest__","",{expires:-1}));return navigator.cookieEnabled};return c.cookie});