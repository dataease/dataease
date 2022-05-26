// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
/* jshint worker: true */
/* global self: true, postMessage: true */
(function(context) {
  var self = context;

  function actionHandler(evt) {
    var msg = evt.data;
    var error, success;
    if (msg.action) {
      switch(msg.action) {
        case 'import-script':
          try {
            if (!Array.isArray(msg.url)) {
              msg.url = [msg.url];
            }
            self.importScripts.apply(self, msg.url);
            success = true;
          } catch (err) {
            error = err;
            postMessage({msgId:msg.msgId, urls:msg.url, status: 'debug', message: 'import failed - '+err.message});
          }
          break;
        case 'add-callback':
          try {
            self.importScripts(msg.url);
            var cb = self[msg.cbName || 'main'];
            if (!cb) {
              error = {
                message: (msg.cbName || 'main') + ' was not found in ' + msg.url
              };
              break;
            }
            self.postMessage = (function(origPostMessage) {
              return function(msg, transfers) {
                if (cb(msg) !== false) { 
                  /*stupid IE can't handle undefined/null transfers argument*/
                  if (transfers) {
                    origPostMessage(msg, transfers);
                  } else {
                    origPostMessage(msg);
                  }
                }
              };
            })(self.postMessage);
            success = true;
          } catch (err) {
            error = err;
          }
          break;
      }
      if (success) {
        var pbMsg = {
          msgId: msg.msgId,
          success: true,
          action: msg.action,
          actionUrl: msg.url
        };
        if (msg.action == 'add-callback') {
          pbMsg.cbName = (msg.cbName || 'main');
        }
        postMessage(pbMsg);
      } else if (error) {
        postMessage({
          status: 'error',
          msgId: msg.msgId,
          message: error.message,
          action: msg.action
        });
      }
    }
  }
  if (!self.__mutable) {
    self.addEventListener('message', actionHandler, false);
  }
  self.__mutable = true;
})(self);