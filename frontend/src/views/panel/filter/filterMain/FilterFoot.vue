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
          </div>
          <div class="custom-component-class">
            <component
              :is="element.component"
              :id="'component' + element.id"
              class="component"
              :style="element.style"
              :element="element"
              :in-draw="false"
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
          />
        </el-card>

        <el-card
          v-if="element.component && element.component === 'de-date' && element.serviceName && element.serviceName === 'timeDateRangeWidget'"
          class="box-card"
        >
          <de-date-range-default
            v-if="element.component === 'de-date' && element.serviceName === 'timeDateRangeWidget'"
            :element="element"
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

    }
  },

  methods: {

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

</style>
