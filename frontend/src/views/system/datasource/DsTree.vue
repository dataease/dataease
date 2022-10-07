<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col class="tree-style">
    <el-col>
      <el-row v-show="showView === 'Datasource'" class="title-css">
        <el-col class="title-text" :span="12">
          {{ $t('commons.datasource') }}
        </el-col>
        <el-col class="title-operate" :span="12">
          <el-tooltip
            class="item"
            effect="dark"
            :content="$t('driver.mgm')"
            placement="top"
          >
            <i
              v-show="user.isAdmin"
              class="el-icon-setting"
              @click="driverMgm"
            />
          </el-tooltip>
          <el-tooltip
            class="item"
            effect="dark"
            :content="$t('datasource.create')"
            placement="top"
          >
            <i class="el-icon-plus" @click="addFolder" />
          </el-tooltip>
        </el-col>
      </el-row>

      <el-input
        v-model="key"
        size="small"
        :placeholder="$t('chart.search')"
        prefix-icon="el-icon-search"
        clearable
        class="main-area-input"
      />

      <el-col class="custom-tree-container de-tree">
        <div class="block">
          <div v-if="!tData.length && !treeLoading" class="no-tdata">
            {{ showView === 'Driver' ? '暂无驱动' : '暂无数据源' }}
            <span @click="() => createDriveOrDs()" class="no-tdata-new">{{
              $t('deDataset.create')
            }}</span>
          </div>
          <el-tree
            v-else
            ref="myDsTree"
            :default-expanded-keys="expandedArray"
            :data="tData"
            node-key="id"
            :expand-on-click-node="true"
            :filter-node-method="filterNode"
            @node-click="nodeClick"
          >
            <span slot-scope="{ data }" class="custom-tree-node-list">
              <span style="display: flex; width: calc(100% - 20px)">
                <span
                  v-if="
                    data.type !== 'folder' &&
                      data.status !== 'Error' &&
                      data.status !== 'Warning'
                  "
                >
                  <svg-icon
                    :icon-class="showView === 'Driver' ? 'driver-de' : 'db-de'"
                  />
                </span>
                <span v-if="data.status === 'Error'">
                  <svg-icon
                    icon-class="de-ds-error"
                    class="ds-icon-scene"
                  />
                </span>
                <span v-if="data.status === 'Warning'">
                  <svg-icon
                    icon-class="de-ds-warning"
                    class="ds-icon-scene"
                  />
                </span>
                <span v-if="data.type === 'folder'">
                  <svg-icon icon-class="scene"/>
                </span>
                <span
                  style="
                    margin-left: 6px;
                    width: 100%;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                  "
                >
                  <el-tooltip
                    v-if="['Error', 'Warning'].includes(data.status)"
                    effect="dark"
                    :content="
                      data.status === 'Warning'
                        ? $t('datasource.warning')
                        : $t('datasource.in_valid')
                    "
                    placement="right"
                  >
                    <span>
                      {{ data.name }}
                    </span>
                  </el-tooltip>
                  <template v-else>
                    {{ data.name }}
                  </template>
                </span>
              </span>
              <span class="child">
                <el-tooltip
                  v-if="data.type === 'folder'"
                  class="item"
                  effect="dark"
                  :content="
                    $t(
                      showView === 'Driver'
                        ? 'driver.add'
                        : 'datasource.add_data_source'
                    )
                  "
                  placement="top"
                >
                  <i
                    class="el-icon-plus"
                    @click.stop
                    @click="addFolderWithType(data)"
                  />
                </el-tooltip>

                <el-dropdown
                  v-else-if="hasDataPermission('manage', data.privileges)"
                  size="medium"
                  trigger="click"
                  @command="(type) => handleCommand(type, data)"
                >
                  <i class="el-icon-more" @click.stop />
                  <el-dropdown-menu slot="dropdown" class="de-card-dropdown">
                    <slot>
                      <el-dropdown-item command="edit">
                        <i class="el-icon-edit" />
                        {{ $t('chart.edit') }}
                      </el-dropdown-item>
                      <el-dropdown-item command="delete">
                        <i class="el-icon-delete" />
                        {{ $t('chart.delete') }}
                      </el-dropdown-item>
                    </slot>
                  </el-dropdown-menu>
                </el-dropdown>
              </span>
            </span>
          </el-tree>
        </div>
      </el-col>

      <el-dialog
        :title="dialogTitle"
        class="de-dialog-form"
        :visible.sync="editDriver"
        width="600px"
        append-to-body
      >
        <el-form
          ref="driverForm"
          :model="driverForm"
          class="de-form-item"
          label-position="right"
          label-width="100px"
          size="small"
          :rules="rule"
        >
          <el-form-item :label="$t('datasource.driver_name')" prop="name">
            <el-input
              v-model="driverForm.name"
              :placeholder="$t('fu.search_bar.please_input')"
            />
          </el-form-item>
          <el-form-item :label="$t('datasource.drive_type')" prop="type">
            <el-select
              v-model="driverForm.type"
              :placeholder="$t('fu.search_bar.please_select')"
              class="de-select"
              :disabled="!!driverForm.id"
              filterable
            >
              <el-option
                v-for="item in dsTypesForDriver"
                :key="item.type"
                :label="item.name"
                :value="item.type"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('commons.description')">
            <deTextarea v-model="driverForm.desc" />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <deBtn secondary @click="close()">{{ $t('commons.cancel') }}</deBtn>
          <deBtn
            type="primary"
            size="mini"
            @click="saveDriver(driverForm)"
          >{{ $t('commons.save') }}
          </deBtn>
        </div>
      </el-dialog>

      <el-dialog
        v-dialogDrag
        :title="$t('datasource.create')"
        :visible.sync="dsTypeRelate"
        width="1200px"
        class="de-dialog-form none-scroll-bar"
        append-to-body
      >
        <el-tabs v-model="tabActive">
          <el-tab-pane :label="$t('datasource.all')" name="all" />
          <el-tab-pane
            :label="$t('datasource.relational_database')"
            name="RDBMS"
          />
          <el-tab-pane
            :label="$t('datasource.non_relational_database')"
            name="NORDBMS"
          />
          <el-tab-pane :label="$t('datasource.other')" name="OTHER" />
        </el-tabs>
        <div class="db-container">
          <div
            v-for="(db, index) in databaseList"
            :key="db.type"
            class="db-card"
            :class="[{ marLeft: index % 4 === 0 }]"
            @click="addDb(db)"
          >
            <img
              v-if="!db.isPlugin"
              :src="require('../../../assets/datasource/' + db.type + '.jpg')"
              alt=""
            >
            <img
              v-if="db.isPlugin"
              :src="`/api/pluginCommon/staticInfo/${db.type}/jpg`"
            >
            <p class="db-name">{{ db.name }}</p>
          </div>
        </div>
      </el-dialog>
    </el-col>
  </el-col>
