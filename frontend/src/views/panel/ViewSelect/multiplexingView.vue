<template>
  <el-col v-loading="loading" style="height: 100%; overflow-y: auto">
    <el-row style="margin-top: 5px">
      <el-row style="margin-left: 5px;margin-right: 5px">
        <el-col>
          <el-input
            v-model="templateFilterText"
            :placeholder="$t('panel.filter_keywords')"
            size="mini"
            clearable
            prefix-icon="el-icon-search"
          />
        </el-col>
      </el-row>
      <el-row style="margin-top: 5px">
        <el-tree
          ref="templateTree"
          :data="viewData"
          node-key="id"
          :props="defaultProps"
          :expand-on-click-node="true"
          :filter-node-method="filterNode"
          :highlight-current="true"
          @node-click="showDetails"
        >
          <span slot-scope="{ node, data }" class="custom-tree-node-list father">
            <span style="display: flex; flex: 1 1 0%; width: 0px;">
              <span v-if="data.modelInnerType==='history'">
                <i class="el-icon-collection" />
              </span>
              <span v-else-if="data.nodeType === 'spine'">
                <i class="el-icon-folder" />
              </span>
              <span v-else-if="data.modelType==='panel'&& data.nodeType === 'leaf'">
                <svg-icon icon-class="panel" class="ds-icon-scene" />
              </span>
              <span v-else>
                <svg-icon :icon-class="data.isPlugin && data.modelInnerType && data.modelInnerType !== 'buddle-map' ? ('/api/pluginCommon/staticInfo/' + data.modelInnerType + '/svg') : data.modelInnerType" style="width: 14px;height: 14px" />
              </span>
              <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;" :title="data.name">{{ data.name }}</span>
            </span>
            <span v-if="data.mode===1" class="child">
              <span @click.stop>
                <el-button
                  icon="el-icon-delete"
                  type="text"
                  size="small"
                  @click="deleteHistory(data, node)"
                />
              </span>
            </span>
          </span>
        </el-tree>
      </el-row>
    </el-row>
  </el-col>
</template>

<script>
import { mapState } from 'vuex'
import { deleteCircle } from '@/api/chart/chart'

export default {
  name: 'MultiplexingView',
  props: {
    selectModel: {
      type: Boolean,
      default: false
    },
    viewData: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      templateFilterText: '',
      defaultProps: {
        children: 'children',
        label: 'name',
        disabled: 'disabled'
      },
      data: [],
      detailItem: null,
      loading: false,
      plugins: null
    }
  },
  computed: {
    ...mapState([
      'canvasStyleData'
    ])
  },
  watch: {
    templateFilterText(val) {
      this.$refs.templateTree.filter(val)
    }
  },
  created() {
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true
      return data.name.indexOf(value) !== -1
    },
    deleteHistory(data, node) {
      deleteCircle(data.id).then(() => {
        this.$success(this.$t('commons.delete_success'))
        this.remove(node, data)
        // this.loadData()
      })
    },
    remove(node, data) {
      const parent = node.parent
      const children = parent.data.children || parent.data
      const index = children.findIndex(d => d.id === data.id)
      children.splice(index, 1)
    },
    showDetails(node, data) {
      if (data.data.nodeType === 'leaf') {
        this.$emit('showDetails', {
          'showType': data.data.modelType,
          'showId': data.data.id
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
  .top-div-class {
    max-height: calc(100vh - 335px);
    width: 100%;
    position: fixed;
    overflow-y : auto
  }
  .detail-class {
    width: 300px;
    position: fixed;
    bottom: 0px;
  }
  .view-list-thumbnails {
    width: 100%;
    height: 100%;
  }

  .father .child {
    /*display: none;*/
    visibility: hidden;
  }
  .father:hover .child {
    /*display: inline;*/
    visibility: visible;
  }

  .custom-tree-node-list {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding:0 8px;
  }
</style>
