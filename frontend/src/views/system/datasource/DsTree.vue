<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col class="tree-style">
    <el-col>
      <el-row class="title-css" v-show="showView === 'Datasource'">
        <el-col class="title-text" :span="12">
          {{ $t("commons.datasource") }}
        </el-col>
        <el-col class="title-operate" :span="12">
          <el-tooltip
            class="item"
            effect="dark"
            :content="$t('driver.mgm')"
            placement="top"
          >
            <i
              @click="driverMgm"
              v-show="user.isAdmin"
              class="el-icon-setting"
            ></i>
          </el-tooltip>
          <el-tooltip
            class="item"
            effect="dark"
            :content="$t('datasource.create')"
            placement="top"
          >
            <i @click="addFolder" class="el-icon-plus"></i>
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
          <el-tree
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
                    icon-class="exclamationmark"
                    class="ds-icon-scene"
                  />
                </span>
                <span v-if="data.status === 'Warning'">
                  <svg-icon
                    icon-class="exclamationmark2"
                    class="ds-icon-scene"
                  />
                </span>
                <span v-if="data.type === 'folder'">
                  <svg-icon icon-class="scene" class="ds-icon-scene" />
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
                    effect="dark"
                    v-if="['Error', 'Warning'].includes(data.status)"
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
                  class="item"
                  v-if="data.type === 'folder'"
                  effect="dark"
                  :content="
                    $t(showView === 'Driver' ? 'driver.add' : 'datasource.add_data_source')
                  "
                  placement="top"
                >
                  <i
                    @click.stop
                    @click="addFolderWithType(data)"
                    class="el-icon-plus"
                  ></i>
                </el-tooltip>

                <el-dropdown
                  v-else-if="hasDataPermission('manage', data.privileges)"
                  size="medium"
                  trigger="click"
                  @command="(type) => handleCommand(type, data)"
                >
                  <i @click.stop class="el-icon-more"></i>
                  <el-dropdown-menu class="de-card-dropdown" slot="dropdown">
                    <slot>
                      <el-dropdown-item command="edit">
                        <i class="el-icon-edit"></i>
                        {{ $t("chart.edit") }}
                      </el-dropdown-item>
                      <el-dropdown-item command="delete">
                        <i class="el-icon-delete"></i>
                        {{ $t("chart.delete") }}
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
        v-dialogDrag
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
            <el-input v-model="driverForm.name" :placeholder="$t('fu.search_bar.please_input')" />
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
            <deTextarea v-model="driverForm.desc"></deTextarea>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <deBtn secondary @click="close()">{{ $t("commons.cancel") }}</deBtn>
          <deBtn type="primary" size="mini" @click="saveDriver(driverForm)"
            >{{ $t("commons.save") }}
          </deBtn>
        </div>
      </el-dialog>

      <el-dialog
        v-dialogDrag
        :title="$t('datasource.create')"
        :visible.sync="dsTypeRelate"
        width="1200px"
        class="de-dialog-form"
        append-to-body
      >
        <el-tabs v-model="tabActive" @tab-click="changeTab">
          <el-tab-pane :label="$t('datasource.all')" name="all"> </el-tab-pane>
          <el-tab-pane :label="$t('datasource.relational_database')" name="reDb"> </el-tab-pane>
          <el-tab-pane :label="$t('datasource.non_relational_database')" name="noReDb">
          </el-tab-pane>
          <el-tab-pane :label="$t('datasource.other')" name="other"> </el-tab-pane>
          <div class="db-container">
            <div
              @click="addDb(db)"
              v-for="db in dsTypes"
              :key="db.type"
              class="db-card"
            >
              <img
                src="https://gimg3.baidu.com/search/src=http%3A%2F%2Fgips0.baidu.com%2Fit%2Fu%3D3429312337%2C2430696955%26fm%3D3030%26app%3D3030%26f%3DJPEG%3Fw%3D121%26h%3D74%26s%3D51321C7281B1598818E875C1030010B0&refer=http%3A%2F%2Fwww.baidu.com&app=2021&size=f242,150&n=0&g=0n&q=100&fmt=auto?sec=1662483600&t=53d1d1fe51561bb626c94ce63b5b31b8"
                alt=""
              />
              <p class="db-name">{{ db.name }}</p>
            </div>
          </div>
        </el-tabs>
      </el-dialog>
    </el-col>
  </el-col>
