<template>
  <el-row>
    <el-form
      ref="form"
      size="mini"
      label-width="70px"
    >
      <el-form-item :label="$t('panel.enable_carousel')">
        <el-switch
          v-model="carouselEnable"
          size="mini"
        />
      </el-form-item>
      <el-form-item :label="$t('panel.switch_time')">
        <el-input
          v-model="switchTime"
          :disabled="!carouselEnable"
          type="number"
          size="mini"
          :min="2"
          :max="3600"
          class="hide-icon-number number-padding"
          @change="switchTimeChange"
        >
          <template slot="append">S</template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          @click="onSubmit"
        >{{ $t('panel.confirm') }}</el-button>
        <el-button @click="onClose">{{ $t('panel.cancel') }}</el-button>
      </el-form-item>
    </el-form>
  </el-row>
</template>

<script>
import { mapState } from 'vuex'

export default {
  data() {
    return {
      carouselEnable: false,
      switchTime: 50
    }
  },
  computed: {
    ...mapState([
      'curComponent'
    ])
  },
  created() {
    this.carouselEnable = this.curComponent.style.carouselEnable
    this.switchTime = this.curComponent.style.switchTime
  },
  mounted() {
  },
  methods: {
    switchTimeChange() {
      if (!this.switchTime || this.switchTime < 2) {
        this.switchTime = 2
      } else if (this.switchTime && this.switchTime > 3600) {
        this.switchTime = 3600
      }
    },
    onSubmit() {
      this.curComponent.style.carouselEnable  = this.carouselEnable
      this.curComponent.style.switchTime  = this.switchTime
      this.$store.commit('canvasChange')
      this.onClose()
    },
    onClose() {
      this.$emit('onClose')
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
  .icon-font{
    color: white;
  }

</style>
