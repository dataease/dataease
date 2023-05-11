<script lang="ts" setup>
import { ref, reactive, shallowRef, provide } from 'vue'
import draggable from 'vuedraggable'
import FilterHead from './FilterHead.vue'
const initials = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j']

const selectValue = ref()
const options = Array.from({ length: 10 }).map((_, idx) => ({
  value: `Option ${idx + 1}`,
  label: `${initials[idx % 10]}${idx}`
}))
const multiple = ref(false)
const activeName = ref('setting')
const activeNameData = ref('dataset')
const allParams = shallowRef([])
const curTableViews = shallowRef([])
const fieldType = (deType: number) => {
  return ['text', 'time', 'value', 'value', 'location'][deType]
}
const widget = reactive({
  showSwitch: false,
  isTimeWidget: () => true,
  isSortWidget: () => true
})
const customStyle = reactive({
  border: '',
  background: '',
  text: ''
})
provide('$custom-style-filter', customStyle)
const expandedD = ref(false)
const expandedQ = ref(false)

const quota = shallowRef([])
const dimensions = shallowRef([])

dimensions.value = [
  {
    name: '123',
    deType: 0,
    id: '123'
  }
]

quota.value = [
  {
    name: 'dfad',
    deType: 2,
    id: '1239'
  },
  {
    name: 'dflad',
    deType: 2,
    id: '12399'
  }
]

const num = ref(0)
const datasetList = shallowRef([])
const chartList = shallowRef([])
const tagList = ref([])
const attrs = reactive({
  multiple: false,
  showTime: false,
  defaultValue: false,
  showTitle: false,
  parameters: [],
  title: '',
  enableRange: false,
  enableParameters: false,
  viewIds: []
})

const multipleChange = (val: boolean) => {
  multiple.value = val
  selectValue.value = val ? [] : undefined
}

const handleTabClick = () => {
  console.log('handleTabClick')
}
</script>

