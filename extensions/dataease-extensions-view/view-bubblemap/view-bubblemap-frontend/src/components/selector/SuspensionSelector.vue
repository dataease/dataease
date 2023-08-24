<template>
  <div style="width: 100%;">
    <el-col>
      <el-form
        ref="suspensionForm"
        :model="suspensionForm"
        label-width="80px"
        size="mini"
      >
        <el-form-item
          :label="$t('chart.show')"
          class="form-item"
        >
          <el-checkbox
            v-model="suspensionForm.show"
            @change="changeSuspensionAttr('show')"
          >{{ $t('chart.show') }}
          </el-checkbox>
        </el-form-item>

      </el-form>
    </el-col>
  </div>
</template>

<script>
import { DEFAULT_SUSPENSION } from '@/utils/map'

export default {
  name: 'SuspensionForm',
  props: {
    param: {
      type: Object,
      required: true
    },
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      suspensionForm: JSON.parse(JSON.stringify(DEFAULT_SUSPENSION)),
      fontSize: []
    }
  },
  watch: {
    'chart': {
      handler: function() {
        this.initData()
      }
    }
  },
  mounted() {
    this.initData()
  },
  methods: {
    initData() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.customAttr) {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        if (customAttr.suspension) {
          this.suspensionForm = customAttr.suspension
        }
      }
    },

    changeSuspensionAttr(modifyName) {
      this.suspensionForm['modifyName'] = modifyName
      this.$emit('onSuspensionChange', this.suspensionForm)
    }
  }
}
</script>

<style scoped>
  .shape-item{
    padding: 6px;
    border: none;
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
.form-item-slider ::v-deep .el-form-item__label{
  font-size: 12px;
  line-height: 38px;
}
.form-item ::v-deep .el-form-item__label{
  font-size: 12px;
}
.el-select-dropdown__item{
  padding: 0 20px;
}
  span{
    font-size: 12px
  }
  .el-form-item{
    margin-bottom: 6px;
  }

  .switch-style{
    position: absolute;
    right: 10px;
    margin-top: -4px;
  }
  .color-picker-style{
    cursor: pointer;
    z-index: 1003;
  }
</style>
