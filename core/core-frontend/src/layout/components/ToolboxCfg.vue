<script lang="ts" setup>
import toolboxData_fill from '@/assets/svg/toolbox-data_fill.svg'
import toolboxIcon_template from '@/assets/svg/toolbox-icon_template.svg'
import toolboxLog from '@/assets/svg/toolbox-log.svg'
import sysTools from '@/assets/svg/sys-tools.svg'
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import TopDocCard from '@/layout/components/TopDocCard.vue'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
const appearanceStore = useAppearanceStoreWithOut()
const navigateBg = computed(() => appearanceStore.getNavigateBg)
const { push, resolve } = useRouter()

const showToolbox = ref(true)

const cardInfoList = ref([] as unknown[])
const iconMap = {
  'toolbox-data_fill': toolboxData_fill,
  'toolbox-icon_template': toolboxIcon_template,
  'toolbox-log': toolboxLog
}
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
      icon: iconMap['toolbox-' + item.meta.icon]
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
    trigger="hover"
  >
    <top-doc-card
      :span="12"
      v-for="(item, index) in cardInfoList"
      :key="index"
      :card-info="item"
      @click="toRouter(item)"
    ></top-doc-card>
    <template #reference>
      <div
        class="sys-setting"
        :class="{
          'hidden-toolbox': !showToolbox,
          'is-light-setting': navigateBg && navigateBg === 'light'
        }"
      >
        <el-icon>
          <Icon name="sys-tools"><sysTools class="svg-icon" /></Icon>
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
.is-light-setting {
  &:hover {
    background-color: #1f23291a !important;
  }
}
</style>
