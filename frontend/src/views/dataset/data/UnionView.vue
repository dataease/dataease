<template>
  <el-row>
    <el-button
      v-if="hasDataPermission('manage', param.privileges)"
      size="mini"
      icon="el-icon-circle-plus-outline"
      @click="showUnionEdit"
      >{{ $t('dataset.add_union') }}</el-button
    >
    <el-row>
      <el-table
        size="mini"
        :data="unionData"
        :height="height"
        border
        style="width: 100%; margin-top: 10px"
      >
        <el-table-column
          prop="sourceTableName"
          :label="$t('dataset.source_table')"
        />
        <el-table-column
          prop="sourceTableFieldName"
          :label="$t('dataset.source_field')"
        />
        <el-table-column
          prop="sourceUnionRelation"
          :label="$t('dataset.union_relation')"
        >
          <template slot-scope="scope">
            <span style="font-size: 12px">
              <span v-if="scope.row.sourceUnionRelation === '1:N'">{{
                $t('dataset.left_join')
              }}</span>
              <span v-if="scope.row.sourceUnionRelation === 'N:1'">{{
                $t('dataset.right_join')
              }}</span>
              <span v-if="scope.row.sourceUnionRelation === '1:1'">{{
                $t('dataset.inner_join')
              }}</span>
              <span v-if="scope.row.sourceUnionRelation === 'N:N'">{{
                $t('dataset.full_join')
              }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="targetTableName"
          :label="$t('dataset.target_table')"
        />
        <el-table-column
          prop="targetTableFieldName"
          :label="$t('dataset.target_field')"
        />
        <el-table-column align="left" :label="$t('dataset.operate')">
          <template slot-scope="scope">
            <el-button
              v-if="hasDataPermission('manage', param.privileges)"
              type="text"
              size="mini"
              @click="edit(scope.row)"
              >{{ $t('dataset.edit') }}</el-button
            >
            <el-button
              v-if="hasDataPermission('manage', param.privileges)"
              type="text"
              size="mini"
              @click="deleteUnion(scope.row)"
              >{{ $t('dataset.delete') }}</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-row>

    <el-dialog
      v-dialogDrag
      :title="$t('dataset.union_setting')"
      :visible="editUnion"
      :show-close="false"
      width="600px"
      class="dialog-css"
    >
      <el-row
        style="display: flex; align-items: center; justify-content: center"
      >
        <el-col :span="6">
          <p class="table-name-css" :title="table.name">{{ table.name }}</p>
          <el-select
            v-model="union.sourceTableFieldId"
            :placeholder="$t('dataset.pls_slc_union_field')"
            filterable
            clearable
            size="mini"
          >
            <el-option
              v-for="item in sourceFieldOption"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <span>
                <span v-if="item.deType === 0">
                  <svg-icon
                    v-if="item.deType === 0"
                    icon-class="field_text"
                    class="field-icon-text"
                  />
                </span>
                <span v-if="item.deType === 1">
                  <svg-icon
                    v-if="item.deType === 1"
                    icon-class="field_time"
                    class="field-icon-time"
                  />
                </span>
                <span v-if="item.deType === 2 || item.deType === 3">
                  <svg-icon
                    v-if="item.deType === 2 || item.deType === 3"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                </span>
                <span v-if="item.deType === 5">
                  <svg-icon
                    v-if="item.deType === 5"
                    icon-class="field_location"
                    class="field-icon-location"
                  />
                </span>
              </span>
              <span>
                {{ item.name }}
              </span>
            </el-option>
          </el-select>
        </el-col>

        <el-col :span="6">
          <el-radio-group
            v-model="union.sourceUnionRelation"
            size="mini"
            style="display: block; width: 100%; text-align: center"
          >
            <el-radio class="union-relation-css" label="1:N">{{
              $t('dataset.left_join')
            }}</el-radio>
            <el-radio class="union-relation-css" label="N:1">{{
              $t('dataset.right_join')
            }}</el-radio>
            <el-radio class="union-relation-css" label="1:1">{{
              $t('dataset.inner_join')
            }}</el-radio>
            <!--            <el-radio class="union-relation-css" label="N:N">{{ $t('dataset.full_join') }}</el-radio>-->
          </el-radio-group>
        </el-col>

        <el-col :span="6">
          <el-popover
            ref="targetTable"
            placement="bottom"
            width="500"
            trigger="click"
          >
            <dataset-group-selector-tree
              :fix-height="true"
              show-mode="union"
              :table="table"
              :custom-type="customType"
              :mode="table.mode"
              @getTable="getTable"
            />
            <el-button slot="reference" size="mini" style="width: 100%">
              <p
                class="table-name-css"
                :title="targetTable.name || $t('dataset.pls_slc_union_table')"
              >
                {{ targetTable.name || $t('dataset.pls_slc_union_table') }}
              </p>
            </el-button>
          </el-popover>

          <el-select
            v-model="union.targetTableFieldId"
            :placeholder="$t('dataset.pls_slc_union_field')"
            filterable
            clearable
            size="mini"
          >
            <el-option
              v-for="item in targetFieldOption"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <span>
                <span v-if="item.deType === 0">
                  <svg-icon
                    v-if="item.deType === 0"
                    icon-class="field_text"
                    class="field-icon-text"
                  />
                </span>
                <span v-if="item.deType === 1">
                  <svg-icon
                    v-if="item.deType === 1"
                    icon-class="field_time"
                    class="field-icon-time"
                  />
                </span>
                <span v-if="item.deType === 2 || item.deType === 3">
                  <svg-icon
                    v-if="item.deType === 2 || item.deType === 3"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                </span>
                <span v-if="item.deType === 5">
                  <svg-icon
                    v-if="item.deType === 5"
                    icon-class="field_location"
                    class="field-icon-location"
                  />
                </span>
              </span>
              <span>
                {{ item.name }}
              </span>
            </el-option>
          </el-select>
        </el-col>
      </el-row>

      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeUnion">{{
          $t('dataset.cancel')
        }}</el-button>
        <el-button type="primary" size="mini" @click="saveUnion">{{
          $t('dataset.confirm')
        }}</el-button>
      </div>
    </el-dialog>
  </el-row>
