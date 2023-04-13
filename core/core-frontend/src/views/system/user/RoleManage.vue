<script lang="ts" setup>
import { ref, reactive, onMounted, h } from 'vue'
import { Icon } from '@/components/icon-custom'
import { searchRoleApi, userOptionForRoleApi, userSelectedForRoleApi, roleDelApi } from '@/api/user'
import RoleForm from './RoleForm.vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
const roleKeyword = ref('')
const optionKeyword = ref('')
const selectedKeyword = ref('')
const roleFormRef = ref(null)
const { t } = useI18n()
interface Tree {
  id: string
  name: string
  readonly: boolean
  children?: Tree[]
  disabled: boolean
}

const handleNodeClick = (data: Tree) => {
  optionSearch(data.id)
  selectedSearch(data.id)
}

const checkList = ref(['test0'])

const state = reactive({
  optionUserList: [],
  addedUserList: [],
  roleData: []
})

state.roleData = [
  {
    id: 'admin',
    name: '组织管理员',
    children: null,
    disabled: true
  },
  {
    id: 'readonly',
    name: '普通用户',
    children: null,
    disabled: true
  }
]

const optionSearch = (rid?: string) => {
  const param = { rid, keyword: optionKeyword.value }
  rid &&
    userOptionForRoleApi(param).then(res => {
      state.optionUserList = res.data
    })
}

const selectedSearch = (rid?: string) => {
  const param = { rid, keyword: selectedKeyword.value }
  rid &&
    userSelectedForRoleApi(param).then(res => {
      state.addedUserList = res.data
    })
}

const roleSearch = () => {
  searchRoleApi(roleKeyword.value).then(res => {
    const roles = res.data
    const map = groupBy(roles)
    state.roleData[0].children = map.get(false)
    state.roleData[1].children = map.get(true)
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
  value: 'id'
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
    roleDelApi(row.id).then(() => {
      ElMessage.success(t('common.delete_success'))
      roleSearch()
    })
  })

  console.log(row.id)
}

const roleSaved = () => {
  roleSearch()
}

onMounted(() => {
  roleSearch()
})
</script>

<template>
  <div class="role-manage">
    <div class="role-list role-height">
      <div class="title">
        角色列表
        <el-button type="primary" @click="roleAdd">
          <template #icon> <Icon name="icon_add_outlined"></Icon> </template>添加角色
        </el-button>
        <el-input class="m24 w100" v-model="roleKeyword" clearable>
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>

      <el-tree menu :data="state.roleData" :props="defaultProps" @node-click="handleNodeClick">
        <template #default="{ node, data }">
          <span class="custom-tree-node">
            <span :title="node.label">{{ node.label }}</span>
            <div v-if="!data.disabled" class="operate-icon-container">
              <div><Icon name="edit" @click.stop="roleEdit(data)"></Icon></div>
              <div><Icon name="delete" @click.stop="delHandler(data)"></Icon></div>
            </div>
          </span>
        </template>
      </el-tree>
    </div>
    <div class="added-user-list role-height">
      <div class="title">
        已添加用户
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
      <div v-else :key="ele.id" v-for="ele in state.addedUserList" class="user-list-item">
        {{ ele.name }}
      </div>
    </div>
    <div class="add-user-list role-height">
      <div class="title">
        可添加用户
        <el-icon>
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
      <el-checkbox-group v-else v-model="checkList">
        <div :key="ele.id" v-for="ele in state.optionUserList" class="user-list-item">
          <el-checkbox :label="ele.name" />
        </div>
      </el-checkbox-group>
    </div>
  </div>
  <role-form ref="roleFormRef" @saved="roleSaved" />
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

  .role-list {
    width: 269px;
    padding: 24px;
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

    .el-input {
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
    width: 150px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }
  .operate-icon-container {
    display: none;
  }
  &:hover {
    span {
      width: 135px;
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
          color: var(--el-text-color-regular);
          background-color: var(--el-color-white);
        }
      }
      div:hover {
        svg {
          color: var(--el-color-primary) !important;
          background: var(--el-color-primary-light-7) !important;
        }
      }
    }
  }
}
</style>
