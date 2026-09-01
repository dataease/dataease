<script lang="ts" setup>
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_enter from '@/assets/svg/icon-enter.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_more_outlined from '@/assets/svg/icon_more_outlined.svg'
import icon_ban_filled from '@/assets/svg/icon_ban_filled.svg'
import icon_succeed_filled from '@/assets/svg/icon_succeed_filled.svg'
import icon_assigned_outlined from '@/assets/svg/icon_assigned_outlined.svg'
import { ref, reactive, onMounted, h } from 'vue'
import { ElIcon, ElMessage, ElMessageBox, ElTree, ElCheckbox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { Icon } from '@/components/icon-custom'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import DeptEditor from '@/views/menu/system/org/DeptEditor.vue'
import AddMemberDialog from '@/views/menu/system/org/AddMemberDialog.vue'
import {
  lazyTreeApi,
  deleteApi,
  sysOrgMemberPageApi,
  sysOrgMemberRemoveApi,
  sysOrgMemberSwitchRoleApi,
  sysOrgMemberRoleOptionsApi
} from '@/views/menu/system/org/api'
import { useUserStoreWithOut } from '@/store/modules/user'
import { rsaEncryp } from '@/utils/encryption'

const { t } = useI18n()
const userStore = useUserStoreWithOut()

// ==================== Left: Organization Tree ====================
const deptEditorRef = ref()
const addMemberDialogRef = ref()
const keyword = ref('')
const currentOrgId = ref<string | null>(null)
const currentOrgName = ref('')
const expandRowKeys = ref<string[]>([])
const syncDelResource = ref<boolean>(false)
interface OrgNode {
  id: string
  name: string
  readOnly?: boolean
  children?: OrgNode[]
  hasChildren?: boolean
  isLeaf?: boolean
}

const treeProps = {
  children: 'children',
  label: 'name',
  isLeaf: 'isLeaf'
}

const treeData = ref<OrgNode[]>([])

const loadTree = async () => {
  const param = { keyword: keyword.value || null }
  const res = await lazyTreeApi(param)
  const childNodes = res.data.nodes || []

  if (keyword.value) {
    expandRowKeys.value = res.data.expandKeyList || []
    // Search mode: flatten children
    const stack = [...childNodes]
    while (stack.length) {
      const item = stack.pop()
      delete item.hasChildren
      if (item.children?.length) {
        item.children.forEach((kid: any) => stack.push(kid))
      }
    }
  } else {
    expandRowKeys.value = []
    // Non-search: set isLeaf based on hasChildren
    const stack = [...childNodes]
    while (stack.length) {
      const item = stack.pop()
      item.isLeaf = !item.hasChildren
      if (item.children?.length) {
        item.children.forEach((kid: any) => stack.push(kid))
      }
    }
  }

  treeData.value = childNodes
}

const handleTreeSearch = () => {
  loadTree()
}

const handleNodeClick = (data: OrgNode) => {
  currentOrgId.value = data.id
  currentOrgName.value = data.name
  loadRoleOptions(currentOrgId.value)
  loadMembers()
}

// Lazy load children on expand
const handleNodeExpand = (data: OrgNode) => {
  if (data.children && data.children.length > 0) return
  lazyTreeApi({ pid: data.id }).then(res => {
    const childNodes = res.data.nodes || []
    childNodes.forEach((item: OrgNode) => {
      item.isLeaf = !(item as any).hasChildren
    })
    data.children = childNodes
  })
}

const handleOrgCreated = () => {
  loadTree()
}

const handleAddOrg = () => {
  const pid = userStore.getUid === '1' ? null : userStore.getOid
  deptEditorRef.value?.createOrg(pid)
}

const handleAddChild = (data: OrgNode) => {
  deptEditorRef.value?.createOrg(data.id)
}

const handleRename = (data: OrgNode) => {
  deptEditorRef.value?.editOrg({ id: data.id, name: data.name })
}

const handleDelete = (data: OrgNode) => {
  if (data.id === 1 || data.id === '1') {
    ElMessage.warning(t('org.default_cannot_move'))
    return
  }
  /* ElMessageBox.confirm(t("org.confirm_delete"), {
    confirmButtonType: "danger",
    type: "warning",
    confirmButtonText: t("common.delete"),
    cancelButtonText: t("dataset.cancel"),
    autofocus: false,
    showClose: false,
  }) */
  syncDelResource.value = false
  ElMessageBox.confirm(t('org.confirm_delete'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    confirmButtonText: t('common.delete'),
    cancelButtonText: t('dataset.cancel'),
    title: t('org.confirm_delete'),
    message: () =>
      h('div', { class: 'org-del-container' }, [
        h('p', { class: 'org-del-tips' }, t('org.delete_role_tips')),
        h(ElCheckbox, {
          modelValue: syncDelResource.value,
          label: t('org.sync_delete_resource'),
          'onUpdate:modelValue': (val: boolean | string | number) => {
            syncDelResource.value = val as boolean
          }
        })
      ]),
    showClose: false
  })
    .then(() => {
      deleteApi({ id: data.id, delResource: syncDelResource.value }).then(() => {
        ElMessage.success(t('common.delete_success'))
        loadTree()
        if (currentOrgId.value === data.id) {
          currentOrgId.value = null
        }
      })
    })
    .finally(() => {
      syncDelResource.value = false
    })
}

const handleEnterOrg = (data: OrgNode) => {
  userStore.setProxyInfo({
    proxy: true,
    proxyOid: data.id,
    proxySecret: rsaEncryp(data.id)
  })
  location.href = location.origin + location.pathname
}

// ==================== Right: Member List ====================
const memberLoading = ref(false)
const memberKeyword = ref('')
const memberList = ref<any[]>([])
const roleOptions = ref<any[]>([])
const memberPage = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})
const imgType = ref('noneWhite')
const emptyDesc = ref('')

