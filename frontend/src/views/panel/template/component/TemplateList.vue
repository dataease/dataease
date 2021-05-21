<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col>
    <el-row>
      <el-button icon="el-icon-folder-add" type="primary" size="mini" @click="add()">
        {{ $t('panel.add_category') }}
      </el-button>
    </el-row>
    <el-row style="margin-top: 5px">
      <el-row>
        <el-input
          v-model="templateFilterText"
          :placeholder="$t('panel.filter_keywords')"
          size="mini"
          clearable
          prefix-icon="el-icon-search"
        />
      </el-row>
      <el-row style="margin-top: 5px">
        <el-tree
          ref="templateTree"
          :default-expanded-keys="defaultExpandedKeys"
          :data="templateList"
          node-key="id"
          :expand-on-click-node="true"
          :filter-node-method="filterNode"
          :highlight-current="true"
          @node-click="nodeClick"
        >
          <span slot-scope="{ node, data }" class="custom-tree-node">
            <span style="display: flex; flex: 1 1 0%; width: 0px;">
              <span>
                <i class="el-icon-folder" />
              </span>
              <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">{{ data.name }}</span>
            </span>
            <span>
              <span @click.stop>
                <el-dropdown trigger="click" size="small" @command="clickMore">
                  <span class="el-dropdown-link">
                    <el-button
                      icon="el-icon-more"
                      type="text"
                      size="small"
                    />
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item icon="el-icon-upload2" :command="beforeClickMore('import',data,node)">
                      {{ $t('panel.import') }}
                    </el-dropdown-item>
                    <el-dropdown-item icon="el-icon-edit" :command="beforeClickMore('edit',data,node)">
                      {{ $t('panel.rename') }}
                    </el-dropdown-item>
                    <el-dropdown-item icon="el-icon-delete" :command="beforeClickMore('delete',data,node)">
                      {{ $t('panel.delete') }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </span>
            </span>
          </span>

        </el-tree>
      </el-row>
    </el-row>
  </el-col>
</template>

<script>
export default {
  name: 'TemplateList',
  components: { },
  props: {
    templateType: {
      type: String,
      default: ''
    },
    templateList: {
      type: Array,
      default: function() {
        return []
      }
    }
  },
  data() {
    return {
      templateFilterText: '',
      defaultExpandedKeys: [],
      currentTemplateShowList: []
    }
  },
  computed: {

  },
  watch: {
    templateFilterText(val) {
      this.$refs.templateTree.filter(val)
    }
  },
  methods: {
    clickAdd() {

    },
    clickMore(param) {
      switch (param.type) {
        case 'edit':
          this.templateEdit(param.data)
          break
        case 'delete':
          this.templateDelete(param.data)
          break
        case 'import':
          this.templateImport(param.data)
          break
      }
    },
    beforeClickMore(type, data, node) {
      return {
        'type': type,
        'data': data,
        'node': node
      }
    },

    filterNode(value, data) {
      if (!value) return true
      return data.label.indexOf(value) !== -1
    },
    nodeClick(data, node) {
      this.$emit('showCurrentTemplate', data.id)
    },
    add() {
      this.$emit('showTemplateEditDialog', 'new')
    },
    templateDelete(template) {
      this.$alert(this.$t('panel.confirm_delete') + this.$t('panel.category') + ': ' + template.name + '？', '', {
        confirmButtonText: this.$t('panel.confirm_delete'),
        callback: (action) => {
          if (action === 'confirm') {
            this.$emit('templateDelete', template.id)
          }
        }
      })
    },
    templateEdit(template) {
      this.$emit('templateEdit', template)
    },
    templateImport(template) {
      this.$emit('templateImport', template.id)
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
    padding-right: 8px;
  }
</style>
