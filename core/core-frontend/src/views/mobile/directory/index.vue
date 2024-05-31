<script lang="ts" setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useCache } from '@/hooks/web/useCache'
import treeSort from '@/utils/treeSortUtils'
import { BusiTreeRequest } from '@/models/tree/TreeNode'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import DashboardCell from '@/views/mobile/components/DashboardCell.vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useRouter } from 'vue-router'
import VanSticky from 'vant/es/sticky'
import VanNavBar from 'vant/es/nav-bar'
import 'vant/es/nav-bar/style'
import 'vant/es/sticky/style'
import { cloneDeep } from 'lodash-es'
const anyManage = ref(false)
const rootManage = ref(false)
const tableData = ref([])
const directName = ref([])
const directId = ref([])
const activeDirectName = ref('')
const interactiveStore = interactiveStoreWithOut()
const dvMainStore = dvMainStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)
const { wsCache } = useCache('sessionStorage')
const { t } = useI18n()

const dfsTree = (ids, arr) => {
  const id = ids.shift()
  return arr.reduce((pre, ele) => {
    if (id && ele.id === id) {
      if (!ids.length) {
        return ele.children || []
      }
      const children = dfsTree([...ids], ele.children || [])
      pre = children || []
    }
    return pre
  }, [])
}

let rawTableData = []

const activeTableData = computed(() => {
  return directId.value.length ? dfsTree([...directId.value], tableData.value) : tableData.value
})

const emits = defineEmits(['hiddenTabbar', 'setLoading'])
const onClickLeft = () => {
  directName.value.pop()
  activeDirectName.value = directName.value[directName.value.length - 1]
  directId.value.pop()
  if (!!directName.value.length) {
    tableData.value = cloneDeep(rawTableData)
    emits('hiddenTabbar', false)
  }
}

const onClickPanel = () => {
  directName.value = []
  directId.value = []
  activeDirectName.value = ''
}
const router = useRouter()

const handleCellClick = ele => {
  wsCache.set('directName', directName.value)
  wsCache.set('activeDirectName', activeDirectName.value)
  wsCache.set('activeTabbar', 'direct')
  wsCache.set('directId', directId.value)
  router.push({
    path: '/panel/mobile',
    query: {
      dvId: ele.id
    }
  })
}

const filterText = ref('')
const curSortType = ref('time_desc')
const sortList = [
  {
    name: '按创建时间升序',
    value: 'time_asc'
  },
  {
    name: '按创建时间降序',
    value: 'time_desc',
    divided: true
  },
  {
    name: '按照名称升序',
    value: 'name_asc'
  },
  {
    name: '按照名称降序',
    value: 'name_desc'
  }
]
const sortTypeChange = sortType => {
  tableData.value = treeSort(cloneDeep(rawTableData), sortType)
  curSortType.value = sortType
}

const searchTree = (tree, val) => {
  return tree.filter(ele => {
    if (ele.name?.toLocaleLowerCase().includes(val.toLocaleLowerCase())) {
      return true
    } else if (!!ele.children?.length) {
      ele.children = searchTree(ele.children, val)
      return !!ele.children.length
    }
    return false
  })
}

watch(filterText, val => {
  tableData.value = searchTree(cloneDeep(rawTableData), val)
})

const dataClick = val => {
  filterText.value = ''
  if (val.leaf) {
    emits('hiddenTabbar', true)
    handleCellClick(val)
    return
  }
  directName.value.push(val.name)
  activeDirectName.value = val.name
  directId.value.push(val.id)
}

const handleDir = index => {
  if (index === directId.value.length - 1) return
  directId.value = directId.value.slice(0, index + 1)
  directName.value = directName.value.slice(0, index + 1)
  activeDirectName.value = directName.value[directName.value.length - 1]
}

const dfsTableData = arr => {
  return arr.filter(ele => {
    if (!!ele.children?.length) {
      ele.children = dfsTableData(ele.children)
    }
    return ele.extraFlag === 1 || ele.children?.length
  })
}

const getTree = async () => {
  const request = { busiFlag: 'dashboard' } as BusiTreeRequest
  await interactiveStore.setInteractive(request)
  const interactiveData = interactiveStore.getPanel
  const nodeData = interactiveData.treeNodes
  rootManage.value = interactiveData.rootManage
  anyManage.value = interactiveData.anyManage
  if (dvInfo.value && dvInfo.value.id && !JSON.stringify(nodeData).includes(dvInfo.value.id)) {
    dvMainStore.resetDvInfo()
  }
  if (nodeData.length && nodeData[0]['id'] === '0' && nodeData[0]['name'] === 'root') {
    tableData.value = dfsTableData(nodeData[0]['children'] || [])
    rawTableData = cloneDeep(tableData.value)
    setSortType()
    return
  }
  tableData.value = dfsTableData(nodeData)
  rawTableData = cloneDeep(tableData.value)
  setSortType()
}

