<template>

  <div class="dialog-footer">
    <el-button @click="cancel">{{$t('commons.cancel')}}</el-button>
    <el-button type="primary" @click="confirm" @keydown.enter.native.prevent>{{$t('commons.confirm')}}</el-button>
    <el-button type="primary" v-if="isShow" @click="saveAsEdit" @keydown.enter.native.prevent>{{title}}</el-button>
  </div>

</template>

<script>
  export default {
    name: "MsDialogFooter",
    props: {
      isShow: {
        type: Boolean,
        default: false,
      },
      title:String,
    },
    methods: {
      cancel() {
        this.$emit("cancel");
      },
      confirm() {
        this.$emit("confirm");
      },
      saveAsEdit() {
        this.$emit("saveAsEdit");
      }
    }
  }
</script>

<style scoped>

</style>
