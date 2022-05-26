// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
/* eslint-env worker */

var globalId = 0;
var outgoing = new Map();
var configured = false;

// to handshake with worker the main thread
var HANDSHAKE = 0;
// to configure the worker
var CONFIGURE = 1;
// for worker to indicated it's configured
var CONFIGURED = 2;
// to open a connection
var OPEN = 3;
// response a connection
var OPENED = 4;
// to send a response to a call
var RESPONSE = 5;
// to invoke a method on the other side
var INVOKE = 6;
// to cancel a call
var CANCEL = 7;

function mapDelete(map, key) {
  /*eslint-disable */
  map["delete"](key);
  /*eslint-enable */
}

function receiveMessage(event) {
  if (!event || !event.data) {
    return null;
  }
  if (typeof event.data === "string") {
    return JSON.parse(event.data);
  }
  return event.data;
}

/*eslint-disable */
function invokeStaticMessage(methodName, data) {
  // Deferred has already been loaded at this point
  var Deferred = require("dojo/Deferred");
  var jobId = globalId++;

  var deferred = new Deferred(function(reason) {
    // post a cancel message in order to cancel on the main thread
    self.postMessage({
      type: CANCEL,
      methodName: methodName,
      jobId: jobId
    });

    mapDelete(outgoing, jobId);
  });

  outgoing.set(jobId, deferred);

  // post to main thread
  self.postMessage({
    type: INVOKE,
    methodName: methodName,
    jobId: jobId,
    data: data
  });

  return deferred.promise;
}
/*eslint-enable */

function messageHandler(event /* FmkMessageEvent */) {
  var message = receiveMessage(event);

  if (!message) {
    return;
  }

  var jobId = message.jobId;

  switch (message.type) {

    // Configure the AMD loader
    case CONFIGURE:
      var configuration = message.configure;

      if (configured) {
        return;
      }

      self.dojoConfig = configuration.loaderConfig;
      self.importScripts(configuration.loaderUrl);

      if (typeof require.config === "function") {
        require.config(configuration.loaderConfig);
      }

      require([
        "esri/config"
      ], function(esriConfig) {
        for (var name in configuration.esriConfig) {
          if (Object.prototype.hasOwnProperty.call(configuration.esriConfig, name)) {
            esriConfig[name] = configuration.esriConfig[name];
          }
        }
        self.postMessage({
          type: CONFIGURED
        });
      });
      break;

  // Loads a module
  case OPEN:
    var modulePath = message.modulePath;

    require([
      "esri/layers/vectorTiles/core/workers/RemoteClient",
      modulePath
    ],
    function(RemoteClient, Module) {
      var port = RemoteClient.connect(Module);

      self.postMessage({
        type: OPENED,
        jobId: jobId,
        data: port
      }, [port]);
    });
    break;

  // response to a static message
  case RESPONSE:
    if (outgoing.has(jobId)) {
      var deferred = outgoing.get(jobId);

      mapDelete(outgoing, jobId);

      if (message.error) {
        deferred.reject(JSON.parse(message.error));
      }
      else {
        deferred.resolve(message.data);
      }
    }

    break;
  }
}

self.addEventListener("message", messageHandler);

// Handshake with the main thread
self.postMessage({ type: HANDSHAKE });
