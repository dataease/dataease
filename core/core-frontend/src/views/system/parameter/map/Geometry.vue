<template>
  <el-container class="geometry-container">
    <el-aside class="geonetry-aside">
      <div class="geo-title">
        <span>{{ t('online_map.geometry') }}</span>
      </div>
      <div class="geo-search">
        <el-input
          class="m16 w100"
          v-model="keyword"
          clearable
          :placeholder="t('commons.search')"
          @change="filterResource"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"
                ><icon_searchOutline_outlined class="svg-icon"
              /></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>
      <div class="map-tree-container">
        <el-scrollbar class="menu-tree">
          <el-tree
            menu
            ref="areaTreeRef"
            node-key="id"
            :data="treeData"
            @node-click="handleNodeClick"
            :highlight-current="true"
            :expand-on-click-node="false"
            :default-expand-all="false"
            :filter-node-method="filterResourceNode"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node" :class="{ 'is-disabled': node.disabled || data.root }">
                <span
                  class="geo-name-span"
                  :title="data.name"
                  v-html="data.colorName && keyword ? data.colorName : data.name"
                />
                <span v-if="data.id === '000'" class="add-icon-span" @click.stop="add()">
                  <el-icon>
                    <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
                  </el-icon>
                </span>
                <span class="geo-operate-container" v-if="data.custom">
                  <el-tooltip
                    class="box-item"
                    effect="dark"
                    :content="t('common.delete')"
                    placement="top"
                  >
                    <el-icon @click.stop="delHandler(data)" class="hover-icon">
                      <Icon name="icon_delete-trash_outlined">
                        <icon_deleteTrash_outlined class="svg-icon" />
                      </Icon>
                    </el-icon>
                  </el-tooltip>
                </span>
              </span>
            </template>
          </el-tree>
          <el-tree
            menu
            ref="customAreaTreeRef"
            node-key="id"
            :data="customTreeData"
            :highlight-current="true"
            :expand-on-click-node="false"
            :default-expand-all="false"
            :filter-node-method="filterResourceNode"
            @node-click="loadCustomSubArea"
          >
            <template #default="{ data }">
              <span class="custom-area-root">
                <span class="label" :title="data.name">
                  {{ data.name }}
                </span>
                <span class="opt-icon" v-if="data.id === '000'" @click.stop="editCustomArea()">
                  <el-icon>
                    <Icon name="icon_add_outlined"><icon_add_outlined /></Icon>
                  </el-icon>
                </span>
                <el-dropdown placement="bottom-end" popper-class="area-opt-popper" v-else>
                  <span class="opt-icon">
                    <el-icon>
                      <Icon name="icon_more_outlined"><icon_more_outlined /></Icon>
                    </el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="editCustomArea(data)">
                        {{ t('chart.rename') }}
                      </el-dropdown-item>
                      <el-dropdown-item @click.stop="deleteCustomArea(data)">
                        {{ t('common.delete') }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </span>
            </template>
          </el-tree>
        </el-scrollbar>
      </div>
    </el-aside>
    <el-main class="geometry-main">
      <template v-if="showGeoJson">
        <div class="geo-content-container" v-if="!selectedData">
          <EmptyBackground img-type="noneWhite" :description="t('system.on_the_left')" />
        </div>
        <div v-else class="geo-content-container">
          <div class="geo-content-top">
            <span>{{ selectedData.name }}</span>
          </div>
          <div class="geo-content-middle">
            <div class="geo-area">
              <div class="area-label">
                <span>{{ t('system.region_code') }}</span>
              </div>
              <div class="area-content">
                <span>{{ selectedData.id }}</span>
              </div>
            </div>
            <div class="geo-area">
              <div class="area-label">
                <span>{{ t('system.superior_region') }}</span>
              </div>
              <div class="area-content">
                <span>{{ selectedData.parentName || '-' }}</span>
                <span v-if="selectedData.pid" class="area-secondary">{{
                  '(' + selectedData.pid + ')'
                }}</span>
              </div>
            </div>
          </div>
          <div class="geo-content-bottom">
            <div class="area-label">
              <span>{{ t('system.coordinate_file') }}</span>
            </div>
            <el-scrollbar class="area-content-geo">
              <span>{{ selectedData.geoJson }}</span>
            </el-scrollbar>
          </div>
        </div>
      </template>
      <template v-else>
        <div v-if="showCustomEmpty">
          <EmptyBackground img-type="noneWhite" :description="t('system.on_the_left')" />
        </div>
        <div class="sub-area-view" v-else>
          <div id="map-container" class="map-container"></div>
          <div class="sub-area-editor">
            <el-divider />
            <span class="header">
              <span class="label">
                <span>{{ t('system.custom_area') }} </span>
                <span>({{ t('system.custom_area_tip') }})</span>
              </span>
              <span class="add-btn" @click="editCustomSubArea()">
                <el-icon>
                  <Icon name="icon_add_outlined"><icon_add_outlined /></Icon>
                </el-icon>
                <span>{{ t('system.add_area') }}</span>
              </span>
            </span>
            <el-table :data="subAreaList" stripe style="width: 100%" :height="300">
              <el-table-column prop="name" :label="t('system.area_name')">
                <template #default="{ row, $index }">
                  <span
                    class="area-color-symbol"
                    :style="{ backgroundColor: AREA_COLOR[$index % AREA_COLOR.length] }"
                  ></span>
                  <span>{{ row.name }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="scopeName" :label="t('system.area_scope')" />
              <el-table-column fixed="right" :label="t('system.operation')" min-width="120">
                <template #default="{ row }">
                  <div class="area-edit-btn">
                    <span @click="editCustomSubArea(row)">
                      <el-icon>
                        <Icon name="icon_edit_outlined"><icon_edit_outlined /></Icon>
                      </el-icon>
                    </span>
                    <span @click="deleteCustomSubArea(row)">
                      <el-icon>
                        <Icon name="icon_delete-trash_outlined"><icon_deleteTrash_outlined /></Icon>
                      </el-icon>
                    </span>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </template>
    </el-main>
  </el-container>
  <geometry-edit ref="editor" @saved="loadTreeData(false)" />
  <el-dialog
    v-model="customAreaDialog"
    class="create-dialog"
    :title="`${editedCustomArea.id ? t('common.edit') : t('common.add')} ${t(
      'system.custom_area'
    )}`"
    width="500"
    destroy-on-close
  >
    <el-form
      ref="areaFormRef"
      :model="editedCustomArea"
      label-position="top"
      label-width="auto"
      :rules="areaRules"
    >
      <el-form-item label-position="top" required prop="name">
        <el-input v-model="editedCustomArea.name" :minlenegth="1" :maxlength="50" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="customAreaDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveGeoArea()"> {{ t('common.sure') }} </el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog
    v-model="customSubAreaDialog"
    :title="`${customSubArea.id ? t('common.edit') : t('common.add')} ${t('system.custom_area')}`"
    width="500"
    destroy-on-close
  >
    <el-form
      ref="subAreaFormRef"
      :model="customSubArea"
      label-position="top"
      label-width="auto"
      :rules="areaRules"
    >
      <el-form-item :label="t('system.area_name')" label-position="top" prop="name">
        <el-input v-model="customSubArea.name" :minlenegth="1" :maxlength="50" />
      </el-form-item>
      <el-form-item :label="t('system.sub_area_tip')" label-position="top" prop="scopeArr">
        <el-select v-model="customSubArea.scopeArr" multiple style="width: 100%" filterable>
          <el-option
            v-for="item in subAreaOptions"
            :key="item.id"
            :value="item.id"
            :label="item.name"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="customSubAreaDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveGeoSubArea()"> {{ t('common.sure') }} </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import icon_more_outlined from '@/assets/svg/icon_more_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import { onBeforeMount, reactive, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import {
  getWorldTree,
  listCustomGeoArea,
  saveCustomGeoArea,
  saveCustomGeoSubArea,
  listSubAreaOptions,
  deleteCustomGeoArea,
  getCustomGeoArea,
  deleteCustomGeoSubArea
} from '@/api/map'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { getGeoJsonFile } from '@/views/chart/components/js/util'
import { cloneDeep, debounce } from 'lodash-es'
import { setColorName } from '@/utils/utils'
import GeometryEdit from './GeometryEdit.vue'
import { useCache } from '@/hooks/web/useCache'
import { ElMessage, ElMessageBox, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
import { Choropleth } from '@antv/l7plot/dist/esm/plots/choropleth'
import { ChoroplethOptions, TextLayer } from '@antv/l7plot/dist/esm'
import { nextTick } from 'vue'
import { centroid } from '@turf/centroid'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
const { wsCache } = useCache()
const { t } = useI18n()
const keyword = ref('')
const treeData = ref([])
const editor = ref()
interface Tree {
  label: string
  children?: Tree[]
}
const areaTreeRef = ref(null)
const loading = ref(false)
const selectedData = ref(null)
const showGeoJson = ref(true)

const handleNodeClick = async (data: Tree) => {
  selectedData.value = data
  customAreaTreeRef.value?.setCurrentKey(null)
  mapInstance?.destroy()
  mapInstance = null
  curCustomGeoArea.id = ''
  const geoJson = cloneDeep(await getGeoJsonFile(data['id']))
  selectedData.value['geoJson'] = JSON.stringify(geoJson)
  const pid = data['pid']
  if (pid) {
    const parent = areaTreeRef.value.getNode(pid)
    if (parent) {
      selectedData.value.parentName = parent.data.name
    }
  }
  showGeoJson.value = true
}
const delHandler = data => {
  ElMessageBox.confirm(t('system.delete_this_node'), {
    confirmButtonType: 'danger',
    type: 'warning',
    confirmButtonText: t('common.delete'),
    cancelButtonText: t('dataset.cancel'),
    autofocus: false,
    showClose: false
  })
    .then(() => {
      const url = '/geometry/delete/' + data.id
      request.post({ url }).then(() => {
        if (selectedData.value?.id === data.id) {
          selectedData.value = null
        }
        ElMessage.success(t('common.delete_success'))
        loadTreeData(false)
      })
    })
    .catch(() => {
      loading.value = false
    })
}
const filterResource = val => {
  areaTreeRef.value?.filter(val)
}
const filterResourceNode = (value: string, data) => {
  setColorName(data, value)
  if (!value) return true
  return data.name.toLocaleLowerCase().includes(value.toLocaleLowerCase())
}

const loadTreeData = (cache?: boolean) => {
  const key = 'de-area-tree'
  const result = wsCache.get(key)
  if (result && cache) {
    treeData.value = result
    return
  }
  getWorldTree()
    .then(res => {
      const root = res.data
      treeData.value = [root]
      wsCache.set(key, treeData.value)
    })
    .catch(e => {
      console.error(e)
    })
}

const add = (pid?: string) => {
  editor?.value.edit(pid)
}

loadTreeData(true)

// geoArea
const customTreeData = ref([
  {
    id: '000',
    name: t('system.custom_area')
  }
])
const customAreaDialog = ref(false)
const curCustomGeoArea: CustomGeoArea = reactive({
  id: '',
  name: ''
})
const customAreaTreeRef = ref()
const areaFormRef = ref()
const saveGeoArea = async () => {
  areaFormRef.value?.validate(async valid => {
    if (valid) {
      const res = await saveCustomGeoArea(editedCustomArea)
      if (res.code) {
        ElMessage.error(res.msg)
        return
      }
      await loadCustomGeoArea()
      customAreaDialog.value = false
    }
  })
}
const loadCustomGeoArea = async () => {
  await listCustomGeoArea().then(res => {
    if (res.data?.length) {
      customTreeData.value[0].children = res.data
      if (curCustomGeoArea.id) {
        nextTick(() => {
          customAreaTreeRef.value?.setCurrentKey(curCustomGeoArea.id)
        })
      }
    } else {
      customTreeData.value[0].children = null
    }
  })
}
const editedCustomArea = reactive({
  id: '',
  name: ''
})
const editCustomArea = (data?) => {
  if (data) {
    editedCustomArea.id = data.id
    editedCustomArea.name = data.name
  } else {
    editedCustomArea.id = ''
    editedCustomArea.name = ''
  }
  customAreaDialog.value = true
}
const deleteCustomArea = data => {
  ElMessageBox.confirm(t('system.delete_custom_area_tip'), '', {
    type: 'warning',
    confirmButtonType: 'danger',
    customClass: 'area-delete-dialog',
    autofocus: false,
    confirmButtonText: t('common.delete'),
    showClose: false
  })
    .then(async () => {
      await deleteCustomGeoArea(data.id)
      await loadCustomGeoArea()
      if (!customTreeData.value[0].children?.length || data.id === curCustomGeoArea.id) {
        showCustomEmpty.value = true
        mapInstance?.destroy()
        mapInstance = null
      } else {
        showCustomEmpty.value = false
      }
    })
    .catch(() => {
      //
    })
}
let areaNameMap: Record<string, string> = null
const loadSubAreaOptions = () => {
  listSubAreaOptions().then(res => {
    subAreaOptions.value.splice(0, subAreaOptions.value.length, ...res.data)
    if (!areaNameMap) {
      areaNameMap = subAreaOptions.value.reduce((p, n) => {
        p[n.id] = n.name
        return p
      }, {})
    }
  })
}

const showCustomEmpty = ref(false)
const loadCustomSubArea = async (node, reload?) => {
  areaTreeRef.value?.setCurrentKey(null)
  if (node.id === '000' || (curCustomGeoArea.id === node.id && reload !== true)) {
    return
  }
  showCustomEmpty.value = false
  curCustomGeoArea.id = node.id
  getCustomGeoArea(node.id).then(res => {
    const tmpList = res.data.reduce((p, n) => {
      const ids = n.scope?.split(',') || []
      n.scopeArr = ids
      const nameArr = []
      ids.forEach(id => {
        nameArr.push(areaNameMap?.[id])
      })
      const area = {
        ...n,
        scopeName: nameArr.join(',')
      }
      p.push(area)
      return p
    }, [])
    subAreaList.value.splice(0, subAreaList.value.length, ...tmpList)
    showGeoJson.value = false
    nextTick(() => {
      debounceRender()
    })
  })
}
// geoSubArea
const customSubAreaDialog = ref(false)
const customSubArea = reactive({
  id: '',
  name: '',
  scope: '',
  geoAreaId: '',
  scopeArr: []
})
const subAreaOptions = ref([])
const subAreaList = ref([])
const subAreaFormRef = ref()
const areaRules = reactive<FormRules>({
  name: [
    { type: 'string', required: true, message: t('common.input_name'), trigger: 'change' },
    { min: 1, max: 50, message: t('common.input_limit', [1, 50]), trigger: 'blur' }
  ],
  scopeArr: [
    {
      type: 'array',
      required: true,
      message: t('system.please_select_area'),
      trigger: 'change'
    }
  ]
})
const editCustomSubArea = (subArea?) => {
  customSubArea.geoAreaId = curCustomGeoArea.id
  if (!subArea) {
    customSubArea.name = ''
    customSubArea.scopeArr = []
    customSubArea.id = ''
    customSubArea.scope = ''
  } else {
    customSubArea.name = subArea.name
    customSubArea.scopeArr = subArea.scopeArr
    customSubArea.id = subArea.id
    customSubArea.scope = subArea.scope
  }
  customSubAreaDialog.value = true
}
const saveGeoSubArea = async () => {
  subAreaFormRef.value?.validate(async valid => {
    if (!valid) {
      return
    }
    customSubArea.scope = customSubArea.scopeArr.join(',')
    const res = await saveCustomGeoSubArea(customSubArea)
    if (res.code) {
      ElMessage.error(res.msg)
      return
    }
    await loadCustomSubArea({ id: curCustomGeoArea.id }, true)
    customSubAreaDialog.value = false
  })
}
const deleteCustomSubArea = async data => {
  ElMessageBox.confirm(t('system.delete_custom_sub_area_tip'), '', {
    type: 'warning',
    confirmButtonType: 'danger',
    customClass: 'area-delete-dialog',
    autofocus: false,
    confirmButtonText: t('common.delete'),
    showClose: false
  })
    .then(async () => {
      await deleteCustomGeoSubArea(data.id)
      await loadCustomSubArea({ id: curCustomGeoArea.id }, true)
    })
    .catch(() => {
      //
    })
}
loadCustomGeoArea()
loadSubAreaOptions()

const AREA_COLOR = [
  '#1E90FF',
  '#90EE90',
  '#00CED1',
  '#E2BD84',
  '#7A90E0',
  '#3BA272',
  '#2BE7FF',
  '#0A8ADA',
  '#FFD700'
]
const mapOption: ChoroplethOptions = {
  map: {
    type: 'mapbox',
    style: 'blank'
  },
  geoArea: {
    type: 'geojson'
  },
  source: {
    data: [],
    joinBy: {
      sourceField: 'name',
      geoField: 'name'
    }
  },
  viewLevel: {
    adcode: 'all',
    level: 'world'
  },
  autoFit: true,
  chinaBorder: false,
  style: {
    stroke: 'grey',
    opacity: 1,
    lineWidth: 0.6,
    lineOpacity: 1
  },
  label: false,
  state: {
    active: { stroke: 'green', lineWidth: 1 }
  },
  legend: false,
  tooltip: false,
  // 禁用线上地图数据
  customFetchGeoData: () => null
}
let mapInstance: Choropleth = null
const renderMap = async () => {
  if (!mapOption.source.joinBy.geoData) {
    const chinaGeoJson = cloneDeep(await getGeoJsonFile('156'))
    mapOption.source.joinBy.geoData = chinaGeoJson
  }
  const areaMap = mapOption.source.joinBy.geoData.features.reduce((p, n) => {
    if (n.properties['adcode']) {
      p['156' + n.properties['adcode']] = n
    }
    return p
  }, {})
  const areaTextLocation = []
  subAreaList.value?.forEach(area => {
    const areaJsonArr = []
    area.scopeArr?.forEach(adcode => {
      const json = areaMap[adcode]
      json && areaJsonArr.push(json)
    })
    if (areaJsonArr.length) {
      const areaJson: FeatureCollection = {
        type: 'FeatureCollection',
        features: areaJsonArr
      }
      const center = centroid(areaJson)
      areaTextLocation.push({
        name: area.name,
        x: center.geometry.coordinates[0],
        y: center.geometry.coordinates[1]
      })
    }
  })
  const areaTextLayer = new TextLayer({
    name: 'areaTextLayer',
    source: {
      data: areaTextLocation,
      parser: {
        type: 'json',
        x: 'x',
        y: 'y'
      }
    },
    field: 'name',
    style: {
      fill: 'black',
      fontSize: 20,
      opacity: 1,
      fontWeight: 'bold',
      textAnchor: 'center',
      textAllowOverlap: true
    }
  })
  if (mapInstance) {
    const layer = mapInstance.getLayerByName('areaTextLayer')
    if (layer) {
      mapInstance.removeLayer(layer)
    }
    mapInstance.addLayer(areaTextLayer)
    mapInstance.update({})
  } else {
    mapOption.color = {
      field: ['name', 'adcode'],
      value: area => {
        let color = 'white'
        subAreaList.value?.forEach((subArea, i) => {
          if (subArea.scopeArr?.includes('156' + area.adcode)) {
            color = AREA_COLOR[i % AREA_COLOR.length]
          }
        })
        return color
      },
      scale: {
        type: 'quantize',
        unknown: 'white'
      }
    }
    mapInstance = new Choropleth('map-container', mapOption)
    mapInstance.on('loaded', () => {
      mapInstance.addLayer(areaTextLayer)
    })
  }
}
const debounceRender = debounce(renderMap, 500)
onBeforeMount(() => {
  mapInstance?.destroy()
})
</script>

<style lang="less" scoped>
.geometry-container {
  height: 100%;
  .geonetry-aside {
    width: 280px !important;
    border-right: 1px solid #1f232926;
    padding: 16px;
    height: 100%;
    .geo-title {
      display: flex;
      justify-content: space-between;
      height: 24px;
      line-height: 24px;
      span:first-child {
        font-size: 16px;
        font-weight: 500;
        line-height: 24px;
      }
      margin-bottom: 16px;
    }
    .geo-search {
      margin-bottom: 16px;
    }
    .map-tree-container {
      height: calc(100% - 96px);
      overflow-y: auto;
    }
  }
  .geometry-main {
    padding: 16px !important;
  }
}
.geo-content-container {
  width: 100%;
  height: 100%;
  .geo-content-top {
    height: 24px;
    line-height: 24px;
    margin-bottom: 16px;
    span {
      font-weight: 500;
      font-size: 16px;
      color: #1f2329;
    }
  }
  .geo-content-middle {
    display: flex;
    .geo-area {
      height: 48px;
      width: 50%;
    }
    margin-bottom: 16px;
  }
  :deep(.area-label) {
    height: 22px;
    line-height: 22px;
    span {
      font-size: 14px;
      color: #646a73;
      font-weight: 400;
    }
  }
  :deep(.area-content) {
    line-height: 22px;
    height: 22px;
    span {
      font-size: 14px;
      color: #1f2329;
      font-weight: 400;
    }
    .area-secondary {
      color: #646a73;
    }
  }
  .geo-content-bottom {
    width: 100%;
    height: calc(100% - 110px);
    .area-content-geo {
      line-height: 22px;
      overflow-x: hidden;
      overflow-y: auto;
      height: calc(100% - 30px);
      span {
        font-size: 14px;
        color: #1f2329;
        font-weight: 400;
      }
    }
  }
}
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  padding-right: 4px;
  overflow: hidden;
  justify-content: space-between;
  .geo-name-span {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .geo-operate-container {
    display: none;
  }

  &:hover {
    .geo-operate-container {
      display: inline-flex;
      padding-left: 4px;
    }
  }
  .add-icon-span {
    color: var(--ed-color-primary);
    padding: 3px;
    border-radius: 3px;
    line-height: 1;
    &:hover {
      background: #1f232926;
      cursor: pointer;
    }
  }
}
.custom-area-root {
  display: flex;
  flex: 1;
  justify-content: space-between;
  align-items: center;
  align-content: center;
  padding-right: 4px;
  .label {
    max-width: 150px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .opt-icon {
    color: var(--ed-color-primary);
    padding: 3px;
    border-radius: 3px;
    line-height: 1;
    &:hover {
      background: #1f232926;
      cursor: pointer;
    }
  }
}
.sub-area-view {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  width: 100%;
  height: 100%;
  .map-container {
    flex: 1;
  }
  .ed-divider {
    margin: 10px 0;
  }
  .sub-area-editor {
    height: 350px;
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      .label {
        :first-child {
          font-size: 18px;
          font-weight: bold;
          margin-right: 10px;
        }
        :last-child {
          font-size: 16px;
        }
      }
      .add-btn {
        display: flex;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        color: var(--ed-color-primary);
        padding: 3px;
        :first-child {
          margin-right: 2px;
        }
        &:hover {
          border-radius: 6px;
          background: #1f232926;
        }
      }
    }
    .area-color-symbol {
      width: 10px;
      height: 10px;
      display: inline-block;
      border-radius: 5px;
      margin-right: 4px;
    }
  }
  .area-edit-btn {
    color: var(--ed-color-primary);
    span {
      padding: 3px;
      cursor: pointer;
      &:hover {
        border-radius: 6px;
        background: #1f232926;
      }
    }
  }
}
</style>
<style lang="less">
.area-opt-popper {
  margin-right: -20px !important;
}

.area-delete-dialog {
  padding: 24px;
  .ed-message-box__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    .ed-message-box__headerbtn {
      position: static;
    }
  }
}
</style>
