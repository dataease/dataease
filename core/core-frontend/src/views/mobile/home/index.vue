<script lang="ts" setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { shortcutOption } from '@/views/workbranch/ShortcutOption'
import { XpackComponent } from '@/components/plugin'
import { useRouter } from 'vue-router'
import VanTabs from 'vant/es/tabs'
import VanTab from 'vant/es/tab'
import VanCell from 'vant/es/cell'
import VanSticky from 'vant/es/sticky'
import VanCellGroup from 'vant/es/cell-group'
import 'vant/es/sticky/style'
import 'vant/es/tab/style'
import 'vant/es/tabs/style'
import 'vant/es/cell/style'
import 'vant/es/cell-group/style'

const router = useRouter()

const activeTab = ref('recent')
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
      state.tableData = res.data
    })
    .finally(() => {
      emits('setLoading', false)
    })
}

const tablePaneList = ref([
  { title: '最近使用', name: 'recent', disabled: false },
  { title: '我的收藏', name: 'store', disabled: false }
])

const panelLoad = paneInfo => {
  tablePaneList.value.push({
    title: paneInfo.title,
    name: paneInfo.name,
    disabled: tablePaneList.value[1].disabled
  })
}
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
  }
}
onMounted(() => {
  !!busiAuthList.length &&
    handleClick({
      name: 'recent',
      disabled: false
    })
})

const handleCellClick = ele => {
  router.push({
    path: '/panel/mobile',
    query: {
      dvId: ele.id
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
    <van-cell-group>
      <van-cell
        @click="handleCellClick(ele)"
        v-for="ele in state.tableData"
        :key="ele.id"
        size="large"
        :title="ele.name"
        :value="formatterTime(ele.lastEditTime)"
        icon="bar-chart-o"
      />
    </van-cell-group>
    <div style="width: 100%; height: 50px"></div>
    <XpackComponent jsname="c2hhcmUtcGFuZWw=" @loaded="panelLoad" />
  </div>
</template>
