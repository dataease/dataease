import DojoExternalInterface;

class Storage{
	public static var SUCCESS = "success";
	public static var FAILED = "failed";
	public static var PENDING = "pending";
	
	//	Wait the following number of milliseconds before flushing
	public static var FLUSH_DELAY_DEFAULT = 500;
	
	public var flush_delay;
	public var so;
	public var timer;
	
	private var _NAMESPACE_KEY = "allNamespaces";
	
	public function Storage(){
		flush_delay = Storage.FLUSH_DELAY_DEFAULT;
	
		DojoExternalInterface.initialize();
		DojoExternalInterface.addCallback("put", this, put);
		DojoExternalInterface.addCallback("putMultiple", this, putMultiple);
		DojoExternalInterface.addCallback("get", this, get);
		DojoExternalInterface.addCallback("getMultiple", this, getMultiple);
		DojoExternalInterface.addCallback("showSettings", this, showSettings);
		DojoExternalInterface.addCallback("clear", this, clear);
		DojoExternalInterface.addCallback("getKeys", this, getKeys);
		DojoExternalInterface.addCallback("getNamespaces", this, getNamespaces);
		DojoExternalInterface.addCallback("remove", this, remove);
		DojoExternalInterface.addCallback("removeMultiple", this, removeMultiple);
		DojoExternalInterface.addCallback("flush", this, flush);
		DojoExternalInterface.addCallback("setFlushDelay", this, setFlushDelay);
		DojoExternalInterface.addCallback("getFlushDelay", this, getFlushDelay);
		DojoExternalInterface.loaded();
		
		// preload the System Settings finished button movie for offline
		// access so it is in the cache
		_root.createEmptyMovieClip("_settingsBackground", 1);
		_root._settingsBackground.loadMovie(DojoExternalInterface.dojoPath 
																				+ "../dojox/storage/storage_dialog.swf");
	}

  //  FIXME: Whoever added this Flush code did not document why it
  //  exists. Please also put your name and a bug number so I know 
  //  who to contact. -- Brad Neuberg
	
	//	Set a new value for the flush delay timer.
	//	Possible values:
	//	  0 : Perform the flush synchronously after each "put" request
	//	> 0 : Wait until 'newDelay' ms have passed without any "put" request to flush
	//	 -1 : Do not automatically flush
	public function setFlushDelay(newDelay){
		flush_delay = Number(newDelay);
	}
	
	public function getFlushDelay(){
		return String(flush_delay);
	}
	
	public function flush(namespace){
		if(timer){
			_global.clearTimeout(timer);
			delete timer;
		}
	
		var so = SharedObject.getLocal(namespace);
		var flushResults = so.flush();

		// return results of this command to JavaScript
		var statusResults;
		if(flushResults == true){
			statusResults = Storage.SUCCESS;
		}else if(flushResults == "pending"){
			statusResults = Storage.PENDING;
		}else{
			statusResults = Storage.FAILED;
		}
		
		DojoExternalInterface.call("dojox.storage._onStatus", statusResults, 
		                            null, namespace);
	}

	public function put(keyName, keyValue, namespace){
		// Get the SharedObject for these values and save it
		so = SharedObject.getLocal(namespace);
		
		//  Save the key and value
		so.data[keyName] = keyValue;
		
		// Save the namespace
		// FIXME: Tie this into the flush/no-flush stuff below; right now
		// we immediately write out this namespace. -- Brad Neuberg
    addNamespace(namespace, keyName);

		//	Do all the flush/no-flush stuff
		var keyNames = new Array(); 
		keyNames[0] = keyName;
		postWrite(so, keyNames, namespace);
	}
	
	public function putMultiple(metaKey, metaValue, metaLengths, namespace){
		// Get the SharedObject for these values and save it
		so = SharedObject.getLocal(namespace);
		
		//	Create array of keys and value lengths
		var keys = metaKey.split(",");
		var lengths = metaLengths.split(",");
		
		//	Loop through the array and write the values
		for(var i = 0; i < keys.length; i++){
			so.data[keys[i]] = metaValue.slice(0,lengths[i]);
			metaValue = metaValue.slice(lengths[i]);
		}
		
		// Save the namespace
		// FIXME: Tie this into the flush/no-flush stuff below; right now
		// we immediately write out this namespace. -- Brad Neuberg
    addNamespace(namespace, null);
		
		//	Do all the flush/no-flush stuff
		postWrite(so, keys, namespace);
	}

