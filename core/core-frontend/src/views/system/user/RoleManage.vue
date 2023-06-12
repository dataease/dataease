<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { Icon } from '@/components/icon-custom'
import {
  searchRoleApi,
  userOptionForRoleApi,
  userSelectedForRoleApi,
  roleDelApi,
  beforeUnmountInfoApi,
  unMountUserApi,
  mountUserApi
} from '@/api/user'
import RoleForm from './RoleForm.vue'
import OutUserForm from './OutUserForm.vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
const selectedRoleId = ref('')
const roleKeyword = ref('')
const optionKeyword = ref('')
const selectedKeyword = ref('')
const roleFormRef = ref(null)
const outUserFormRef = ref(null)
const { t } = useI18n()
const loading = ref(false)
interface Tree {
  id: string
  name: string
  readonly: boolean
  children?: Tree[]
  disabled: boolean
}
const treeRef = ref(null)
const handleNodeClick = (data: Tree) => {
  if (data.disabled) {
    return
  }
  selectedRoleId.value = data.id
  optionSearch(data.id)
  selectedSearch(data.id)
}

const state = reactive({
  optionUserList: [],
  addedUserList: [],
  roleData: [],
  checkList: []
})

state.roleData = [
  {
    id: 'admin',
    name: t('role.org_admin'),
    children: null,
    disabled: true
  },
  {
    id: 'readonly',
    name: t('role.average_role'),
    children: null,
    disabled: true
  }
]

const optionSearch = (rid?: string) => {
  const param = { rid }
  if (rid) {
    loading.value = true
    userOptionForRoleApi(param).then(res => {
      if (res?.data?.length) {
        state.optionUserList = res.data
      } else {
        state.optionUserList = []
      }
      loading.value = false
    })
  }
}

const selectedSearch = (rid?: string) => {
  const param = { rid }
  if (rid) {
    loading.value = true
    userSelectedForRoleApi(param).then(res => {
      if (res?.data?.length) {
        state.addedUserList = res.data
      } else {
        state.addedUserList = []
      }
      loading.value = false
    })
  }
}

const roleSearch = () => {
  loading.value = true
  searchRoleApi(null).then(res => {
    const roles = res.data
    const map = groupBy(roles)
    state.roleData[0].children = map.get(false)
    state.roleData[1].children = map.get(true)
    loading.value = false
  })
}

const groupBy = (list: Tree[]) => {
  const map = new Map()
  list.forEach(item => {
    const readonly = item.readonly
    let arr = map.get(readonly)
    if (!arr) {
      arr = []
    }
    item.disabled = false
    arr.push(item)
    map.set(readonly, arr)
  })
  return map
}

const defaultProps = {
  children: 'children',
  label: 'name',
  value: 'id',
  disabled: 'disabled'
}
const roleAdd = () => {
  roleFormRef.value.init()
}

const roleEdit = row => {
  roleFormRef.value.edit(row.id)
}

const delHandler = row => {
  ElMessageBox.confirm(t('role.confirm_delete'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    dangerouslyUseHTMLString: true,
    message:
      '<strong style="font-size: 16px;">' +
      t('role.confirm_delete') +
      '</strong></br>' +
      t('role.delete_tips'),
    showClose: false
  }).then(() => {
    loading.value = true
    roleDelApi(row.id).then(() => {
      ElMessage.success(t('common.delete_success'))
      roleSearch()
    })
  })
}
const emits = defineEmits(['refresh', 'refresh-grid'])
const roleSaved = () => {
  roleSearch()
  emits('refresh')
}
const outUserSaved = () => {
  selectedSearch(selectedRoleId.value)
  emits('refresh-grid')
}
const bindUser = () => {
  const param = { rid: selectedRoleId.value, uids: state.checkList }
  loading.value = true
  mountUserApi(param).then(() => {
    ElMessage({
      message: t('role.bind_success'),
      type: 'success'
    })
    moveOption2Selected(param.uids)
    state.checkList = []
    emits('refresh-grid')
    loading.value = false
  })
}
const unBindUser = (uid: string) => {
  const param = { uid, rid: selectedRoleId.value }
  loading.value = true
  beforeUnmountInfoApi(param).then(res => {
    if (res.data) {
      const msg = res.data === 2 ? t('role.clear_in_system') : t('role.clear_in_org')
      ElMessageBox.confirm(t('role.confirm_unbind_user'), {
        confirmButtonType: 'danger',
        type: 'warning',
        autofocus: false,
        tip: msg,
        showClose: false
      }).then(() => {
        unMountUserHandler(param, () => {
          moveSelected([uid])
        })
      })
    } else {
      // 删除用户角色映射
      unMountUserHandler(param, () => {
        moveSelected2Option([uid])
      })
    }
  })
}