const getEmptyImg = (): string => {
  if (memberKeyword.value) return 'tree'
  return 'noneWhite'
}

const getEmptyDesc = (): string => {
  if (memberKeyword.value) return t('work_branch.relevant_content_found')
  return ''
}

const loadMembers = async () => {
  if (!currentOrgId.value) return
  memberLoading.value = true
  try {
    const res = await sysOrgMemberPageApi({
      orgId: currentOrgId.value,
      keyword: memberKeyword.value || null,
      currentPage: memberPage.currentPage,
      pageSize: memberPage.pageSize
    })
    memberList.value = res.data.records || []
    // 初始化临时角色 ID 用于多选
    memberList.value.forEach((row: any) => {
      row.tempRoleIds = (row.roles || []).map((r: any) => r.roleId)
      row.rolePopoverVisible = false
    })
    memberPage.total = res.data.total || 0
    imgType.value = getEmptyImg()
    emptyDesc.value = getEmptyDesc()
  } finally {
    memberLoading.value = false
  }
}

const memberSearch = () => {
  memberPage.currentPage = 1
  loadMembers()
}

const memberPageChange = (index: number) => {
  memberPage.currentPage = index
  loadMembers()
}

const memberSizeChange = (size: number) => {
  memberPage.pageSize = size
  memberPage.currentPage = 1
  loadMembers()
}

// Role options
const loadRoleOptions = async (oid: string) => {
  const res = await sysOrgMemberRoleOptionsApi(oid)
  roleOptions.value = res.data || []
}

const setRoleRef = (el: any, row: any) => {
  row.roleRef = el
}

const closeRolePopover = (row: any) => {
  row.rolePopoverVisible = false
}

// 确认选择
const confirmRole = async (row: any) => {
  await sysOrgMemberSwitchRoleApi({
    userId: row.id,
    orgId: currentOrgId.value,
    roleIds: row.tempRoleIds || []
  })
  closeRolePopover(row)
  await loadMembers()
  ElMessage.success(t('common.save_success'))
}

