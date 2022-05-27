define(['../maybe', '../Error', '../events', '../promiseUtils', './registry', './utils', '../../views/support/Scheduler'], function (esri_core_maybe, esri_core_Error, esri_core_events, esri_core_promiseUtils, esri_core_workers_registry, esri_core_workers_utils, esri_views_support_Scheduler) { 'use strict';

    const {
      CLOSE,
      ABORT,
      INVOKE,
      RESPONSE,
      OPEN_PORT,
      ON
    } = esri_core_workers_utils.MessageType;

    let InvokeMessageQueue = function () {
      function InvokeMessageQueue(invoke) {
        this._timer = null;
        this._cancelledJobIds = new Set();
        this._invokeMessages = [];
        this._invoke = invoke;
        this._timer = null;
        this._process = this._process.bind(this);
      }

      var _proto = InvokeMessageQueue.prototype;

      _proto.push = function push(message) {
        if (message.type === esri_core_workers_utils.MessageType.ABORT) {
          this._cancelledJobIds.add(message.jobId);
        } else {
          this._invokeMessages.push(message);

          if (this._timer === null) {
            this._timer = setTimeout(this._process, 0);
          }
        }
      };

      _proto.clear = function clear() {
        this._invokeMessages.length = 0;

        this._cancelledJobIds.clear();

        this._timer = null;
      };

      _proto._process = function _process() {
        this._timer = null;

        for (const message of this._invokeMessages) {
          if (!this._cancelledJobIds.has(message.jobId)) {
            this._invoke(message);
          }
        }

        this._cancelledJobIds.clear();

        this._invokeMessages.length = 0;
      };

      return InvokeMessageQueue;
    }();

    let RemoteClient = function () {
      function RemoteClient(_port, options) {
        this._port = _port;
        this._outJobs = new Map();
        this._inJobs = new Map();
        this._invokeQueue = new InvokeMessageQueue(message => this._onInvokeMessage(message));
        this._messageQueue = new Array();
        this._client = options.client;
        this._onMessage = this._onMessage.bind(this);
        this._channel = options.channel;

        if (options.scheduler) {
          this._frameTask = options.scheduler.registerTask(esri_views_support_Scheduler.Task.REMOTE_CLIENT, budget => this._update(budget), () => this._messageQueue.length > 0);
        }

        this._port.addEventListener("message", this._onMessage);

        this._port.start();
      }

      RemoteClient.connect = function connect(Module) {
        const channel = new MessageChannel();
        let client;

        if (typeof Module === "function") {
          client = new Module();
        } else if ("default" in Module && typeof Module.default === "function") {
          client = new Module.default();
        } else {
          client = Module;
        }

        const remoteClient = new RemoteClient(channel.port1, {
          channel,
          client
        });

        if (typeof client === "object" && "remoteClient" in client) {
          client.remoteClient = remoteClient;
        }

        RemoteClient.clients.set(remoteClient, client);
        return channel.port2;
      };

      RemoteClient.loadWorker = function loadWorker(moduleId) {
        const loader = esri_core_workers_registry.registry[moduleId];
        return loader ? loader() : esri_core_promiseUtils.resolve(null);
      };

      var _proto2 = RemoteClient.prototype;

      _proto2.close = function close() {
        this._post({
          type: CLOSE
        });

        this._close();
      };

      _proto2.isBusy = function isBusy() {
        return this._outJobs.size > 0;
      };

      _proto2.invoke = function invoke(methodName, data, options) {
        const signal = options && options.signal;
        const transferList = options && options.transferList;

        if (!this._port) {
          return esri_core_promiseUtils.reject(new esri_core_Error("worker:port-closed", `Cannot call invoke('${methodName}'), port is closed`, {
            methodName,
            data
          }));
        }

        const jobId = esri_core_workers_utils.newJobId();
        return esri_core_promiseUtils.create((resolve, reject) => {
          const abortHandle = esri_core_promiseUtils.onAbortOrThrow(signal, () => {
            var _outJob$abortHandle;

            const outJob = this._outJobs.get(jobId);

            if (!outJob) {
              return;
            }

            this._outJobs.delete(jobId);

            (_outJob$abortHandle = outJob.abortHandle) == null ? void 0 : _outJob$abortHandle.remove();

            this._post({
              type: ABORT,
              jobId
            });

            reject(esri_core_promiseUtils.createAbortError());
          });
          const job = {
            resolve,
            reject,
            abortHandle,
            debugInfo: methodName
          };

          this._outJobs.set(jobId, job);

          this._post({
            type: INVOKE,
            jobId,
            methodName,
            abortable: signal != null
          }, data, transferList);
        });
      };

      _proto2.on = function on(eventType, listener) {
        const channel = new MessageChannel();

        this._port.postMessage({
          type: esri_core_workers_utils.MessageType.ON,
          eventType,
          port: channel.port2
        }, [channel.port2]);

        function eventHandler(message) {
          listener(message.data);
        }

        channel.port1.addEventListener("message", eventHandler);
        channel.port1.start();
        return {
          remove() {
            channel.port1.postMessage({
              type: esri_core_workers_utils.MessageType.CLOSE
            });
            channel.port1.close();
            channel.port1.removeEventListener("message", eventHandler);
          }

        };
      };

      _proto2.openPort = function openPort() {
        const channel = new MessageChannel();

        this._post({
          type: OPEN_PORT,
          port: channel.port2
        });

        return channel.port1;
      };

      _proto2._close = function _close() {
        this._channel && (this._channel = null);

        this._port.removeEventListener("message", this._onMessage);

        this._port.close();

        this._outJobs.forEach(job => {
          var _job$abortHandle;

          (_job$abortHandle = job.abortHandle) == null ? void 0 : _job$abortHandle.remove();
          job.reject(esri_core_promiseUtils.createAbortError(`Worker closing, aborting job calling '${job.debugInfo}'`));
        });

        this._inJobs.clear();

        this._outJobs.clear();

        this._invokeQueue.clear();

        this._port = this._client = null;
        this._frameTask = esri_core_maybe.removeMaybe(this._frameTask);
      };

      _proto2._onMessage = function _onMessage(event) {
        if (esri_core_maybe.isSome(this._frameTask)) {
          this._messageQueue.push(event);
        } else {
          this._processMessage(event);
        }
      };

      _proto2._processMessage = function _processMessage(event) {
        const message = esri_core_workers_utils.receiveMessage(event);

        if (!message) {
          return;
        }

        switch (message.type) {
          case RESPONSE:
            this._onResponseMessage(message);

            break;

          case INVOKE:
            this._invokeQueue.push(message);

            break;

          case ABORT:
            this._onAbortMessage(message);

            break;

          case CLOSE:
            this._onCloseMessage();

            break;

          case OPEN_PORT:
            this._onOpenPortMessage(message);

            break;

          case ON:
            this._onOnMessage(message);

            break;
        }
      };

      _proto2._onAbortMessage = function _onAbortMessage(message) {
        const inJobs = this._inJobs;
        const jobId = message.jobId;
        const job = inJobs.get(jobId);

        this._invokeQueue.push(message);

        if (!job) {
          return;
        }

        if (job.controller) {
          job.controller.abort();
        }

        inJobs.delete(jobId);
      };

      _proto2._onCloseMessage = function _onCloseMessage() {
        const client = this._client;

        this._close();

        if (client && "destroy" in client && RemoteClient.clients.get(this) === client) {
          client.destroy();
        }

        RemoteClient.clients.delete(this);

        if (client && client.remoteClient) {
          client.remoteClient = null;
        }
      };

      _proto2._onInvokeMessage = function _onInvokeMessage(message) {
        const {
          methodName,
          jobId,
          data,
          abortable
        } = message;
        const controller = abortable ? esri_core_promiseUtils.createAbortController() : null;
        const inJobs = this._inJobs;
        let thisArgs = this._client;
        let func = thisArgs[methodName];
        let resultOrPromise;

        try {
          if (!func && methodName && methodName.indexOf(".") !== -1) {
            const split = methodName.split(".");

            for (let i = 0; i < split.length - 1; i++) {
              thisArgs = thisArgs[split[i]];
              func = thisArgs[split[i + 1]];
            }
          }

          if (typeof func !== "function") {
            throw new TypeError(`${methodName} is not a function`);
          }

          resultOrPromise = func.call(thisArgs, data, {
            client: this,
            signal: controller ? controller.signal : null
          });
        } catch (error) {
          this._post({
            type: RESPONSE,
            jobId,
            error: esri_core_workers_utils.toInvokeError(error)
          });

          return;
        }

        if (!esri_core_promiseUtils.isPromiseLike(resultOrPromise)) {
          this._post({
            type: RESPONSE,
            jobId
          }, resultOrPromise);
        } else {
          inJobs.set(jobId, {
            controller,
            promise: resultOrPromise
          });
          resultOrPromise.then(result => {
            if (!inJobs.has(jobId)) {
              return;
            }

            inJobs.delete(jobId);

            this._post({
              type: RESPONSE,
              jobId
            }, result);
          }, error => {
            if (!inJobs.has(jobId)) {
              return;
            }

            inJobs.delete(jobId);

            if (!esri_core_promiseUtils.isAbortError(error)) {
              this._post({
                type: RESPONSE,
                jobId,
                error: esri_core_workers_utils.toInvokeError(error || {
                  message: `Error encountered at method ${methodName}`
                })
              });
            }
          });
        }
      };

      _proto2._onOpenPortMessage = function _onOpenPortMessage(message) {
        new RemoteClient(message.port, {
          client: this._client
        });
      };

      _proto2._onOnMessage = function _onOnMessage(message) {
        const {
          port
        } = message;

        const handle = this._client.on(message.eventType, event => {
          port.postMessage(event);
        });

        const closeHandle = esri_core_events.on(message.port, "message", event => {
          const message = esri_core_workers_utils.receiveMessage(event);

          if (message.type === esri_core_workers_utils.MessageType.CLOSE) {
            closeHandle.remove();
            handle.remove();
            port.close();
          }
        });
      };

      _proto2._onResponseMessage = function _onResponseMessage(message) {
        var _job$abortHandle2;

        const {
          jobId,
          error,
          data
        } = message;
        const outJobs = this._outJobs;

        if (!outJobs.has(jobId)) {
          return;
        }

        const job = outJobs.get(jobId);
        outJobs.delete(jobId);
        (_job$abortHandle2 = job.abortHandle) == null ? void 0 : _job$abortHandle2.remove();

        if (error) {
          job.reject(esri_core_Error.fromJSON(JSON.parse(error)));
        } else {
          job.resolve(data);
        }
      };

      _proto2._update = function _update(budget) {
        while (!budget.done && this._messageQueue.length > 0) {
          this._processMessage(this._messageQueue.shift());

          budget.madeProgress();
        }
      };

      _proto2._post = function _post(message, data, transferList) {
        return esri_core_workers_utils.postMessage(this._port, message, data, transferList);
      };

      return RemoteClient;
    }();

    RemoteClient.clients = new Map();

    return RemoteClient;

});
