<template>
  <div>
    <operator title="system_parameter_setting.engine_mode_setting">
      <deBtn
        v-if="showCancel"
        secondary
        @click="cancel"
      >
        {{ $t("commons.cancel") }}
      </deBtn>
      <deBtn
        secondary
        @click="validaDatasource"
      >
        {{ $t("commons.validate") }}
      </deBtn>
      <deBtn
        v-if="showEdit"
        type="primary"
        @click="edit"
      >
        {{ $t("commons.edit") }}
      </deBtn>
      <deBtn
        v-if="showSave"
        type="primary"
        @click="save"
      >
        {{ $t("commons.save") }}
      </deBtn>
    </operator>
    <el-form
      ref="form"
      v-loading="loading"
      :model="form"
      :rules="rules"
      class="de-form-item"
      :disabled="show"
      label-width="180px"
      label-position="top"
      size="small"
    >
      <el-form-item
        :label="$t('datasource.doris_host')"
        prop="configuration.host"
      >
        <el-input v-model="form.configuration.host" />
      </el-form-item>
      <el-form-item
        :label="$t('datasource.data_base')"
        prop="configuration.dataBase"
      >
        <el-input v-model="form.configuration.dataBase" />
      </el-form-item>
      <el-form-item :label="$t('datasource.user_name')">
        <el-input v-model="form.configuration.username" />
      </el-form-item>
      <el-form-item :label="$t('datasource.password')">
        <dePwd v-model="form.configuration.password" />
      </el-form-item>
      <el-form-item
        :label="$t('datasource.query_port')"
        prop="configuration.port"
      >
        <el-input-number
          v-model="form.configuration.port"
          controls-position="right"
          autocomplete="off"
          type="number"
          min="0"
        />
      </el-form-item>
      <el-form-item
        :label="$t('datasource.http_port')"
        prop="configuration.httpPort"
      >
        <el-input-number
          v-model="form.configuration.httpPort"
          controls-position="right"
          autocomplete="off"
          type="number"
          min="0"
        />
      </el-form-item>

      <span
        class="de-expand"
        @click="showPriority = !showPriority"
      >{{ $t("datasource.priority")
       }}<i
         v-if="showPriority"
         class="el-icon-arrow-up"
       />
        <i
          v-else
          class="el-icon-arrow-down"
        /></span>
      <template v-if="showPriority">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item
              :label="$t('datasource.replication_num')"
              prop="configuration.replicationNum"
            >
              <el-input-number
                v-model="form.configuration.replicationNum"
                controls-position="right"
                autocomplete="off"
                type="number"
                min="1"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              :label="$t('datasource.bucket_num')"
              prop="configuration.bucketNum"
            >
              <el-input-number
                v-model="form.configuration.bucketNum"
                controls-position="right"
                autocomplete="off"
                type="number"
                min="1"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item
              :label="$t('datasource.initial_pool_size')"
              prop="configuration.initialPoolSize"
            >
              <el-input-number
                v-model="form.configuration.initialPoolSize"
                controls-position="right"
                autocomplete="off"
                type="number"
                min="0"
                size="small"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              :label="$t('datasource.min_pool_size')"
              prop="configuration.minPoolSize"
            >
              <el-input-number
                v-model="form.configuration.minPoolSize"
                controls-position="right"
                autocomplete="off"
                type="number"
                min="0"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item
              :label="$t('datasource.max_pool_size')"
              prop="configuration.maxPoolSize"
            >
              <el-input-number
                v-model="form.configuration.maxPoolSize"
                controls-position="right"
                autocomplete="off"
                type="number"
                min="0"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </template>
    </el-form>
  </div>
</template>