const setSortType = () => {
  const type = wsCache.get('mobile-sort-type')
  sortTypeChange(type || curSortType.value)
}

onUnmounted(() => {
  wsCache.set('mobile-sort-type', curSortType.value)
})

onMounted(() => {
  getTree()
  activeDirectName.value = wsCache.get('activeDirectName')
  if (wsCache.get('activeTabbar') !== 'direct' || !activeDirectName.value) return
  directName.value = wsCache.get('directName')
  directId.value = wsCache.get('directId')
  wsCache.set('directName', [])
  wsCache.set('activeDirectName', '')
  wsCache.set('directId', [])
  wsCache.set('activeTabbar', '')
})
</script>

<template>
  <div class="mobile-dashboard">
    <van-sticky>
      <van-nav-bar
        safe-area-inset-top
        :title="activeDirectName || '仪表板'"
        :left-arrow="!!activeDirectName"
        @click-left="onClickLeft"
      />
      <div class="direct-name-arr" v-if="directName.length">
        <div @click="onClickPanel" key="仪表板">
          <span class="label primary-name">仪表板</span>
          <el-icon>
            <Icon name="icon_right_outlined"></Icon>
          </el-icon>
        </div>
        <div v-for="(ele, index) in [...directName]" @click="handleDir(index)" :key="ele">
          <span class="label ellipsis" :class="index !== directName.length - 1 && 'primary-name'">{{
            ele
          }}</span>
          <el-icon v-if="index !== directName.length - 1">
            <Icon name="icon_right_outlined"></Icon>
          </el-icon>
        </div>
      </div>
    </van-sticky>
    <div :class="!!directName.length && 'dashboard-cell-group-tab'" class="dashboard-cell-group">
      <div class="dashboard-cell-group_filter" v-if="!directName.length">
        <el-input
          :placeholder="t('commons.search')"
          v-model="filterText"
          clearable
          class="search-bar"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
        <el-dropdown @command="sortTypeChange" trigger="click">
          <el-icon class="filter-icon-span">
            <Icon v-if="curSortType.includes('asc')" name="dv-sort-asc" class="opt-icon"></Icon>
            <Icon v-show="curSortType.includes('desc')" name="dv-sort-desc" class="opt-icon"></Icon>
          </el-icon>
          <template #dropdown>
            <el-dropdown-menu style="width: 246px">
              <template :key="ele.value" v-for="ele in sortList">
                <el-dropdown-item
                  class="ed-select-dropdown__item"
                  :class="ele.value === curSortType && 'selected'"
                  :command="ele.value"
                >
                  {{ ele.name }}
                </el-dropdown-item>
                <li v-if="ele.divided" class="ed-dropdown-menu__item--divided"></li>
              </template>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <DashboardCell
        v-for="ele in activeTableData"
        :key="ele.id"
        @click="dataClick(ele)"
        :label="ele.name"
        :nextlevel="!ele.leaf"
        :prefix-icon="ele.leaf ? 'icon_dashboard' : 'dv-folder'"
      />
    </div>
  </div>
</template>

<style lang="less" scoped>
.mobile-dashboard {
  background: #f5f6f7;

  .dashboard-cell-group {
    overflow-y: auto;
    height: calc(100vh - 102px);
    margin-top: 8px;

    .dashboard-cell-group_filter {
      padding: 0 8px;
    }

    .search-bar {
      padding-bottom: 8px;
      width: calc(100% - 40px);
    }
    .filter-icon-span {
      border: 1px solid #bbbfc4;
      width: 32px;
      height: 32px;
      border-radius: 4px;
      color: #1f2329;
      padding: 8px;
      margin-left: 8px;
      font-size: 16px;
      cursor: pointer;

      .opt-icon:focus {
        outline: none !important;
      }
      &:hover {
        background: #f5f6f7;
      }

      &:active {
        background: #eff0f1;
      }
    }

    &.dashboard-cell-group-tab {
      margin-top: 0;
      height: calc(100vh - 146px);
    }
  }

  .direct-name-arr {
    height: 44px;
    padding: 12px 16px;
    color: #646a73;
    display: flex;
    width: 100%;
    overflow-x: auto;
    align-items: center;
    & > div {
      display: flex;
      align-items: center;
      white-space: nowrap;
    }

    .label {
      font-size: 14px;
      font-weight: 400;
      line-height: 20px;
      max-width: 250px;
    }

    .ed-icon {
      font-size: 12px;
      margin: 0 4px;
    }

    .primary-name {
      color: var(--ed-color-primary);
    }
  }
}
</style>
