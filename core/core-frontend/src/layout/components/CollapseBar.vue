<script lang="ts" setup>
import icon_sideFold_outlined from '@/assets/svg/icon_side-fold_outlined.svg'
import icon_sideExpand_outlined from '@/assets/svg/icon_side-expand_outlined.svg'
import { useCache } from '@/hooks/web/useCache'
import { useEmitt } from '@/hooks/web/useEmitt'
import { ref, onMounted } from 'vue'
const props = defineProps({
  isCollapse: Boolean
})
const emits = defineEmits(['setCollapse'])
const setCollapse = () => {
  if (props.isCollapse) {
    setWidth()
  } else {
    width.value = 64
  }
  emits('setCollapse', !props.isCollapse)
}
const width = ref(280)
const { wsCache } = useCache('localStorage')
const setWidth = (value?: number) => {
  const num = value ?? wsCache.get('current-collapse_bar')
  if (!num) return
  width.value = num
}
onMounted(() => {
  useEmitt({
    name: 'current-collapse_bar',
    callback: setWidth
  })
  setWidth()
})
</script>

<template>
  <div class="de-collapse-bar" :style="{ width: (width || 280) + 'px' }" @click="setCollapse">
    <el-icon style="color: #646a73">
      <Icon
        ><component
          :is="!isCollapse ? icon_sideFold_outlined : icon_sideExpand_outlined"
        ></component
      ></Icon>
    </el-icon>
    {{ !isCollapse ? $t('commons.collapse_navigation') : '' }}
  </div>
</template>

<style lang="less" scoped>
.de-collapse-bar {
  position: fixed;
  cursor: pointer;
  z-index: 10;
  left: 0;
  bottom: 0;
  height: 48px;
  padding: 14px 22px;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  display: flex;
  align-items: center;
  background: #fff;

  &::after {
    content: '';
    width: 100%;
    height: 1px;
    background: #1f232926;
    position: absolute;
    top: 0;
    left: 0;
  }

  .ed-icon {
    font-size: 20px;
    margin-right: 10px;
  }
}
</style>
