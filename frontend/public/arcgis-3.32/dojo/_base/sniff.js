/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/_base/sniff",["./kernel","./lang","../sniff"],function(b,c,a){b._name="browser";c.mixin(b,{isBrowser:!0,isFF:a("ff"),isIE:a("ie"),isKhtml:a("khtml"),isWebKit:a("webkit"),isMozilla:a("mozilla"),isMoz:a("mozilla"),isOpera:a("opera"),isSafari:a("safari"),isChrome:a("chrome"),isMac:a("mac"),isIos:a("ios"),isAndroid:a("android"),isWii:a("wii"),isQuirks:a("quirks"),isAir:a("air")});return a});