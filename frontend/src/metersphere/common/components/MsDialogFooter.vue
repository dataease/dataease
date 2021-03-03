<template>

  <div class="dialog-footer">
    <el-button @click="cancel">{{ $t('commons.cancel') }}</el-button>
    <el-button v-if="isShowValidate" type="warning" @click="validate" @keydown.enter.native.prevent>{{ $t('commons.validate') }}</el-button>
    <el-button type="primary" :disabled="disabled" @click="confirm" @keydown.enter.native.prevent>{{ $t('commons.confirm') }}</el-button>
    <el-button v-if="isShow" type="primary" @click="saveAsEdit" @keydown.enter.native.prevent>{{ title }}</el-button>
  </div>

</template>

<script>
export default {
  name: 'MsDialogFooter',
  props: {
    isShow: {
      type: Boolean,
      default: false
    },
    isShowValidate: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: null
    }
  },
  methods: {
    cancel() {
      this.$emit('cancel')
    },
    validate() {
      this.$emit('validate')
    },
    confirm() {
      this.$emit('confirm')
    },
    saveAsEdit() {
      this.$emit('saveAsEdit')
    }
  }
}
</script>

<style scoped>

</style>
