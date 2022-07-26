<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col v-loading="loading" class="tree-main">
    <el-row v-if="showExtent" class="tree-head">
      <span style="float: left;padding-left: 10px">{{ dataInfo.head }}</span>
      <span v-for="auth in defaultAuthDetails" :key="auth.privilegeName" class="auth-span">
        {{ auth.privilegeName }}
      </span>
    </el-row>
    <el-row style="margin-top: 5px">
      <el-tree
        :props="defaultProps"
        :load="loadNodes"
        :data="treeData"
        :node-key="defaultProps.id"
        :highlight-current="highlightCurrent"
        :default-expanded-keys="expandedKey"
        lazy
        @node-click="nodeClick"
      >
        <span slot-scope="{ node, data }" class="custom-tree-node">
          <span>
            <span style="margin-left: 6px" v-html="data.name" />
          </span>
          <span v-if="showExtent" @click.stop>
            <div v-if="authDetails[data.id]">
              <span v-for="auth in authDetails[data.id]" :key="auth.privilegeType" class="auth-span">
                <!-- 1-{{ auth.privilegeType }}-{{ auth.privilegeValue }}-->
                <a href="javascript:;" @click="clickAuth(data.id,auth)">
                  <svg-icon style="width: 22px;height: 22px" :icon-class="auth.privilegeValue===1?'lock_open':'lock_closed'" />
                </a>
              </span>
            </div>
            <div v-else>
              <span v-for="auth in defaultAuthDetails" :key="auth.privilegeType" class="auth-span">
                <!--2-{{ auth.privilegeType }}-{{ auth.privilegeValue }}-->
                <a href="javascript:;" @click="clickAuth(data.id,auth)">
                  <svg-icon style="width: 22px;height: 22px" :icon-class="auth.privilegeValue===1?'lock_open':'lock_closed'" />
                </a>
              </span>
            </div></span>
        </span>
      </el-tree>
    </el-row>
  </el-col>
</template>

