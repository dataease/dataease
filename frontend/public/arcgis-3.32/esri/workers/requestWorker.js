// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
/* jshint worker: true */
/* global self: true, postMessage: true */

/* REQUIRES esri/workers/mutableWorker */

function sendRequest(evt) {
    var args = evt.data;
    var method = args.method;
    var url = args.url;
    var options = args.options || {};
    var data = options.data;
    var async = !options.sync;
    var query = options.query;

    if(!url){
        //not a request, let another handler work with it
        return;
    }
    if(args.action){
        //we don't do action here. this is some else's
        return;
    }

    var xhr = new XMLHttpRequest();

    if(!data && query){
        url += '?'+query;
    }

    addEventListeners(xhr, args);
    xhr.open(method, url, async, options.user || undefined, options.password || undefined);

    if (options.withCredentials) {
        xhr.withCredentials = options.withCredentials;
    }

    var headers = options.headers;
    var contentType = 'application/x-www-form-urlencoded';
    if (headers) {
        for (var hdr in headers) {
            if (hdr.toLowerCase() === 'content-type') {
                contentType = headers[hdr];
            } else if (headers[hdr]) {
                //Only add header if it has a value. This allows for instance, skipping
                //insertion of X-Requested-With by specifying empty value.
                xhr.setRequestHeader(hdr, headers[hdr]);
            }
        }
    }

    if (contentType && contentType !== false) {
        xhr.setRequestHeader('Content-Type', contentType);
    }

    if (args.handleAs) {
        var handleAs = args.handleAs.toLowerCase();
        switch (handleAs) {
            case 'xml':
            case 'html':
                xhr.responseType = 'document';
                var mime = (handleAs == 'xml') ? 'text/xml' : 'text/html';
                xhr.overrideMimeType(mime);
                break;
            case 'json':
            case 'text':
            case 'blob':
            case 'arraybuffer':
                xhr.responseType = handleAs;
                break;
        }
    }
    postMessage({
        msgId: args.msgId,
        status: "progress",
        "url": url,
        headers: headers,
        data: data
    });
    xhr.send(data);
}

function addEventListeners(xhr, args) {
    function onError(evt) {
        var _xhr = evt.target;
        var error = new Error('Unable to load ' + args.url + ' status: ' + _xhr.status, args);
        postMessage({
            msgId: args.msgId,
            status: 'error',
            url: args.url,
            message: error.message/*,
            error: error*/
        });
    }

    function onProgress(evt) {
        var response = {
            msgId: args.msgId,
            status: 'progress',
            url: args.url
        };
        if (evt.lengthComputable) {
            response.loaded = evt.loaded;
            response.total = evt.total;
        }
        postMessage(response);
    }

    function onLoad(evt) {
        var _xhr = evt.target;
        var parsed = (_xhr.response instanceof Object) ? _xhr.response : parseResponse(_xhr, args.options.handleAs);
        var headerText = _xhr.getAllResponseHeaders();
        var keys = headerText.match(/\w+[\-]?\w+?\:\s/g);
        headerText = headerText.split('\n');
        var headers = keys.reduce(function(acc, val, ndx) {
            acc[val.split(':')[0]] = headerText[ndx].split(val)[1];
            return acc;
        }, {});
        postMessage({
            msgId: args.msgId,
            url: args.url,
            response: parsed,
            headers: headers,
            statusText: _xhr.statusText,
            status: _xhr.status,
            readyState: _xhr.readyState
        });
    }

    xhr.addEventListener('load', onLoad, false);
    xhr.addEventListener('error', onError, false);
    xhr.addEventListener('progress', onProgress, false);
}

function parseResponse(xhr, overrideParser) {
    var resp = xhr.responseText;
    var contentType = xhr.getResponseHeader('content-type');
    //TODO handle jsonp
    //json
    if (contentType.indexOf('json') > -1 || overrideParser == 'json') {
        try {
            resp =  JSON.parse(xhr.responseText);
        } catch (e){}
    }
    //xml
    else if ((contentType.indexOf('xml') > -1 || overrideParser == 'xml') && xhr.responseXML) {
        resp = xhr.responseXML;
    }
    return resp;
}
self.addEventListener('message', sendRequest);