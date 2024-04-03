<script>
import DeContainer from '@/components/dataease/DeContainer.vue'
import DeAsideContainer from '@/components/dataease/DeAsideContainer.vue'
import NoSelect from './NoSelect.vue'
import ViewTable from './ViewTable.vue'
import { listForm, saveForm, updateForm, deleteForm, getWithPrivileges } from '@/views/dataFilling/form/dataFilling'
import { cloneDeep } from 'lodash-es'
import { hasPermission } from '@/directive/Permission'

export default {
  name: 'DataFillingForm',
  components: { DeAsideContainer, DeContainer, NoSelect, ViewTable },
  data() {
    return {
      treeLoading: false,
      requiredRule: { required: true, message: this.$t('commons.required'), trigger: ['blur', 'change'] },
      activeName: 'forms',
      showFolderCreateForm: false,
      folderForm: {
        name: undefined,
        pid: '0',
        level: 0,
        nodeType: 'folder'
      },
      formList: [],
      expandedArray: [],
      updateFormData: {},
      showUpdateName: false,
      displayFormData: undefined
    }
  },
  mounted() {
    this.treeLoading = true
    listForm({}).then(res => {
      this.formList = res.data || []
    }).finally(() => {
      this.treeLoading = false
    })
  },
  methods: {
    hasPermission(binding) {
      return hasPermission(binding)
    },
    createFolder(folder) {
      this.folderForm.name = undefined
      this.folderForm.pid = folder.id
      if (folder.firstFolder) {
        this.folderForm.level = folder.level
      } else {
        this.folderForm.level = folder.level + 1
      }
      this.showFolderCreateForm = true
    },
    closeSaveFolder() {
      this.showFolderCreateForm = false
    },
    doSaveFolder() {
      this.$refs['mFolderForm'].validate((valid) => {
        if (valid) {
          const data = {
            name: this.folderForm.name,
            pid: this.folderForm.pid,
            level: this.folderForm.level,
            nodeType: 'folder'
          }
          saveForm(data).then(res => {
            this.closeSaveFolder()
            listForm({}).then(res => {
              this.formList = res.data || []
            })
          })
        }
      })
    },
    beforeData(type, data) {
      return {
        ...data,
        createType: type
      }
    },
    clickMore(param) {
      switch (param.optType) {
        case 'rename':
          this.openUpdateForm(param)
          break
        case 'delete':
          this.delete(param.data)
          break
      }
    },
    openUpdateForm(param) {
      this.updateFormData = cloneDeep(param.data)
      this.showUpdateName = true
    },
    closeUpdateForm() {
      this.showUpdateName = false
    },
    doUpdateForm() {
      this.$refs['mUpdateNameForm'].validate((valid) => {
        if (valid) {
          const data = {
            id: this.updateFormData.id,
            name: this.updateFormData.name
          }
          updateForm(data).then(res => {
            this.closeUpdateForm()
            listForm({}).then(res => {
              this.formList = res.data || []
            })
            if (this.updateFormData.nodeType !== 'folder' && this.updateFormData.id === this.displayFormData.id) {
              getWithPrivileges(data.id).then(res => {
                this.displayFormData = res.data
              })
            }
          })
        }
      })
    },
    delete(data) {
      this.$confirm(
        this.$t('data_fill.form.confirm_delete'),
        this.$t('dataset.tips'),
        {
          confirmButtonText: this.$t('dataset.confirm'),
          cancelButtonText: this.$t('dataset.cancel'),
          type: 'warning'
        }
      ).then(() => {
        deleteForm(data.id).then((response) => {
          if (this.displayFormData && this.displayFormData.id === data.id) {
            this.displayFormData = undefined
          }
          listForm({}).then(res => {
            this.formList = res.data || []
          })
        })
      }).catch(() => {
      })
    },
    beforeClickMore(optType, data, node) {
      return {
        type: data.nodeType,
        data: data,
        node: node,
        optType: optType
      }
    },
    clickTreeAddBtn(data) {
      if (data.createType === 'folder') {
        this.createFolder(data)
      } else {
        this.createForm(data)
      }
    },
    createForm(data) {
      const _param = {
        folder: data.id,
        level: data.level + 1
      }
      this.$router.push({ name: 'data-filling-form-create', query: _param })
    },
    filterNode(value, data) {
      if (!value) return true
      if (this.searchType === 'folder') {
        if (
          data.label.indexOf(value) !== -1
        ) {
          this.searchPids.push(data.id)
          return true
        }
        if (this.searchPids.indexOf(data.pid) !== -1) {
          this.searchPids.push(data.id)
          return true
        }
      } else {
        return data.label.indexOf(value) !== -1
      }
      return false
    },
    nodeExpand(data) {
      if (data.id) {
        this.expandedArray.push(data.id)
      }
    },
    nodeCollapse(data) {
      if (data.id) {
        this.expandedArray.splice(this.expandedArray.indexOf(data.id), 1)
      }
    },
    nodeClick(data, node) {
      // 展示对应的表数据
      if (data.nodeType !== 'folder') {
        getWithPrivileges(data.id).then(res => {
          this.displayFormData = res.data
        })
      }
    },
    tabClick() {
      if (this.activeName === 'my-tasks') {
        this.$router.push('/data-filling/my-jobs')
      }
    }
  }
}
</script>

