<template>
  <span>
    <ms-table-operator-button v-for="(btn, index) in buttons" :key="index" :isTesterPermission="isTesterPermission(btn)"
                              :tip="btn.tip" :icon="btn.icon" :type="btn.type"
                              @exec="click(btn)" @click.stop="clickStop(btn)"/>
  </span>
</template>

<script>
  import MsTableOperatorButton from "./MsTableOperatorButton";

  export default {
    name: "MsTableOperators",
    components: {MsTableOperatorButton},
    props: {
      row: Object,
      buttons: Array
    },
    methods: {
      click(btn) {
        if (btn.exec instanceof Function) {
          btn.exec(this.row);
        }
      },
      clickStop(btn) {
        if (btn.stop instanceof Function) {
          btn.stop(this.row);
        }
      },
    },
    computed: {
      isTesterPermission() {
        return function (btn) {
          return btn.isTesterPermission !== false;
        }
      }
    }
  }
</script>

<style scoped>

</style>
