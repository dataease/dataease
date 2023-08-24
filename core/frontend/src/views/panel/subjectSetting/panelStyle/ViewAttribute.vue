<template>
  <div style="width: 100%;">
    <el-form
      ref="viewAttributeForm"
      :model="viewAttributeForm"
      label-width="70px"
      size="mini"
    >background_color
      <el-form-item
        :label="$t('panel.background_color')"
        class="form-item"
      >
        <el-color-picker
          v-model="viewAttributeForm.color"
          :predefine="predefineColors"
          size="mini"
          style="cursor: pointer;z-index: 1004;"
          @change="viewAttributeChange"
        />
      </el-form-item>
      <el-form-item
        :label="$t('panel.opacity')"
        class="form-item"
      >
        <el-slider
          v-model="viewAttributeForm.alpha"
          show-input
          :show-input-controls="false"
          input-size="mini"
        />
      </el-form-item>
    </el-form>
  </div>
</template>

<script>

import { mapState } from 'vuex'
import { COLOR_PANEL } from '@/views/chart/chart/chart'

export default {
  name: 'ViewAttribute',
  data() {
    return {
      predefineColors: COLOR_PANEL,
      viewAttributeForm: {
        color: '#FFFFFF',
        alpha: 100
      }
    }
  },
  computed: mapState([
    'canvasStyleData'
  ]),
  watch: {
    // deep监听panel 如果改变 提交到 store
  },
  created() {

  },
  methods: {
    viewAttributeChange() {

    }

  }
}
</script>

<style scoped>

.form-item ::v-deep .el-form-item__label{
  font-size: 12px;
}

</style>
