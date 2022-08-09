<template>
  <div>
    <el-form ref="form" size="mini" :rules="rules" :model="form" label-width="80px">
      <el-form-item :label="$t('desresetbutton.text')" prop="text">
        <el-input v-model="currentElement.options.value" maxlength="10" show-word-limit />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="sure">{{ $t('commons.confirm') }}</el-button>
        <el-button @click="cancel">{{ $t('commons.cancel') }}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>

export default {
  name: 'ButtonResetDialog',
  props: {
    widgetInfo: {
      type: Object,
      default: null
    },
    element: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      form: {

      },
      currentElement: null,
      rules: {
        text: [
          { min: 0, max: 10, message: '长度在 0 到 10 个字符', trigger: 'blur' }
        ]
      }
    }
  },

  computed: {

  },
  watch: {

  },
  created() {
    this.currentElement = JSON.parse(JSON.stringify(this.element))
  },
  methods: {
    sure() {
      this.$emit('reset-button-handler')
    },
    cancel() {
      this.$emit('cancel-button-handler')
    },
    getElementInfo() {
      return this.currentElement
    }
  }
}
</script>
