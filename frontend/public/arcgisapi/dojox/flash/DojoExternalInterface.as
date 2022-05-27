/**
	A wrapper around Flash 8's ExternalInterface; this is needed
	because ExternalInterface has a number of serialization bugs that we 
	need to correct for.
	
	@author Brad Neuberg
*/

import flash.external.ExternalInterface;

class DojoExternalInterface{
	public static var available:Boolean;
	public static var dojoPath = "";
		
	public static function initialize(){
		//trace("DojoExternalInterface.initialize");
		
		// extract the dojo base path
		DojoExternalInterface.dojoPath = DojoExternalInterface.getDojoPath();
		
		// see if we need to do an express install
		var install:ExpressInstall = new ExpressInstall();
		if(install.needsUpdate){
			install.init();
		}
		
		// set whether communication is available
		DojoExternalInterface.available = ExternalInterface.available;
		
		// make sure we can play nice in XD settings
		// Note, this has been commented out as it may expose a cross domain sec issue
		//System.security.allowDomain(unescape(_root.xdomain));
	}
	
	/** Called when we are finished adding methods through addCallback. */
	public static function done(){
		//trace("done");
		DojoExternalInterface.call("dojox.flash.loaded");
	}
	
	public static function addCallback(methodName:String, instance:Object, 
									   method:Function):Boolean{
		//trace("addCallback");
		ExternalInterface.addCallback(methodName, instance, function(){
			instance = (instance) ? instance : null;
			var params = [];
			if(arguments && arguments.length){
				for(var i = 0; i < arguments.length; i++){
					params[i] = DojoExternalInterface.decodeData(arguments[i]);
				}
			}
			
			var results = method.apply(instance, params);
			results = DojoExternalInterface.encodeData(results);
			
			return results;
		});
		
		// tell JavaScript about DojoExternalInterface new method so we can create a proxy
		ExternalInterface.call("dojox.flash.comm._addExternalInterfaceCallback", 
													 methodName);
													 
		return true;
	}
	
	public static function call(methodName:String):Void{
		// we might have any number of optional arguments, so we have to 
		// pass them in dynamically; strip out the results callback
		var parameters = new Array();
		for(var i = 0; i < arguments.length; i++){
			parameters.push(arguments[i]);
		}
		
		// FIXME: Should we be encoding or decoding the data to get
		// around Flash's serialization bugs?
		
		var results = ExternalInterface.call.apply(ExternalInterface, parameters);
		
		return results;
	}
	
	/** 
		Called by Flash to indicate to JavaScript that we are ready to have
		our Flash functions called. Calling loaded()
		will fire the dojox.flash.loaded() event, so that JavaScript can know that
		Flash has finished loading and adding its callbacks, and can begin to
		interact with the Flash file.
	*/
	public static function loaded(){
		DojoExternalInterface.call("dojox.flash.loaded");
	}
	
	/**
		Utility trace implementation that prints out to console.debug.
	*/
	public static function trace(msg){
		DojoExternalInterface.call("console.debug", "FLASH: " + msg);
	}
	
	private static function decodeData(data):String{
		if(!data || typeof data != "string"){
			return data;
		}
		
		//      JAC: Using unicode character 0001 to store instead of Unicode null 
		//      which causes trouble 
		data = replaceStr(data, "&custom_null;", "\u0001");
		
		// we have to use custom encodings for certain characters when passing
		// them over; for example, passing a backslash over as //// from JavaScript
		// to Flash doesn't work
		data = replaceStr(data, "&custom_backslash;", "\\");
		
		return data;
	}
	
	private static function encodeData(data):String{	
		if(!data || typeof data != "string"){
			return data;
		}
		
		// double encode all entity values, or they will be mis-decoded 
		// by Flash when returned 
		data = replaceStr(data, "&", "&amp;");
		
		// certain XMLish characters break Flash's wire serialization for
		// ExternalInterface; encode these into a custom encoding, rather than
		// the standard entity encoding, because otherwise we won't be able to
		// differentiate between our own encoding and any entity characters
		// that are being used in the string itself
		data = replaceStr(data, '<', '&custom_lt;');
		data = replaceStr(data, '>', '&custom_gt;');
		
		// needed for IE
		data = replaceStr(data, '\\', '&custom_backslash;');
		data = replaceStr(data, "\u0001", "&custom_null;");
		
		// encode control characters and JavaScript delimiters
		data = replaceStr(data, "\n", "\\n");
		data = replaceStr(data, "\r", "\\r");
		data = replaceStr(data, "\f", "\\f");
		data = replaceStr(data, "'", "\\'");
		data = replaceStr(data, '"', '\"');
		
		return data;
	}
	
	/** 
			Flash ActionScript has no String.replace method or support for
			Regular Expressions! We roll our own very simple one.
	*/
	public static function replaceStr(inputStr:String, replaceThis:String, 
										withThis:String):String{
		var splitStr = inputStr.split(replaceThis);
		if(!splitStr){
			return inputStr;
		}
		
		inputStr = splitStr.join(withThis);
		return inputStr;
	}
	
	private static function getDojoPath(){
		var url = _root._url;
		var start = url.indexOf("baseUrl=") + "baseUrl=".length;
		var path = url.substring(start);
		var end = path.indexOf("&");
		if(end != -1){
			path = path.substring(0, end);
		}
		
		// some browsers append a junk string at the end: '%20'%20quality=
		if(path.indexOf("'%20'%20quality=") != -1){
			path = path.substring(0, path.indexOf("'%20'%20quality="));
		}
		return unescape(path);
	}
}
