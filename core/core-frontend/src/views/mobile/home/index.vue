<script lang="ts" setup>
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { useI18n } from '@/hooks/web/useI18n'
import { shortcutOption } from '@/views/workbranch/ShortcutOption'
import { useRouter } from 'vue-router_2'
import { useCache } from '@/hooks/web/useCache'
import Workbranch from '@/views/mobile/components/Workbranch.vue'
import request from '@/config/axios'
import nothingNone from '@/assets/img/none.png'
import VanTabs from 'vant/es/tabs'
import VanNavBar from 'vant/es/nav-bar'
import VanTab from 'vant/es/tab'
import VanSticky from 'vant/es/sticky'
import 'vant/es/sticky/style'
import 'vant/es/tab/style'
import 'vant/es/nav-bar/style'
import 'vant/es/tabs/style'
import { cloneDeep, map } from 'lodash-es'
import { XpackComponent } from '@/components/plugin'

const router = useRouter()
const { t } = useI18n()
const { wsCache } = useCache('sessionStorage')

const activeTab = ref('recent')
const emptyTips = ref('')
const state = reactive({
  tableData: [],
  curTypeList: []
})
const interactiveStore = interactiveStoreWithOut()

const emits = defineEmits(['setLoading'])
const loadTableData = () => {
  emits('setLoading', true)
  shortcutOption
    .loadData({ type: 'panel', keyword: '', asc: false })
    .then(res => {
      state.tableData = (res.data || []).filter(ele => ele.extFlag === 1)
    })
    .finally(() => {
      emits('setLoading', false)
      setEmptyTips()
    })
}

const loadShareTableData = () => {
  emits('setLoading', true)
  request
    .post({
      url: '/share/query',
      data: { type: 'panel', keyword: '', asc: false }
    })
    .then(res => {
      state.tableData = (res.data || []).filter(ele => ele.extFlag === 1)
    })
    .finally(() => {
      emits('setLoading', false)
      setEmptyTips()
    })
}

const baseTablePaneList = ref([
  { title: t('work_branch.recent'), name: 'recent', disabled: false },
  { title: '我的收藏', name: 'store', disabled: false },
  { title: t('visualization.share_out'), name: 'share', disabled: false }
])

const computedBaseTablePaneNameList = computed(() => {
  return map(baseTablePaneList.value, l => l.name)
})

const dfTablePaneList = ref([])

const tablePaneList = computed(() => {
  const list = cloneDeep(!!busiAuthList.length ? baseTablePaneList.value : [])
  for (const valueElement of dfTablePaneList.value) {
    list.push(valueElement)
  }
  return list
})

const busiDataMap = computed(() => interactiveStore.getData)

const getBusiListWithPermission = () => {
  const baseFlagList = ['panel', 'screen', 'dataset', 'datasource']
  const busiFlagList: string[] = []
  for (const key in busiDataMap.value) {
    if (busiDataMap.value[key].menuAuth) {
      busiFlagList.push(baseFlagList[parseInt(key)])
    }
  }
  baseTablePaneList.value[0].disabled = !busiFlagList?.length
  baseTablePaneList.value[1].disabled =
    !busiFlagList.includes('panel') && !busiFlagList.includes('screen')
  return busiFlagList
}

const busiAuthList = getBusiListWithPermission()

const shortName = {
  recent: '数据',
  store: '收藏',
  share: '分享'
}

const loadedDataFilling = data => {
  dfTablePaneList.value.push(data)
  shortName[data.name] = data.shortName
}

const setEmptyTips = () => {
  emptyTips.value = state.tableData.length ? '' : `暂无${shortName[activeTab.value]}`
}

const firstChangeActiveName = ref(false)

watch(
  () => tablePaneList.value.length,
  () => {
    if (tablePaneList.value.length > 0 && !firstChangeActiveName.value) {
      firstChangeActiveName.value = true
      activeTab.value = tablePaneList.value[0].name
    }
  }
)

const handleClick = ({ name, disabled }) => {
  if (disabled) return
  if (name === 'recent' || name === 'store') {
    emits('setLoading', true)
    shortcutOption.setBusiFlag(name)
    loadTableData()
  } else if (name === 'share') {
    loadShareTableData()
  } else {
    emptyTips.value = undefined
  }
}
onMounted(() => {
  activeTab.value = wsCache.get('activeTab') || 'recent'
  wsCache.set('activeTab', '')
  !!busiAuthList.length &&
    handleClick({
      name: activeTab.value,
      disabled: false
    })
})

const handleCellClick = ele => {
  if (ele.extFlag1 === 0) return
  wsCache.set('activeTab', activeTab.value)
  router.push({
    path: '/panel/mobile',
    query: {
      dvId: ele.resourceId
    }
  })
}

const formatterTime = val => {
  return new Date(val).toLocaleString()
}
</script>

<template>
  <div class="mobile-panel-list">
    <van-sticky>
      <van-nav-bar safe-area-inset-top title="工作台" />
      <van-tabs @click-tab="handleClick" v-model:active="activeTab">
        <van-tab
          v-for="item in tablePaneList"
          :key="item.name"
          :disabled="item.disabled"
          :name="item.name"
          :title="item.title"
        ></van-tab>
      </van-tabs>
    </van-sticky>
    <div class="workbranch-cell-group">
      <template v-if="computedBaseTablePaneNameList.includes(activeTab)">
        <Workbranch
          @click="handleCellClick(ele)"
          v-for="ele in state.tableData"
          :key="ele.id"
          :style="{ color: ele.extFlag1 === 0 ? '#bbbfc4' : '#1f2329' }"
          size="large"
          :label="ele.name"
          :time="formatterTime(ele.lastEditTime || ele.time)"
        />
      </template>
      <XpackComponent
        jsname="L21lbnUvZGF0YS9kYXRhLWZpbGxpbmcvZmlsbC9UYWJQYW5lVGFibGU="
        v-else-if="activeTab === 'data-filling'"
      />
    </div>
    <div class="empty-img-mobile" v-if="!!emptyTips">
      <img width="125" height="125" :src="nothingNone" alt="" />
      <div class="empty-tips">
        {{ emptyTips }}
      </div>
    </div>
  </div>

  <XpackComponent
    jsname="L21lbnUvZGF0YS9kYXRhLWZpbGxpbmcvZmlsbC9UYWJQYW5l"
    @loaded="loadedDataFilling"
  />
</template>

<style lang="less" scoped>
.mobile-panel-list {
  background: #f5f6f7;

  .workbranch-cell-group {
    overflow-y: auto;
    height: calc(100vh - 142px);
    margin-top: 8px;
  }

  .empty-img-mobile {
    position: absolute;
    top: 33%;
    left: 0;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;

    .empty-tips {
      color: #646a73;
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
    }

    img {
      margin-bottom: 8px;
    }
  }
}
</style>
