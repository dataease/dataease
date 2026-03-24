<script lang="ts" setup>
import { nextTick, onBeforeMount, PropType, reactive, ref, toRefs } from 'vue'
import { debounce, forEach, cloneDeep } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const props = defineProps({
  selectedData: {
    type: Object as PropType<{ [key: string]: string }>,
    required: true
  },
  themes: {
    type: String,
    default: 'plain'
  }
})

const emit = defineEmits(['onPlaceNameMappingChange'])

const { selectedData, themes } = toRefs(props)
const dialogVisible = ref(false)
const editAreaId = ref('body')
const curMappedName = ref('')
const curOriginName = ref('')
const curEditIndex = ref(-1)
const isEdit = ref(false)
const search = ref('')
const areaNameInput = ref(null)
const areaData = reactive([])
const showLoading = ref(false)

// 本地编辑数据（用户修改后的数据）
const state = reactive({
  currentData: [] as Array<{ originName: string; mappedName: string }>
})

// 原始数据备份（用于取消时恢复）
const originalData = ref<Array<{ originName: string; mappedName: string }>>([])

const pageInfo = reactive({
  pageSize: 10,
  total: 100,
  currentPage: 1
})

const init = () => {
  search.value = ''
  areaData.length = 0
  dialogVisible.value = true
  const chartObj = JSON.parse(selectedData.value.geoJson)
  let curAreaMapping = chartObj.deMapping
  if (!curAreaMapping) {
    curAreaMapping = getAreaMapping()
  }
  const tmp = []
  forEach(curAreaMapping, (val, key) => {
    tmp.push({
      originName: key,
      mappedName: val
    })
  })
  state.currentData = tmp
  // 备份原始数据
  originalData.value = cloneDeep(tmp)
  pageInfo.total = state.currentData.length
  updateAreaData()
  showLoading.value = false
}

const getAreaMapping = () => {
  const geoJson = JSON.parse(selectedData.value.geoJson)
  const names = Object.keys(geoJson?.features[0]?.properties).filter(key => key.startsWith('NAME_'))
  const nameKey = names[names.length - 1]
  return geoJson.features.reduce((p, n) => {
    if (n.properties.name) {
      p[n.properties.name] = n.properties.name
    } else {
      p[n.properties[nameKey]] = n.properties[nameKey]
    }
    return p
  }, {})
}

const triggerEdit = scope => {
  editAreaId.value = `#area-${scope.$index}-input`
  curOriginName.value = scope.row.originName
  curMappedName.value = scope.row.mappedName
  curEditIndex.value = scope.$index
  isEdit.value = true
  nextTick(() => areaNameInput.value?.focus())
}

const finishEdit = () => {
  if (!isEdit.value) return

  editAreaId.value = 'body'
  isEdit.value = false

  // 只更新本地数据，不触发上层更新
  if (curMappedName.value?.trim()) {
    // 找到当前编辑的项在 currentData 中的位置
    const actualIndex = state.currentData.findIndex(item => item.originName === curOriginName.value)
    if (actualIndex !== -1) {
      state.currentData[actualIndex].mappedName = curMappedName.value.trim()
    }
    // 更新当前页显示的数据
    const pageItem = areaData.find(item => item.originName === curOriginName.value)
    if (pageItem) {
      pageItem.mappedName = curMappedName.value.trim()
    }
  }

  curEditIndex.value = -1
}

const updateAreaData = debounce(() => {
  const filteredData = state.currentData.filter(item => {
    if (!search.value?.trim()) {
      return item.originName
    }
    return item.mappedName?.includes(search.value)
  })
  const start = (pageInfo.currentPage - 1) * pageInfo.pageSize
  const end = start + pageInfo.pageSize
  areaData.splice(0, areaData.length, ...filteredData.slice(start, end))
  pageInfo.total = filteredData.length
}, 300)

const resetForm = () => {
  // 取消时恢复原始数据
  state.currentData = cloneDeep(originalData.value)
  dialogVisible.value = false
}

const submitForm = () => {
  // 构建映射对象
  const mappingForm = state.currentData.reduce((p, n) => {
    p[n.originName] = n.mappedName
    return p
  }, {})
  // 统一提交到上层
  emit('onPlaceNameMappingChange', mappingForm)
  dialogVisible.value = false
}
onBeforeMount(() => {
  showLoading.value = true
})
defineExpose({
  init
})
</script>
<template>
  <el-drawer
    :title="t('chart.place_name_mapping')"
    v-model="dialogVisible"
    modal-class="geometry-info-drawer"
    size="600px"
    direction="rtl"
  >
    <div style="width: 100%">
      <el-table
        class="area-map-table"
        :class="'area-map-table-' + themes"
        :header-cell-class-name="'area-map-table-header-cell-' + themes"
        :header-row-class-name="'area-map-table-header-row-' + themes"
        :cell-class-name="'area-map-table-cell-' + themes"
        :row-class-name="'area-map-table-row-' + themes"
        :data="areaData"
        v-loading="showLoading"
        v-if="areaData"
      >
        <el-table-column label="图形" prop="originName" show-overflow-tooltip />
        <el-table-column>
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
        <template #empty>
          <span>暂无数据</span>
        </template>
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
        style="padding-top: 24px"
        v-if="areaData && areaData.length"
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
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm()">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm()">
          {{ t('commons.save') }}
        </el-button>
      </span>
    </template>
  </el-drawer>
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
    &:hover {
      background-color: #1a1a1a;
    }
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
    :deep(.ed-table__header-wrapper) {
      border-top: unset;
    }
  }
}
</style>
