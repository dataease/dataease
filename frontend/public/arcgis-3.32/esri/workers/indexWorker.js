// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
/* jshint worker: true */
/* global self: true, postMessage: true */

/* REQUIRES esri/workers/scripts/indexInterface */

/* global Indexer: false, geomToBbox: false, merge: false */

(function(context) {
    var self = context;
    var index;
    var system;

    function createIndex(msg) {
        try {
            index = new Indexer(msg);
            system = index.system;
            postMessage({
                msgId: msg.msgId,
                insert: msg.data && msg.data.length
            });
        } catch (err) {
            postMessage({
                msgId: msg.msgId,
                status: 'error',
                message: err.message
            });
        }
    }

    function updateIndex(msg) {
        if (msg.insert || msg.update) {
            if (!index) { //no indexer instance got created yet
                createIndex({
                    'data': msg.insert || msg.update,
                    'system': msg.system, //if this is null or undefined, Indexer will use default
                    'indexOptions': msg.options,
                    'idField': msg.idField,
                    'layerId': msg.layerId
                });
            } else if (!index.index) { //indexer instance but no index created yet
                index.create(msg.insert || msg.update, merge({
                    layerId: msg.layerId
                }, msg.options));
            } else { //we have an indexer with an active index
                if(msg.insert){
                    var len = msg.insert.length;
                    while (len--) {
                        index.insert(msg.insert[len], null, msg.layerId);
                    }
                } //TODO Support Update
                  /*else {
                   var len = msg.insert.length;
                    while (len--) {
                        index.search()
                    } 
                }*/
            }
            postMessage({
                msgId: msg.msgId,
                insert: msg.insert.length
            });
        } else if (msg.remove) {
            if (!index || !index.index) {
                postMessage({
                    msgId: msg.msgId,
                    status: 'error',
                    message: 'no active index to remove from'
                });
            } else {
                msg.remove.forEach(index.remove, index);
            }
            postMessage({
                msgId: msg.msgId,
                remove: msg.remove.length
            });
        }
    }

    function search(msg) {
        if (!index || !index.index) {
            postMessage({
                msgId: msg.msgId,
                status: 'error',
                message: 'no active index to search'
            });
        } else {
            var layer = msg.layerId;
            var results = index.search(msg.search, msg.returnNode);
            var found=[], len=results.length, item;
            if(!layer){
                found = results;
            } else {
                while(len--){
                    item = results[len];
                    if(item.layerId === layer){
                        found.push(item);
                    }
                }
            }
            postMessage({
                msgId: msg.msgId,
                'results': found
            });
        }
    }

    function getIndex(msg) {
        if (!index || !index.index) {
            postMessage({
                msgId: msg.msgId,
                status: 'error',
                message: 'no active index to serialize'
            });
        } else {
            var indexJson = index.serialize();
            postMessage({
                msgId: msg.msgId,
                index: indexJson
            });
        }
    }

    function loadIndex(msg) {
        index = new Indexer({
            system: msg.system
        });
        try {
            index.load(msg.index);
            postMessage({
                msgId: msg.msgId
            });
        } catch (err) {
            postMessage({
                msgId: msg.msgId,
                status: 'error',
                message: err.message
            });
        }
    }

    function messageHandler(evt) {
        var msg = evt.data || {};
        if (msg.index) {
            loadIndex(msg);
        } else if (msg.insert || msg.remove || msg.update) {
            updateIndex(msg);
        } else if ((msg.data && Array.isArray(msg.data)) || msg.system) {
            createIndex(msg);
        } else if (msg.search) {
            search(msg);
        } else if (msg.action && msg.action == 'getIndex') {
            getIndex(msg);
        }
    }

    self.addEventListener('message', messageHandler, false);

    self.searchIndex = function(criteria, returnNodes){
        return index.search(criteria, returnNodes);
    };

    self.main = function(msg) {
        var response = msg.response;
        var inserts;
        if (response && response.features) {
            var features = response.features;
            if (!features[0].geometry) {
                //returned without geometry, can't do anything
                return true;
            }
            //if points then don't need to modify
            //otherwise convert geoms to bbox
            if (!features[0].geometry.x && !features[0].geometry.y) {
                inserts = features.map(function(feat) {
                    var item = geomToBbox(feat.geometry);
                    item.id = feat.attributes[response.objectIdFieldName];
                    return item;
                });
            } else {
                inserts = features.map(function(feat) {
                    feat.geometry.id = feat.attributes[response.objectIdFieldName];
                    return feat.geometry;
                });
            }
            updateIndex({
                'inserts': inserts
            });
        }
    };
})(self);