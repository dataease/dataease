<template>
  <el-row>
    <el-col :span="11">
      <div class="filter-options-left">
        <el-checkbox
          v-model="element.options.attrs.required"
          @change="requiredChange"
        >{{ $t('commons.required') }}</el-checkbox>
        <el-checkbox
          v-if="widget.showSwitch"
          v-model="attrs.multiple"
          @change="multipleChange"
        >{{ $t('panel.multiple_choice') }}</el-checkbox>
        <span
          v-if="widget.isSortWidget && widget.isSortWidget()"
        >
          <filter-sort
            :widget="widget"
            :element="element"
            @sort-change="sortChange"
          />
        </span>
      </div>
    </el-col>
    <el-dialog
      :visible.sync="dialogVisible"
      append-to-body
      :show-close="false"
      :close-on-press-escape="false"
      :close-on-click-modal="false"
      :before-close="sureRequired"
    >
      {{ $t('time.dropdown_display_must') }}
      <div style="text-align: end;margin-top: 16px;">
        <span slot="footer">
          <el-button
            size="mini"
            @click="dialogVisible = false"
          >{{ $t('commons.cancel') }}</el-button>
          <el-button
            type="primary"
            size="mini"
            @click="sureRequired"
          >{{ $t('commons.confirm') }}</el-button>
        </span>
      </div>
    </el-dialog>
    <el-col :span="13">
      <div class="filter-options-right">
        <span style="padding-right: 10px;">
          <el-checkbox
            v-model="attrs.showTitle"
            @change="showTitleChange"
          >{{ $t('panel.show_title') }}
          </el-checkbox>
          <el-popover
            v-model="titlePopovervisible"
            placement="bottom-end"
            :disabled="!attrs.showTitle"
            width="200"
          >
            <div style="width: 100%;overflow-y: auto;overflow-x: hidden;word-break: break-all;position: relative;">
              <el-input
                v-model="attrs.title"
                :placeholder="$t('panel.input_title')"
                type="textarea"
                maxlength="15"
                show-word-limit
              />
            </div>

            <i
              slot="reference"
              :class="{'i-filter-active': attrs.showTitle, 'i-filter-inactive': !attrs.showTitle}"
              class="el-icon-setting i-filter"
            />
          </el-popover>
        </span>
        <span style="padding-left: 10px;">
          <el-checkbox
            v-model="attrs.enableRange"
            @change="enableRangeChange"
          ><span>
            {{ $t('panel.custom_scope') }} </span> </el-checkbox>

          <el-popover
            v-model="popovervisible"
            placement="bottom-end"
            :disabled="!attrs.enableRange"
            width="200"
          >
            <div class="view-container-class">
              <el-checkbox-group
                v-model="attrs.viewIds"
                @change="checkedViewsChange"
              >
                <el-checkbox
                  v-for="(item ) in curTableViews"
                  :key="item.id"
                  :label="item.id"
                  class="de-checkbox"
                >
                  <div class="span-div">
                    <svg-icon
                      :icon-class="item.type"
                      class="chart-icon"
                    />
                    <span
                      v-if="item.name && item.name.length <= 7"
                      style="margin-left: 6px"
                    >{{ item.name }}</span>
                    <el-tooltip
                      v-else
                      class="item"
                      effect="dark"
                      :content="item.name"
                      placement="left"
                    >
                      <span style="margin-left: 6px">{{ item.name }}</span>
                    </el-tooltip>
                  </div>

                </el-checkbox>
              </el-checkbox-group>
            </div>

            <i
              slot="reference"
              :class="{'i-filter-active': attrs.enableRange, 'i-filter-inactive': !attrs.enableRange}"
              class="el-icon-setting i-filter"
            />
          </el-popover>
        </span>
        <span
          style="padding-left: 10px;"
        >
          <el-popover
            popper-class="popover-more-parent"
            :visible-arrow="false"
            placement="bottom-start"
            :width="182"
            trigger="click"
          >
            <template
              v-if="showMore"
              #reference
            >
              <div class="more-select-btn icon iconfont icon-icon-more">
                {{ $t('panel.more') }}
              </div>
            </template>
            <div class="check-item">
              <el-tooltip
                v-model="visibleShowEmpty"
                class="box-item"
                effect="dark"
                :content="$t('time.passing_parameters')"
                placement="top"
                manual
              >
                <el-checkbox
                  v-if="widget.name && ['textSelectWidget', 'textSelectGridWidget'].includes(widget.name)"
                  v-model="attrs.showEmpty"
                  :disabled="attrs.enableParameters"
                ><span
                  @mouseenter="handlerVisibleShowEmpty"
                  @mouseleave="handlerVisibleShowEmpty"
                >{{ $t('panel.show_empty') }}</span>
                </el-checkbox>
              </el-tooltip>
            </div>
            <div class="check-item">
              <el-checkbox
                v-if="widget.isTimeWidget && widget.isTimeWidget()"
                v-model="attrs.showTime"
                @change="showTimeChange"
              >
                <span>{{ $t('panel.show_time') }} </span>
              </el-checkbox>
              <el-popover
                v-model="timePopovervisible"
                placement="bottom-end"
                :disabled="!attrs.showTime"
                width="140"
              >
                <div style="width: 100%;overflow-y: auto;overflow-x: hidden;word-break: break-all;position: relative;">
                  <ul class="de-ul">
                    <li
                      v-for="(node, i) in accuracyOptions"
                      :key="node.id"
                      :index="i"
                      class="de-sort-field-span"
                      :class="attrs.accuracy === node.id ? 'de-active-li': ''"
                      @click="attrs.accuracy = node.id"
                    >

                      <span>{{ node.name }}</span>
                    </li>
                  </ul>

                </div>

                <i
                  v-if="widget.isTimeWidget && widget.isTimeWidget()"
                  slot="reference"
                  :class="{'i-filter-active': attrs.showTime, 'i-filter-inactive': !attrs.showTime}"
                  class="el-icon-setting i-filter"
                />
              </el-popover>
            </div>
            <div class="check-item">
              <el-checkbox
                v-if="widget.name && ['timeDateRangeWidget'].includes(widget.name)"
                v-model="attrs.setTimeRange"
              >{{ $t('time.set_time_filtering_range') }}
              </el-checkbox>
              <el-popover
                ref="popoverSetting"
                :disabled="!attrs.setTimeRange"
                popper-class="popover-setting-parent"
                :visible-arrow="false"
                placement="bottom-start"
                :width="452"
                trigger="click"
              >
                <RangeFilterTime
                  :time-range-data="attrs.timeRange"
                  @changeData="changeData"
                />
                <i
                  v-if="widget.name && ['timeDateRangeWidget'].includes(widget.name)"
                  slot="reference"
                  :class="{'i-filter-active': attrs.setTimeRange, 'i-filter-inactive': !attrs.setTimeRange}"
                  class="el-icon-setting i-filter"
                />
              </el-popover>
            </div>
            <div class="check-item">
              <el-tooltip
                v-model="visibleEnableParameters"
                manual
              >
                <template #content>
                  <span>{{ $t('time.not_supported') }}</span>
                </template>
                <el-checkbox
                  v-if="showParams"
                  v-model="attrs.enableParameters"
                  :disabled="attrs.showEmpty"
                  @change="enableParametersChange"
                ><span
                  @mouseenter="handlerVisibleEnableParameters"
                  @mouseleave="handlerVisibleEnableParameters"
                >
                  {{ $t('panel.binding_parameters') }} </span>
                </el-checkbox>
              </el-tooltip>
              <el-popover
                placement="bottom-end"
                :disabled="!attrs.enableParameters"
                width="420"
              >
                <div class="view-container-class">
                  <el-tabs
                    v-if="isRangeParamWidget"
                    v-model="activeName"
                  >
                    <el-tab-pane
                      v-for="(item, index) in tabsOption"
                      :key="item.name + index"
                      :label="item.label"
                      :name="item.name"
                    >
                      <el-checkbox-group
                        v-model="attrs[item.name + 'Parameters']"
                        @change="val => {changeDynamicParams(val, item.name)}"
                      >
                        <el-checkbox
                          v-for="(ele ) in allParams"
                          :key="ele.id"
                          :label="ele.id"
                          :disabled="attrs[tabsOption[(index + 1)%2].name + 'Parameters'] && attrs[tabsOption[(index + 1)%2].name + 'Parameters'].includes(ele.id)"
                          class="de-checkbox"
                        >
                          <div class="span-div">
                            <span
                              v-if="ele.alias && ele.alias.length <= 7"
                              style="margin-left: 6px"
                            >{{ ele.alias }}</span>
                            <el-tooltip
                              v-else
                              class="item"
                              effect="dark"
                              :content="ele.alias"
                              placement="left"
                            >
                              <span style="margin-left: 6px">{{ ele.alias }}</span>
                            </el-tooltip>
                          </div>

                        </el-checkbox>
                      </el-checkbox-group>
                    </el-tab-pane>
                  </el-tabs>
                  <el-checkbox-group
                    v-else
                    v-model="attrs.parameters"
                  >
                    <el-checkbox
                      v-for="(item ) in allParams"
                      :key="item.id"
                      :label="item.id"
                      class="de-checkbox"
                    >
                      <div class="span-div2">
                        <span
                          v-if="item.alias && item.alias.length <= 7"
                          style="margin-left: 6px"
                        >{{ item.alias }}</span>
                        <el-tooltip
                          v-else
                          class="item"
                          effect="dark"
                          :content="item.alias"
                          placement="left"
                        >
                          <span style="margin-left: 6px">{{ item.alias }}</span>
                        </el-tooltip>
                      </div>
                    </el-checkbox>
                  </el-checkbox-group>
                </div>

                <i
                  v-if="showParams"
                  slot="reference"
                  :class="{'i-filter-active': attrs.enableParameters, 'i-filter-inactive': !attrs.enableParameters}"
                  class="el-icon-setting i-filter"
                />
              </el-popover>
            </div>
          </el-popover>
        </span>
      </div>
    </el-col>
  </el-row>
