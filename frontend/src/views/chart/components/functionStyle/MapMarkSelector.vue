<template>
  <div style="width: 100%;">
    <el-col>
      <el-form
        ref="markForm"
        :model="markForm"
        label-width="40px"
        size="mini"
        :rules="rules"
      >

        <el-form-item
          class="form-item"
          :label="$t('chart.mark_field')"
          prop="fieldId"
        >
          <el-select
            v-model="markForm.fieldId"
            style="width: 100%;"
            clearable
            :placeholder="($t('commons.please_select') + $t('chart.mark_field'))"
            @change="changeFields"
          >

            <el-option-group
              v-for="group in fieldOptions"
              :key="group.label"
              :label="group.label"
            >
              <el-option
                v-for="item in group.options"
                :key="item.id"
                :label="item.name"
                :value="item.id"
                :disabled="item.disabled"
              />
            </el-option-group>
          </el-select>
        </el-form-item>

        <el-form-item
          v-for="(condition, index) in markForm.conditions"
          :key="index"
          :label="$t('chart.mark_value')"
          class="form-item"
        >
          <el-col
            class="col-start"
            :span="6"
          >
            <el-select
              v-model="condition.calc"
              placeholder="请选择"
              @change="changeMarkAttr('conditions')"
            >
              <el-option
                v-for="item in currentCalcOption"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-col>
          <el-col
            v-if="condition.calc === 'between'"
            class="col-center"
            :span="10"
          >
            <el-input
              v-model="condition.startv"
              class="number-range-input"
              placeholder=""
              @input="changeMarkAttr('conditions')"
            />
            <span
              class="number-range-span"
              style="padding: 0 2px;"
            >-</span>
            <el-input
              v-model="condition.endv"
              class="number-range-input"
              placeholder=""
              @input="changeMarkAttr('conditions')"
            />
          </el-col>
          <el-col
            v-else
            class="col-center"
            :span="10"
          >
            <el-input
              v-model="condition.value"
              placeholder=""
              @input="changeMarkAttr('conditions')"
            />
          </el-col>
          <el-col
            class="col-center"
            :span="6"
          >
            <de-icon-group-picker
              v-model="condition.icon"
              :options="iconOptions"
              :color="condition.color"
              size="mini"
              @change="changeMarkAttr('conditions')"
              @set-color="c => {setConditionColor(index, c)}"
            />
          </el-col>
          <el-col
            class="col-end"
            :span="2"
          >
            <div>
              <div
                v-if="markForm.conditions.length > 1"
                class="i-row-container"
                :class="index === (markForm.conditions.length - 1) ? 'flex-column': ''"
              >
                <i
                  class="ope-item el-icon-minus"
                  @click.stop="del(index)"
                />
              </div>
              <div
                v-if="index === (markForm.conditions.length - 1)"
                class="i-row-container"
                :class="markForm.conditions.length > 1 ? 'flex-column': ''"
              >
                <i
                  class="ope-item el-icon-plus"
                  @click.stop="add"
                />
              </div>
            </div>
          </el-col>
        </el-form-item>
      </el-form>

    </el-col>
  </div>
</template>

