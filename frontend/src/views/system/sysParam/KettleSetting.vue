<template>
  <div class="kettle-setting">
    <deBtn
      type="primary"
      icon="el-icon-plus"
      @click="create(undefined)"
    >{{
      $t("kettle.add")
    }}</deBtn>
    <div class="table-box">
      <grid-table
        :table-data="data"
        :pagination="paginationConfig"
        @size-change="sizeChange"
        @current-change="currentChange"
      >
        <el-table-column
          prop="configuration.carte"
          :label="$t('kettle.carte')"
        />
        <el-table-column
          prop="configuration.port"
          :label="$t('kettle.port')"
        />
        <el-table-column
          prop="status"
          :label="$t('kettle.status')"
        >
          <template slot-scope="scope">
            <span
              v-if="scope.row.status === 'Error'"
              class="de-tag"
              style="color: #646a73; background: rgba(31, 35, 41, 0.1)"
            >{{ $t("datasource.invalid") }}</span>
            <span
              v-if="scope.row.status === 'Success'"
              class="de-tag"
              style="color: green; background: rgba(52, 199, 36, 0.2)"
            >{{ $t("datasource.valid") }}</span>
          </template>
        </el-table-column>
        <el-table-column
          slot="__operation"
          :label="$t('commons.operating')"
          fixed="right"
          width="168"
        >
          <template slot-scope="scope">
            <el-button
              class="de-text-btn"
              type="text"
              @click="create(scope.row)"
            >{{ $t("commons.edit") }}</el-button>
            <el-button
              class="de-text-btn"
              type="text"
              @click="validateById(scope.row)"
            >{{ $t("commons.validate") }}</el-button>
            <el-button
              class="de-text-btn"
              type="text"
              @click="del(scope.row)"
            >{{
              $t("commons.delete")
            }}</el-button>
          </template>
        </el-table-column>
      </grid-table>
    </div>
    <el-dialog
      v-dialogDrag
      :title="edit_dialog_title"
      :visible="show_dialog"
      :before-close="closeDialog"
      :show-close="true"
      width="50%"
      class="dialog-css de-kettle"
      append-to-body
    >
      <el-col>
        <el-form
          ref="kettleform"
          class="de-form-item"
          :form="form"
          :model="form"
          label-width="120px"
          :rules="rule"
        >
          <el-form-item
            :label="$t('kettle.carte')"
            prop="configuration.carte"
          >
            <el-input
              v-model="form.configuration.carte"
              :placeholder="$t('components.enter_kettle_address')"
            />
          </el-form-item>
          <el-form-item
            :label="$t('kettle.port')"
            prop="configuration.port"
          >
            <el-input-number
              v-model="form.configuration.port"
              controls-position="right"
              :placeholder="$t('components.enter_the_port')"
            />
          </el-form-item>
          <el-form-item
            :label="$t('kettle.user')"
            prop="configuration.user"
          >
            <el-input
              v-model="form.configuration.user"
              :placeholder="$t('components.one_user_name')"
            />
          </el-form-item>
          <el-form-item
            :label="$t('kettle.passwd')"
            prop="configuration.passwd"
          >
            <dePwd
              v-model="form.configuration.passwd"
              :placeholder="$t('components.input_a_password')"
            />
          </el-form-item>
        </el-form>
      </el-col>
      <div
        slot="footer"
        class="dialog-footer"
      >
        <deBtn
          secondary
          @click="closeDialog"
        >
          {{ $t("commons.cancel") }}
        </deBtn>
        <deBtn
          secondary
          @click="validate()"
        >{{
          $t("commons.validate")
        }}</deBtn>
        <deBtn
          type="primary"
          @click="save()"
        >{{
          $t("commons.confirm")
        }}</deBtn>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  deleteKettle,
  validate,
  save,
  pageList,
  validateById
} from '@/api/system/kettle'
import GridTable from '@/components/gridTable/index.vue'
import dePwd from '@/components/deCustomCm/DePwd.vue'
import msgCfm from '@/components/msgCfm'