</template>

<script>
import { post, fieldList } from '../../../api/dataset/dataset'
import DatasetGroupSelectorTree from '../common/DatasetGroupSelectorTree'

export default {
  name: 'UnionView',
  components: { DatasetGroupSelectorTree },
  props: {
    table: {
      type: Object,
      required: true
    },
    param: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      height: 500,
      union: {
        id: null,
        sourceTableId: this.table.id,
        sourceTableFieldId: '',
        sourceUnionRelation: '',
        targetTableId: '',
        targetTableFieldId: '',
        targetUnionRelation: ''
      },
      unionData: [],
      editUnion: false,
      sourceFieldOption: [],
      targetFieldOption: [],
      targetTable: {},
      customType: ['db', 'sql', 'excel', 'api']
    }
  },
  watch: {
    table: function () {
      this.initUnion()
    }
  },
  mounted() {
    this.calHeight()
    this.initUnion()
  },
  methods: {
    calHeight() {
      const that = this
      setTimeout(function () {
        const currentHeight = document.documentElement.clientHeight
        that.height = currentHeight - 56 - 30 - 26 - 25 - 55 - 38 - 28 - 10
      }, 10)
    },
    initUnion() {
      if (this.table.id) {
        if (this.table.mode === 0) {
          this.customType = ['db']
        } else {
          this.customType = ['db', 'sql', 'excel', 'api']
        }
        post('dataset/union/listByTableId/' + this.table.id, {}).then(
          (response) => {
            this.unionData = response.data
          }
        )
      }
    },

    showUnionEdit() {
      // 校验同步状态
      // post('/dataset/table/checkDorisTableIsExists/' + this.table.id, {}, true).then(response => {
      //   if (response.data) {
      this.union.sourceTableId = this.table.id
      fieldList(this.table.id).then((response) => {
        this.sourceFieldOption = JSON.parse(
          JSON.stringify(response.data)
        ).filter((ele) => ele.extField === 0)
      })
      this.editUnion = true
      //   } else {
      //     this.$message({
      //       type: 'error',
      //       message: this.$t('dataset.invalid_table_check'),
      //       showClose: true
      //     })
      //   }
      // })
    },
    saveUnion() {
      if (
        !this.union.sourceTableFieldId ||
        !this.union.sourceUnionRelation ||
        !this.union.targetTableId ||
        !this.union.targetTableFieldId
      ) {
        this.$message({
          type: 'error',
          message: this.$t('dataset.pls_setting_union_success'),
          showClose: true
        })
        return
      }
      this.union.targetUnionRelation = this.union.sourceUnionRelation
        .split('')
        .reverse()
        .join('')
      post('dataset/union/save', this.union).then((response) => {
        this.$message({
          type: 'success',
          message: this.$t('dataset.save_success'),
          showClose: true
        })
        this.closeUnion()
        this.initUnion()
      })
    },
    closeUnion() {
      this.editUnion = false
      this.resetUnion()
    },
    resetUnion() {
      this.union = {
        id: null,
        sourceTableId: this.table.id,
        sourceTableFieldId: '',
        sourceUnionRelation: '',
        targetTableId: '',
        targetTableFieldId: '',
        targetUnionRelation: ''
      }
      this.targetTable = {}
      this.targetFieldOption = []
    },

    edit(item) {
      this.union = JSON.parse(JSON.stringify(item))
      this.targetTable.name = this.union.targetTableName
      fieldList(this.union.targetTableId).then((response) => {
        this.targetFieldOption = response.data
        this.showUnionEdit()
      })
    },
    deleteUnion(item) {
      this.$confirm(
        this.$t('dataset.confirm_delete'),
        this.$t('dataset.tips'),
        {
          confirmButtonText: this.$t('dataset.confirm'),
          cancelButtonText: this.$t('dataset.cancel'),
          type: 'warning'
        }
      ).then(() => {
        post('dataset/union/delete', item).then((response) => {
          this.$message({
            type: 'success',
            message: this.$t('dataset.delete_success'),
            showClose: true
          })
          this.initUnion()
        })
      })
    },
    getTable(param) {
      if (param.id === this.table.id) {
        this.$message({
          type: 'error',
          message: this.$t('dataset.can_not_union_self'),
          showClose: true
        })
        return
      }
      if (this.table.mode === 0) {
        if (param.dataSourceId !== this.table.dataSourceId) {
          this.$message({
            type: 'error',
            message: this.$t('dataset.can_not_union_diff_datasource'),
            showClose: true
          })
          return
        }
      }
      this.targetTable = param
      this.union.targetTableId = param.id
      this.union.targetTableFieldId = ''
      fieldList(param.id).then((response) => {
        this.targetFieldOption = JSON.parse(
          JSON.stringify(response.data)
        ).filter((ele) => ele.extField === 0)
      })
      this.$refs['targetTable'].doClose()
    }
  }
}
</script>

<style scoped>
.table-name-css {
  margin: 4px 2px;
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
}
.union-relation-css {
  display: block;
  width: 100%;
  padding: 4px 10px;
}

.dialog-css ::v-deep .el-dialog__title {
  font-size: 14px;
}
.dialog-css ::v-deep .el-dialog__header {
  padding: 20px 20px 0;
}
.dialog-css ::v-deep .el-dialog__body {
  padding: 10px 20px 20px;
}
</style>
