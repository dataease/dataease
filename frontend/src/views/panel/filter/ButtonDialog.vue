<template>
  <div>
    <el-form size="mini" ref="form" :model="form" label-width="100px">
      <el-form-item label="名称">
        <el-input v-model="currentElement.options.value"></el-input>
      </el-form-item>
      
      
      <el-form-item label="自定义范围">
        <el-switch v-model="myAttrs.customRange" @change="customRangeChange"></el-switch>
        <el-link style="margin-left: 10px;" type="warning" disabled>默认关联全部过滤组件</el-link>
      </el-form-item>

      <el-form-item label="关联组件" v-if="myAttrs.customRange">
        <el-select style="width: 300px;" multiple clearable v-model="myAttrs.filterIds" placeholder="请选择活动区域">
          <el-option  v-for="(filter, index) in filters" :key="filter.id" :label="filter.showName" :value="filter.id" />
        </el-select>
      </el-form-item>

      
      <el-form-item>
        <el-button type="primary" @click="sure">确定</el-button>
        <el-button @click="cancel">取消</el-button>
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
      default: null,
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
      myAttrs: null
    }
  },
  created() {
    this.widget = this.widgetInfo
    this.currentElement = JSON.parse(JSON.stringify(this.element))
    this.myAttrs = this.currentElement.options.attrs
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
        if(showName) {
          result = this.$t(showName)
        }
        if(item.options.attrs.title) {
          result += '【' + item.options.attrs.title + '】'
        }

        item.showName = result
      })
      return datas
    }
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
    }
  }
}
</script>