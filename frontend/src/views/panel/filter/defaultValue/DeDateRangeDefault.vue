<template>
  <div v-if="element">
    <el-form ref="form" :model="element.options.attrs.default" label-width="100px">

      <el-form-item :label="$t('dynamic_time.set_default')">
        <el-radio-group v-model="element.options.attrs.default.isDynamic" @change="dynamicChange">
          <el-radio :label="false">{{ $t('dynamic_time.fix') }}</el-radio>
          <el-radio :label="true">{{ $t('dynamic_time.dynamic') }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="element.options.attrs.default.isDynamic" :label="$t('dynamic_time.relative')">

        <el-select
          v-model="element.options.attrs.default.dkey"
          placeholder=""
          class="relative-time"
          @change="dkeyChange"
        >
          <el-option :label="$t('dynamic_time.cweek')" :value="0" />
          <el-option :label="$t('dynamic_time.cmonth')" :value="1" />
          <el-option :label="$t('dynamic_time.cquarter')" :value="2" />
          <el-option :label="$t('dynamic_time.cyear')" :value="3" />

          <el-option :label="$t('dynamic_time.custom')" :value="4" />
        </el-select>

      </el-form-item>

      <div class="inline-first">

        <el-form-item
          v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === 4"
          :label="$t('dataset.start_time')"
        >
          <el-input
            v-model="element.options.attrs.default.sDynamicPrefix"
            type="number"
            size="mini"
            :min="0"
            :max="10"
            @input="sDynamicPrefixChange"
          />
        </el-form-item>

        <el-form-item
          v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === 4"
          label=""
          class="no-label-item"
        >
          <el-select
            v-model="element.options.attrs.default.sDynamicInfill"
            size="mini"
            placeholder=""
            @change="dynamicInfillChange"
          >
            <el-option :label="$t('dynamic_time.date')" value="day" />
            <el-option :label="$t('dynamic_time.week')" value="week" />
            <el-option :label="$t('dynamic_time.month')" value="month" />
            <el-option :label="$t('dynamic_time.year')" value="year" />
          </el-select>
        </el-form-item>

        <el-form-item
          v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === 4"
          label=""
          class="no-label-item"
        >

          <el-select
            v-model="element.options.attrs.default.sDynamicSuffix"
            size="mini"
            placeholder=""
            @change="dynamicSuffixChange"
          >
            <el-option :label="$t('dynamic_time.before')" value="before" />
            <el-option :label="$t('dynamic_time.after')" value="after" />
          </el-select>
        </el-form-item>

      </div>

      <div class="inline-first">

        <el-form-item
          v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === 4"
          :label="$t('dataset.end_time')"
        >
          <el-input
            v-model="element.options.attrs.default.eDynamicPrefix"
            type="number"
            size="mini"
            :min="0"
            :max="10"
            @input="eDynamicPrefixChange"
          />
        </el-form-item>

        <el-form-item
          v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === 4"
          label=""
          class="no-label-item"
        >
          <el-select
            v-model="element.options.attrs.default.eDynamicInfill"
            size="mini"
            placeholder=""
            @change="dynamicInfillChange"
          >
            <el-option :label="$t('dynamic_time.date')" value="day" />
            <el-option :label="$t('dynamic_time.week')" value="week" />
            <el-option :label="$t('dynamic_time.month')" value="month" />
            <el-option :label="$t('dynamic_time.year')" value="year" />
          </el-select>
        </el-form-item>

        <el-form-item
          v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === 4"
          label=""
          class="no-label-item"
        >

          <el-select
            v-model="element.options.attrs.default.eDynamicSuffix"
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
          :type="element.options.attrs.type"
          disabled
          class="relative-time"
          placeholder=""
        />
      </el-form-item>

      <el-form-item v-else :label="$t('dynamic_time.set')">
        <component
          :is="element.component"
          :id="'component' + element.id"
          :style="element.style"
          :element="element"
          class="relative-time"
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
import bus from '@/utils/bus'
export default {
  name: 'DeDateRangeDefault',
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

    sDynamicPrefixChange(value) {
      if (value < 0) {
        value = 0
        this.element.options.attrs.default.sDynamicPrefix = 0
      }
      this.setDval()
    },
    eDynamicPrefixChange(value) {
      if (value < 0) {
        value = 0
        this.element.options.attrs.default.eDynamicPrefix = 0
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
      bus.$emit('valid-values-change', (!time || time.length === 0 || time[1] > time[0]))
      this.element.options.manualModify = false
    }
  }
}

</script>

<style lang="scss" scoped>
  .inline-first,
  .inline {
    display: flex;
  }

  .inline-first {
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
