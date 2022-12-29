<template>
  <div
    v-loading="positionCheck('datasource')?formLoading:false"
    :class="positionCheck('datasource')?'de-ds-form':'de-ds-form-app'"
  >
    <div
      v-if="positionCheck('datasource')"
      class="de-ds-top"
    >
      <span class="name">
        <i
          class="el-icon-arrow-left"
          @click="logOutTips"
        />
        {{ tips }}
      </span>
      <div class="apply">
        <template v-if="canEdit">
          <deBtn
            secondary
            @click="logOutTips"
          >{{ $t('commons.cancel') }}
          </deBtn>
          <deBtn
            v-if="addOrHasDataPermission"
            secondary
            @click="validaDatasource"
          >{{ $t('commons.validate') }}
          </deBtn>
          <deBtn
            v-if="addOrHasDataPermission"
            type="primary"
            @click="save"
          >{{ $t('commons.save') }}
          </deBtn>
        </template>
        <template v-else>
          <deBtn
            v-if="addOrHasDataPermission"
            @click="validaDatasource"
          >{{ $t('commons.validate') }}
          </deBtn>
        </template>
      </div>
    </div>
    <div style="height: calc(100% - 56px)">
      <DsFormContent
        ref="DsFormContent"
        @editeTodisable="backToList"
        :outer-params="outerParams"
        :reference-position="referencePosition"
        :form-type.sync="formType"
        :type-map.sync="typeMap"
        :can-edit.sync="canEdit"
        :form-loading.sync="formLoading"
        @refreshType="refreshType"
        @closeDraw="closeDraw"
        @setParams="setParams"
      />
    </div>
  </div>
</template>

<script>
import DsFormContent from './DsFormContent.vue'
import msgCfm from '@/components/msgCfm'

export default {
  name: 'DsForm',
  components: {
    DsFormContent
  },
  mixins: [msgCfm],
  props: {
    referencePosition: {
      type: String,
      default: 'datasource'
    },
    outerParams: {
      type: Object,
      request: false
    }
  },
  data() {
    return {
      typeMap: '',
      canEdit: false,
      formLoading: false,
      params: {},
      formType: 'add'
    }
  },
  computed: {
    addOrHasDataPermission() {
      return this.formType === 'add' || this.hasDataPermission('manage', this.params.privileges)
    },
    tips() {
      const { id, showModel } = this.params
      if(showModel === 'copy'){
        return this.$t('datasource.copy')
      }
      return id && showModel === 'show' && !this.canEdit
        ? this.$t('datasource.show_info')
        : this.formType === 'add'
          ? `${this.$t('commons.create') + this.typeMap}${this.$t('commons.datasource')}`
          : this.$t('datasource.modify')
    }
  },
  methods: {
    closeDraw() {
      this.$emit('closeDraw')
    },
    refreshType(form) {
      this.$emit('refresh-type', form)
    },
    setParams(params) {
      this.params = params
    },
    positionCheck(referencePosition) {
      return this.referencePosition === referencePosition
    },
    validaDatasource() {
      this.$refs.DsFormContent.validaDatasource()
    },
    save() {
      this.$refs.DsFormContent.save()
    },
    backToList() {
      this.$router.push('/datasource/index')
    },
    logOutTips() {
      const options = {
        title: 'role.tips',
        confirmButtonText: this.$t('commons.confirm'),
        content: 'system_parameter_setting.sure_to_exit',
        type: 'primary',
        cb: () => {
          this.backToList()
        }
      }
      this.handlerConfirm(options)
    }
  }
}
</script>
<style lang="scss" scoped>
.de-ds-form-app {
  width: 100%;
  height: 100%;

  .de-ds-cont {
    display: flex;
    width: 100%;
    height: 100%;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 12px 24px 24px 24px;

    .de-ds-inner {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .w600 {
      width: 600px;
      height: 100%;
    }
  }

  .de-ds-bottom {
    display: flex;
    text-align: right;
    align-items: center;
    justify-content: space-between;
    height: 56px;
    padding: 12px 24px;
    box-shadow: 2px 2px 4px rgba(31, 35, 41, 0.08);

    .name {
      font-family: 'PingFang SC';
      font-style: normal;
      font-weight: 500;
      font-size: 16px;
      line-height: 24px;
      color: var(--deTextPrimary, #1f2329);
    }

    i {
      cursor: pointer;
    }
  }
}

.de-ds-form {
  width: 100vw;
  height: 100vh;

  .de-ds-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 56px;
    padding: 12px 24px;
    box-shadow: 0px 2px 4px rgba(31, 35, 41, 0.08);

    .name {
      font-family: 'PingFang SC';
      font-style: normal;
      font-weight: 500;
      font-size: 16px;
      line-height: 24px;
      color: var(--deTextPrimary, #1f2329);
    }

    i {
      cursor: pointer;
    }
  }
}
</style>
