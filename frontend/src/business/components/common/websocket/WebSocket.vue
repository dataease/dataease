<template>
  <div class="test">

  </div>
</template>

<script>
  export default {
    name: "MsWebSocket",
    data() {
      return {
        websocket: null,
      }
    },
    created() {
      this.initWebSocket();
    },
    destroyed() {
      // this.websocket.close() //离开路由之后断开websocket连接
    },
    methods: {
      initWebSocket() {
        // window.console.log("init WebSocket");
        const uri = "ws://" + window.location.host + "/socket";
        this.websocket = new WebSocket(uri);
        this.websocket.onmessage = this.onMessage;
        this.websocket.onopen = this.onOpen;
        this.websocket.onerror = this.onError;
        this.websocket.onclose = this.onClose;
      },
      onOpen() {
        let actions = {"test": "12345"};
        this.send(JSON.stringify(actions));
      },
      onError(e) {
        window.console.error(e)
      },
      onMessage(e) {
        // window.console.log(e.data)
      },
      onClose(e) {
        // window.console.log('断开连接', e);
      },
      send(Data) {
        this.websocket.send(Data);
      }
    }
  }
</script>

<style scoped>

</style>