<template>
  <el-select-v2
    v-model="selectValue"
    filterable
    :show-checked="multiple"
    :multiple="multiple"
    :collapse-tags="multiple"
    :options="options"
    :collapse-tags-tooltip="multiple"
    placeholder="Please select"
    style="width: 240px"
  >
    <template v-if="!multiple" #default="{ item }">
      <el-radio-group v-model="selectValue">
        <el-radio :label="item.value">{{ item.label }}</el-radio>
      </el-radio-group>
    </template>
  </el-select-v2>
  <div class="config-filter">
    <div class="name">
      选项值来源
      <el-icon>
        <Icon name="icon_view-list_outlined"></Icon>
      </el-icon>
    </div>
    <el-tabs v-model="activeNameData" @tab-click="handleTabClick">
      <el-tab-pane label="数据集" name="dataset">
        <div class="setting-item">
          <div class="value">
            <el-tree-select v-model="attrs.title" :data="chartList" :render-after-expand="false" />
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="图表" name="chart">
        <div class="setting-item">
          <div class="value">
            <el-select filterable v-model="attrs.title">
              <el-option
                v-for="item in datasetList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    <div class="filed-list field-d">
      <div :class="['filed-name', { expanded: expandedD }]" @click="expandedD = !expandedD">
        <ElIcon class="expand">
          <Icon name="icon_expand-right_filled"></Icon>
        </ElIcon>
        &nbsp;维度
      </div>
      <template v-if="expandedD">
        <draggable
          :list="dimensions"
          :group="{ name: 'dimension', pull: 'clone', put: false }"
          item-key="id"
        >
          <template #item="{ element }">
            <div class="filter-db-row">
              <el-icon>
                <Icon
                  :name="`field_${fieldType(element.deType)}`"
                  :className="`field-icon-${fieldType(element.deType)}`"
                ></Icon>
              </el-icon>
              <span :title="element.name">{{ element.name }}</span>
            </div>
          </template>
        </draggable>
      </template>
    </div>
    <div class="filed-list field-d">
      <div :class="['filed-name', { expanded: expandedQ }]" @click="expandedQ = !expandedQ">
        <ElIcon class="expand">
          <Icon name="icon_expand-right_filled"></Icon>
        </ElIcon>
        &nbsp;指标
      </div>
      <template v-if="expandedQ">
        <draggable
          :list="quota"
          :group="{ name: 'dimension', pull: 'clone', put: false }"
          item-key="id"
        >
          <template #item="{ element }">
            <div class="filter-db-row">
              <el-icon>
                <Icon
                  :name="`field_${fieldType(element.deType)}`"
                  :className="`field-icon-${fieldType(element.deType)}`"
                ></Icon>
              </el-icon>
              <span :title="element.name">{{ element.name }}</span>
            </div>
          </template>
        </draggable>
      </template>
    </div>
  </div>
  <div class="config-filter">
    <div class="name">
      文本下拉组件
      <el-icon>
        <Icon name="icon_view-list_outlined"></Icon>
      </el-icon>
    </div>
    <el-tabs v-model="activeName">
      <el-tab-pane label="设置" name="setting">
        <div class="setting-item">
          <div class="title">选项值</div>
          <div class="value drag-area">
            <FilterHead :drag-items="tagList"></FilterHead>
          </div>
        </div>
        <div class="setting-item">
          <div class="title">
            <el-checkbox v-model="attrs.showTitle">标题</el-checkbox>
          </div>
          <div class="value">
            <el-input v-model="attrs.title"></el-input>
          </div>
        </div>
        <div class="setting-item">
          <div class="title">
            <el-checkbox v-model="attrs.enableParameters">参数</el-checkbox>
          </div>
          <div class="value">
            <el-select filterable v-model="attrs.parameters">
              <el-option
                v-for="item in allParams"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </div>
        </div>
        <div class="setting-item">
          <div class="title">
            <el-checkbox v-model="attrs.enableRange">控制范围</el-checkbox>
          </div>
          <div class="value">
            <el-select multiple filterable v-model="attrs.viewIds">
              <el-option
                v-for="item in curTableViews"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </div>
        </div>
        <div class="setting-item">
          <div class="title">
            <el-checkbox @change="multipleChange" v-model="attrs.multiple"
              >是否允许多选</el-checkbox
            >
          </div>
        </div>
        <div class="setting-item">
          <div class="title">
            <el-checkbox v-model="attrs.defaultValue">设置默认值</el-checkbox>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="样式" name="style">
        <div class="setting-item">
          <div class="title">
            <el-checkbox v-model="attrs.enableRange">颜色</el-checkbox>
          </div>
          <div class="value">
            <div class="color">
              背景: <el-color-picker v-model="customStyle.background" show-alpha />
            </div>
            <div class="color">
              边框: <el-color-picker v-model="customStyle.border" show-alpha />
            </div>
            <div class="color">颜色: <el-color-picker v-model="customStyle.text" show-alpha /></div>
          </div>
        </div>
        <div class="setting-item">
          <div class="title">
            <el-checkbox v-model="attrs.enableRange">内边距</el-checkbox>
          </div>
          <div class="value">
            <el-input-number
              v-model="num"
              :min="1"
              :max="10"
              size="small"
              controls-position="right"
            />
          </div>
        </div>
        <div class="setting-item">
          <div class="title">
            <el-checkbox v-model="attrs.enableRange">边框半径</el-checkbox>
          </div>
          <div class="value">
            <el-input-number
              v-model="num"
              :min="1"
              :max="10"
              size="small"
              controls-position="right"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style lang="less" scoped>
.config-filter {
  height: 100%;
  overflow: auto;
  width: 200px;
  padding: 16px;
  float: right;

  .name {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .setting-item {
    margin-top: 12px;
    .title {
      margin-bottom: 8px;
    }

    .value {
      .color {
        padding-left: 16px;
        margin-bottom: 8px;
      }
    }

    .drag-area {
      width: 100%;
      height: 32px;
      border: 1px solid #ccc;
      border-radius: 3px;
      display: flex;
      align-items: center;
      padding: 0 10px;
    }
  }

  .filed-list {
    padding: 0 8px;
    position: relative;

    .filter-db-row {
      background-color: #3685f2;
      color: #fff;
      margin: 5px 0;
      span {
        margin-left: 6px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
    .filed-name {
      cursor: pointer;
      position: sticky;
      top: 0;
      height: 32px;
      font-family: 'PingFang SC';
      font-style: normal;
      font-weight: 500;
      font-size: 14px;
      color: #1f2329;
      display: flex;
      align-items: center;

      i {
        color: #646a73;
      }

      &.expanded {
        .expand {
          transform: rotate(90deg);
        }
      }
    }
    max-height: 200px;
    overflow-y: auto;
  }

  .field-d {
    border-bottom: 1px solid rgba(31, 35, 41, 0.15);
  }
}
</style>
