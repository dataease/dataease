<script lang="ts" setup>
import { computed, nextTick, onMounted, PropType, reactive, ref, watch } from 'vue'
import { getGeoJsonFile, parseJson } from '../../../js/util'
import { forEach, debounce } from 'lodash-es'
import { toRefs } from 'vue'

const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
    required: true
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const emit = defineEmits(['onMapMappingChange'])
const { chart, themes } = toRefs(props)
watch(
  [() => chart.value?.senior.areaMapping, () => chart.value?.customAttr.map.id],
  () => {
    init()
  },
  { deep: true }
)
const editAreaId = ref('body')
const curMappedName = ref('')
const curOriginName = ref('')
const isEdit = ref(false)
const search = ref('')
const areaNameInput = ref(null)
const areaData = reactive([])
const dynamicAreaId = computed(() => {
  return chart.value?.customAttr.map.id
})
const state = reactive({
  mappingForm: {},
  currentData: []
})
const pageInfo = reactive({
  pageSize: 10,
  total: 100,
  currentPage: 1
})
const init = async () => {
  const chartObj = JSON.parse(JSON.stringify(chart.value))
  if (chartObj?.senior) {
    let senior = parseJson(chartObj.senior)
    state.mappingForm = senior.areaMapping
    let curAreaMapping = state.mappingForm?.[dynamicAreaId.value]
    if (!curAreaMapping) {
      curAreaMapping = await getAreaMapping(dynamicAreaId.value)
    }
    const tmp = []
    forEach(curAreaMapping, (val, key) => {
      tmp.push({
        originName: key,
        mappedName: val
      })
    })
    state.currentData.splice(0, state.currentData.length, ...tmp)
    pageInfo.total = state.currentData.length
    updateAreaData()
  }
}

const getAreaMapping = async areaId => {
  if (!areaId) {
    return {}
  }
  const geoJson = await getGeoJsonFile(areaId)
  return geoJson.features.reduce((p, n) => {
    p[n.properties.name] = n.properties.name
    return p
  }, {})
}
const triggerEdit = scope => {
  editAreaId.value = `#area-${scope.$index}-input`
  curOriginName.value = scope.row.originName
  curMappedName.value = scope.row.mappedName
  isEdit.value = true
  nextTick(areaNameInput.value?.focus)
}
const finishEdit = () => {
  editAreaId.value = 'body'
  isEdit.value = false
  let areaNameMap = state.mappingForm[dynamicAreaId.value]
  if (!areaNameMap) {
    areaNameMap = state.currentData.reduce((p, n) => {
      p[n.originName] = n.mappedName
      return p
    }, {})
  }
  const oldMappedName = areaNameMap[curOriginName.value]
  if (oldMappedName === curMappedName.value) {
    return
  }
  areaNameMap[curOriginName.value] = curMappedName.value
  if (!state.mappingForm[dynamicAreaId.value]) {
    state.mappingForm[dynamicAreaId.value] = areaNameMap
  }
  onMapMappingChange()
}
const updateAreaData = debounce(() => {
  const filteredData = state.currentData.filter(item => {
    if (!search.value?.trim()) {
      return true
    }
    return item.mappedName?.includes(search.value)
  })
  const start = (pageInfo.currentPage - 1) * pageInfo.pageSize
  const end = start + pageInfo.pageSize
  areaData.splice(0, areaData.length, ...filteredData.slice(start, end))
  pageInfo.total = filteredData.length
}, 300)
const onMapMappingChange = () => {
  emit('onMapMappingChange', state.mappingForm)
}
onMounted(() => {
  init()
})
</script>
<template>
  <div style="width: 100%">
    <el-table
      size="mini"
      class="area-map-table"
      :class="'area-map-table-' + themes"
      :header-cell-class-name="'area-map-table-header-cell-' + themes"
      :header-row-class-name="'area-map-table-header-row-' + themes"
      :cell-class-name="'area-map-table-cell-' + themes"
      :row-class-name="'area-map-table-row-' + themes"
      :data="areaData"
    >
      <el-table-column label="图形" prop="originName" width="80" show-overflow-tooltip />
      <el-table-column width="140">
        <template #header>
          <span>属性</span>
          <el-input
            v-model="search"
            size="small"
            class="area-filter"
            :effect="themes"
            @input="updateAreaData"
          />
        </template>
        <template #default="scope">
          <div :id="`area-${scope.$index}-input`"></div>
          <el-button
            v-show="!isEdit || editAreaId !== `#area-${scope.$index}-input`"
            class="area-edit-btn"
            size="small"
            :class="'area-edit-btn-' + themes"
            @click="triggerEdit(scope)"
          >
            <span :title="scope.row.mappedName">{{ scope.row.mappedName }}</span>
            <el-icon><Edit /></el-icon>
          </el-button>
        </template>
      </el-table-column>
      <template #empty> 暂无数据 </template>
    </el-table>
    <el-pagination
      small
      hide-on-single-page
      layout="prev, pager, next"
      class="area-page"
      v-model:current-page="pageInfo.currentPage"
      :total="pageInfo.total"
      :page-size="pageInfo.pageSize"
      :pager-count="5"
      @currentChange="updateAreaData"
    />
    <teleport :to="editAreaId" :disabled="!isEdit">
      <div v-show="isEdit">
        <el-input
          v-model="curMappedName"
          size="small"
          ref="areaNameInput"
          :effect="themes"
          @blur="finishEdit()"
          @keyup.enter="$event.target.blur()"
        />
      </div>
    </teleport>
  </div>
</template>
<style lang="less" scoped>
.area-filter {
  display: inline-block;
  width: 80px;
  margin-left: 8px;
}
.area-edit-btn {
  width: 116px;
  padding: 0 4px;
  :deep(> span) {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    width: 100%;
    span {
      max-width: 100px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  &-dark {
    border-color: rgba(255, 255, 255, 0.3);
    color: #fff;
    background-color: transparent;
  }
}
:deep(.area-page) {
  button,
  button[disabled] {
    color: grey;
    background: transparent !important;
    &:active,
    &:hover {
      background: transparent !important;
    }
  }
  ul li {
    &:not(.is-active) {
      color: grey;
    }
    background: transparent !important;
  }
}
.area-map-table {
  width: 100%;
  font-size: 12px;
  :deep(.area-map-table-header-cell-light) {
    background-color: #f5f6f7;
  }
  :deep(.area-map-table-header-cell-dark) {
    background-color: #1a1a1a;
    color: @canvas-main-font-color-dark;
  }
  :deep(.area-map-table-row-dark) {
    .area-map-table-cell-dark {
      background-color: @side-content-background;
      color: @canvas-main-font-color-dark;
      border-bottom: 1px solid #373737;
    }
    &:hover {
      .area-map-table-cell-dark {
        background-color: #434343;
      }
    }
  }
  &-dark {
    :deep(.ed-table__empty-block) {
      background-color: @side-content-background;
    }
  }
}
</style>
