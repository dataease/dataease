<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TopDocCard from '@/layout/components/TopDocCard.vue'
const { push, resolve } = useRouter()

const showToolbox = ref(true)

const cardInfoList = ref([] as unknown[])

const loadInfoList = () => {
  const toolboxMenu = resolve('/toolbox')
  if (!toolboxMenu) {
    showToolbox.value = false
    return
  }
  const children = toolboxMenu.matched[0].children
  if (!children?.length) {
    showToolbox.value = false
    return
  }
  children.forEach(item => {
    const temp = {
      name: item.meta.title,
      rName: item.name,
      path: item.path,
      icon: 'toolbox-' + item.meta.icon
    }
    cardInfoList.value.push(temp)
  })
}

const toRouter = item => {
  push({ name: item.rName })
}
onMounted(() => {
  loadInfoList()
})
</script>

<template>
  <el-popover
    :show-arrow="false"
    popper-class="toolbox-top-popover"
    placement="bottom-end"
    width="208"
    trigger="click"
  >
    <top-doc-card
      :span="12"
      v-for="(item, index) in cardInfoList"
      :key="index"
      :card-info="item"
      @click="toRouter(item)"
    ></top-doc-card>
    <template #reference>
      <div class="sys-setting" :class="{ 'hidden-toolbox': !showToolbox }">
        <el-icon>
          <Icon name="sys-tools" />
        </el-icon>
      </div>
    </template>
  </el-popover>
</template>

<style lang="less">
.toolbox-top-popover {
  height: 82px;
  min-width: 208px !important;
  padding: 16px !important;
  display: flex;
  .doc-card {
    margin: auto;
  }
}
</style>
<style lang="less" scoped>
.hidden-toolbox {
  display: none !important;
}
.sys-setting {
  margin: 0 0 0 10px;
  padding: 5px;
  height: 28px;
  width: 28px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  &:hover {
    background-color: #1e2738;
  }
}
</style>