</template>

<script>
import FilterSort from './FilterSort'
import RangeFilterTime from '@/views/panel/filter/filterMain/RangeFilterTime.vue'
export default {
  name: 'FilterControl',
  components: { FilterSort, RangeFilterTime },
  props: {
    widget: {
      type: Object,
      default: null
    },
    controlAttrs: {
      type: Object,
      default: null
    },

    childViews: {
      type: Object,
      default: () => {
      }
    },
    element: {
      type: Object,
      default: null
    },
    datasetParams: {
      type: Array,
      default: () => []
    }

  },
  data() {
    return {
      activeName: 'start',
      dialogVisible: false,
      tabsOption: [
        { label: this.$t('dataset.start_time'), name: 'start' },
        { label: this.$t('dataset.end_time'), name: 'end' }
      ],
      visibleEnableParameters: false,
      visibleShowEmpty: false,
      showParams: false,
      isRangeParamWidget: false,
      attrs: null,
      titlePopovervisible: false,
      popovervisible: false,
      parametersVisible: false,
      timePopovervisible: false,
      accuracyOptions: [
        { id: 'HH', name: 'HH' },
        { id: 'HH:mm', name: 'HH:mm' },
        { id: 'HH:mm:ss', name: 'HH:mm:ss' }
      ],
      allParams: []
    }
  },
  computed: {
    fieldIds() {
      return this.element.options.attrs.fieldId || []
    },
    curTableViews() {
      const tableIdList = this.element.options.attrs.dragItems.map(item => item.tableId) || []

      const views = this.childViews.viewInfos.filter(view => tableIdList.includes(view.tableId))
      return views
    },
    showMore() {
      return (this.widget.name && ['textSelectWidget', 'textSelectGridWidget', 'timeDateRangeWidget'].includes(this.widget.name)) || (this.widget.isTimeWidget && this.widget.isTimeWidget()) || this.showParams
    }
  },
  watch: {
    'childViews.datasetParams': {
      handler(newName, oldName) {
        if (this.attrs.parameters?.length && this.attrs.parameters[0].indexOf('|DE|') === -1) {
          const parameters = []
          for (var i = 0; i < this.attrs.parameters.length; i++) {
            if (this.attrs.parameters[i].indexOf('|DE|') === -1) {
              for (var j = 0; j < this.childViews.datasetParams.length; j++) {
                if (this.childViews.datasetParams[j].id.split('|DE|')[1] === this.attrs.parameters[i]) {
                  parameters.push(this.childViews.datasetParams[j].id)
                }
              }
            } else {
              parameters.push(this.attrs.parameters[i])
            }
          }
          this.attrs.parameters = parameters
        }
        this.allParams = JSON.parse(JSON.stringify(this.childViews.datasetParams))
      }
    },
    'datasetParams': {
      handler(newName, oldName) {
        this.allParams = JSON.parse(JSON.stringify(this.childViews.datasetParams))
        if (this.datasetParams.length > 0) {
          for (let j = 0; j < this.datasetParams.length; j++) {
            let hasParam = false
            for (let i = 0; i < this.childViews.datasetParams.length; i++) {
              if (this.childViews.datasetParams[i].id === this.datasetParams[j].id) {
                hasParam = true
              }
            }
            if (!hasParam) {
              this.allParams.push(this.datasetParams[j])
            }
          }
        }
      }
    }
  },

  created() {
    this.attrs = this.controlAttrs
    if (!this.attrs.timeRange) {
      this.$set(this.attrs, 'timeRange', {
        intervalType: 'none',
        dynamicWindow: false,
        maximumSingleQuery: 0,
        regularOrTrends: 'fixed',
        regularOrTrendsValue: '',
        relativeToCurrent: 'custom',
        timeNum: 0,
        relativeToCurrentType: 'year',
        around: 'f',
        timeNumRange: 0,
        relativeToCurrentTypeRange: 'year',
        aroundRange: 'f'
      })
    }
    if (this.widget.isTimeWidget) {
      this.showParams = true
      this.isRangeParamWidget = this.widget.isRangeParamWidget && this.widget.isRangeParamWidget()
    }
    if ('textInputWidget,timeYearWidget,timeMonthWidget,timeDateWidget,textSelectWidget,numberSelectWidget,numberSelectGridWidget,textSelectGridWidget'.indexOf(this.widget.name) !== -1) {
      this.showParams = true
    }
  },
  methods: {
    sureRequired() {
      this.element.options.attrs.required = false
      this.dialogVisible = false
      this.$emit('required-change', false)
    },
    requiredChange(val) {
      if (val === false && (this.element.style.showMode && this.element.style.showMode === 'radio' && !this.element.options.attrs.multiple)) {
        this.dialogVisible = true
        this.$nextTick(() => {
          this.element.options.attrs.required = true
        })
        return
      }
      this.$emit('required-change', val)
    },
    handlerVisibleEnableParameters() {
      if (this.attrs.showEmpty) {
        this.visibleEnableParameters = !this.visibleEnableParameters
      }
    },
    handlerVisibleShowEmpty() {
      if (this.attrs.enableParameters) {
        this.visibleShowEmpty = !this.visibleShowEmpty
      }
    },
    changeDynamicParams(val, name) {
      const start = this.attrs.startParameters ? JSON.parse(JSON.stringify(this.attrs.startParameters)) : []

      let end = this.attrs.endParameters ? JSON.parse(JSON.stringify(this.attrs.endParameters)) : []
      if (end?.length) {
        end = end.map(item => {
          item = item + '_START_END_SPLIT'
          return item
        })
      }
      this.attrs.parameters = [...new Set([...start, ...end])]
    },
    changeData(val) {
      if (val) {
        this.attrs.timeRange = val
      }
      this.$refs.popoverSetting.doClose()
    },
    sortChange(param) {
      this.element.options.attrs.sort = param
    },

    multipleChange(value) {
      this.fillAttrs2Filter()
    },
    showTimeChange(value) {
      this.attrs.accuracy = this.accuracyOptions[1].id
      if (this.widget.name !== 'timeDateRangeWidget') {
        this.attrs.default.isDynamic = false
      }

      this.fillAttrs2Filter()
    },
    checkedViewsChange(values) {
      this.fillAttrs2Filter()
    },
    enableRangeChange(value) {
      if (!value) {
        this.attrs.viewIds = []
      }
      this.fillAttrs2Filter()
    },
    enableParametersChange(value) {
      if (!value) {
        this.attrs.parameters = []
        if (this.attrs.startParameters?.length) {
          this.attrs.startParameters = []
        }
        if (this.attrs.endParameters?.length) {
          this.attrs.endParameters = []
        }
      }
      this.fillAttrs2Filter()
    },
    showTitleChange(value) {
      if (!value) {
        this.element.style.backgroundColor = ''
      }
      this.fillAttrs2Filter()
    },
    showVisualChange(value) {
      this.fillAttrs2Filter()
    },

    fillAttrs2Filter() {
    }
  }
}