const unMountUserHandler = (param: any, callback?) => {
  loading.value = true
  unMountUserApi(param).then(() => {
    ElMessage({
      message: t('role.unbind_success'),
      type: 'success'
    })
    emits('refresh-grid')
    callback && callback()
    loading.value = false
  })
}

const moveOption2Selected = (uids: string[]) => {
  let len = state.optionUserList.length
  while (len--) {
    const item = state.optionUserList[len]
    if (uids.includes(item.id)) {
      state.optionUserList.splice(len, 1)
      state.addedUserList.push({ ...item })
    }
  }
}
const moveSelected = (uids: string[]): any[] => {
  const result = []
  let len = state.addedUserList.length
  while (len--) {
    const item = state.addedUserList[len]
    if (uids.includes(item.id)) {
      state.addedUserList.splice(len, 1)
      result.push({ ...item })
    }
  }
  return result
}
const moveSelected2Option = (uids: string[]) => {
  const result = moveSelected(uids)
  if (result?.length) {
    result.forEach(item => {
      state.optionUserList.push({ ...item })
    })
  }
  /* let len = state.addedUserList.length
  while (len--) {
    const item = state.addedUserList[len]
    if (uids.includes(item.id)) {
      state.addedUserList.splice(len, 1)
      state.optionUserList.push({ ...item })
    }
  } */
}
const openOutUser = () => {
  if (!selectedRoleId.value) {
    ElMessage.error('请先选择角色')
    return
  }
  outUserFormRef.value.init(selectedRoleId.value)
}
const filterRoleNode = (value: string, data: Tree) => {
  if (!value) return true
  return data.name.includes(value)
}
const triggerFilterRole = val => {
  treeRef.value?.filter(val)
}
onMounted(() => {
  roleSearch()
})
</script>

<template>
  <div class="role-manage" v-loading="loading">
    <div class="role-list role-height">
      <div class="title">
        {{ t('role.role_title') }}
        <el-button type="primary" @click="roleAdd">
          <template #icon> <Icon name="icon_add_outlined"></Icon> </template
          >{{ t('role.add_title') }}
        </el-button>
        <el-input class="m24 w100" v-model="roleKeyword" clearable @change="triggerFilterRole">
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>
      <el-scrollbar class="role-tree-container">
        <el-tree
          menu
          :data="state.roleData"
          ref="treeRef"
          :highlight-current="true"
          :props="defaultProps"
          @node-click="handleNodeClick"
          :filter-node-method="filterRoleNode"
        >
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <span :title="node.label">{{ node.label }}</span>
              <div v-if="!data.disabled && !data.root" class="operate-icon-container">
                <div><Icon name="edit" @click.stop="roleEdit(data)"></Icon></div>
                <div><Icon name="delete" @click.stop="delHandler(data)"></Icon></div>
              </div>
            </span>
          </template>
        </el-tree>
      </el-scrollbar>
    </div>
    <div class="added-user-list role-height">
      <div class="title">
        {{ t('role.bound_user') }}
        <el-input v-model="selectedKeyword" clearable>
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>
      <el-empty
        v-if="!state.addedUserList || !state.addedUserList.length"
        description="description"
      />
      <div
        v-else
        :key="ele.id"
        v-for="ele in state.addedUserList.filter(
          item => !selectedKeyword || item.name.includes(selectedKeyword)
        )"
        class="user-list-item"
      >
        <span>{{ ele.name }}</span>
        <div>
          <Icon
            @click.stop="unBindUser(ele.id)"
            class="role-remove-icon"
            name="icon_close_filled"
          />
        </div>
      </div>
    </div>
    <div class="add-user-list role-height-option">
      <div class="title">
        {{ t('role.option_user') }}
        <el-icon class="add-out-icon" @click.stop="openOutUser">
          <Icon name="icon_add_outlined"></Icon>
        </el-icon>
        <el-input class="m24 w100" v-model="optionKeyword" clearable>
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>
      <el-empty
        v-if="!state.optionUserList || !state.optionUserList.length"
        description="description"
      />
      <div v-else class="content">
        <el-checkbox-group v-model="state.checkList">
          <div
            :key="ele.id"
            v-for="ele in state.optionUserList.filter(
              item => !optionKeyword || item.name.includes(optionKeyword)
            )"
            class="user-list-item"
          >
            <el-checkbox :label="ele.id">{{ ele.name }}</el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <div class="foot1" v-if="state.optionUserList && state.optionUserList.length">
        <el-button
          :disabled="!state.checkList || !state.checkList.length"
          @click="bindUser"
          type="primary"
          >{{ t('role.add_user', [state.checkList.length]) }}</el-button
        >
      </div>
    </div>
  </div>
  <role-form ref="roleFormRef" @saved="roleSaved" />
  <out-user-form ref="outUserFormRef" @saved="outUserSaved" />
