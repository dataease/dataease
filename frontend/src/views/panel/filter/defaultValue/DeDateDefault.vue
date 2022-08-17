<template>
  <div v-if="element" class="default-value-div">
    <el-form ref="form" :model="element.options.attrs.default" label-width="100px" size="mini">

      <el-form-item :label="$t('dynamic_time.set_default')">
        <el-radio-group v-model="element.options.attrs.default.isDynamic" @change="dynamicChange">

          <el-radio
            v-for="(item, index) in defaultSetting.radioOptions"
            :key="index"
            :disabled="isTimeWidget && element.options.attrs.showTime && item.value"
            :label="item.value"
          >
            {{ $t(item.text) }}
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="element.options.attrs.default.isDynamic" :label="$t('dynamic_time.relative')">

        <el-select
          v-model="element.options.attrs.default.dkey"
          placeholder=""
          class="relative-time"
          popper-class="date-filter-poper"
          @change="dkeyChange"
        >
          <el-option
            v-for="(item, index) in defaultSetting.relativeOptions"
            :key="item.value + index"
            :label="$t(item.text)"
            :value="item.value"
          />

        </el-select>

      </el-form-item>

      <div class="inline">

        <el-form-item
          v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === customValue"
          label=""
        >
          <el-input-number
            v-model="element.options.attrs.default.dynamicPrefix"
            controls-position="right"
            size="mini"
            :min="1"
            :max="100"
            @change="dynamicPrefixChange"
          />
        </el-form-item>

        <el-form-item
          v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === customValue"
          label=""
          class="no-label-item"
        >
          <el-select
            v-model="element.options.attrs.default.dynamicInfill"
            size="mini"
            placeholder=""
            :disabled="defaultSetting.custom && defaultSetting.custom.unitsOptions && defaultSetting.custom.unitsOptions.length === 1"
            @change="dynamicInfillChange"
          >
            <el-option
              v-for="(item, index) in defaultSetting.custom.unitsOptions"
              :key="item.value + index"
              :label="$t(item.text)"
              :value="item.value"
            />

          </el-select>
        </el-form-item>

        <el-form-item
          v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === customValue"
          label=""
          class="no-label-item"
        >

          <el-select
            v-model="element.options.attrs.default.dynamicSuffix"
            size="mini"
            placeholder=""
            @change="dynamicSuffixChange"
          >
            <el-option :label="$t('dynamic_time.before')" value="before" />
            <el-option :label="$t('dynamic_time.after')" value="after" />
          </el-select>
        </el-form-item>

      </div>

      <el-form-item v-if="element.options.attrs.default.isDynamic" :label="$t('dynamic_time.preview')">
        <el-date-picker
          v-model="dval"
          :type="componentType"
          :format="labelFormat"
          disabled
          placeholder=""
          class="relative-time"
        />
      </el-form-item>

      <el-form-item v-else :label="$t('dynamic_time.set')">
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
import {
  ApplicationContext
} from '@/utils/ApplicationContext'
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
  computed: {
    defaultSetting() {
      const widget = ApplicationContext.getService(this.element.serviceName)
      const setting = widget.defaultSetting()
      return setting
    },
    isTimeWidget() {
      const widget = ApplicationContext.getService(this.element.serviceName)
      return widget.isTimeWidget && widget.isTimeWidget()
    },
    componentType() {
      let result = this.element.options.attrs.type
      if (this.isTimeWidget && this.element.options.attrs.showTime) {
        result = 'datetime'
      }
      return result
    },
    labelFormat() {
      const result = 'yyyy-MM-dd'
      if (this.isTimeWidget && this.element.options.attrs.showTime && this.element.options.attrs.accuracy) {
        return result + ' ' + this.element.options.attrs.accuracy
      }
      return null
    },
    customValue() {
      const widget = ApplicationContext.getService(this.element.serviceName)
      if (widget.customValue) {
        return widget.customValue()
      }
      return 2
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
      this.element.options.manualModify = false
    }
  }
}

</script>

<style lang="scss" scoped>
  .inline {
    display: flex;

  }

  .inline {
    .el-form-item {
      margin-bottom: 5px !important;

      .el-form-item__content>.el-input--mini {
        min-width: 70px;
      }
    }
  }

  .relative-time {
    width: 100% !important;
  }

</style>
