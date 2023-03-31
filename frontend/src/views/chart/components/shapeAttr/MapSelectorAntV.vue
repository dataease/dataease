<template>
  <div style="width: 100%">
    <el-col>
      <el-form
        ref="mapForm"
        :model="mapForm"
        label-width="80px"
        size="mini"
      >
        <el-form-item
          :label="$t('chart.map_style')"
          class="form-item"
        >
          <el-select
            v-model="mapForm.mapStyle"
            @change="changeMapAttr('mapStyle')"
          >
            <el-option
              v-for="item in mapStyleOptions"
              :key="item.name"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          :label="$t('chart.map_pitch')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="mapForm.mapPitch"
            :min="0"
            :max="90"
            @change="changeMapAttr('mapPitch')"
          />
        </el-form-item>
        <el-form-item
          :label="$t('chart.map_line_type')"
          class="form-item"
        >
          <el-select
            v-model="mapForm.lineType"
            @change="changeMapAttr('lineType')"
          >
            <el-option
              v-for="item in lineTypeOptions"
              :key="item.name"
              :label="item.name"
              :value="item.value"
              :disabled="checkLineType(item)"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          :label="$t('chart.map_line_width')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="mapForm.lineWidth"
            :min="1"
            :max="10"
            @change="changeMapAttr('lineWidth')"
          />
        </el-form-item>
        <el-form-item
          :label="$t('panel.opacity')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="mapForm.lineOpacity"
            :min="0"
            :max="1"
            :step="0.01"
            @change="changeMapAttr('lineOpacity')"
          />
        </el-form-item>
        <el-form-item
          :label="$t('chart.map_line_linear')"
          class="form-item"
        >
          <el-checkbox
            v-model="mapForm.lineLinear"
            :disabled="checkLineLinear"
            @change="changeMapAttr('lineLinear')"
          />
        </el-form-item>
        <el-form-item
          :label="mapForm.lineLinear ? $t('chart.map_line_color_source_color') : $t('chart.color')"
          class="form-item"
        >
          <el-color-picker
            v-model="mapForm.lineSourceColor"
            @change="changeMapAttr('lineSourceColor')"
          />
        </el-form-item>
        <el-form-item
          v-if="mapForm.lineLinear"
          :label="$t('chart.map_line_color_target_color')"
          class="form-item"
        >
          <el-color-picker
            v-model="mapForm.lineTargetColor"
            @change="changeMapAttr('lineTargetColor')"
          />
        </el-form-item>
        <el-form-item
          :label="$t('chart.map_line_animate')"
          class="form-item"
        >
          <el-checkbox
            v-model="mapForm.lineAnimate"
            :disabled="checkLineAnimate"
            @change="changeMapAttr('lineAnimate')"
          />
        </el-form-item>
        <div v-if="mapForm.lineAnimate">
          <el-form-item
            :label="$t('chart.map_line_animate_duration')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="mapForm.lineAnimateDuration"
              :min="0"
              :max="20"
              @change="changeMapAttr('lineAnimateDuration')"
            />
          </el-form-item>
          <el-form-item
            :label="$t('chart.map_line_animate_interval')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="mapForm.lineAnimateInterval"
              :min="0"
              :max="1"
              :step="0.1"
              @change="changeMapAttr('lineAnimateInterval')"
            />
          </el-form-item>
          <el-form-item
            :label="$t('chart.map_line_animate_trail_length')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="mapForm.lineAnimateTrailLength"
              :min="0"
              :max="1"
              :step="0.1"
              @change="changeMapAttr('lineAnimateTrailLength')"
            />
          </el-form-item>
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { DEFAULT_MAP } from '@/views/chart/chart/chart'
import _ from 'lodash'
import { equalsAny } from '@/utils/StringUtils'

