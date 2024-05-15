<script lang="ts" setup>
const props = defineProps({
  isCollapse: Boolean
})
const emits = defineEmits(['setCollapse'])
const setCollapse = () => {
  emits('setCollapse', !props.isCollapse)
}
</script>

<template>
  <div class="de-collapse-bar" @click="setCollapse">
    <el-icon style="color: #646a73">
      <Icon :name="!isCollapse ? 'icon_side-fold_outlined' : 'icon_side-expand_outlined'"></Icon>
    </el-icon>
    {{ !isCollapse ? '收起导航' : '' }}
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

  .ed-icon {
    font-size: 20px;
    margin-right: 10px;
  }
}
</style>