</template>
<script>
import { mapGetters } from 'vuex'
import i18n from '@/lang'
import {
  listDatasource,
  listDatasourceByType,
  delDs,
  listDatasourceType,
  listDrivers,
  addDriver,
  delDriver,
  listDriverByType,
  updateDriver
} from '@/api/system/datasource'
import deTextarea from '@/components/deCustomCm/deTextarea.vue'
import msgCfm from '@/components/msgCfm'
export default {
  name: 'DsTree',
  components: { deTextarea },
  mixins: [msgCfm],
  props: {
    datasource: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      tabActive: 'all',
      dsTypeRelate: false,
      expandedArray: [],
      tData: [],
      treeLoading: false,
      dsTypes: [],
      dsTypesForDriver: [],
      showSearchInput: false,
      key: '',
      showView: 'Datasource',
      dialogTitle: '',
      editDriver: false,
      driverForm: {
        name: '',
        desc: '',
        type: ''
      },
      params: {},
      rule: {
        name: [
          {
            required: true,
            message: i18n.t('datasource.input_name'),
            trigger: 'blur'
          },
          {
            min: 2,
            max: 50,
            message: i18n.t('datasource.input_limit_2_25', [2, 25]),
            trigger: 'blur'
          }
        ],
        desc: [
          {
            required: true,
            message: i18n.t('datasource.input_name'),
            trigger: 'blur'
          },
          {
            min: 2,
            max: 200,
            message: i18n.t('datasource.input_limit_2_25', [2, 25]),
            trigger: 'blur'
          }
        ],
        type: [
          {
            required: true,
            message: i18n.t('datasource.please_choose_type'),
            trigger: 'blur'
          }
        ]
      }
    }
  },
  watch: {
    key(val) {
      this.$refs.myDsTree.filter(val)
    }
  },
  computed: {
    ...mapGetters(['user']),
    databaseList() {
      if (this.tabActive === 'all') {
        return this.dsTypes
      }
      return this.dsTypes.filter(
        (ele) => ele.databaseClassification === this.tabActive
      )
    }
  },
  created() {
    this.queryTreeDatas()
    this.datasourceTypes()
  },
  methods: {
    createDriveOrDs() {
      if (this.showView === 'Driver') {
        this.addDriver()
      } else {
        this.addFolder()
      }
    },
    dataUpdate(row) {
      this.dfsTdata(this.tData, row)
    },
    dfsTdata(arr = [], row) {
      arr.some(ele => {
        if (ele.id === row.id) {
          ele.driverClass = row.driverClass
          return true
        } else if (ele.children?.length) {
          this.dfsTdata(ele.children, row)
        }
        return false
      })
    },
    filterNode(value, data) {
      if (!value) return true
      return data.name.indexOf(value) !== -1
    },
    showSearchWidget() {
      this.showSearchInput = true
    },
    closeSearchWidget() {
      this.key = ''
      this.showSearchInput = false
    },
    queryTreeDatas() {
      this.treeLoading = true
      if (this.showView === 'Datasource') {
        listDatasource().then((res) => {
          this.tData = this.buildTree(res.data)
        }).finally(() => {
        this.treeLoading = false
      })
      }
      if (this.showView === 'Driver') {
        listDrivers().then((res) => {
          this.tData = this.buildTree(res.data)
        }).finally(() => {
        this.treeLoading = false
      })
      }
    },
    datasourceTypes() {
      listDatasourceType().then((res) => {
        this.dsTypes = res.data
        this.dsTypes.forEach((item) => {
          if (item.isJdbc) {
            this.dsTypesForDriver.push(item)
          }
        })
      })
    },
    refreshType(datasource) {
      const method =
        this.showView === 'Datasource' ? listDatasourceByType : listDriverByType
      let typeData = []
      method(datasource.type).then((res) => {
        typeData = this.buildTree(res.data)
        if (typeData.length === 0) {
          const index = this.tData.findIndex((item) => {
            if (item.id === datasource.type) {
              return true
            }
          })
          this.tData.splice(index, 1)
        } else {
          let find = false
          for (let index = 0; index < this.tData.length; index++) {
            if (typeData[0].id === this.tData[index].id) {
              this.tData[index].children = typeData[0].children
              for (let i = 0; i < this.tData[index].children.length; i++) {
                if (this.tData[index].children[i].id === datasource.id) {
                  this.showInfo({ data: this.tData[index].children[i] })
                }
              }
              find = true
            }
          }
          if (!find) {
            this.tData.push(typeData[0])
          }
        }
      })
    },
    buildTree(array = []) {
      const types = {}
      const newArr = []
      for (let index = 0; index < array.length; index++) {
        const element = array[index]
        if (this.msgNodeId) {
          if (element.id === this.msgNodeId) {
            element.msgNode = true
          }
        }
        if (!(element.type in types)) {
          types[element.type] = []
          newArr.push({
            id: element.type,
            name: element.typeDesc,
            type: 'folder',
            children: types[element.type]
          })
        }
        types[element.type].push(element)
      }
      return newArr
    },
    addFolder() {
      if (this.showView === 'Driver') {
        this.dialogTitle = this.$t('datasource.add_driver')
        this.editDriver = true
        this.switchMain('DriverForm', {}, this.tData, this.dsTypes)
      } else {
        this.dsTypeRelate = true
      }
    },
    addDriver() {
      this.dialogTitle = this.$t('datasource.add_driver')
      this.editDriver = true
    },
    driverMgm() {
      this.$emit('switch-main', {})
      this.$emit('switch-mgm', 'driverMgm')
      this.showView = 'Driver'
      this.expandedArray = []
      this.tData = []
      this.queryTreeDatas()
    },
    dsMgm() {
      this.$emit('switch-main', {})
      this.showView = 'Datasource'
      this.expandedArray = []
      this.tData = []
      this.queryTreeDatas()
    },
    addDb({ type }) {
      const name = (this.dsTypes.find(ele => type === ele.type ) || {}).name
      this.$router.push({
        path: '/ds-form',
        query: { type, name }
      })
    },
    addFolderWithType(data) {
      if (this.showView === 'Driver') {
        this.driverForm.type = data.id
        this.dialogTitle = this.$t('datasource.add_driver')
        this.editDriver = true
      } else {
        const name = (this.dsTypes.find(ele => data.id === ele.type ) || {}).name
        this.$router.push({
          path: '/ds-form',
          query: { type: data.id, name }
        })
      }
    },
    nodeClick(node, data) {
      if (node.type === 'folder') return
      this.showInfo(data)
    },
    clickFileMore(param) {
      const { optType, data } = param
      switch (optType) {
        case 'edit':
          this.edit(data)
          break
        case 'delete':
          this._handleDelete(data)
          break
        default:
          break
      }
    },
    showInfo(row) {
      if (this.showView === 'Driver') {
        const param = { ...row.data, ...{ showModel: 'show' }}
        this.switchMain(
          this.showView === 'Datasource' ? 'DsForm' : 'DriverForm',
          param,
          this.tData,
          this.dsTypes
        )
      } else {
        this.switchMain('dsTable', row.data)
      }
    },
    handleCommand(type, data) {
      switch (type) {
        case 'edit':
          this._handleEditer(data)
          break
        case 'delete':
          this._handleDelete(data)
          break
        default:
          break
      }
    },
    _handleEditer(row) {
      if (this.showView === 'Datasource') {
        const param = { ...row, ...{ showModel: 'show' }}
        this.switchMain('DsForm', param, this.tData, this.dsTypes)
        return
      }
      this.editDriver = true
      this.dialogTitle = this.$t('datasource.edit_driver')
      this.driverForm = {...row}
    },
    _handleDelete(datasource) {
      const params = {
        title:
          this.showView === 'Datasource'
            ? 'datasource.this_data_source'
            : 'datasource.delete_this_driver',
        cb: () => {
          let method = delDriver
          let parma = { type: datasource.type, id: datasource.id }
          if (this.showView === 'Datasource') {
            method = delDs
            parma = datasource.id
          }
          method(parma).then((res) => {
            if (res.success) {
              this.openMessageSuccess('commons.delete_success')
              this.switchMain('', {}, this.tData, this.dsTypes)
              this.refreshType(datasource)
            } else {
              this.openMessageSuccess(res.message, 'error')
            }
          })
        }
      }
      this.handlerConfirm(params)
    },
    switchMain(component, componentParam, tData, dsTypes) {
      if (component === 'DsForm') {
        const { id, type, showModel } = componentParam
        this.$router.push({
          path: '/ds-form',
          query: {
            id,
            type,
            showModel,
            msgNodeId: this.msgNodeId
          }
        })
        return
      }
      this.$emit('switch-main', {
        component,
        componentParam,
        tData,
        dsTypes
      })
    },
    markInvalid(msgParam) {
      const param = JSON.parse(msgParam)
      const msgNodeId = param.id
      this.msgNodeId = msgNodeId
      this.$nextTick(() => {
        this.tData.forEach((folder) => {
          const nodes = folder.children
          nodes.forEach((node) => {
            if (node.id === msgNodeId) {
              node.msgNode = true
            }
          })
        })
      })
    },
    close() {
      this.$refs['driverForm'].resetFields()
      this.editDriver = false
      this.driverForm = {
        name: '',
        desc: '',
        type: ''
      }
    },
    saveDriver(driverForm) {
      this.$refs['driverForm'].validate((valid) => {
        if (valid) {
          const req = driverForm.id ? updateDriver : addDriver
          req(driverForm).then((res) => {
            this.openMessageSuccess('dataset.save_success')
            this.refreshType(driverForm)
            this.close()
          })
        } else {
          return false
        }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.custom-tree-container {
  margin-top: 16px;
  .no-tdata {
    text-align: center;
    margin-top: 80px;
    font-family: PingFang SC;
    font-size: 14px;
    color: var(--deTextSecondary, #646a73);
    font-weight: 400;
    .no-tdata-new {
      cursor: pointer;
      color: var(--primary, #3370ff);
    }
  }
}
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
  width: 100%;
}
.custom-tree-node-list {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding: 0 8px;
  width: calc(100% - 40px);
  .child {
    /*display: none;*/
    visibility: hidden;
  }
  &:hover .child {
    /*display: inline;*/
    visibility: visible;
  }
}
.tree-list ::v-deep .el-tree-node__expand-icon.is-leaf {
  display: none;
}
.tree-style {
  padding: 16px 24px;
  height: 100%;
  overflow-y: auto;
  .title-text {
    line-height: 26px;
    color: #1f2329;
    margin-bottom: 16px;
  }
  .title-operate {
    text-align: right;
    i {
      margin-left: 22.91px;
    }
  }
}
</style>
<style lang="scss">
.none-scroll-bar::-webkit-scrollbar { display: none; }

.db-container {
  width: 100%;
  max-height: 65vh;
  overflow-y: auto;
  display: flex;
  flex-wrap: wrap;
  margin-top: -3px;
  position: relative;
  z-index: 10;
  .db-card {
    height: 193px;
    width: 270px;
    display: flex;
    flex-wrap: wrap;
    background: #ffffff;
    border: 1px solid #dee0e3;
    border-radius: 4px;
    margin-bottom: 24px;
    margin-left: 22px;
    img {
      width: 100%;
      height: 154.58px;
      border-top-left-radius: 4px;
      border-top-right-radius: 4px;
    }
    .db-name {
      width: 100%;
      margin: 0;
      display: flex;
      align-items: center;
      padding: 8px 12px;
      border-top: 1px solid rgba(#1f2329, 0.15);
    }
    &:hover {
      box-shadow: 0px 6px 24px rgba(31, 35, 41, 0.08);
    }
  }

  .marLeft {
    margin-left: 0;
  }
}
</style>
