<template>
  <div>
    <el-form ref="form" size="mini" :rules="rules" :model="form" label-width="80px">
      <el-form-item :label="$t('desearchbutton.text')" prop="text">
        <el-input v-model="currentElement.options.value" maxlength="10" show-word-limit />
      </el-form-item>

      <el-form-item :label="$t('desearchbutton.auto_trigger')">
        <el-switch v-model="myAttrs.autoTrigger" @change="autoTriggerChange" />
        <el-link style="margin-left: 10px;" type="info" disabled>{{ $t('desearchbutton.auto_trigger_tip') }}</el-link>
      </el-form-item>

      <el-form-item :label="$t('desearchbutton.range')">
        <el-switch v-model="myAttrs.customRange" @change="customRangeChange" />
        <el-link style="margin-left: 10px;" type="warning" disabled>{{ $t('desearchbutton.range_tip') }}</el-link>
      </el-form-item>

      <el-form-item v-if="myAttrs.customRange" :label="$t('desearchbutton.relative')">
        <el-select v-model="myAttrs.filterIds" style="width: 100%;" multiple clearable>
          <el-option v-for="(filter, index) in filters" :key="filter.id + index" :label="filter.showName" :value="filter.id" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="sure">{{ $t('commons.confirm') }}</el-button>
        <el-button @click="cancel">{{ $t('commons.cancel') }}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { ApplicationContext } from '@/utils/ApplicationContext'
import { mapState } from 'vuex'

export default {
  name: 'ButtonDialog',
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
      widget: null,
      myAttrs: null,
      rules: {
        text: [
          { min: 0, max: 10, message: '长度在 0 到 10 个字符', trigger: 'blur' }
        ]
      }
    }
  },

  computed: {

    ...mapState([
      'componentData'
    ]),
    filters() {
      const datas = this.componentData.filter(item => item.type === 'custom')
      datas.forEach(item => {
        const serviceName = item.serviceName
        const widget = ApplicationContext.getService(serviceName)
        const showName = widget.initLeftPanel().label
        let result = ''
        if (showName) {
          result = this.$t(showName)
        }
        if (item.options.attrs.showTitle && item.options.attrs.title) {
          result += '【' + item.options.attrs.title + '】'
        }

        item.showName = result
      })
      return datas
    }
  },
  watch: {

  },
  created() {
    this.widget = this.widgetInfo
    this.currentElement = JSON.parse(JSON.stringify(this.element))
    this.myAttrs = this.currentElement.options.attrs
  },
  methods: {
    sure() {
      this.$emit('sure-handler')
    },
    cancel() {
      this.$emit('cancel-handler')
    },
    getElementInfo() {
      return this.currentElement
    },
    customRangeChange(val) {
      this.myAttrs.filterIds = []
    },
    autoTriggerChange(val) {

    }
  }
}
</script>
