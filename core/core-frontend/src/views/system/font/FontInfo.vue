<script lang="ts" setup>
withDefaults(
  defineProps<{
    name?: string
    size?: string
  }>(),
  {
    name: '',
    size: ''
  }
)

const emits = defineEmits(['del'])
const del = () => {
  emits('del')
}
</script>

<template>
  <div class="font-info">
    <el-icon class="font">
      <Icon name="icon_file_font_colorful"></Icon>
    </el-icon>
    <div class="info">
      <p class="name ellipsis">{{ name || '-' }}</p>
      <p class="size ellipsis">{{ size || '-' }}</p>
    </div>
    <el-icon @click="del" class="delete">
      <Icon name="icon_delete-trash_outlined"></Icon>
    </el-icon>
  </div>
</template>

<style lang="less" scoped>
.font-info {
  display: flex;
  align-items: center;
  width: 100%;
  height: 58px;
  padding: 0 16px 0 12px;
  border-radius: 4px;
  border: 1px solid #dee0e3;
  .font {
    font-size: 40px;
    margin-right: 8px;
  }

  .info {
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    font-style: normal;
    font-weight: 400;
    width: 80%;

    .name {
      color: #1f2329;
      font-size: 14px;
      line-height: 22px;
      width: 100%;
    }

    .size {
      color: #8f959e;
      font-size: 12px;
      line-height: 20px;
    }
  }

  .delete {
    cursor: pointer;
    font-size: 16px;
    margin-left: auto;
  }
}
</style>
