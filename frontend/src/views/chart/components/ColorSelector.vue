<template>
  <div>
    <div style="width:100%;height: 32px;margin:0;padding:0 4px;border-radius: 4px;border: 1px solid #DCDFE6;display: flex;align-items: center;">
      <el-popover
        placement="right"
        width="400"
        trigger="click"
      >
        <el-col>
          <el-form ref="colorForm" :model="colorForm" label-width="80px" size="mini">
            <el-form-item :label="$t('chart.color_case')" class="form-item">
              <el-select v-model="colorForm.colorCase" :placeholder="$t('chart.pls_slc_color_case')" size="mini" @change="changeColorCase">
                <el-option v-for="option in colorCases" :key="option.value" :label="option.name" :value="option.value" style="display: flex;align-items: center;">
                  <div style="float: left">
                    <span v-for="(c,index) in option.colors" :key="index" :style="{width: '20px',height: '20px',float: 'left',backgroundColor: c}" />
                  </div>
                  <span style="margin-left: 4px;">{{ option.name }}</span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-form>
        </el-col>
        <el-button slot="reference" size="mini" class="shape-item">{{ $t('chart.color') }}<i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
      <!--todo other color attr-->
    </div>
  </div>
</template>

<script>
export default {
  name: 'ColorSelector',
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      colorCases: [
        {
          name: this.$t('chart.color_default'),
          value: 'default',
          colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
        },
        {
          name: this.$t('chart.color_retro'),
          value: 'retro',
          colors: ['#0780cf', '#765005', '#fa6d1d', '#0e2c82', '#b6b51f', '#da1f18', '#701866', '#f47a75', '#009db2']
        },
        {
          name: this.$t('chart.color_elegant'),
          value: 'elegant',
          colors: ['#95a2ff', '#fa8080', '#ffc076', '#fae768', '#87e885', '#3cb9fc', '#73abf5', '#cb9bff', '#434348']
        },
        {
          name: this.$t('chart.color_future'),
          value: 'future',
          colors: ['#63b2ee', '#76da91', '#f8cb7f', '#f89588', '#7cd6cf', '#9192ab', '#7898e1', '#efa666', '#eddd86']
        },
        {
          name: this.$t('chart.color_gradual'),
          value: 'gradual',
          colors: ['#71ae46', '#96b744', '#c4cc38', '#ebe12a', '#eab026', '#e3852b', '#d85d2a', '#ce2626', '#ac2026']
        },
        {
          name: this.$t('chart.color_simple'),
          value: 'simple',
          colors: ['#929fff', '#9de0ff', '#ffa897', '#af87fe', '#7dc3fe', '#bb60b2', '#433e7c', '#f47a75', '#009db2']
        },
        {
          name: this.$t('chart.color_business'),
          value: 'business',
          colors: ['#194f97', '#555555', '#bd6b08', '#00686b', '#c82d31', '#625ba1', '#898989', '#9c9800', '#007f54']
        },
        {
          name: this.$t('chart.color_gentle'),
          value: 'gentle',
          colors: ['#5b9bd5', '#ed7d31', '#70ad47', '#ffc000', '#4472c4', '#91d024', '#b235e6', '#02ae75', '#5b9bd5']
        },
        {
          name: this.$t('chart.color_technology'),
          value: 'technology',
          colors: ['#05f8d6', '#0082fc', '#fdd845', '#22ed7c', '#09b0d3', '#1d27c9', '#f9e264', '#f47a75', '#009db2']
        }
      ],
      colorForm: {
        colorCase: 'default'
      }
    }
  },
  watch: {
    'chart': {
      handler: function() {
        const chart = JSON.parse(JSON.stringify(this.chart))
        this.colorForm.colorCase = chart.customAttr.color.value
      }
    }
  },
  mounted() {
  },
  methods: {
    changeColorCase() {
      const that = this
      const items = this.colorCases.filter(ele => {
        return ele.value === that.colorForm.colorCase
      })
      this.$emit('onColorChange', {
        value: items[0].value,
        colors: items[0].colors
      })
    }
  }
}
</script>

<style scoped lang="scss">
.shape-item{
  padding: 6px;
  border: none;
}
.form-item>>>.el-form-item__label{
  font-size: 12px;
}
.el-select-dropdown__item{
  padding: 0 20px;
}
  span{font-size: 12px}
</style>