</template>
<script>
import { mapGetters } from "vuex";
import i18n from "@/lang";
import {
  listDatasource,
  listDatasourceByType,
  delDs,
  listDatasourceType,
  listDrivers,
  addDriver,
  delDriver,
  listDriverByType,
  updateDriver,
} from "@/api/system/datasource";
import { ApplicationContext } from "@/utils/ApplicationContext";
import deTextarea from "@/components/deCustomCm/deTextarea.vue";
import msgCfm from "@/components/msgCfm";
export default {
  name: "DsTree",
  mixins: [msgCfm],
  components: { deTextarea },
  props: {
    datasource: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      tabActive: "all",
      dsTypeRelate: false,
      expandedArray: [],
      tData: [],
      dsTypes: [],
      dsTypesForDriver: [],
      showSearchInput: false,
      key: "",
      showView: "Datasource",
      dialogTitle: "",
      editDriver: false,
      driverForm: {
        name: "",
        desc: "",
        type: "",
      },
      params: {},
      rule: {
        name: [
          {
            required: true,
            message: i18n.t("datasource.input_name"),
            trigger: "blur",
          },
          {
            min: 2,
            max: 50,
            message: i18n.t("datasource.input_limit_2_25", [2, 25]),
            trigger: "blur",
          },
        ],
        desc: [
          {
            required: true,
            message: i18n.t("datasource.input_name"),
            trigger: "blur",
          },
          {
            min: 2,
            max: 200,
            message: i18n.t("datasource.input_limit_2_25", [2, 25]),
            trigger: "blur",
          },
        ],
        type: [
          {
            required: true,
            message: i18n.t("datasource.please_choose_type"),
            trigger: "blur",
          },
        ],
      },
    };
  },
  watch: {
    key(val) {
      this.$refs.myDsTree.filter(val);
    },
  },
  computed: {
    ...mapGetters(["user"]),
  },
  created() {
    this.queryTreeDatas();
    this.datasourceTypes();
  },
  methods: {
    changeTab() {},
    filterNode(value, data) {
      if (!value) return true;
      return data.name.indexOf(value) !== -1;
    },
    showSearchWidget() {
      this.showSearchInput = true;
    },
    closeSearchWidget() {
      this.key = "";
      this.showSearchInput = false;
    },
    queryTreeDatas() {
      if (this.showView === "Datasource") {
        listDatasource().then((res) => {
          this.tData = this.buildTree(res.data);
        });
      }
      if (this.showView === "Driver") {
        listDrivers().then((res) => {
          this.tData = this.buildTree(res.data);
        });
      }
    },
    datasourceTypes() {
      listDatasourceType().then((res) => {
        this.dsTypes = res.data;
        this.dsTypes.forEach((item) => {
          if (item.isJdbc) {
            this.dsTypesForDriver.push(item);
          }
        });
      });
    },
    refreshType(datasource) {
      const method =
        this.showView === "Datasource"
          ? listDatasourceByType
          : listDriverByType;
      let typeData = [];
      method(datasource.type).then((res) => {
        typeData = this.buildTree(res.data);
        if (typeData.length === 0) {
          const index = this.tData.findIndex((item) => {
            if (item.id === datasource.type) {
              return true;
            }
          });
          this.tData.splice(index, 1);
        } else {
          let find = false;
          for (let index = 0; index < this.tData.length; index++) {
            if (typeData[0].id === this.tData[index].id) {
              this.tData[index].children = typeData[0].children;
              for (let i = 0; i < this.tData[index].children.length; i++) {
                if (this.tData[index].children[i].id === datasource.id) {
                  this.showInfo({ data: this.tData[index].children[i] });
                }
              }
              find = true;
            }
          }
          if (!find) {
            this.tData.push(typeData[0]);
          }
        }
      });
    },
    buildTree(array) {
      const types = {};
      const newArr = [];
      for (let index = 0; index < array.length; index++) {
        const element = array[index];
        if (this.msgNodeId) {
          if (element.id === this.msgNodeId) {
            element.msgNode = true;
          }
        }
        if (!(element.type in types)) {
          types[element.type] = [];
          newArr.push({
            id: element.type,
            name: element.typeDesc,
            type: "folder",
            children: types[element.type],
          });
        }
        types[element.type].push(element);
      }
      return newArr;
    },
    addFolder() {
      if (this.showView === "Driver") {
        this.dialogTitle = this.$t("datasource.add_driver");
        this.editDriver = true;
        this.switchMain("DriverForm", {}, this.tData, this.dsTypes);
      } else {
        this.dsTypeRelate = true;
      }
    },
    addDriver() {
      this.dialogTitle = this.$t("datasource.add_driver");
      this.editDriver = true;
    },
    driverMgm() {
      this.$emit("switch-main", {});
      this.$emit("switch-mgm", "driverMgm");
      this.showView = "Driver";
      this.expandedArray = [];
      this.tData = [];
      this.queryTreeDatas();
    },
    dsMgm() {
      this.$emit("switch-main", {});
      this.showView = "Datasource";
      this.expandedArray = [];
      this.tData = [];
      this.queryTreeDatas();
    },
    addDb({ type }) {
      this.$router.push({
        path: "/ds-form",
        query: { type },
      });
    },
    addFolderWithType(data) {
      if (this.showView === "Driver") {
        this.driverForm.type = data.id;
        this.dialogTitle = this.$t("datasource.add_driver");
        this.editDriver = true;
      } else {
        this.$router.push({
          path: "/ds-form",
          query: { type: data.id },
        });
      }
    },
    nodeClick(node, data) {
      if (node.type === "folder") return;
      this.showInfo(data);
    },
    clickFileMore(param) {
      const { optType, data } = param;
      switch (optType) {
        case "edit":
          this.edit(data);
          break;
        case "delete":
          this._handleDelete(data);
          break;
        default:
          break;
      }
    },
    showInfo(row) {
      if (this.showView === "Driver") {
        const param = { ...row.data, ...{ showModel: "show" } };
        this.switchMain(
          this.showView === "Datasource" ? "DsForm" : "DriverForm",
          param,
          this.tData,
          this.dsTypes
        );
      } else {
        this.switchMain("dsTable", row.data);
      }
    },
    handleCommand(type, data) {
      switch (type) {
        case "edit":
          this._handleEditer(data);
          break;
        case "delete":
          this._handleDelete(data);
          break;
        default:
          break;
      }
    },
    _handleEditer(row) {
      if (this.showView === "Datasource") {
        const param = { ...row, ...{ showModel: "show" } };
        this.switchMain(
          'DsForm',
          param,
          this.tData,
          this.dsTypes
        );
        return
      }
      this.editDriver = true;
      this.dialogTitle = this.$t('datasource.edit_driver');
      this.driverForm = row;
    },
    _handleDelete(datasource) {
      const params = {
        title: this.showView === "Datasource" ? 'datasource.this_data_source' : 'datasource.delete_this_driver',
        cb: () => {
          let method = delDriver;
          let parma = { type: datasource.type, id: datasource.id };
          if (this.showView === "Datasource") {
            method = delDs;
            parma = datasource.id;
          }
          method(parma).then((res) => {
            if (res.success) {
              this.openMessageSuccess("commons.delete_success");
              this.switchMain("", {}, this.tData, this.dsTypes);
              this.refreshType(datasource);
            } else {
              this.openMessageSuccess(res.message, "error")
            }
          });
        }
      }
      this.handlerConfirm(params)
    },
    switchMain(component, componentParam, tData, dsTypes) {
      if (component === "DsForm") {
        const { id, type, showModel } = componentParam;
        this.$router.push({
          path: "/ds-form",
          query: {
            id,
            type,
            showModel,
            msgNodeId: this.msgNodeId,
          },
        });
        return;
      }
      this.$emit("switch-main", {
        component,
        componentParam,
        tData,
        dsTypes,
      });
    },
    markInvalid(msgParam) {
      const param = JSON.parse(msgParam);
      const msgNodeId = param.id;
      this.msgNodeId = msgNodeId;
      this.$nextTick(() => {
        this.tData.forEach((folder) => {
          const nodes = folder.children;
          nodes.forEach((node) => {
            if (node.id === msgNodeId) {
              node.msgNode = true;
            }
          });
        });
      });
    },
    close() {
      this.$refs["driverForm"].resetFields();
      this.editDriver = false;
      this.driverForm = {
        name: "",
        desc: "",
        type: "",
      };
    },
    saveDriver(driverForm) {
      this.$refs["driverForm"].validate((valid) => {
        if (valid) {
          const req = driverForm.id ?  updateDriver : addDriver;
          req(driverForm).then((res) => {
            this.openMessageSuccess("dataset.save_success")
            this.refreshType(driverForm);
            this.close();
          });
        } else {
          return false;
        }
      });
    },
  },
};
</script>
<style lang="scss" scoped>
.custom-tree-container {
  margin-top: 16px;
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
.db-container {
  width: 100%;
  max-height: 674px;
  overflow-y: auto;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  .db-card {
    height: 193px;
    width: 270px;
    display: flex;
    flex-wrap: wrap;
    background: #ffffff;
    border: 1px solid #dee0e3;
    border-radius: 4px;
    margin-top: 24px;
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
}
</style>