	public function postWrite(so, keyNames, namespace){
		//	TODO: Review all this 'handler' stuff. In particular, the flush 
		//  could now be with keys pending from several different requests, not 
		//  only the ones passed in this method call

		// prepare a storage status handler
		var self = this;
		so.onStatus = function(infoObject:Object){
			//trace("onStatus, infoObject="+infoObject.code);
			
			// delete the data value if the request was denied
			if(infoObject.code == "SharedObject.Flush.Failed"){
				for(var i=0;i<keyNames.length;i++){
					delete self.so.data[keyNames[i]];
				}
			}
			
			var statusResults;
			if(infoObject.code == "SharedObject.Flush.Failed"){
				statusResults = Storage.FAILED;
			}else if(infoObject.code == "SharedObject.Flush.Pending"){
				statusResults = Storage.PENDING;
			}else if(infoObject.code == "SharedObject.Flush.Success"){
				// if we have succeeded saving our value, see if we
				// need to update our list of namespaces
				if(self.hasNamespace(namespace) == true){
					statusResults = Storage.SUCCESS;
				}else{
					// we have a new namespace we must store
					self.addNamespace(namespace, keyNames[0]);
					return;
				}
			}
			//trace("onStatus, statusResults="+statusResults);
			
			// give the status results to JavaScript
			DojoExternalInterface.call("dojox.storage._onStatus", statusResults, 
			                            keyNames[0], namespace);
		}
		
		//	Clear any pending flush timers
		if(timer){
			_global.clearTimeout(timer);
		}
		
		//	If we have a flush delay set, set a timer for its execution
		if(flush_delay > 0){
			timer = _global.setTimeout(flush, flush_delay, namespace);
		//	With a flush_delay value of 0, execute the flush request synchronously
		}else if(flush_delay == 0){
			flush(namespace);
		}
		//	Otherwise just don't flush - will be probably be flushed manually
	}

	public function get(keyName, namespace){
		// Get the SharedObject for these values and save it
		so = SharedObject.getLocal(namespace);
		var results = so.data[keyName];
		
		return results;
	}
	
	//	Returns an array with the contents of each key value on the metaKeys array
	public function getMultiple(metaKeys, namespace){
		//	get the storage object
		so = SharedObject.getLocal(namespace);
		
		//	Create array of keys to read
		var keys = metaKeys.split(",");
		var results = new Array();
		
		//	Read from storage into results array
		for(var i = 0;i < keys.length;i++){
			var val = so.data[keys[i]];
			val = val.split("\\").join("\\\\");
			val = val.split('"').join('\\"');
			results.push( val);
		}
			
		//	Make the results array into a string
		var metaResults = '["' + results.join('","') + '"]';
		
		return metaResults;
	}	
	
	public function showSettings(){
		// Show the configuration options for the Flash player, opened to the
		// section for local storage controls (pane 1)
		System.showSettings(1);
		
		// there is no way we can intercept when the Close button is pressed, allowing us
		// to hide the Flash dialog. Instead, we need to load a movie in the
		// background that we can show a close button on.
		_root.createEmptyMovieClip("_settingsBackground", 1);
		_root._settingsBackground.loadMovie(DojoExternalInterface.dojoPath 
																				+ "../dojox/storage/storage_dialog.swf");
	}
	
	public function clear(namespace){
		so = SharedObject.getLocal(namespace);
		so.clear();
		so.flush();
		
		// remove this namespace entry now
		removeNamespace(namespace);
	}
	
	public function getKeys(namespace) : String{
		// Returns a list of the available keys in this namespace
		
		// get the storage object
		so = SharedObject.getLocal(namespace);
		// get all of the keys
		var results = [];
		for(var i in so.data){
			results.push(i);	
		}
		
		// remove our key that records our list of namespaces
		for(var i = 0; i < results.length; i++){
			if(results[i] == _NAMESPACE_KEY){
				results.splice(i, 1);
				break;
			}
		}
		
		// a bug in ExternalInterface transforms Arrays into
		// Strings, so we can't use those here! -- BradNeuberg
		results = results.join(",");
		
		return results;
	}
	