</template>

<style lang="less" scoped>
.role-manage {
  display: flex;
  width: 100%;
  height: 100%;

  .role-height {
    height: calc(100vh - 170px);
    overflow: auto;
    position: relative;
  }
  .role-height-option {
    height: calc(100vh - 170px);
    position: relative;
    overflow: hidden;
    .content {
      height: calc(100% - 140px);
      overflow: auto;
    }
  }

  .role-list {
    width: 269px;
    padding: 24px;
    .role-tree-container {
      height: calc(100% - 112px);
    }
  }

  .title {
    display: flex;
    justify-content: space-between;
    font-family: PingFang SC;
    font-size: 20px;
    font-weight: 500;
    color: var(--TextPrimary, #1f2329);
    box-sizing: border-box;
    flex-wrap: wrap;
    position: sticky;
    top: 0;
    left: 24px;
    z-index: 5;
    background: white;
    &::before {
      content: '';
      width: 100%;
      height: 24px;
      top: -24px;
      position: absolute;
      z-index: 5;
      left: 0;
      background: white;
    }
  }
  .foot1 {
    display: flex;
    margin-top: 10px;
    button {
      width: 100%;
    }
  }

  .m24 {
    margin: 24px 0;
  }
  .w100 {
    width: 100%;
  }

  .added-user-list {
    flex: 1;
    border-right: 2px solid var(--MainBG, #f5f6f7);
    border-left: 2px solid var(--MainBG, #f5f6f7);

    .ed-input {
      width: 120px;
      height: 32px;
    }
    .title {
      padding: 24px 24px 0 24px;
      width: 100%;
      height: 56px;
      &::before {
        display: none;
      }
    }

    .user-list-item {
      float: left;
      width: 150px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 24px 0 0 24px;
      border: 1px solid #ccc;
      .role-remove-icon {
        display: none;
        top: 0;
        right: 0;
        width: 12px;
        height: 12px;
        color: var(--ed-color-primary);
        background: var(--ed-color-primary-light-7);
      }
      &:hover {
        cursor: pointer;
        border-color: var(--ed-color-primary-light-7);
        .role-remove-icon {
          display: block;
        }
      }
    }
  }

  .add-user-list {
    width: 269px;
    padding: 24px;

    .user-list-item {
      width: 100%;
      height: 30px;
      margin-bottom: 24px;
      padding-left: 24px;
      border: 1px solid #ccc;
    }
  }
}
.custom-tree-node {
  display: flex;
  span {
    width: 160px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }
  .operate-icon-container {
    display: none;
  }
  &:hover {
    span {
      width: 120px;
    }
    .operate-icon-container {
      text-align: end;
      font-size: 16px;
      display: flex;
      div {
        width: 24px;
        height: 20px;
        padding: 0 3px;
        cursor: pointer;
        svg {
          width: 16px;
          height: 16px;
          color: var(--ed-text-color-regular);
          background-color: var(--ed-color-white);
        }
      }
      div:hover {
        svg {
          color: var(--ed-color-primary) !important;
          background: var(--ed-color-primary-light-7) !important;
        }
      }
    }
  }
}
.add-out-icon {
  cursor: pointer;
  color: var(--ed-text-color-regular);
  background-color: var(--ed-color-white);
  :hover {
    color: var(--ed-color-primary) !important;
    background: var(--ed-color-primary-light-7) !important;
  }
}
</style>
