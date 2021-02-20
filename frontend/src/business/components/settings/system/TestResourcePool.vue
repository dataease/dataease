<template>
  <div>
    <el-card class="table-card" v-loading="result.loading">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="search" @create="create"
                         :create-tip="$t('test_resource_pool.create_resource_pool')"
                         :title="$t('commons.test_resource_pool')"/>
      </template>
      <el-table border class="adjust-table" :data="items" style="width: 100%">
        <el-table-column prop="name" :label="$t('commons.name')"/>
        <el-table-column prop="description" :label="$t('commons.description')"/>
        <el-table-column prop="type" :label="$t('test_resource_pool.type')">
          <template v-slot:default="scope">
            <span v-if="scope.row.type === 'NODE'">Node</span>
            <span v-if="scope.row.type === 'K8S'" v-xpack>Kubernetes</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('test_resource_pool.enable_disable')">
          <template v-slot:default="scope">
            <el-switch v-model="scope.row.status"
                       inactive-color="#DCDFE6"
                       active-value="VALID"
                       inactive-value="INVALID"
                       @change="changeSwitch(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('commons.create_time')" width="180">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" :label="$t('commons.update_time')" width="180">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <ms-table-operator @editClick="edit(scope.row)" @deleteClick="del(scope.row)"/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <el-dialog
      :close-on-click-modal="false"
      :title="form.id ? $t('test_resource_pool.update_resource_pool') : $t('test_resource_pool.create_resource_pool')"
      :visible.sync="dialogVisible" width="70%"
      @closed="closeFunc"
      :destroy-on-close="true"
      v-loading="result.loading"
    >
      <el-form :model="form" label-position="right" label-width="120px" size="small" :rules="rule"
               ref="testResourcePoolForm">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input v-model="form.description" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.image')" prop="image">
          <el-input v-model="form.image" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('test_resource_pool.type')" prop="type">
          <el-select v-model="form.type" :placeholder="$t('test_resource_pool.select_pool_type')"
                     @change="changeResourceType(form.type)">
            <el-option key="NODE" value="NODE" label="Node">Node</el-option>
            <el-option key="K8S" value="K8S" label="Kubernetes" v-xpack>Kubernetes</el-option>
          </el-select>
        </el-form-item>
        <div v-for="(item,index) in infoList " :key="index">
          <div class="node-line" v-if="form.type === 'K8S'" v-xpack>
            <el-row>
              <el-col>
                <el-form-item label="Master URL"
                              :rules="requiredRules">
                  <el-input v-model="item.masterUrl" autocomplete="new-password"/>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col>
                <el-form-item label="Token"
                              :rules="requiredRules">
                  <el-input v-model="item.token" type="password" show-password autocomplete="new-password"/>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col>
                <el-form-item label="Namespace"
                              :rules="requiredRules">
                  <el-input v-model="item.namespace" type="text"/>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col>
                <el-form-item :label="$t('test_resource_pool.max_threads')"
                              :rules="requiredRules">
                  <el-input-number v-model="item.maxConcurrency" :min="1" :max="1000000000"/>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
          <div class="node-line" v-if="form.type === 'NODE'">
            <el-row>
              <el-col :span="8">
                <el-form-item label="IP" :rules="requiredRules">
                  <el-input v-model="item.ip" autocomplete="off"/>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="Port" style="padding-left: 20px"
                              :rules="requiredRules">
                  <el-input-number v-model="item.port" :min="1" :max="65535"></el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item :label="$t('test_resource_pool.max_threads')"
                              :rules="requiredRules"
                              style="padding-left: 20px">
                  <el-input-number v-model="item.maxConcurrency" :min="1" :max="1000000000"></el-input-number>
                </el-form-item>
              </el-col>
              <el-col :offset="2" :span="2">
                <span class="box">
                    <el-button @click="addResourceInfo()" type="success" size="mini" circle>
                        <font-awesome-icon :icon="['fas', 'plus']"/>
                    </el-button>
                </span>
                <span class="box">
                    <el-button @click="removeResourceInfo(index)" type="danger" size="mini" circle>
                        <font-awesome-icon :icon="['fas', 'minus']"/>
                    </el-button>
                </span>
              </el-col>
            </el-row>
          </div>
        </div>

      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          v-if="form.id"
          @cancel="dialogVisible = false"
          @confirm="updateTestResourcePool()"/>
        <ms-dialog-footer
          v-else
          @cancel="dialogVisible = false"
          @confirm="createTestResourcePool()"/>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import MsCreateBox from "../CreateBox";
import MsTablePagination from "../../common/pagination/TablePagination";
import MsTableHeader from "../../common/components/MsTableHeader";
import MsTableOperator from "../../common/components/MsTableOperator";
import MsDialogFooter from "../../common/components/MsDialogFooter";
import {listenGoBack, removeGoBackListener} from "@/common/js/utils";