	public function getNamespaces() : String{
		var allNamespaces = SharedObject.getLocal(_NAMESPACE_KEY);
		var results = [];
		
		for(var i in allNamespaces.data){
			results.push(i);
		}
		
		// a bug in ExternalInterface transforms Arrays into
		// Strings, so we can use those here! -- BradNeuberg
		results = results.join(",");
		
		return results;
	}
	
	public function remove(keyName, namespace){
		// Removes a key

		// get the storage object
		so = SharedObject.getLocal(namespace);
		
		// delete this value
		delete so.data[keyName];
		
		// save the changes
		so.flush();
		
		// see if we are the last entry for this namespace
		var availableKeys = getKeys(namespace);
		if(availableKeys == ""){
			// we are empty
			removeNamespace(namespace);
		}
	}
	
	//	Removes all the values for each keys on the metaKeys array
	public function removeMultiple(metaKeys, namespace){		
		//	get the storage object
		so = SharedObject.getLocal(namespace);
		
		//	Create array of keys to read
		var keys = metaKeys.split(",");
		var results = new Array();

		//	Delete elements
		for(var i=0;i<keys.length;i++){
			delete so.data[keys[i]];
		}

		// see if there are no more entries for this namespace
		var availableKeys = getKeys(namespace);
		if(availableKeys == ""){
			// we are empty
			removeNamespace(namespace);
		}
	}
	
	private function hasNamespace(namespace):Boolean{
		// Get the SharedObject for the namespace list
		var allNamespaces = SharedObject.getLocal(_NAMESPACE_KEY);
		
		var results = false;
		for(var i in allNamespaces.data){
			if(i == namespace){
				results = true;
				break;
			}
		}
		
		return results;
	}
	
	// FIXME: This code has gotten ugly -- refactor
	private function addNamespace(namespace, keyName){
		if(hasNamespace(namespace) == true){
			return;
		}
		
		// Get the SharedObject for the namespace list
		var allNamespaces = SharedObject.getLocal(_NAMESPACE_KEY);
		
		// prepare a storage status handler if the keyName is
		// not null
		if(keyName != null && typeof keyName != "undefined"){
			var self = this;
			allNamespaces.onStatus = function(infoObject:Object){
				// delete the data value if the request was denied
				if(infoObject.code == "SharedObject.Flush.Failed"){
					delete self.so.data[keyName];
				}
				
				var statusResults;
				if(infoObject.code == "SharedObject.Flush.Failed"){
					statusResults = Storage.FAILED;
				}else if(infoObject.code == "SharedObject.Flush.Pending"){
					statusResults = Storage.PENDING;
				}else if(infoObject.code == "SharedObject.Flush.Success"){
					statusResults = Storage.SUCCESS;
				}
				
				// give the status results to JavaScript
				DojoExternalInterface.call("dojox.storage._onStatus", statusResults, 
				                            keyName, namespace);
			}
		}
		
		// save the namespace list
		allNamespaces.data[namespace] = true;
		var flushResults = allNamespaces.flush();
		
		// return results of this command to JavaScript
		if(keyName != null && typeof keyName != "undefined"){
			var statusResults;
			if(flushResults == true){
				statusResults = Storage.SUCCESS;
			}else if(flushResults == "pending"){
				statusResults = Storage.PENDING;
			}else{
				statusResults = Storage.FAILED;
			}
			
			DojoExternalInterface.call("dojox.storage._onStatus", statusResults, 
			                            keyName, namespace);
		}
	}
	
	// FIXME: This code has gotten ugly -- refactor
	private function removeNamespace(namespace){
		if(hasNamespace(namespace) == false){
			return;
		}
		
		// try to save the namespace list; don't have a return
		// callback; if we fail on this, the worst that will happen
		// is that we have a spurious namespace entry
		var allNamespaces = SharedObject.getLocal(_NAMESPACE_KEY);
		delete allNamespaces.data[namespace];
		allNamespaces.flush();
	}

	static function main(mc){
		_root.app = new Storage(); 
	}
}

