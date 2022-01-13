<template>
  <div v-if="element" class="default-value-div">
    <el-form ref="form" :model="element.options.attrs.default" label-width="100px">

      <el-form-item :label="$t('dynamic_time.set_default')">
        <el-radio-group v-model="element.options.attrs.default.isDynamic" @change="dynamicChange">

          <el-radio
            v-for="(item, index) in defaultSetting.radioOptions"
            :key="index"
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
          v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === (defaultSetting.relativeOptions.length - 1)"
          label=""
        >
          <el-input
            v-model="element.options.attrs.default.dynamicPrefix"
            type="number"
            size="mini"
            :min="1"
            :max="12"
            @input="dynamicPrefixChange"
          />
        </el-form-item>

        <el-form-item
          v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === (defaultSetting.relativeOptions.length - 1)"
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
          v-if="element.options.attrs.default.isDynamic && element.options.attrs.default.dkey === (defaultSetting.relativeOptions.length - 1)"
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
          :type="element.options.attrs.type"
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