export default {
  name: 'MapSelectorAntV',
  props: {
    chart: {
      type: Object,
      required: true
    },
    propertyInner: {
      type: Array,
      required: false,
      default: function() {
        return []
      }
    }
  },
  data() {
    return {
      mapForm: _.cloneDeep(DEFAULT_MAP),
      mapStyleOptions: [
        { name: this.$t('chart.map_style_normal'), value: 'normal' },
        { name: this.$t('chart.map_style_light'), value: 'light' },
        { name: this.$t('chart.map_style_dark'), value: 'dark' },
        { name: this.$t('chart.map_style_whitesmoke'), value: 'whitesmoke' },
        { name: this.$t('chart.map_style_fresh'), value: 'fresh' },
        { name: this.$t('chart.map_style_grey'), value: 'grey' },
        { name: this.$t('chart.map_style_graffiti'), value: 'graffiti' },
        { name: this.$t('chart.map_style_macaron'), value: 'macaron' },
        { name: this.$t('chart.map_style_darkblue'), value: 'darkblue' },
        { name: this.$t('chart.map_style_blue'), value: 'blue' },
        { name: this.$t('chart.map_style_wine'), value: 'wine' }
      ],
      lineTypeOptions: [
        { name: this.$t('chart.map_line_type_line'), value: 'line' },
        { name: this.$t('chart.map_line_type_arc'), value: 'arc' },
        { name: this.$t('chart.map_line_type_arc_3d'), value: 'arc3d' },
        { name: this.$t('chart.map_line_type_great_circle'), value: 'greatcircle' }
      ]
    }
  },
  computed: {
    checkLineLinear() {
      return this.mapForm.lineAnimate && equalsAny(this.mapForm.lineType, 'line', 'arc')
    },
    checkLineAnimate() {
      return this.mapForm.lineLinear && equalsAny(this.mapForm.lineType, 'line', 'arc')
    }
  },
  watch: {
    'chart': {
      handler: function() {
        this.initData()
      }
    }
  },
  mounted() {
    this.initData()
  },
  methods: {
    checkLineType(item) {
      if (this.mapForm.lineLinear && this.mapForm.lineAnimate) {
        return equalsAny(item.value, 'line', 'arc')
      }
      return false
    },
    changeMapAttr(modifyName) {
      this.mapForm['modifyName'] = modifyName
      this.$emit('onMapChange', this.mapForm)
    },
    initData() {
      if (this.chart.customAttr) {
        if (this.chart.customAttr) {
          let customAttr = null
          if (Object.prototype.toString.call(this.chart.customAttr) === '[object Object]') {
            customAttr = JSON.parse(JSON.stringify(this.chart.customAttr))
          } else {
            customAttr = JSON.parse(this.chart.customAttr)
          }
          if (customAttr.map) {
            const map = customAttr.map
            this.mapForm.mapStyle = map.mapStyle ?? 'normal'
            this.mapForm.mapPitch = map.mapPitch ?? 0
            this.mapForm.lineType = map.lineType ?? 'line'
            this.mapForm.lineWidth = map.lineWidth ?? 1
            this.mapForm.lineOpacity = map.lineOpacity ?? 1
            this.mapForm.lineSourceColor = map.lineSourceColor ?? '#000000'
            this.mapForm.lineTargetColor = map.lineTargetColor ?? '#000000'
            this.mapForm.lineAnimateDuration = map.lineAnimateDuration ?? 4
            this.mapForm.lineAnimateInterval = map.lineAnimateInterval ?? 0.5
            this.mapForm.lineAnimateTrailLength = map.lineAnimateTrailLength ?? 0.1
          }
        }
      }
    }
  },
}
</script>

<style scoped>
.shape-item {
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-item-slider ::v-deep .el-form-item__label {
  font-size: 12px;
  line-height: 38px;
}

.form-item ::v-deep .el-form-item__label {
  font-size: 12px;
}

.el-select-dropdown__item {
  padding: 0 20px;
}

span {
  font-size: 12px
}

.el-form-item {
  margin-bottom: 6px;
}

.el-divider--horizontal {
  margin: 10px 0
}

.divider-style ::v-deep .el-divider__text {
  color: #606266;
  font-size: 12px;
  font-weight: 400;
  padding: 0 10px;
}
.form-flex >>> .el-form-item__content {
  display: flex;
}
</style>
