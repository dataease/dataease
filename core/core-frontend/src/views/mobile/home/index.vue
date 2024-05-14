<script lang="ts" setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { useI18n } from '@/hooks/web/useI18n'
import { shortcutOption } from '@/views/workbranch/ShortcutOption'
import { useRouter } from 'vue-router'
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

const setEmptyTips = () => {
  emptyTips.value = state.tableData.length
    ? ''
    : `暂无${
        {
          recent: '数据',
          store: '收藏',
          share: '分享'
        }[activeTab.value]
      }`
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

const tablePaneList = ref([
  { title: '最近使用', name: 'recent', disabled: false },
  { title: '我的收藏', name: 'store', disabled: false },
  { title: t('visualization.share_out'), name: 'share', disabled: false }
])

const busiDataMap = computed(() => interactiveStore.getData)

const getBusiListWithPermission = () => {
  const baseFlagList = ['panel', 'screen', 'dataset', 'datasource']
  const busiFlagList: string[] = []
  for (const key in busiDataMap.value) {
    if (busiDataMap.value[key].menuAuth) {
      busiFlagList.push(baseFlagList[parseInt(key)])
    }
  }
  tablePaneList.value[0].disabled = !busiFlagList?.length
  tablePaneList.value[1].disabled =
    !busiFlagList.includes('panel') && !busiFlagList.includes('screen')
  return busiFlagList
}

const busiAuthList = getBusiListWithPermission()

const handleClick = ({ name, disabled }) => {
  if (disabled) return
  if (name === 'recent' || name === 'store') {
    emits('setLoading', true)
    shortcutOption.setBusiFlag(name)
    loadTableData()
  } else {
    loadShareTableData()
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
      <Workbranch
        @click="handleCellClick(ele)"
        v-for="ele in state.tableData"
        :key="ele.id"
        size="large"
        :label="ele.name"
        :time="formatterTime(ele.lastEditTime || ele.time)"
      />
    </div>
    <div class="empty-img-mobile" v-if="!!emptyTips">
      <img width="125" height="125" :src="nothingNone" alt="" />
      <div class="empty-tips">
        {{ emptyTips }}
      </div>
    </div>
  </div>
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