export default {
  name: "MsTestResourcePool",
  components: {MsCreateBox, MsTablePagination, MsTableHeader, MsTableOperator, MsDialogFooter},
  data() {
    return {
      result: {},
      dialogVisible: false,
      infoList: [],
      queryPath: "testresourcepool/list",
      condition: {},
      items: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      form: {},
      requiredRules: [{required: true, message: this.$t('test_resource_pool.fill_the_data'), trigger: 'blur'}],
      rule: {
        name: [
          {required: true, message: this.$t('test_resource_pool.input_pool_name'), trigger: 'blur'},
          {min: 2, max: 20, message: this.$t('commons.input_limit', [2, 20]), trigger: 'blur'},
          {
            required: true,
            pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9.·-]+$/,
            message: this.$t('test_resource_pool.pool_name_valid'),
            trigger: 'blur'
          }
        ],
        description: [
          {max: 60, message: this.$t('commons.input_limit', [0, 60]), trigger: 'blur'}
        ],
        type: [
          {required: true, message: this.$t('test_resource_pool.select_pool_type'), trigger: 'blur'}
        ]
      }
    }
  },
  activated() {
    this.initTableData();
  },
  methods: {
    initTableData() {

      this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
        let data = response.data;
        this.items = data.listObject;
        this.total = data.itemCount;
      })
    },
    changeResourceType(type) {
      this.infoList = [];
      let info = {};
      if (type === 'NODE') {
        info.ip = '';
        info.port = '8082';
      }
      if (type === 'K8S') {
        info.masterUrl = '';
        info.token = '';
        info.namespace = '';
      }
      info.maxConcurrency = 100;
      this.infoList.push(info);
    },

    addResourceInfo() {
      this.infoList.push({})
    },
    removeResourceInfo(index) {
      if (this.infoList.length > 1) {
        this.infoList.splice(index, 1)
      } else {
        this.$warning(this.$t('test_resource_pool.cannot_remove_all_node'))
      }
    },
    validateResourceInfo() {
      if (this.infoList.length <= 0) {
        return {validate: false, msg: this.$t('test_resource_pool.cannot_empty')}
      }

      let resultValidate = {validate: true, msg: this.$t('test_resource_pool.fill_the_data')};
      this.infoList.forEach(function (info) {
        for (let key in info) {
          if (info[key] != '0' && !info[key]) {
            resultValidate.validate = false
            return false;
          }
        }

        if (!info.maxConcurrency) {
          resultValidate.validate = false
          return false;
        }
      });

      return resultValidate;
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    search() {
      this.initTableData();
    },
    create() {
      this.dialogVisible = true;
      listenGoBack(this.closeFunc);
    },
    edit(row) {
      this.dialogVisible = true;
      this.form = JSON.parse(JSON.stringify(row));
      this.convertResources();
      listenGoBack(this.closeFunc);
    },
    convertResources() {
      let resources = [];
      if (this.form.resources) {
        this.form.resources.forEach(function (resource) {
          let configuration = JSON.parse(resource.configuration);
          configuration.id = resource.id
          resources.push(configuration);
        })
      }
      this.infoList = resources;
    },
    del(row) {
      this.$confirm(this.$t('test_resource_pool.delete_prompt'), this.$t('commons.prompt'), {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.result = this.$get(`/testresourcepool/delete/${row.id}`, () => {
          this.initTableData();
          this.$success(this.$t('commons.delete_success'));
        });
      }).catch(() => {
        this.$info(this.$t('commons.delete_cancel'));
      });
    },
    createTestResourcePool() {
      this.$refs.testResourcePoolForm.validate(valid => {
        if (valid) {
          let vri = this.validateResourceInfo();
          if (vri.validate) {
            this.convertSubmitResources();
            this.result = this.$post("/testresourcepool/add", this.form, () => {
              this.$message({
                  type: 'success',
                  message: this.$t('commons.save_success')
                },
                this.dialogVisible = false,
                this.initTableData());
            });
          } else {
            this.$warning(vri.msg);
            return false;
          }

        } else {
          return false;
        }
      })
    },
    convertSubmitResources() {
      let resources = [];
      let poolId = this.form.id;
      this.infoList.forEach(function (info) {
        let configuration = JSON.stringify(info);
        let resource = {"configuration": configuration, id: info.id};
        if (poolId) {
          resource.testResourcePoolId = poolId;
        }
        resources.push(resource);
      });
      this.form.resources = resources;
    },
    updateTestResourcePool() {
      this.$refs.testResourcePoolForm.validate(valid => {
        if (valid) {
          let vri = this.validateResourceInfo();
          if (vri.validate) {
            this.convertSubmitResources();
            this.result = this.$post("/testresourcepool/update", this.form, () => {
              this.$message({
                  type: 'success',
                  message: this.$t('commons.modify_success')
                },
                this.dialogVisible = false,
                this.initTableData(),
                self.loading = false);
            });
          } else {
            this.$warning(vri.msg);
            return false;
          }
        } else {
          return false;
        }
      });
    },
    closeFunc() {
      this.form = {};
      this.dialogVisible = false;
      removeGoBackListener(this.closeFunc);
    },
    changeSwitch(row) {
      this.result.loading = true;
      this.$info(this.$t('test_resource_pool.check_in'), 1000);
      this.$get('/testresourcepool/update/' + row.id + '/' + row.status)
        .then(() => {
          this.$success(this.$t('test_resource_pool.status_change_success'));
          this.result.loading = false;
        }).catch(() => {
        this.$error(this.$t('test_resource_pool.status_change_failed'));
        row.status = 'INVALID';
        this.result.loading = false;
      })
    }
  }
}
</script>

<style scoped>

.box {
  padding-left: 5px;
}

</style>
