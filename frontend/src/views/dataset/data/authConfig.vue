<template>
  <de-container style="height: auto">
    <de-aside-container style="height: auto">
      <el-button v-show="!showTargetSearchInput" class="de-icon" icon="el-icon-search" circle size="mini" @click="showTargetSearchWidget" />
      <div v-show="showTargetSearchInput" class="de-input">
        <el-input v-model="targetFilterText" class="main-area-input">
          <el-button slot="append" icon="el-icon-close" @click="closeTargetSearchWidget" />
        </el-input>
      </div>
      <el-tabs v-model="targetActiveName" :class="{'de-search-header': showTargetSearchInput}" @tab-click="handleClick">
        <el-tab-pane v-for="(targetInfo, index) in targetInfoArray" :key="index" :lazy="true" :label="targetInfo.tabName" :name="targetInfo.authType">
          <lazy-tree
            v-if="targetActiveName===targetInfo.authType"
            :active-name="targetActiveName"
            :filter-text="targetFilterText"
            :data-info="targetInfo"
            highlight-current
            @nodeClick="authNodeClick"
            @execute-axios="executeAxios"
          />
        </el-tab-pane>
      </el-tabs>
    </de-aside-container>
    <de-main-container class="de-main-container-auth">
      <el-button v-show="!showSourceSearchInput" class="de-icon" icon="el-icon-search" circle size="mini" @click="showSourceSearchWidget" />
      <div v-show="showSourceSearchInput" class="de-input">
        <el-input v-model="sourceFilterText" class="main-area-input">
          <el-button slot="append" icon="el-icon-close" @click="closeSourceSearchWidget" />
        </el-input>
      </div>
      <el-tabs v-model="sourceActiveName" :class="{'de-search-header': showSourceSearchInput}" @tab-click="handleClick">
        <el-tab-pane v-for="(sourceInfo, index) in sourceInfoTabs" :key="index" :lazy="true" :label="sourceInfo.tabName" :name="sourceInfo.authType">
          <lazy-tree
            v-if="authCondition"
            :active-name="sourceActiveName"
            :filter-text="sourceFilterText"
            :data-info="sourceInfo"
            show-extent
            :auth-condition="authCondition"
            @execute-axios="executeAxios"
            :attach-active-name="targetActiveName"
          />
        </el-tab-pane>
      </el-tabs>
    </de-main-container>
  </de-container>

</template>

<script>
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import DeMainContainer from '@/components/dataease/DeMainContainer'
import LazyTree from './components/LazyTree'

export default {
  name: 'Authority',
  components: { LazyTree, DeMainContainer, DeAsideContainer, DeContainer },
  props: {
    resourceId: {
      type: String,
      default: null
    }
  },
  data () {
    return {
      targetInfoArray:
        [
          {
            tabName: this.$t('auth.dept'),
            head: this.$t('auth.deptHead'),
            direction: 'target',
            authType: 'dept'
          },
          {
            tabName: this.$t('auth.role'),
            head: this.$t('auth.roleHead'),
            direction: 'target',
            authType: 'role'
          },
          {
            tabName: this.$t('auth.user'),
            head: this.$t('auth.userHead'),
            direction: 'target',
            authType: 'user'
          }],
      sourceInfoArray:
        [
          {
            tabName: this.$t('auth.linkAuth'),
            head: this.$t('auth.linkAuthHead'),
            direction: 'source',
            authType: 'link',
            authTargets: 'dept,role,user'
          },
          {
            tabName: this.$t('auth.datasetAuth'),
            head: this.$t('auth.datasetAuthHead'),
            direction: 'source',
            authType: 'dataset',
            authTargets: 'dept,role,user'
          },
          {
            tabName: this.$t('auth.chartAuth'),
            head: this.$t('auth.chartAuthHead'),
            direction: 'source',
            authType: 'chart',
            authTargets: 'dept,role,user'
          },
          {
            tabName: this.$t('auth.panelAuth'),
            head: this.$t('auth.panelAuthHead'),
            direction: 'source',
            authType: 'panel',
            authTargets: 'dept,role,user'
          },
          {
            tabName: this.$t('auth.menuAuth'),
            head: this.$t('auth.menuAuthHead'),
            direction: 'source',
            authType: 'menu',
            authTargets: 'dept,role,user'
          }
        ],
      targetActiveName: null,
      sourceActiveName: null,
      showSourceSearchInput: false,
      showTargetSearchInput: false,
      sourceFilterText: '',
      targetFilterText: '',
      timeMachine: null,
      authCondition: null
    }
  },
  computed: {
    sourceInfoTabs () {
      const tabs = []
      this.sourceInfoArray.forEach(item => {
        if (item.authTargets.indexOf(this.targetActiveName) > -1) {
          tabs.push(item)
        }
      })
      return tabs
    }
  },
  created () {
    this.targetActiveName = this.targetInfoArray[0].authType
    this.sourceActiveName = this.sourceInfoArray[0].authType
  },

  methods: {
    executeAxios (param) {
      this.$emit('execute-axios', param)
    },
    handleClick (tab, event) {
    },
    showSourceSearchWidget () {
      this.showSourceSearchInput = true
    },
    closeSourceSearchWidget () {
      this.sourceFilterText = ''
      this.showSourceSearchInput = false
    },
    showTargetSearchWidget () {
      this.showTargetSearchInput = true
    },
    closeTargetSearchWidget () {
      this.targetFilterText = ''
      this.showTargetSearchInput = false
    },
    save () {
      this.$refs[this.activeName].save()
      this.$emit('close-grant', 0)
    },
    cancel () {
      this.$refs[this.activeName].cancel()
      this.$emit('close-grant', 0)
    },
    authNodeClick (val) {
      this.authCondition = val
    },
    clickAuth (auth) {
    }
  }
}
</script>

<style lang="scss" scoped>
  .de-tab {
    border:1px solid #E6E6E6;
    min-height:200px !important;
    max-height:300px !important;
    overflow:auto;
  }
  .de-icon {
    position: absolute;
    right: 10px;
    top: 15px;
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
  .de-main-container-auth{
    border: 1px solid #E6E6E6;
    height: auto;
  }

  .blackTheme .de-main-container-auth {
      border-color: #495865;
  }

  // ::-webkit-scrollbar {

  // }
</style>
