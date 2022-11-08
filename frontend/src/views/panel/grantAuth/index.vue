<template>
  <div>
    <el-button
      v-show="!showSearchInput"
      class="de-icon"
      icon="el-icon-search"
      circle
      size="mini"
      @click="showSearchWidget"
    />
    <div
      v-show="showSearchInput"
      class="de-input"
    >
      <el-input
        v-model="key"
        class="main-area-input"
      >
        <el-button
          slot="append"
          icon="el-icon-close"
          @click="closeSearchWidget"
        />
      </el-input>
    </div>

    <el-tabs
      v-model="activeName"
      :class="{'de-search-header': showSearchInput}"
      @tab-click="handleClick"
    >
      <el-tab-pane
        :lazy="true"
        class="de-tab"
        :label="$t('commons.organization')"
        :name="tabNames[0]"
      ><grant-dept
        :ref="tabNames[0]"
        :resource-id="resourceId"
        :key-word="key"
      /></el-tab-pane>
      <el-tab-pane
        :lazy="true"
        class="de-tab"
        :label="$t('commons.role')"
        :name="tabNames[1]"
      ><grant-role
        :ref="tabNames[1]"
        :resource-id="resourceId"
        :key-word="key"
      /></el-tab-pane>
      <el-tab-pane
        :lazy="true"
        class="de-tab"
        :label="$t('commons.user')"
        :name="tabNames[2]"
      ><grant-user
        :ref="tabNames[2]"
        :resource-id="resourceId"
        :key-word="key"
      /></el-tab-pane>
    </el-tabs>
    <div class="auth-root-class">
      <span slot="footer">
        <el-button
          size="mini"
          @click="cancel"
        >{{ $t('commons.cancel') }}</el-button>
        <el-button
          type="primary"
          size="mini"
          @click="save"
        >{{ $t('commons.confirm') }}</el-button>
      </span>
    </div>
  </div>

</template>

<script>
import GrantDept from './dept'
import GrantRole from './role'
import GrantUser from './user'
import { fineSave } from '@/api/panel/share'
export default {
  name: 'GrantAuth',
  components: { GrantDept, GrantRole, GrantUser },
  props: {
    resourceId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      tabNames: ['grantDept', 'grantRole', 'grantUser'],
      activeName: null,
      showSearchInput: false,
      key: ''
    }
  },
  created() {
    this.activeName = this.tabNames[0]
  },

  methods: {
    handleClick(tab, event) {
    },
    showSearchWidget() {
      this.showSearchInput = true
    },
    closeSearchWidget() {
      this.key = ''
      this.showSearchInput = false
    },
    save() {
      this.fineSave()
    },
    fineSave() {
      let targetDto = {}
      this.tabNames.forEach(tabName => {
        if (this.$refs[tabName] && this.$refs[tabName].getSelected) {
          const tempSelected = this.$refs[tabName].getSelected()
          targetDto = Object.assign({}, targetDto, tempSelected)
        }
      })
      const resourceId = this.resourceId
      const param = {
        resourceId,
        authURD: targetDto
      }
      fineSave(param).then(res => {
        this.$success(this.$t('commons.share_success'))
        this.$emit('close-grant', 0)
      })
    },
    cancel() {
      this.$refs[this.activeName].cancel()
      this.$emit('close-grant', 0)
    }
  }
}
</script>

<style lang="scss" scoped>
.de-tab {
    border:1px solid #E6E6E6;
    min-height:400px !important;
    max-height:400px !important;
    overflow:auto;
}
.de-icon {
    position: absolute;
    right: 20px;
    top: 70px;
    z-index: 99;
}
.el-input-group__append{
    background-color: #ffffff;
}
.el-input__inner{
    border-right: none;
}

.auth-root-class {
    margin: 15px 0px 5px;
    text-align: right;
}

// ::-webkit-scrollbar {

// }
</style>