<script>
import { authChange, authDetails, authDetailsModel, authModel } from '@/api/system/sysAuth'
export default {
  name: 'LazyTree',
  components: { },
  props: {
    filterText: {
      type: String,
      required: false,
      default: ''
    },
    authCondition: {
      type: Object,
      required: false,
      default: null
    },
    dataInfo: {
      type: Object,
      required: true
    },
    activeName: {
      type: String,
      required: true
    },
    attachActiveName: {
      type: String,
      default: null
    },
    defaultProps: {
      type: Object,
      required: false,
      default: function() {
        return {
          children: 'children',
          label: 'name',
          id: 'id',
          parentId: 'pid',
          isLeaf: 'leaf'
        }
      }
    },
    showExtent: Boolean,
    highlightCurrent: Boolean
  },
  data() {
    return {
      loading: false,
      treeData: [],
      changeIndex: 0,
      timeMachine: null,
      expandedKey: [], // 展开节点 搜索时默认展开父级节点
      defaultCondition: { // pid 是0的时候 查询的是顶级的节点
        pid: '0'
      },
      authDetails: {},
      defaultAuthDetails: [],
      searchStatus: false, // 当前是否在搜索状态 （搜索状态 展开不加载子节点）
      // 当前已经加载的节点ID 备用（当前把当前authTarget的所有授权加载进来）
      loadedNodeIds: new Set()
    }
  },
  computed: {
  },
  watch: {
    filterText(val) {
      this.expandedKey = []
      if (val && val.length > 0) {
        this.searchStatus = true
      }
      // 当组件名和 activeName 相等时 才进行查询
      if (this.dataInfo.authType === this.activeName) {
        this.destroyTimeMachine()
        this.changeIndex++
        this.filterNode(this.changeIndex)
      }
    },
    authCondition: {
      handler(newVal, oldVla) {
        this.loadAuth()
      },
      deep: true
    },
    attachActiveName: {
      handler(newVal, oldVla) {
        this.authDetails = {}
      },
      deep: true
    }
  },
  created() {
    // 初始化授权模板
    if (this.showExtent) {
      authDetailsModel(this.dataInfo.authType).then(res => {
        this.defaultAuthDetails = res.data
      })
      this.loadAuth()
    }
  },
  methods: {
    loadAuth() {
      if (this.authCondition && this.showExtent) {
        let authQueryCondition = {}
        if (this.dataInfo.direction === 'source') {
          // 当前为授权数据 获取当前authTarget 的授权信息 authSource
          authQueryCondition = {
            authTarget: this.authCondition.id,
            authTargetType: this.authCondition.type,
            authSourceType: this.dataInfo.authType
          }
        } else {
          authQueryCondition = {
            authSource: this.authCondition.id,
            authSourceType: this.authCondition.type
          }
        }
        authDetails(authQueryCondition).then(res => {
          this.authDetails = res.data
        })
      }
    },
    loadNodes(node, resolve) {
      if (!this.searchStatus) {
        if (node.level === 0) {
          const queryCondition = {
            modelType: this.dataInfo.authType,
            ...
            this.defaultCondition
          }
          authModel(queryCondition).then(res => {
            const data = res.data
            resolve(data)
          })
        } else {
          const queryCondition = {
            modelType: this.dataInfo.authType
          }
          queryCondition[this.defaultProps.parentId] = node.data[this.defaultProps.id]
          authModel(queryCondition).then(res => {
            const data = res.data
            resolve(data)
          })
        }
      } else {
        resolve(node.data.children)
      }
    },
    filterNode(index) {
      this.timeMachine = setTimeout(() => {
        if (index === this.changeIndex) {
          const queryCondition = {
            withExtend: 'parent',
            modelType: this.dataInfo.authType
          }
          queryCondition[this.defaultProps.label] = this.filterText
          authModel(queryCondition).then(res => {
            // 高亮显示
            this.highlights(res.data)
            this.treeData = this.buildTree(res.data)
            // 恢复searchStatus 状态 可以允许继续展开父级
            this.$nextTick(() => (this.searchStatus = false))
          })
        }
        this.destroyTimeMachine()
      }, 1500)
    },
    nodeClick(data, node) {
      this.$emit('nodeClick', { id: data.id, type: this.dataInfo.authType })
    },
    destroyTimeMachine() {
      this.timeMachine && clearTimeout(this.timeMachine)
      this.timeMachine = null
    },
    buildTree(arrs) {
      const idMapping = arrs.reduce((acc, el, i) => {
        acc[el[this.defaultProps.id]] = i
        return acc
      }, {})
      const roots = []
      arrs.forEach(el => {
        // 判断根节点 ###
        if (el[this.defaultProps.parentId] === null || el[this.defaultProps.parentId] === 0 || el[this.defaultProps.parentId] === '0') {
          roots.push(el)
          return
        }
        // 用映射表找到父元素
        const parentEl = arrs[idMapping[el[this.defaultProps.parentId]]]
        // 把当前元素添加到父元素的`children`数组中
        parentEl.children = [...(parentEl.children || []), el]

        // 设置展开节点 如果没有子节点则不进行展开
        if (parentEl.children.length > 0) {
          this.expandedKey.push(parentEl[this.defaultProps.id])
        }
      })
      return roots
    },
    // 权限修改
    clickAuth(dataId, auth) {
      let authChangeCondition = {}
      if (this.dataInfo.direction === 'source') { // 当前为授权数据
        authChangeCondition = {
          authSource: dataId,
          authSourceType: this.dataInfo.authType,
          authTarget: this.authCondition.id,
          authTargetType: this.authCondition.type,
          authDetail: auth

        }
      } else {
        authChangeCondition = {
          authTarget: dataId,
          authTargetType: this.dataInfo.authType,
          authSource: this.authCondition.id,
          authSourceType: this.authCondition.type,
          authDetail: auth
        }
      }
      this.loading = true
      authChange(authChangeCondition).then(res => {
        // 重新加载权限
        this.loadAuth()
        this.loading = false
      }).catch((e) => {
        // this.$warning(e)
        this.loading = false
      })
    },
    // 高亮显示搜索内容
    highlights(data) {
      if (data && this.filterText && this.filterText.length > 0) {
        const replaceReg = new RegExp(this.filterText, 'g')// 匹配关键字正则
        const replaceString = '<span style="color: #faaa39">' + this.filterText + '</span>' // 高亮替换v-html值
        data.forEach(item => {
          item.name = item.name.replace(replaceReg, replaceString) // 开始替换
        })
      }
    }

  }
}
</script>

<style scoped>
  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-left: 8px;
  }
  .tree-main{
    overflow-y: auto!important;
  }
  /* .tree-head{
    height: 30px;
    line-height: 30px;
    border-bottom: 1px solid #e6e6e6;
    background-color: #f7f8fa;
    font-size: 12px;
    color: #3d4d66 ;
  } */
  .tree-head{
    height: 30px;
    line-height: 30px;
    border-bottom: 1px solid var(--TableBorderColor, #e6e6e6);
    background-color: var(--SiderBG, #f7f8fa);
    font-size: 12px;
    color: var(--TableColor, #3d4d66) ;
  }

  .auth-span{
    float: right;
    width:50px;
    margin-right: 30px
  }
  .highlights-text {
    color: #faaa39 !important;
  }

</style>
