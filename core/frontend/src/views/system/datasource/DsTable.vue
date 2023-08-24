<template>
  <div class="ds-table-detail">
    <el-tabs
      v-model="activeName"
      class="de-tabs"
    >
      <el-tab-pane
        :label="$t('datasource.data_source_configuration')"
        name="detail"
      />
      <el-tab-pane
        :label="$t('datasource.data_source_table')"
        name="table"
      />
    </el-tabs>
    <div class="el-tabs-content">
      <ds-table-tabs
        v-if="activeName === 'table'"
        :params="params"
      />
      <template v-else>
        <div class="ds-top">
          <deBtn
            v-if="privileges && canEdit"
            secondary
            key="cancel"
            @click="editDatasource(false)"
          >{{ $t('commons.cancel') }}
          </deBtn>
          <deBtn
            v-if="privileges && !canEdit"
            key="edit"
            secondary
            @click="editDatasource(true)"
          >{{ $t('commons.edit') }}
          </deBtn>
          <el-tooltip
            class="item"
            effect="dark"
            :content="$t('commons.no_target_permission')"
            placement="top"
          >
            <span v-if="!privileges">
              <deBtn
                disabled
                secondary
              >{{ $t('commons.edit') }}
              </deBtn>
            </span>
          </el-tooltip>
          <deBtn
            v-if="privileges"
            style="margin-left: 12px;"
            secondary
            @click="validaDatasource"
          >{{ $t('commons.validate') }}
          </deBtn>
          <deBtn
            v-if="privileges && canEdit"
            type="primary"
            @click="save"
          >{{ $t('commons.save') }}
          </deBtn>
        </div>
        <div style="height: calc(100% - 36px)">
          <ds-form-content
            @editeTodisable="editDatasource"
            :canEdit="canEdit"
            ref="DsFormContent"
            @refresh-type="refreshType"
            :config-from-tabs="configFromTabs"
          />
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import DsTableTabs from './DsTableTabs'
import DsFormContent from './DsFormContent'
export default {
  components: { DsTableTabs, DsFormContent },
  props: {
    params: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      activeName: 'detail',
      canEdit: false,
    }
  },
  computed: {
    configFromTabs() {
      return { ...this.params, showModel: 'show' }
    },
    privileges() {
      return this.hasDataPermission('manage', this.params.privileges)
    }
  },
  methods: {
    refreshType(form) {
      this.$emit('refresh-type', form)
    },
    editDatasource(type = false) {
      this.$refs.DsFormContent.editDatasource(type)
      this.canEdit = type
    },
    validaDatasource() {
      this.$refs.DsFormContent.validaDatasource()
    },
    save() {
      this.$refs.DsFormContent.save()
    }
  }
}
</script>

<style lang="less">
.ds-table-detail {
  height: 100%;
  padding: 10px 14px;
  box-sizing: border-box;
  .el-tabs-content {
    height: calc(100% - 56px);
  }

  .ds-top {
    height: 36px;
    text-align: right;
  }

  .w600 {
    padding-top: 0 !important;
  }

  .de-ds-cont {
    padding: 0 !important;
    background: transparent !important;
  }
}
</style>
