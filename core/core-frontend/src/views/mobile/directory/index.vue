<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { BusiTreeRequest } from '@/models/tree/TreeNode'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import VanCell from 'vant/es/cell'
import VanSticky from 'vant/es/sticky'
import VanSearch from 'vant/es/search'
import VanCellGroup from 'vant/es/cell-group'
import VanNavBar from 'vant/es/nav-bar'
import 'vant/es/cell/style'
import 'vant/es/cell-group/style'
import 'vant/es/nav-bar/style'
import 'vant/es/sticky/style'
import 'vant/es/search/style'
const keywords = ref()
const anyManage = ref(false)
const rootManage = ref(false)
const tableData = ref([])
const directName = ref([])
const directId = ref([])
const activeDirectName = ref('')
const interactiveStore = interactiveStoreWithOut()
const dvMainStore = dvMainStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)

const dfsTree = (ids, arr) => {
  const id = ids.shift()
  return arr.reduce((pre, ele) => {
    if (id && ele.id === id) {
      pre = ele.children
    }
    const children = dfsTree([...ids], ele.children || [])
    if (children?.length) {
      pre = children
    }
    return pre
  }, [])
}

const activeTableData = computed(() => {
  return directId.value.length ? dfsTree([...directId.value], tableData.value) : tableData.value
})

const emits = defineEmits(['hiddentabbar', 'setLoading'])
const onClickLeft = () => {
  activeDirectName.value = directName.value.pop()
  directId.value.pop()
  if (!!directName.value.length) {
    emits('hiddentabbar', false)
  }
}

const dataClick = val => {
  directName.value.push(val.name)
  activeDirectName.value = val.name
  directId.value.push(val.id)
  if (directName.value.length === 1) {
    emits('hiddentabbar', true)
  }
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
    tableData.value = nodeData[0]['children'] || []
    return
  }
  tableData.value = nodeData
}

onMounted(() => {
  getTree()
})
</script>

<template>
  <div>
    <van-sticky>
      <van-search
        v-if="!directName.length"
        v-model="keywords"
        shape="round"
        placeholder="请输入仪表板名称"
      />
      <van-nav-bar
        v-else
        :title="activeDirectName"
        left-text="返回"
        left-arrow
        @click-left="onClickLeft"
      />
    </van-sticky>
    <van-cell-group>
      <van-cell
        v-for="ele in activeTableData"
        :key="ele.id"
        size="large"
        @click="dataClick(ele)"
        :title="ele.name"
        :is-link="!ele.leaf"
        :icon="ele.leaf ? 'bar-chart-o' : 'list-switch'"
      />
    </van-cell-group>
  </div>
</template>

<style lang="less" scoped></style>
