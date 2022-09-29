<template>
  <el-row>
    <el-col :span="8">
      <div class="filter-options-left">
        <el-switch
          v-if="widget.showSwitch"
          v-model="attrs.multiple"
          :active-text="$t('panel.multiple_choice')"
          @change="multipleChange"
        />

        <span v-if="widget.isTimeWidget && widget.isTimeWidget()" style="padding-left: 10px;">
          <el-checkbox v-model="attrs.showTime" @change="showTimeChange">
            <span>{{ $t('panel.show_time') }} </span>
          </el-checkbox>

          <el-popover v-model="timePopovervisible" placement="bottom-end" :disabled="!attrs.showTime" width="140">
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
              slot="reference"
              :class="{'i-filter-active': attrs.showTime, 'i-filter-inactive': !attrs.showTime}"
              class="el-icon-setting i-filter"
            />
          </el-popover>
        </span>

      </div>
    </el-col>

    <el-col :span="16">
      <div class="filter-options-right">
        <span style="padding-right: 10px;">
          <el-checkbox v-model="attrs.showTitle" @change="showTitleChange">{{ $t('panel.show_title') }}
          </el-checkbox>
          <el-popover v-model="titlePopovervisible" placement="bottom-end" :disabled="!attrs.showTitle" width="200">
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
          <el-checkbox v-model="attrs.enableRange" @change="enableRangeChange"><span>
            {{ $t('panel.custom_scope') }} </span> </el-checkbox>

          <el-popover v-model="popovervisible" placement="bottom-end" :disabled="!attrs.enableRange" width="200">
            <div class="view-container-class">
              <el-checkbox-group v-model="attrs.viewIds" @change="checkedViewsChange">
                <el-checkbox
                  v-for="(item ) in childViews.viewInfos"
                  :key="item.id"
                  :label="item.id"
                  class="de-checkbox"
                >
                  <div class="span-div">
                    <svg-icon :icon-class="item.type" class="chart-icon" />
                    <span v-if="item.name && item.name.length <= 7" style="margin-left: 6px">{{ item.name }}</span>
                    <el-tooltip v-else class="item" effect="dark" :content="item.name" placement="left">
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
        <span v-if="showParams" style="padding-left: 10px;">
          <el-checkbox v-model="attrs.enableParameters" @change="enableParametersChange"><span>
            {{ $t('panel.binding_parameters') }} </span> </el-checkbox>

          <el-popover placement="bottom-end" :disabled="!attrs.enableParameters" width="200">
            <div class="view-container-class">
              <el-checkbox-group v-model="attrs.parameters">
                <el-checkbox
                  v-for="(item ) in childViews.datasetParams"
                  :key="item.id"
                  :label="item.id"
                  class="de-checkbox"
                >
                  <div class="span-div">
                    <span
                      v-if="item.alias && item.alias.length <= 7"
                      style="margin-left: 6px"
                    >{{ item.alias }}</span>
                    <el-tooltip v-else class="item" effect="dark" :content="item.alias" placement="left">
                      <span style="margin-left: 6px">{{ item.alias }}</span>
                    </el-tooltip>
                  </div>

                </el-checkbox>
              </el-checkbox-group>
            </div>

            <i
              slot="reference"
              :class="{'i-filter-active': attrs.enableParameters, 'i-filter-inactive': !attrs.enableParameters}"
              class="el-icon-setting i-filter"
            />
          </el-popover>
        </span>
      </div>

    </el-col>
  </el-row>
</template>

<script>

export default {
  name: 'FilterControl',
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
    }
  },
  data() {
    return {
      showParams: false,
      attrs: null,
      titlePopovervisible: false,
      popovervisible: false,
      parametersVisible: false,
      timePopovervisible: false,
      accuracyOptions: [
        { id: 'HH', name: 'HH' },
        { id: 'HH:mm', name: 'HH:mm' },
        { id: 'HH:mm:ss', name: 'HH:mm:ss' }
      ]
    }
  },
  computed: {},
  watch: {
    'childViews.datasetParams': {
      handler(newName, oldName) {
        if (this.attrs.parameters.length > 0 && this.attrs.parameters[0].indexOf('|DE|') === -1) {
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
      }
    }
  },

  created() {
    this.attrs = this.controlAttrs
    if ('timeYearWidget,timeMonthWidget,timeDateWidget,textSelectWidget,numberSelectWidget'.indexOf(this.widget.name) !== -1) {
      this.showParams = true
    }
  },
  methods: {
    multipleChange(value) {
      this.fillAttrs2Filter()
    },
    showTimeChange(value) {
      this.attrs.accuracy = this.accuracyOptions[1].id
      this.attrs.default.isDynamic = false
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
}

.filter-options-right {
  align-items: center;
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
  flex-wrap: nowrap;
  height: 50px;
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
