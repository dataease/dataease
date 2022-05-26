define(["build/buildControlDefault"], function(bc){
	// module:
	//		dojox/app/build/buildControlApp
	// summary:
	//		This module extend default build control module to add dojox/app build support
	// enhance buildControl
	bc.discoveryProcs.splice(0, 0, "dojox/app/build/discoverAppConfig");
	return bc;
});
