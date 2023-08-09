<template>
  <el-tree-select
    v-model="value"
    :data="state.orgOption"
    check-strictly
    :render-after-expand="false"
    :props="props"
    @visible-change="visibleChange"
    class="tree-org"
    @change="changeVal"
  />
  <ElIcon :class="[expand ? 'retract' : 'expand', 'tree-org-icon']">
    <Icon name="icon_expand-right_filled"></Icon>
  </ElIcon>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { mountedOrg, switchOrg } from '@/api/user'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useEmitt } from '@/hooks/web/useEmitt'
const userStore = useUserStoreWithOut()
const value = ref()
const props = {
  value: 'id',
  label: 'name',
  disabled: 'readOnly'
}
const state = reactive({
  orgOption: []
})

const changeVal = val => {
  switchHandler(val)
}

const expand = ref(false)

const visibleChange = val => {
  expand.value = val
}

const switchHandler = (id: number | string) => {
  switchOrg(id).then(res => {
    const token = res.data
    userStore.setToken(token)
    window.location.reload()
  })
}

const initOptions = () => {
  mountedOrg(null).then(res => {
    state.orgOption = res.data
    value.value = userStore.getOid
  })
}

onMounted(() => {
  useEmitt({
    name: 'refresh-org-options',
    callback: function () {
      initOptions()
    }
  })
  initOptions()
})
</script>

<style lang="less">
.example-showcase .ed-dropdown-link {
  cursor: pointer;
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
}
</style>

<style lang="less" scoped>
.tree-org-icon {
  font-size: 12px !important;
  margin-left: -20px !important;
}
.expand {
  transform: rotate(90deg);
}

.retract {
  transform: rotate(-90deg);
}
.tree-org {
  width: 140px;
  :deep(.ed-input__wrapper),
  :deep(.ed-input.is-focus .ed-input__wrapper) {
    background: none;
    box-shadow: 0 0 0 1px transparent inset !important;

    .ed-input__suffix {
      display: none;
    }
    .ed-input__inner {
      color: rgba(255, 255, 255, 0.8);
      font-family: PingFang SC;
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      line-height: 22px;
    }
  }
}
</style>
