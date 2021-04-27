<template>
  <div>
    <div style="width: 100%">
      <el-popover
        placement="right"
        width="200"
        trigger="click"
      >
        <el-col>
          <el-form ref="backgroundForm" :model="backgroundForm" label-width="80px" size="mini">
            <el-form-item label="颜色" class="form-item">
              <colorPicker v-model="backgroundForm.color" style="margin-top: 6px;cursor: pointer;z-index: 1004;border: solid 1px black" @change="changeBackgroundStyle" />
            </el-form-item>
            <el-form-item label="图片" class="form-item">
              <colorPicker v-model="backgroundForm.color" style="margin-top: 6px;cursor: pointer;z-index: 1004;border: solid 1px black" @change="changeBackgroundStyle" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-button slot="reference" size="mini" class="shape-item">背景<i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>
import { DEFAULT_BACKGROUND_COLOR } from '@/views/panel/panel'

export default {
  name: 'BackgroundSelector',
  props: {
  },
  data() {
    return {
      backgroundForm: JSON.parse(JSON.stringify(DEFAULT_BACKGROUND_COLOR))
    }
  },
  watch: {

  },
  mounted() {
  },
  methods: {
    changeBackgroundStyle() {
      this.$emit('onChangeBackgroundForm', this.backgroundForm)
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
</style>