// 取消选择
const cancelRole = (row: any) => {
  // 恢复原状态
  row.tempRoleIds = (row.roles || []).map((r: any) => r.roleId)
  closeRolePopover(row)
}

// Remove member
const handleRemoveMember = (row: any) => {
  ElMessageBox.confirm(t('org.remove_member_confirm'), {
    confirmButtonType: 'danger',
    type: 'warning',
    confirmButtonText: t('common.sure'),
    cancelButtonText: t('dataset.cancel'),
    autofocus: false,
    showClose: false
  }).then(async () => {
    await sysOrgMemberRemoveApi(row.id, currentOrgId.value!)
    ElMessage.success(t('common.delete_success'))
    loadMembers()
  })
}

// Add member
const handleAddMember = () => {
  addMemberDialogRef.value?.open(currentOrgId.value!)
}

const handleMemberAdded = () => {
  loadMembers()
}

// ==================== Init ====================
onMounted(async () => {
  await loadTree()
})
</script>

<template>
  <div class="sys-org-container">
    <!-- Left: Organization Tree -->
    <div class="org-tree-panel">
      <div class="org-tree-header">
        <span class="org-tree-title">{{ t('org.org_title') }}</span>
        <el-button text @click="handleAddOrg" type="primary">
          <template #icon>
            <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
          </template>
        </el-button>
      </div>
      <div class="org-tree-search">
        <el-input
          v-model="keyword"
          clearable
          :placeholder="t('commons.search')"
          @change="handleTreeSearch"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined">
                <icon_searchOutline_outlined class="svg-icon" />
              </Icon>
            </el-icon>
          </template>
        </el-input>
      </div>
      <div class="org-tree-body">
        <el-tree
          ref="treeRef"
          :data="treeData"
          lazy
          :props="treeProps"
          node-key="id"
          :default-expanded-keys="expandRowKeys"
          :expand-on-click-node="false"
          highlight-current
          @node-click="handleNodeClick"
          @node-expand="handleNodeExpand"
        >
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <span class="tree-node-label">{{ node.label }}</span>
              <span class="icon-more">
                <el-icon class="hover-icon" @click.stop="handleAddChild(data)">
                  <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
                </el-icon>
                <el-dropdown
                  trigger="click"
                  popper-class="org-node-menu"
                  @command="(cmd: string) => { if (cmd === 'toOrg') handleEnterOrg(data); else if (cmd === 'rename') handleRename(data); else if (cmd === 'delete') handleDelete(data); }"
                >
                  <el-icon class="hover-icon" @click.stop>
                    <Icon name="icon_more_outlined"><icon_more_outlined class="svg-icon" /></Icon>
                  </el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="toOrg">
                        <el-icon
                          ><Icon name="icon_enter"><icon_enter class="svg-icon" /></Icon
                        ></el-icon>
                        进入组织
                      </el-dropdown-item>
                      <el-dropdown-item command="rename" divided>
                        <el-icon
                          ><Icon name="icon_edit_outlined"
                            ><icon_edit_outlined class="svg-icon" /></Icon
                        ></el-icon>
                        {{ t('dataset.rename') }}
                      </el-dropdown-item>
                      <el-dropdown-item v-if="data.id !== 1 && data.id !== '1'" command="delete">
                        <el-icon
                          ><Icon name="icon_delete-trash_outlined"
                            ><icon_deleteTrash_outlined class="svg-icon" /></Icon
                        ></el-icon>
                        <span>{{ t('common.delete') }}</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </span>
            </span>
          </template>
        </el-tree>
      </div>
    </div>

    <!-- Right: Member List -->
    <div class="org-member-panel">
      <template v-if="currentOrgId">
        <div class="member-header">
          <div class="member-header-left">
            <span class="member-org-name">{{ currentOrgName }}</span>
            <span class="member-count">{{ memberPage.total }} {{ t('org.members') }}</span>
          </div>
          <div class="member-header-right">
            <el-button type="primary" @click="handleAddMember">
              {{ t('org.add_member') }}
            </el-button>
            <el-input
              v-model="memberKeyword"
              clearable
              :placeholder="t('org.search_name_account')"
              @change="memberSearch"
              style="width: 240px"
            >
              <template #prefix>
                <el-icon>
                  <Icon name="icon_search-outline_outlined">
                    <icon_searchOutline_outlined class="svg-icon" />
                  </Icon>
                </el-icon>
              </template>
            </el-input>
          </div>
        </div>
        <div class="member-table-wrap">
          <GridTable
            :table-data="memberList"
            :pagination="memberPage"
            :loading="memberLoading"
            :empty-desc="emptyDesc"
            :empty-img="imgType"
            @current-change="memberPageChange"
            @size-change="memberSizeChange"
          >
            <el-table-column
              prop="name"
              :label="t('user.name')"
              show-overflow-tooltip
              width="150"
            />
            <el-table-column prop="account" :label="t('user.account')" show-overflow-tooltip />
            <el-table-column
              prop="email"
              :label="t('common.email')"
              show-overflow-tooltip
              width="200"
            />
            <el-table-column :label="t('user.role')" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="role-text" :ref="el => setRoleRef(el, row)">
                  <span>{{ (row.roles || []).map((r: any) => r.roleName).join(', ') }}</span>
                  <el-icon class="el-icon-animate">
                    <ArrowDownBold />
                  </el-icon>
                </div>
                <el-popover
                  :key="`role-popover-${row.id}-${currentOrgId}`"
                  :virtual-ref="row.roleRef"
                  trigger="click"
                  virtual-triggering
                  v-model:visible="row.rolePopoverVisible"
                  placement="bottom-start"
                  popper-class="role-popover"
                  width="200"
                >
                  <div class="role-container">
                    <el-checkbox-group v-model="row.tempRoleIds">
                      <el-checkbox
                        v-for="option in roleOptions"
                        :key="option.id"
                        :value="option.id"
                        :label="option.name"
                      />
                    </el-checkbox-group>
                  </div>
                  <el-divider class="role-divider" style="margin: 8px 0" />
                  <div class="role-actions">
                    <el-button size="small" @click="cancelRole(row)">{{
                      t('commons.cancel')
                    }}</el-button>
                    <el-button size="small" type="primary" @click="confirmRole(row)">{{
                      t('commons.confirm')
                    }}</el-button>
                  </div>
                </el-popover>
              </template>
            </el-table-column>
            <el-table-column :label="t('user.state')" width="100">
              <template #default="{ row }">
                <!--                <span :class="row.enable ? 'status-enabled' : 'status-disabled'">
                  <span class="status-dot" :class="row.enable ? 'dot-green' : 'dot-gray'" />
                  {{ row.enable ? t('user.enable_success') : t('user.disable_success') }}
                </span>-->
                <div style="display: flex; align-items: center">
                  <el-icon size="16px">
                    <Icon>
                      <icon_succeed_filled v-if="row.enable" />
                      <icon_ban_filled v-else />
                    </Icon>
                  </el-icon>
                  <span style="padding: 0 8px">{{
                    row.enable ? t('user.enable_success') : t('user.disable_success')
                  }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="t('common.operate')" fixed="right" width="80">
              <template #default="{ row }">
                <el-tooltip :content="t('org.remove_member')" placement="top">
                  <el-button text @click="handleRemoveMember(row)">
                    <template #icon>
                      <Icon name="icon_assigned_outlined">
                        <icon_assigned_outlined class="svg-icon" />
                      </Icon>
                    </template>
                  </el-button>
                </el-tooltip>
              </template>
            </el-table-column>
          </GridTable>
        </div>
      </template>
      <template v-else>
        <div class="member-empty">
          <el-empty :description="t('org.select_org_first')" />
        </div>
      </template>
    </div>
  </div>

  <DeptEditor ref="deptEditorRef" @saved="handleOrgCreated" />
  <AddMemberDialog ref="addMemberDialogRef" @saved="handleMemberAdded" />
</template>

<style lang="less" scoped>
.sys-org-container {
  display: flex;
  height: calc(100% - 8px);
  margin-top: 8px;
  padding: 0;
}

.org-tree-panel {
  width: 280px;
  min-width: 280px;
  background: var(--ContentBG, #ffffff);
  display: flex;
  flex-direction: column;
  padding: 16px;
  border-top-left-radius: 12px;
  border-bottom-left-radius: 12px;

  .org-tree-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .org-tree-title {
      font-size: 16px;
      font-weight: 500;
    }
  }

  .org-tree-search {
    margin-bottom: 12px;

    :deep(.ed-input) {
      width: 100%;
    }
  }

  .org-tree-body {
    flex: 1;
    overflow-y: auto;

    :deep(.ed-tree-node__content) {
      height: 32px;
    }

    .custom-tree-node {
      width: calc(100% - 30px);
      display: flex;
      align-items: center;
      padding-right: 4px;

      .tree-node-label {
        flex: 1;
        min-width: 0;
        font-size: 14px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
      }

      .icon-more {
        margin-left: auto;
        display: none;
        align-items: center;
        gap: 4px;

        .hover-icon {
          cursor: pointer;
          border-radius: 4px;
          padding: 2px;
          font-size: 16px;

          &:hover {
            background: #f0f1f3;
          }
        }
      }

      &:hover {
        .tree-node-label {
          width: calc(100% - 48px);
        }
        .icon-more {
          display: inline-flex;
        }
      }
    }
  }
}

.org-member-panel {
  flex: 1;
  background: var(--ContentBG, #ffffff);
  display: flex;
  flex-direction: column;
  padding: 16px;
  overflow: hidden;
  border-top-right-radius: 12px;
  border-bottom-right-radius: 12px;

  .member-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .member-header-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .member-org-name {
        font-size: 16px;
        font-weight: 500;
      }

      .member-count {
        font-size: 13px;
        color: #646a73;
      }
    }

    .member-header-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }

  .member-table-wrap {
    flex: 1;
    overflow: hidden;
  }

  .member-empty {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.role-text {
  cursor: pointer;
  // color: var(--ed-color-primary);
  font-size: 13px;
  display: flex;
  column-gap: 8px;
  align-items: center;
  &:hover {
    opacity: 0.8;
  }
}
</style>

<style lang="less">
.org-del-container {
  margin-top: 8px;
  display: flex !important;
  flex-direction: column;
  gap: 4px;
  .org-del-tips {
    font-size: 12px;
    color: #f59a23;
    white-space: normal;
    overflow-wrap: break-word;
    word-break: break-word;
  }
  .ed-checkbox {
    display: flex;
    align-items: flex-start;
    height: auto;
    white-space: normal;
    .ed-checkbox__label {
      font-size: 12px;
      color: var(--ed-color-danger, #8f959e);
      white-space: normal;
      overflow-wrap: break-word;
      word-break: break-word;
      line-height: 1.4;
    }
  }
}
.org-node-menu {
  min-width: 120px;
  margin-top: -2px !important;

  .handle-icon {
    font-size: 16px;
    color: #646a73;
  }
}

.role-popover {
  padding: var(--ed-popover-padding) 0 !important;

  .ed-popper__arrow {
    display: none;
  }
  .ed-popover__title {
    display: none;
  }

  .ed-popover__content {
    overflow: hidden;
  }

  .role-container {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 0 8px;
    max-height: 200px;
    overflow-y: auto;
    .ed-checkbox-group {
      display: flex;
      flex-direction: column;
      height: fit-content;
    }

    :deep(.ed-checkbox) {
      height: 26px;
    }

    :deep(.ed-checkbox-group) {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    :deep(.ed-checkbox__label) {
      font-size: 13px;
      color: #1f2329;
    }
  }

  .role-divider {
    margin: 8px 0 !important;
  }

  .role-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding: 0 4px;
  }
}
</style>
