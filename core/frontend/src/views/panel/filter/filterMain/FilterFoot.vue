<template>
  <el-row>
    <el-col :span="24">
      <div class="filter-content">
        <el-card
          v-if="element.component && element.component !== 'de-date'"
          class="box-card"
        >
          <div style="margin-bottom: 10px;">
            <span>{{ $t('dynamic_time.set_default') }}</span>

            <el-checkbox
              v-if="element.serviceName === 'textSelectWidget'"
              v-model="element.options.attrs.selectFirst"
              class="select-first-check"
              @change="selectFirstChange"
            >
              {{ $t('panel.first_item') }}
            </el-checkbox>
          </div>
          <div class="custom-component-class">
            <component
              :is="element.component"
              :id="'component' + element.id"
              class="component"
              :style="element.style"
              is-config
              :element="element"
              :in-draw="false"
              @widget-value-changed="widgetValChange"
            />
          </div>

        </el-card>

        <el-card
          v-if="element.component && element.component === 'de-date' && element.serviceName && element.serviceName !== 'timeDateRangeWidget'"
          class="box-card"
        >
          <de-date-default
            v-if="element.component === 'de-date' && element.serviceName !== 'timeDateRangeWidget'"
            :element="element"
            @widget-value-changed="widgetValChange"
          />
        </el-card>

        <el-card
          v-if="element.component && element.component === 'de-date' && element.serviceName && element.serviceName === 'timeDateRangeWidget'"
          class="box-card"
        >
          <de-date-range-default
            v-if="element.component === 'de-date' && element.serviceName === 'timeDateRangeWidget'"
            :element="element"
            @widget-value-changed="widgetValChange"
          />
        </el-card>

      </div>
    </el-col>
  </el-row>

</template>

<script>
import DeDateDefault from '@/views/panel/filter/defaultValue/DeDateDefault'
import DeDateRangeDefault from '@/views/panel/filter/defaultValue/DeDateRangeDefault'
export default {
  name: 'FilterFoot',
  components: {
    DeDateDefault,
    DeDateRangeDefault
  },
  props: {

    element: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      attrs: null
    }
  },
  created() {
  },
  methods: {
    selectFirstChange(val) {
    },
    widgetValChange(val) {
      this.$emit('widget-value-changed', val)
    }
  }
}

</script>

<style lang="scss" scoped>
  .filter-content {
    height: calc(50vh - 120px);
    top: 160px;

  }

  .box-card {
    width: 100%;
    height: 100%;
    max-height: 100%;
    overflow-y: scroll;
  }
  .select-first-check {
    margin-left: 25px;
  }

</style>
