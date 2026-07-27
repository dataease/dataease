<script lang="ts" setup>
import toolboxData_fill from '@/assets/svg/toolbox-data_fill.svg'
import toolboxIcon_template from '@/assets/svg/toolbox-icon_template.svg'
import toolboxLog from '@/assets/svg/toolbox-log.svg'
import toolboxReport from '@/assets/svg/toolbox-report.svg'
import toolboxThreshold from '@/assets/svg/toolbox-threshold.svg'
import toolboxAssociation from '@/assets/svg/toolbox-association.svg'
import sysTools from '@/assets/svg/sys-tools.svg'
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router_2'
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
  'toolbox-log': toolboxLog,
  'toolbox-report': toolboxReport,
  'toolbox-threshold': toolboxThreshold,
  'toolbox-association': toolboxAssociation
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
    trigger="hover"
    width="auto"
  >
    <div :class="cardInfoList.length < 3 ? 'top-doc-card-wrap-small' : 'top-doc-card-wrap'">
      <top-doc-card
        :span="12"
        v-for="(item, index) in cardInfoList"
        :key="index"
        :card-info="item"
        @click="toRouter(item)"
      ></top-doc-card>
    </div>
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
  padding: 8px !important;
  min-width: 100px !important;
  .top-doc-card-wrap-small {
    display: flex;
    flex-wrap: wrap;
  }
  .top-doc-card-wrap {
    display: grid;
    grid-template-columns: repeat(3, minmax(96px, max-content));
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
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
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