<script>
import { DEFAULT_MARK } from '../../chart/chart'
import DeIconGroupPicker from '@/components/deIconPicker/deIconGroupPicker'
import deSvgIcons from '@/deicons'
import { getItemType } from '@/views/chart/components/dragItem/utils'
import bus from '@/utils/bus'
export default {
  name: 'MapMarkSelector',
  components: { DeIconGroupPicker },
  props: {
    param: {
      type: Object,
      required: true
    },
    chart: {
      type: Object,
      required: true
    },
    dimensionData: {
      type: Array,
      required: true
    },
    quotaData: {
      type: Array,
      required: true
    },
    view: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      markForm: JSON.parse(JSON.stringify(DEFAULT_MARK)),
      values: null,
      rules: {
        fieldId: [
          { validator: this.fieldIdValidator, trigger: 'blur' }
        ]
      },
      calcOptions: [{
        value: 'eq',
        label: '等于',
        type: 'String'
      }, {
        value: 'contain',
        label: '包含',
        type: 'String'
      }, {
        value: 'uncontain',
        label: '不包含',
        type: 'String'
      }, {
        value: 'start',
        label: '开头是',
        type: 'String'
      }, {
        value: 'end',
        label: '结尾是',
        type: 'String'
      }, {
        value: 'eq',
        label: '=',
        type: 'Number'
      }, {
        value: 'ne',
        label: '!=',
        type: 'Number'
      }, {
        value: 'gt',
        label: '>',
        type: 'Number'
      }, {
        value: 'ge',
        label: '>=',
        type: 'Number'
      }, {
        value: 'lt',
        label: '<',
        type: 'Number'
      }, {
        value: 'le',
        label: '<=',
        type: 'Number'
      }, {
        value: 'between',
        label: 'between',
        type: 'Number'
      }
      ],
      iconOptions: {
        FontAwesome: false,
        ElementUI: false,
        addIconList: [],
        removeIconList: []
      }
    }
  },
  computed: {
    fieldOptions() {
      const xaxis = this.view.xaxis
      const yaxis = this.view.yaxis
      const locationIds = this.view.viewFields ? this.view.viewFields.filter(item => item.busiType === 'locationXaxis' || item.busiType === 'locationYaxis').map(item => item.id) : []
      const xIds = xaxis ? xaxis.map(item => item.id) : []
      const yIds = yaxis ? yaxis.map(item => item.id) : []
      const disableIds = [...xIds, ...yIds, ...locationIds]

      return [
        {
          label: this.$t('chart.dimension'),
          options: this.dimensionData && this.dimensionData.map(item => {
            item.disabled = disableIds.includes(item.id)
            return item
          })
        },
        {
          label: this.$t('chart.quota'),
          options: this.quotaData && this.quotaData.map(item => {
            item.disabled = disableIds.includes(item.id)
            return item
          })
        }
      ]
    },
    currentCalcOption() {
      const field = this.getField(this.markForm.fieldId)
      if (!field) {
        return []
      }
      const numberType = [2, 3]
      const excludeCalcType = numberType.includes(field.deType) ? 'String' : 'Number'
      return this.calcOptions.filter(option => option.type !== excludeCalcType)
    }

  },
  watch: {
    'chart': {
      handler: function() {
        this.initData()
      }
    }
  },

  beforeDestroy() {
    bus.$off('reset-change-table', this.getItemTagType)
  },
  mounted() {
    bus.$on('reset-change-table', this.getItemTagType)
    this.initData()
    this.loadSvg()
  },
  methods: {
    fieldIdValidator(rule, value, callback) {
      if (!value) {
        return callback()
      }
      const field = this.getField(value)
      if (!field) {
        return callback(new Error(this.$t('chart.mark_field_error')))
      }
      const tagType = getItemType(this.dimensionData, this.quotaData, field)
      if (tagType === 'danger') {
        return callback(new Error(this.$t('chart.mark_field_error')))
      }
      callback()
    },
    setConditionColor(index, color) {
      this.markForm.conditions[index].color = color
      this.changeMarkAttr('conditions')
    },
    loadSvg() {
      const svgList = deSvgIcons
      const groupMap = {}
      svgList.forEach(svg => {
        const arr = svg.split('-')
        if (arr?.length > 3) {
          const groupName = arr[0] + '-' + arr[1]
          const svgName = svg
          groupMap[groupName] = groupMap[groupName] || []
          groupMap[groupName].push(svgName)
        }
      })
      this.iconOptions.addIconGroup = groupMap
    },
    initData() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.customAttr) {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        if (customAttr.mark) {
          this.markForm = customAttr.mark
          if (this.markForm.fieldId) {
            this.getItemTagType()
          }
        }
      }
    },

    add() {
      const newRow = {
        fieldId: this.markForm.fieldId,
        calc: 'eq',
        value: '',
        icon: '',
        color: 'rgba(255, 0, 0, 1)'
      }
      this.markForm.conditions.push(newRow)
    },
    del(index) {
      this.markForm.conditions.splice(index, 1)
      this.changeMarkAttr('modifyName')
    },

    changeMarkAttr(modifyName) {
      this.markForm['modifyName'] = modifyName
      this.$emit('onMarkChange', this.markForm)
    },
    getField(fieldId) {
      const fields = [...JSON.parse(JSON.stringify(this.dimensionData)), ...JSON.parse(JSON.stringify(this.quotaData))]
      return fields.find(item => item.id === fieldId)
    },
    changeFields(fieldId) {
      const baseConditions = [
        {
          fieldId: fieldId,
          calc: 'eq',
          value: '',
          icon: '',
          color: 'rgba(255, 0, 0, 1.0)'
        }
      ]
      this.view.viewFields = this.view.viewFields.filter(item => item.busiType !== 'daxis')
      if (fieldId) {
        this.markForm.conditions = JSON.parse(JSON.stringify(baseConditions))

        const field = this.getField(fieldId)
        if (field) {
          field.busiType = 'daxis'
          this.view.viewFields.push(field)
        }
      } else {
        this.markForm.conditions = []
      }

      this.changeMarkAttr('fieldId')
    },
    getItemTagType() {
      this.$refs['markForm'].validate((valid) => {
      })
    }
  }
}
</script>

<style lang="scss" scoped>

  .color-picker-style{
    cursor: pointer;
    z-index: 1003;
  }
  .col-end {
    text-align: end;
  }
  .col-center {
    padding: 0 5px;
  }
  .ope-item {
    height: 100%;
    line-height: 100%;
    background-color: #FFFFFF;
    background-image: none;
    border: 1px solid #DCDFE6;
    -webkit-box-sizing: border-box;
    box-sizing: border-box;

  }
  .i-row-container {
    width: 15px;
    font-size: 12px !important;
    float: right;
    i {
      cursor: pointer;
      &:hover {
        color: var(--primary, #3370ff);
        border-color: var(--primary, #3370ff);
      }
    }
  }
  .flex-column {
    display: flex;
    flex: none;
    flex-direction: column;
  }
  .number-range-input {
    width: 45%;
  }
  .number-range-span {
    padding: 0 2px;
  }
</style>
