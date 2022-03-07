<template>
  <div style="width: 100%;padding: 0 18px;">
    <el-col>
      <el-button
        :title="$t('chart.edit')"
        icon="el-icon-edit"
        type="text"
        size="small"
        style="width: 24px;margin-left: 4px;"
        @click="editLine"
      />
      <el-col>
        <el-row v-for="(item,index) in assistLine" :key="index" class="line-style">
          <el-col :span="8">
            <span :title="item.name">{{ item.name }}</span>
          </el-col>
          <el-col :span="8">
            <span v-if="item.field === '0'" :title="$t('chart.field_fixed')">{{ $t('chart.field_fixed') }}</span>
          </el-col>
          <el-col :span="8">
            <span :title="item.value">{{ item.value }}</span>
          </el-col>
        </el-row>
      </el-col>
    </el-col>

    <!--编辑辅助线-->
    <el-dialog
      v-if="editLineDialog"
      v-dialogDrag
      :title="$t('chart.assist_line')"
      :visible="editLineDialog"
      :show-close="false"
      width="70%"
      class="dialog-css"
    >
      <assist-line-edit :line="assistLine" @onAssistLineChange="lineChange" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeEditLine">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="changeLine">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import AssistLineEdit from '@/views/chart/components/senior/dialog/AssistLineEdit'
export default {
  name: 'AssistLine',
  components: { AssistLineEdit },
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      assistLine: [],
      editLineDialog: false,
      lineArr: []
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
      if (chart.senior) {
        let senior = null
        if (Object.prototype.toString.call(chart.senior) === '[object Object]') {
          senior = JSON.parse(JSON.stringify(chart.senior))
        } else {
          senior = JSON.parse(chart.senior)
        }
        if (senior.assistLine) {
          this.assistLine = senior.assistLine
        } else {
          this.assistLine = []
        }
      }
    },
    changeAssistLine() {
      this.$emit('onAssistLineChange', this.assistLine)
    },
    lineChange(val) {
      this.lineArr = val
    },

    editLine() {
      this.editLineDialog = true
    },
    closeEditLine() {
      this.editLineDialog = false
    },
    changeLine() {
      // check line config
      for (let i = 0; i < this.lineArr.length; i++) {
        const ele = this.lineArr[i]
        if (!ele.name || ele.name === '') {
          this.$message({
            message: this.$t('chart.name_can_not_empty'),
            type: 'error',
            showClose: true
          })
          return
        }
        if (!ele.value) {
          this.$message({
            message: this.$t('chart.value_can_not_empty'),
            type: 'error',
            showClose: true
          })
          return
        }
        if (parseFloat(ele.value).toString() === 'NaN') {
          this.$message({
            message: this.$t('chart.value_error'),
            type: 'error',
            showClose: true
          })
          return
        }
      }
      this.assistLine = JSON.parse(JSON.stringify(this.lineArr))
      this.changeAssistLine()
      this.closeEditLine()
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
.form-item-slider>>>.el-form-item__label{
  font-size: 12px;
  line-height: 38px;
}
.form-item>>>.el-form-item__label{
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

.line-style{

}
.line-style >>> span{
  display: inline-block;
  width: 100%;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  padding: 0 10px;
}

.dialog-css >>> .el-dialog__title {
  font-size: 14px;
}

.dialog-css >>> .el-dialog__header {
  padding: 20px 20px 0;
}

.dialog-css >>> .el-dialog__body {
  padding: 10px 20px 20px;
}
</style>