<template>
  <de-container
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
    style="background-color: #f7f8fa"
  >
    <de-aside-container type="data-filling">
      <el-tabs
        v-model="activeName"
        class="tab-panel"
        @tab-click="tabClick"
      >
        <el-tab-pane
          v-if="hasPermission(['my-data-filling:manage'])"
          name="my-tasks"
        >
          <span slot="label">
            我的填报
          </span>
        </el-tab-pane>

        <el-tab-pane
          name="forms"
        >
          <span slot="label">
            表单管理
          </span>

          <div style="padding-left: 20px;padding-right: 20px;">

            <div style="display: flex;flex-direction: row;justify-content: space-between;align-items: center;">
              填报表单
              <el-button
                icon="el-icon-plus"
                type="text"
                @click="createFolder({id: '0', level: 0, firstFolder: true})"
              />
            </div>

            <div
              v-if="!formList.length && !treeLoading"
              class="no-tdata"
            >
              暂无表单，点击
              <span
                class="no-tdata-new"
                @click="() => createFolder({id: '0', level: 0, firstFolder: true})"
              >{{
                $t('deDataset.create')
              }}</span>
            </div>
            <el-tree
              v-else
              ref="datasetTreeRef"
              :default-expanded-keys="expandedArray"
              :data="formList"
              node-key="id"
              highlight-current
              :expand-on-click-node="true"
              :filter-node-method="filterNode"
              @node-expand="nodeExpand"
              @node-collapse="nodeCollapse"
              @node-click="nodeClick"
            >
              <template slot-scope="{ node, data }">
                <span
                  class="custom-tree-node-list father"
                >
                  <span style="display: flex; flex: 1; width: 0">
                    <span v-if="data.nodeType === 'folder'">
                      <svg-icon icon-class="scene" />
                    </span>
                    <span
                      style="
                    margin-left: 6px;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                  "
                      :title="data.name"
                    >{{ data.name }}</span>
                  </span>
                  <span
                    v-if="hasDataPermission('manage', data.privileges)"
                    class="child"
                    @click.stop
                  >
                    <template v-if="data.nodeType === 'folder'">
                      <el-dropdown
                        trigger="click"
                        size="small"
                        @command="clickTreeAddBtn"
                      >
                        <span class="el-dropdown-link">
                          <el-button
                            icon="el-icon-plus"
                            type="text"
                            size="small"
                          />
                        </span>
                        <el-dropdown-menu slot="dropdown">
                          <el-dropdown-item
                            :command="beforeData('folder',data)"
                          >
                            <svg-icon icon-class="scene" />
                            <span style="margin-left: 5px">新建文件夹</span>
                          </el-dropdown-item>
                          <el-dropdown-item
                            :command="beforeData('form',data)"
                          >
                            <svg-icon
                              icon-class="panel"
                              class="ds-icon-scene"
                            />
                            <span>新建表单</span>
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                    </template>

                    <span
                      style="margin-left: 12px"
                      @click.stop
                    >
                      <el-dropdown
                        trigger="click"
                        size="small"
                        @command="clickMore"
                      >
                        <span class="el-dropdown-link">
                          <el-button
                            icon="el-icon-more"
                            type="text"
                            size="small"
                          />
                        </span>
                        <el-dropdown-menu slot="dropdown">
                          <el-dropdown-item
                            icon="el-icon-edit-outline"
                            :command="beforeClickMore('rename', data, node)"
                          >
                            {{ $t('panel.rename') }}
                          </el-dropdown-item>
                          <el-dropdown-item
                            icon="el-icon-delete"
                            :command="beforeClickMore('delete', data, node)"
                          >
                            {{ $t('panel.delete') }}
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                    </span>
                  </span>

                </span>
              </template>
            </el-tree>

          </div>
        </el-tab-pane>

      </el-tabs>

    </de-aside-container>

    <el-main style="padding: 0">
      <no-select v-if="!displayFormData" />
      <view-table
        v-else
        :param="displayFormData"
      />
    </el-main>

    <el-dialog
      v-dialogDrag
      append-to-body
      title="新建文件夹"
      :visible.sync="showFolderCreateForm"
      :show-close="true"
      width="600px"
      class="m-dialog"
    >
      <el-container
        v-if="showFolderCreateForm"
        style="width: 100%"
        direction="vertical"
      >
        <el-form
          ref="mFolderForm"
          class="m-form"
          :model="folderForm"
          label-position="top"
          hide-required-asterisk
        >
          <el-main>
            <el-form-item
              prop="name"
              class="form-item"
              :rules="[requiredRule]"
            >
              <template #label>
                名称
                <span
                  style="color: red"
                >*</span>
              </template>
              <el-input
                v-model.trim="folderForm.name"
                required
                size="small"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>
          </el-main>
        </el-form>
        <el-footer class="de-footer">
          <el-button @click="closeSaveFolder">取消</el-button>
          <el-button
            type="primary"
            @click="doSaveFolder"
          >保存
          </el-button>
        </el-footer>
      </el-container>
    </el-dialog>

    <el-dialog
      v-dialogDrag
      append-to-body
      title="重命名"
      :visible.sync="showUpdateName"
      :show-close="true"
      width="600px"
      class="m-dialog"
    >
      <el-container
        style="width: 100%"
        direction="vertical"
      >
        <el-form
          ref="mUpdateNameForm"
          class="m-form"
          :model="updateFormData"
          label-position="top"
          hide-required-asterisk
        >
          <el-main>
            <el-form-item
              prop="name"
              class="form-item"
              :rules="[requiredRule]"
            >
              <template #label>
                名称
                <span
                  style="color: red"
                >*</span>
              </template>
              <el-input
                v-model.trim="updateFormData.name"
                required
                size="small"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>
          </el-main>
        </el-form>
        <el-footer class="de-footer">
          <el-button @click="closeUpdateForm">取消</el-button>
          <el-button
            type="primary"
            @click="doUpdateForm"
          >保存
          </el-button>
        </el-footer>
      </el-container>
    </el-dialog>

  </de-container>
</template>

<style  lang="scss" scoped>
.ms-aside-container {
  height: calc(100vh - 56px);
  padding: 0px;
  min-width: 260px;
  max-width: 460px;
}

.tab-panel {
  height: 100%;
  overflow-y: auto;
}

.tab-panel ::v-deep .el-tabs__nav-wrap {
  padding: 0 10px;
}

.tab-panel ::v-deep .el-tabs__nav-wrap::after {
  height: 1px;
}

.tab-panel ::v-deep .el-tabs__item {
  /* width: 10px; */
  padding: 0 10px;
}

::v-deep.ms-aside-container{
  padding: 0
}
.m-dialog {
  .el-dialog__body {
    padding: 0
  }
}
.de-footer {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-end
}
.custom-tree-node-list {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding: 0 8px;
}
.father .child {
  /*display: none;*/
  visibility: hidden;
}

.father:hover .child {
  /*display: inline;*/
  visibility: visible;
}
.no-tdata {
  text-align: center;
  margin-top: 80px;
  font-family: AlibabaPuHuiTi;
  font-size: 14px;
  color: var(--deTextSecondary, #646a73);
  font-weight: 400;

  .no-tdata-new {
    cursor: pointer;
    color: var(--primary, #3370ff);
  }
}
</style>