<script>
import { engineInfo, validate, save } from '@/api/system/engine'
import i18n from '@/lang'
import operator from './Operator'
import msgCfm from '@/components/msgCfm'
import dePwd from '@/components/deCustomCm/DePwd.vue'
export default {
  name: 'ClusterMode',
  components: {
    operator,
    dePwd
  },
  mixins: [msgCfm],
  data() {
    return {
      showPriority: false,
      form: {
        type: 'engine_doris',
        configuration: {
          host: '',
          dataBase: '',
          username: '',
          password: '',
          port: '',
          httpPort: 8030,
          extraParams:
            'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull',
          replicationNum: 1,
          bucketNum: 10,
          minPoolSize: 5,
          maxPoolSize: 50,
          initialPoolSize: 5
        }
      },
      originConfiguration: {
        host: '',
        dataBase: '',
        username: '',
        password: '',
        port: '',
        httpPort: 8030,
        extraParams:
          'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull',
        replicationNum: 1,
        bucketNum: 10,
        minPoolSize: 5,
        maxPoolSize: 50,
        initialPoolSize: 5
      },
      input: '',
      visible: true,
      showEdit: true,
      showSave: false,
      showCancel: false,
      show: true,
      disabledConnection: false,
      disabledSave: false,
      loading: false,
      rules: {
        'configuration.host': [
          {
            required: true,
            message: this.$t('datasource.please_input_host'),
            trigger: ['change', 'blur']
          }
        ],
        'configuration.port': [
          {
            required: true,
            message: this.$t('datasource.please_input_port'),
            trigger: ['change', 'blur']
          }
        ],
        'configuration.httpPort': [
          {
            required: true,
            message: this.$t('datasource.please_input_port'),
            trigger: ['change', 'blur']
          }
        ],
        'configuration.dataBase': [
          {
            required: true,
            message: this.$t('datasource.please_input_data_base'),
            trigger: ['change', 'blur']
          }
        ],
        'configuration.replicationNum': [
          {
            required: true,
            message: this.$t('datasource.please_input_replication_num'),
            trigger: ['change', 'blur']
          }
        ],
        'configuration.bucketNum': [
          {
            required: true,
            message: this.$t('datasource.please_input_bucket_num'),
            trigger: ['change', 'blur']
          }
        ],
        'configuration.minPoolSize': [
          {
            required: true,
            message: this.$t('datasource.commons.cannot_be_null'),
            trigger: ['change', 'blur']
          }
        ],
        'configuration.initialPoolSize': [
          {
            required: true,
            message: this.$t('commons.cannot_be_null'),
            trigger: ['change', 'blur']
          }
        ],
        'configuration.maxPoolSize': [
          {
            required: true,
            message: this.$t('datasource.commons.cannot_be_null'),
            trigger: ['change', 'blur']
          }
        ]
      },
      allTypes: [
        {
          name: 'engine_mysql',
          label: 'MySQL',
          type: 'jdbc',
          extraParams:
            'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
        }
      ]
    }
  },

  created() {
    this.query()
  },
  methods: {
    query() {
      engineInfo().then((response) => {
        if (response.data.id) {
          this.form = JSON.parse(JSON.stringify(response.data))
          this.form.configuration = JSON.parse(this.form.configuration)
          this.originConfiguration = JSON.parse(
            JSON.stringify(this.form.configuration)
          )
        }
        this.$nextTick(() => {
          this.$refs.form.clearValidate()
        })
      })
    },
    edit() {
      this.showEdit = false
      this.showSave = true
      this.showCancel = true
      this.show = false
    },
    save() {
      if (
        this.form.configuration.dataSourceType === 'jdbc' &&
        (this.form.configuration.port <= 0 ||
          this.form.configuration.httpPort <= 0)
      ) {
        this.$message.error(i18n.t('datasource.port_no_less_then_0'))
        return
      }
      if (
        this.form.configuration.initialPoolSize < 0 ||
        this.form.configuration.minPoolSize < 0 ||
        this.form.configuration.maxPoolSize < 0
      ) {
        this.$message.error(i18n.t('datasource.no_less_then_0'))
        return
      }
      this.$refs.form.validate((valid) => {
        if (!valid) {
          return false
        }
        const form = JSON.parse(JSON.stringify(this.form))
        form.configuration = JSON.stringify(form.configuration)
        save(form).then((res) => {
          this.showEdit = true
          this.showCancel = false
          this.showSave = false
          this.show = true
          this.originConfiguration = JSON.parse(
            JSON.stringify(this.form.configuration)
          )
          this.openMessageSuccess('commons.save_success')
        })
      })
    },
    cancel() {
      this.showEdit = true
      this.showCancel = false
      this.showSave = false
      this.show = true
      this.form.configuration = JSON.parse(
        JSON.stringify(this.originConfiguration)
      )
    },
    changeType() {
      for (let i = 0; i < this.allTypes.length; i++) {
        if (this.allTypes[i].name === this.form.type) {
          this.form.configuration.dataSourceType = this.allTypes[i].type
          this.form.configuration.extraParams = this.allTypes[i].extraParams
        }
      }
    },
    validaDatasource() {
      if (!this.form.configuration.schema && this.form.type === 'oracle') {
        this.$message.error(i18n.t('datasource.please_choose_schema'))
        return
      }
      if (
        this.form.configuration.dataSourceType === 'jdbc' &&
        (this.form.configuration.port <= 0 ||
          this.form.configuration.httpPort <= 0)
      ) {
        this.$message.error(i18n.t('datasource.port_no_less_then_0'))
        return
      }
      this.$refs.form.validate((valid) => {
        if (valid) {
          const data = JSON.parse(JSON.stringify(this.form))
          data.configuration = JSON.stringify(data.configuration)
          validate(data).then((res) => {
            if (res.success) {
              this.openMessageSuccess('datasource.validate_success')
            } else {
              if (res.message.length < 2500) {
                this.$error(res.message)
              } else {
                this.$error(res.message.substring(0, 2500) + '......')
              }
            }
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
.de-expand {
  font-family: PingFang SC;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  color: #3370ff;
  cursor: pointer;
  margin: 12px 0 16px 0;
  display: inline-block;
}
.el-input-number {
  width: 100%;
  ::v-deep .el-input__inner {
    text-align: left;
  }

  ::v-deep.el-input-number__decrease,
  ::v-deep.el-input-number__increase {
    background: transparent;
  }

  &.is-disabled {
    .el-input-number__decrease {
      border-right-color: #e4e7ed;
    }

    .el-input-number__increase {
      border-bottom-color: #e4e7ed;
    }
  }
}
</style>
