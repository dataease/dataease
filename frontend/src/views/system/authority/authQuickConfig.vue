<template>
  <de-container>
    <span>敬请期待</span>
    <!--    <de-aside-container>-->
    <!--      <el-button v-show="!showSourceSearchInput" class="de-icon" icon="el-icon-search" circle size="mini" @click="showSourceSearchWidget" />-->
    <!--      <div v-show="showSourceSearchInput" class="de-input">-->
    <!--        <el-input v-model="sourceFilterText">-->
    <!--          <el-button slot="append" icon="el-icon-close" @click="closeSourceSearchWidget" />-->
    <!--        </el-input>-->
    <!--      </div>-->
    <!--      <el-tabs v-model="sourceActiveName" :class="{'de-search-header': showSourceSearchInput}" @tab-click="handleClick">-->
    <!--        <el-tab-pane v-for="(sourceInfo, index) in sourceInfoArray" :key="index" :lazy="true" :label="sourceInfo.tabName" :name="sourceInfo.authType">-->
    <!--          <lazy-tree-->
    <!--            v-if="authCondition"-->
    <!--            :active-name="sourceActiveName"-->
    <!--            :filter-text="sourceFilterText"-->
    <!--            :data-info="sourceInfo"-->
    <!--            highlight-current-->
    <!--            @nodeClick="authNodeClick"-->
    <!--          />-->
    <!--        </el-tab-pane>-->
    <!--      </el-tabs>-->
    <!--    </de-aside-container>-->
    <!--    <de-main-container class="de-main-container-auth">-->
    <!--      <el-button v-show="!showTargetSearchInput" class="de-icon" icon="el-icon-search" circle size="mini" @click="showTargetSearchWidget" />-->
    <!--      <div v-show="showTargetSearchInput" class="de-input">-->
    <!--        <el-input v-model="targetFilterText">-->
    <!--          <el-button slot="append" icon="el-icon-close" @click="closeTargetSearchWidget" />-->
    <!--        </el-input>-->
    <!--      </div>-->
    <!--      <el-tabs v-model="targetActiveName" :class="{'de-search-header': showTargetSearchInput}" @tab-click="handleClick">-->
    <!--        <el-tab-pane v-for="(targetInfo, index) in targetInfoArray" :key="index" :lazy="true" :label="targetInfo.tabName" :name="targetInfo.authType">-->
    <!--          <lazy-tree-->
    <!--            :active-name="targetActiveName"-->
    <!--            :filter-text="targetFilterText"-->
    <!--            :data-info="targetInfo"-->
    <!--            show-extent-->
    <!--            :auth-condition="authCondition"-->
    <!--          />-->
    <!--        </el-tab-pane>-->
    <!--      </el-tabs>-->
    <!--    </de-main-container>-->
  </de-container>

</template>

<script>
import DeContainer from '../../../components/dataease/DeContainer'
import DeAsideContainer from '../../../components/dataease/DeAsideContainer'
import DeMainContainer from '../../../components/dataease/DeMainContainer'
import LazyTree from './components/LazyTree'

export default {
  name: 'Authority',
  // eslint-disable-next-line vue/no-unused-components
  components: { LazyTree, DeMainContainer, DeAsideContainer, DeContainer },
  props: {
    resourceId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      targetInfoArray:
        [
          {
            tabName: '部门权限',
            head: '所有部门',
            direction: 'target',
            authType: 'dept'
          },
          {
            tabName: '角色权限',
            head: '所有角色',
            direction: 'target',
            authType: 'role'
          },
          {
            tabName: '用户权限',
            head: '所有用户',
            direction: 'target',
            authType: 'user'
          }],
      sourceInfoArray:
        [
          {
            tabName: '数据源',
            head: '所有数据源',
            direction: 'source',
            authType: 'link'
          },
          {
            tabName: '数据集',
            head: '所有数据集',
            direction: 'source',
            authType: 'dataset'
          },
          {
            tabName: '视图',
            head: '所有视图',
            direction: 'source',
            authType: 'chart'
          },
          {
            tabName: '仪表板',
            head: '所有仪表板',
            direction: 'source',
            authType: 'panel'
          }],
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
  created() {
    this.targetActiveName = this.targetInfoArray[0].authType
  },

  methods: {
    handleClick(tab, event) {
    },
    showSourceSearchWidget() {
      this.showSourceSearchInput = true
    },
    closeSourceSearchWidget() {
      this.sourceFilterText = ''
      this.showSourceSearchInput = false
    },
    showTargetSearchWidget() {
      this.showTargetSearchInput = true
    },
    closeTargetSearchWidget() {
      this.targetFilterText = ''
      this.showTargetSearchInput = false
    },
    save() {
      this.$refs[this.activeName].save()
      this.$emit('close-grant', 0)
    },
    cancel() {
      this.$refs[this.activeName].cancel()
      this.$emit('close-grant', 0)
    },
    authNodeClick(val) {
      this.authCondition = val
    },
    clickAuth(auth) {
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
  }

  // ::-webkit-scrollbar {

  // }
</style>
