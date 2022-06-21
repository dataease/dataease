<template>
  <el-col>
    <el-row style="margin-top: 10px;">
      <el-button icon="el-icon-circle-plus-outline" @click="create(undefined)">{{ $t('kettle.add') }}</el-button>
      <fu-table :data="data">
        <el-table-column prop="configuration.carte" :label="$t('kettle.carte')"/>
        <el-table-column prop="configuration.port" :label="$t('kettle.port')"/>
        <el-table-column prop="status" :label="$t('kettle.status')">
          <template slot-scope="scope">
            <span v-if="scope.row.status === 'Error'"   style="color: red">{{ $t('datasource.invalid') }}</span>
            <span v-if="scope.row.status === 'Success'" style="color: green">{{ $t('datasource.valid') }}</span>
          </template>
        </el-table-column>

        <fu-table-operations :buttons="buttons" :label="$t('commons.operating')" fix/>
      </fu-table>
      <div class="pagination">
        <fu-table-pagination :current-page.sync="paginationConfig.currentPage"
                             :page-size.sync="paginationConfig.pageSize"
                             :total="paginationConfig.total"
                             @size-change="sizeChange"
                             @current-change="currentChange"/>
      </div>
    </el-row>


    <el-dialog v-dialogDrag :title="edit_dialog_title" :visible="show_dialog" :before-close="closeDialog"
               :show-close="true" width="50%" class="dialog-css" append-to-body>
      <el-col>
        <el-form ref="kettleform" :form="form" :model="form" label-width="120px" :rules="rule">
          <el-form-item :label="$t('kettle.carte')" prop="configuration.carte">
            <el-input v-model="form.configuration.carte"/>
          </el-form-item>
          <el-form-item :label="$t('kettle.port')" prop="configuration.port">
            <el-input v-model="form.configuration.port" autocomplete="off" type="number" min="0"/>
          </el-form-item>
          <el-form-item :label="$t('kettle.user')" prop="configuration.user">
            <el-input v-model="form.configuration.user"/>
          </el-form-item>
          <el-form-item :label="$t('kettle.passwd')" prop="configuration.passwd">
            <el-input v-model="form.configuration.passwd" show-password/>
          </el-form-item>

        </el-form>
      </el-col>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="validate()">{{ $t('commons.validate') }}</el-button>
        <el-button type="primary" size="mini" @click="save()">{{ $t('commons.save') }}</el-button>
      </div>
    </el-dialog>


  </el-col>
</template>

<script>

import {deleteKettle, validate, save, pageList, validateById} from '@/api/system/kettle'
import i18n from "@/lang";

export default {
  name: 'KettleSetting',
  data() {
    return {
      columns: [],
      buttons: [
        {
          label: this.$t('commons.edit'),
          icon: 'el-icon-edit',
          type: 'primary',
          click: this.create
        },
        {
          label: this.$t('commons.validate'),
          icon: 'el-icon-success',
          type: 'success',
          click: this.validateById
        },
        {
          label: this.$t('commons.delete'),
          icon: 'el-icon-delete',
          type: 'danger',
          click: this.delete,
        }
      ],
      last_condition: null,
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
        'configuration.carte': [{
          required: true,
          message: this.$t('commons.required'),
          trigger: 'blur'
        }],
        'configuration.port': [{
          required: true,
          message: this.$t('commons.required'),
          trigger: 'blur'
        }],
        'configuration.user': [{
          required: true,
          message: this.$t('commons.required'),
          trigger: 'blur'
        }],
        'configuration.passwd': [{
          required: true,
          message: this.$t('commons.required'),
          trigger: 'blur'
        }]
      }
    }
  },

  created() {
    this.search()
  },
  methods: {

    currentChange() {
      this.search()
    },
    sizeChange() {
      this.currentPage = 1;
      this.search()
    },
    search() {
      const {currentPage, pageSize} = this.paginationConfig
      pageList('/kettle/pageList/' + currentPage + '/' + pageSize, {}).then(response => {
        this.data = response.data.listObject
        this.data.forEach(item => {
          item.configuration = JSON.parse(item.configuration)
        })
        this.paginationConfig.total = response.data.itemCount
      })
    },
    delete(item) {
      deleteKettle(item.id).then(response => {
        this.search()
      })
    },
    create(item) {
      if (!item) {
        this.targetObjs = []
        this.form = {configuration: {carte: '', port: '', user: '', passwd: ''}}
        this.edit_dialog_title = this.$t('kettle.add')
      } else {
        this.edit_dialog_title = this.$t('commons.edit')
        this.form = JSON.parse(JSON.stringify(item))
      }
      this.show_dialog = true
    },

    save() {
      this.$refs.kettleform.validate(valid => {
        if (!valid) {
          return false
        }
        const form = JSON.parse(JSON.stringify(this.form))
        form.configuration = JSON.stringify(form.configuration)
        save(form).then(res => {
          this.show_dialog = false
          this.$success(i18n.t('commons.save_success'))
          this.search()
        })
      })
    },

    closeDialog() {
      this.show_dialog = false
      this.form = {configuration: {carte: '', port: '', user: '', passwd: ''}}
    },

    validate() {
      this.$refs.kettleform.validate(valid => {
        if (valid) {
          validate(this.form.configuration).then(res => {
            if (res.success) {
              this.$success(i18n.t('datasource.validate_success'))
            } else {
              if (res.message.length < 2500) {
                this.$error(res.message)
              } else {
                this.$error(res.message.substring(0, 2500) + '......')
              }
            }
          }).catch(res => {
            this.$error(res.message)
          })
        } else {
          return
        }
      })
    },

    validateById(item) {
      validateById(item.id).then(res => {
        if (res.success) {
          item.status = res.data.status
          this.$success(i18n.t('datasource.validate_success'))
        } else {
          item.status = 'Error'
          if (res.message.length < 2500) {
            this.$error(res.message)
          } else {
            this.$error(res.message.substring(0, 2500) + '......')
          }
        }
      }).catch(res => {
        this.$error(res.message)
      })
    },
  }
}
</script>

<style scoped>

</style>
