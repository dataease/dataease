<template>

  <el-popover
    ref="popover"
    width="340"
    trigger="click"
  >
    <el-row>
      <el-form ref="form" :inline="true" size="mini" label-width="70px">

        <el-form-item :label="$t('deshowdate.open_mode')">

          <el-select
            v-model="formatInfo.openMode"
            :placeholder="$t('deshowdate.select_openMode')"
            style="width: 100%;"
            @change="modelChange"
          >
            <el-option
              v-for="item in modelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>

        </el-form-item>

        <el-form-item :label="$t('deshowdate.show_week')">
          <el-switch v-model="formatInfo.showWeek" size="mini" />
        </el-form-item>

        <el-form-item :label="$t('deshowdate.show_date')">
          <el-switch v-model="formatInfo.showDate" size="mini" />
        </el-form-item>

        <el-form-item v-show="formatInfo.showDate" :label="$t('deshowdate.date_format')">
          <el-select
            v-model="formatInfo.dateFormat"
            :placeholder="$t('deshowdate.select_date_format')"
            style="width: 100%;"
          >

            <el-option
              v-for="item in dateOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>

        </el-form-item>

        <el-form-item :label="$t('deshowdate.time_format')">
          <el-select
            v-model="formatInfo.timeFormat"
            filterable
            allow-create
            default-first-option
            :placeholder="$t('deshowdate.select_time_format')"
            style="width: 100%;"
          >

            <el-option
              v-for="item in timeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>

        </el-form-item>

        <!-- <el-form-item>
          <el-button type="primary" @click="onSubmit">{{ $t('panel.confirm') }}</el-button>
          <el-button @click="onClose">{{ $t('panel.cancel') }}</el-button>
        </el-form-item> -->
      </el-form>
    </el-row>
    <i slot="reference" class="icon iconfont icon-shijiangeshizhuanhuan" />
  </el-popover>
</template>

<script>
import { mapState } from 'vuex'
import { deepCopy } from '@/components/canvas/utils/utils'

export default {
  props: {
    formatInfo: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      timeOptions: [

        { value: 'hh:mm:ss', label: 'hh:mm:ss' },
        { value: 'hh时mm分ss秒', label: 'hh时mm分ss秒' },
        { value: '', label: '无' }
      ],
      dateOptions: [
        { value: 'yyyy-MM-dd', label: 'yyyy-MM-dd' },
        { value: 'yyyy/MM/dd', label: 'yyyy/MM/dd' },
        { value: 'yyyy年MM月dd日', label: 'yyyy年MM月dd日' }
      ],
      modelOptions: [
        { value: '0', label: this.$t('deshowdate.m_default') },
        { value: '1', label: this.$t('deshowdate.m_elec') },
        { value: '2', label: this.$t('deshowdate.m_simple') }
        /* { value: '3', label: this.$t('deshowdate.m_complex') } */
      ]
    }
  },
  computed: {
    ...mapState([
      'curComponent',
      'curCanvasScale'
    ])
  },
  watch: {
    formatInfo: {
      handler(newVal, oldVla) {
        this.$store.commit('canvasChange')
      },
      deep: true
    }
  },
  created() {

  },
  methods: {
    onSubmit() {
      this.curComponent.formatInfo = deepCopy(this.formatInfo)
      this.popoverClose()
    },
    onClose() {
      this.$emit('close')
      this.popoverClose()
    },
    popoverClose() {
      this.$refs.popover.showPopper = false
    },
    modelChange(val) {
      if (val === '0') {
        this.curComponent.style.height = 100
        this.curComponent.sizey = Math.round(this.curComponent.style.height / this.curCanvasScale.matrixStyleOriginHeight)
      } else if (val === '1') {
        this.curComponent.style.height = 150
        this.curComponent.sizey = Math.round(this.curComponent.style.height / this.curCanvasScale.matrixStyleOriginHeight)
      } else {
        // this.curComponent.style.width = this.curComponent.style.width / 2
        this.curComponent.style.height = this.curComponent.style.width + 50
        this.curComponent.sizey = Math.round(this.curComponent.style.height / this.curCanvasScale.matrixStyleOriginHeight)
        // this.curComponent.sizex = Math.round(this.curComponent.style.width / this.curCanvasScale.matrixStyleOriginWidth)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
  .slot-class{
    color: white;
  }

  .bottom {
    margin-top: 20px;
    text-align: center;

  }
  .ellip{
    /*width: 100%;*/
    margin-left: 10px;
    margin-right: 10px;
    overflow: hidden;/*超出部分隐藏*/
    white-space: nowrap;/*不换行*/
    text-overflow:ellipsis;/*超出部分文字以...显示*/
    background-color: #f7f8fa;
    color: #3d4d66;
    font-size: 12px;
    line-height: 24px;
    height: 24px;
    border-radius: 3px;
  }

  .select-filed{
    /*width: 100%;*/
    margin-left: 10px;
    margin-right: 10px;
    overflow: hidden;/*超出部分隐藏*/
    white-space: nowrap;/*不换行*/
    text-overflow:ellipsis;/*超出部分文字以...显示*/
    color: #3d4d66;
    font-size: 12px;
    line-height: 35px;
    height: 35px;
    border-radius: 3px;
  }
  ::v-deep .el-popover{
    height: 200px;
    overflow: auto;
  }

</style>