</script>

<style lang="scss" scoped>
.filter-options-left {
  align-items: center;
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  flex-wrap: nowrap;
  height: 50px;

  .el-checkbox {
    margin-right: 20px !important;
  }
}

.filter-options-right {
  align-items: center;
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
  flex-wrap: nowrap;
  height: 50px;

  .more-select-btn {
    display: inline-flex;
    width: 56px;
    height: 26px;
    cursor: pointer;
    margin-right: -4px;
    margin-left: 6px;
    border-radius: 4px;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    align-items: center;
    justify-content: center;
    &:hover {
      background: #3370FF1A;
      color: #3370FF;
    }

    &.icon-icon-more::before {
      margin-right: 4px;
    }
  }
}

.i-filter {
  text-align: center;
  margin-left: 5px;
  margin-top: 1px;
}

.i-filter-inactive {
  color: #9ea6b2 !important;
  cursor: not-allowed !important;
}

.i-filter-active {
  cursor: pointer !important;
}

.view-container-class {

  min-height: 150px;
  max-height: 200px;
  width: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  word-break: break-all;
  position: relative;

}

.span-div {
  width: 135px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.span-div2 {
  width: 350px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.de-ul li {
  margin: 5px 2px;
  cursor: pointer;

  &:hover {
    color: #409EFF;
    border-color: rgb(198, 226, 255);
    background-color: rgb(236, 245, 255);
  }

  &:before {
    content: "";
    width: 6px;
    height: 6px;
    display: inline-block;
    border-radius: 50%;
    vertical-align: middle;
    margin-right: 5px;
  }
}

.de-active-li {
  &:before {
    background: #409EFF;
  }
}

.de-sort-field-span {
  display: inline-flexbox;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

</style>
<style lang="scss">
.popover-more-parent {
  margin-top: 4px !important;
  .check-item {
    margin-bottom: 5px;
    &:last-of-type {
      margin-bottom: 0;
    }
  }
  .el-checkbox {
    margin-right: 0;
  }
}
</style>
