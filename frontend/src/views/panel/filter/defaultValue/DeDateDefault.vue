<template>
  <div v-if="element">
    <el-form ref="form" :model="element.options.attrs.default" label-width="100px">

      <el-form-item label="设定默认值">
        <el-radio-group v-model="element.options.attrs.default.isDynamic" @change="dynamicChange">
          <el-radio :label="false">固定时间</el-radio>
          <el-radio :label="true">动态时间</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="element.options.attrs.default.isDynamic" label="相对当前时间">

        <el-select v-model="element.options.attrs.default.dkey" placeholder="" class="relative-time" @change="dkeyChange">
          <el-option label="今天" :value="0" />
          <el-option label="昨天" :value="1" />
          <el-option label="本月首日" :value="2" />
          <el-option label="自定义" :value="3" />
        </el-select>

      </el-form-item>

      <div class="inline">

        <el-form-item v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === 3" label="">
          <el-input v-model="element.options.attrs.default.dynamicPrefix" type="number" size="mini" :min="1" :max="10" @input="dynamicPrefixChange" />
        </el-form-item>

        <el-form-item v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === 3" label="" class="no-label-item">
          <el-select v-model="element.options.attrs.default.dynamicInfill" size="mini" placeholder="" @change="dynamicInfillChange">
            <el-option label="天" value="day" />
            <el-option label="周" value="week" />
            <el-option label="月" value="month" />
            <el-option label="年" value="year" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === 3" label="" class="no-label-item">

          <el-select v-model="element.options.attrs.default.dynamicSuffix" size="mini" placeholder="" @change="dynamicSuffixChange">
            <el-option label="前" value="before" />
            <el-option label="后" value="after" />
          </el-select>
        </el-form-item>

      </div>

      <el-form-item v-if="element.options.attrs.default.isDynamic" label="预览">
        <el-date-picker
          v-model="dval"
          type="date"
          disabled
          placeholder=""
          class="relative-time"
        />
      </el-form-item>

      <el-form-item v-else label="设置">
        <component
          :is="element.component"
          :id="'component' + element.id"
          class="relative-time"
          :style="element.style"
          :element="element"
          :in-draw="false"
        />
      </el-form-item>

    </el-form>
  </div>
</template>

<script>
import { ApplicationContext } from '@/utils/ApplicationContext'
export default {
  name: 'DeDateDefault',
  props: {

    element: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      dval: null
    }
  },
  created() {
    this.setDval()
  },
  methods: {
    dynamicChange(value) {
      this.setDval()
    },
    dkeyChange(value) {
      this.setDval()
    },

    dynamicPrefixChange(value) {
      if (value < 1) {
        value = 1
        this.element.options.attrs.default.dynamicPrefix = 1
      }
      this.setDval()
    },
    dynamicInfillChange(value) {
      this.setDval()
    },
    dynamicSuffixChange(value) {
      this.setDval()
    },
    setDval() {
      const widget = ApplicationContext.getService(this.element.serviceName)
      const time = widget.dynamicDateFormNow(this.element)
      this.dval = time
    }
  }
}
</script>

<style lang="scss" scoped>
.inline {
  display: flex;
  >>>.el-input--mini {
      min-width: 70px;
  }
}
.relative-time {
    width: 100%;
}

</style>