export default {
  name: 'KettleSetting',
  components: { GridTable, dePwd },
  mixins: [msgCfm],
  data() {
    return {
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      data: [],
      show_dialog: false,
      edit_dialog_title: '',
      form: {
        configuration: {
          carte: '',
          port: '',
          user: '',
          passwd: ''
        }
      },
      rule: {
        'configuration.carte': [
          {
            required: true,
            message: this.$t('components.address_is_required'),
            trigger: 'blur'
          }
        ],
        'configuration.port': [
          {
            required: true,
            message: this.$t('components.port_is_required'),
            trigger: 'blur'
          }
        ],
        'configuration.user': [
          {
            required: true,
            message: this.$t('components.name_is_required'),
            trigger: 'blur'
          }
        ],
        'configuration.passwd': [
          {
            required: true,
            message: this.$t('components.password_is_required'),
            trigger: 'blur'
          }
        ]
      }
    }
  },

  created() {
    this.search()
  },
  methods: {
    initSearch() {
      this.currentChange(1)
    },
    currentChange(currentPage) {
      this.paginationConfig.currentPage = currentPage
      this.search()
    },
    sizeChange(pageSize) {
      this.paginationConfig.pageSize = pageSize
      this.paginationConfig.currentPage = 1
      this.search()
    },
    search() {
      const { currentPage, pageSize } = this.paginationConfig
      pageList('/kettle/pageList/' + currentPage + '/' + pageSize, {}).then(
        (response) => {
          this.data = response.data.listObject
          this.data.forEach((item) => {
            item.configuration = JSON.parse(item.configuration)
          })
          this.paginationConfig.total = response.data.itemCount
        }
      )
    },
    del(item) {
      deleteKettle(item.id).then((response) => {
        this.initSearch()
      })
    },
    create(item) {
      if (!item) {
        this.targetObjs = []
        this.form = {
          configuration: { carte: '', port: '', user: '', passwd: '' }
        }
        this.edit_dialog_title = this.$t('kettle.add')
      } else {
        this.edit_dialog_title = this.$t('commons.edit')
        this.form = JSON.parse(JSON.stringify(item))
      }
      this.show_dialog = true
    },

    save() {
      this.$refs.kettleform.validate((valid) => {
        if (!valid) {
          return false
        }
        const form = JSON.parse(JSON.stringify(this.form))
        form.configuration = JSON.stringify(form.configuration)
        save(form).then((res) => {
          this.show_dialog = false
          this.openMessageSuccess('commons.save_success')
          this.initSearch()
        })
      })
    },

    closeDialog() {
      this.$refs.kettleform.resetFields()
      this.show_dialog = false
      this.form = {
        configuration: { carte: '', port: '', user: '', passwd: '' }
      }
    },

    validate() {
      this.$refs.kettleform.validate((valid) => {
        if (valid) {
          validate(this.form.configuration)
            .then((res) => {
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
            .catch((res) => {
              this.$error(res.message)
            })
        } else {
          return
        }
      })
    },

    validateById(item) {
      validateById(item.id)
        .then((res) => {
          if (res.success) {
            item.status = res.data.status
            this.openMessageSuccess('datasource.validate_success')
          } else {
            item.status = 'Error'
            if (res.message.length < 2500) {
              this.$error(res.message)
            } else {
              this.$error(res.message.substring(0, 2500) + '......')
            }
          }
        })
        .catch((res) => {
          this.$error(res.message)
        })
    }
  }
}
</script>
<style lang="scss" scoped>
.kettle-setting {
  height: 100%;
  .table-box {
    height: calc(100% - 52px);
    margin-top: 16px;
  }
}
</style>
<style lang="scss">
.de-kettle {
  .el-input-number {
    width: 100%;
    .el-input__inner {
      text-align: left;
    }

    .el-input-number__decrease,
    .el-input-number__increase {
      background: transparent;
    }
  }
}
.de-tag {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  border-radius: 2px;
  padding: 1px 6px;
  height: 24px;
}
</style>
