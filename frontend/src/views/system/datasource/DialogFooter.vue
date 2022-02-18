<template>

  <div class="dialog-footer">
    <el-button @click="cancel" :size="btnSize">{{$t('commons.cancel')}}</el-button>
    <el-button type="primary" @click="confirm" @keydown.enter.native.prevent :size="btnSize">{{$t('commons.confirm')}}</el-button>
    <el-button type="primary" v-if="isShow" @click="saveAsEdit" @keydown.enter.native.prevent>{{title}}</el-button>
  </div>

</template>

<script>
  export default {
    name: "DialogFooter",
    props: {
      isShow: {
        type: Boolean,
        default: false,
      },
      title:String,
      btnSize: {
        type: String,
        default() {
          return '';
        }
      }
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
