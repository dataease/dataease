<template>
  <div class="union-container">
    <div class="union-header">
      {{ $t('dataset.union_relation') }}
      <div class="union-header-operator">
        <span class="select-svg-icon">
          <svg-icon
            v-if="unionParam.node.unionToParent.unionType === 'left'"
            icon-class="left-join"
            class="join-icon"
          />
          <svg-icon
            v-else-if="unionParam.node.unionToParent.unionType === 'right'"
            icon-class="right-join"
            class="join-icon"
          />
          <svg-icon
            v-else-if="unionParam.node.unionToParent.unionType === 'inner'"
            icon-class="inner-join"
            class="join-icon"
          />
          <svg-icon v-else icon-class="no-join" class="join-icon" />
        </span>
        <el-select
          v-model="unionParam.node.unionToParent.unionType"
          size="small"
          class="union-selector"
        >
          <el-option
            v-for="item in unionOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <deBtn
          type="primary"
          icon="el-icon-plus"
          class="union-add"
          @click="addUnion"
          >{{ $t('dataset.add_union_field') }}</deBtn
        >
      </div>
    </div>
    <div class="union-body">
      <div class="union-body-header">
        <span class="column" :title="unionParam.parent.currentDs.name">{{
          unionParam.parent.currentDs.name
        }}</span>
        <span class="column" :title="unionParam.node.currentDs.name">{{
          unionParam.node.currentDs.name
        }}</span>
        <span class="column-last">{{ $t('dataset.operator') }}</span>
      </div>
      <div class="union-body-container">
        <div
          v-for="(field, index) in unionParam.node.unionToParent.unionFields"
          :key="index"
          class="union-body-item"
        >
          <!--左侧父级field-->
          <span class="column">
            <el-select
              v-model="field.parentField.id"
              :placeholder="$t('dataset.pls_slc_union_field')"
              filterable
              clearable
              size="small"
              class="select-field"
            >
              <el-option
                v-for="item in parentFieldList"
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
          </span>
          <svg-icon icon-class="join-join" class="join-icon" />
          <!--右侧孩子field-->
          <span class="column">
            <el-select
              v-model="field.currentField.id"
              :placeholder="$t('dataset.pls_slc_union_field')"
              filterable
              clearable
              size="small"
              class="select-field"
            >
              <el-option
                v-for="item in nodeFieldList"
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
          </span>

          <span class="column-last">
            <el-button
              :disabled="
                unionParam.node.unionToParent.unionFields &&
                unionParam.node.unionToParent.unionFields.length === 1
              "
              type="text"
              icon="el-icon-delete"
              @click="removeUnionItem(index)"
            />
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UnionItemEdit',
  props: {
    parentFieldList: {
      type: Array,
      required: true
    },
    nodeFieldList: {
      type: Array,
      required: true
    },
    unionParam: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      unionOptions: [
        { label: this.$t('dataset.left_join'), value: 'left' },
        { label: this.$t('dataset.right_join'), value: 'right' },
        { label: this.$t('dataset.inner_join'), value: 'inner' }
      ]
    }
  },
  watch: {},
  mounted() {
    this.init()
  },
  methods: {
    init() {
      if (this.unionParam.node.unionToParent.unionFields.length < 1) {
        const item = {
          parentField: {},
          currentField: {}
        }
        this.unionParam.node.unionToParent.unionFields.push(item)
      }
    },
    addUnion() {
      const item = {
        parentField: {},
        currentField: {}
      }
      this.unionParam.node.unionToParent.unionFields.push(item)
    },
    removeUnionItem(index) {
      this.unionParam.node.unionToParent.unionFields.splice(index, 1)
    }
  }
}
</script>

<style lang="scss" scoped>
.union-container {
  height: 275px;
  font-family: PingFang SC;
}
.union-header {
  display: flex;
  align-items: center;
  width: 100%;
  justify-content: space-between;
  margin-bottom: 8px;
  color: var(--deTextPrimary, #1f2329);
  font-size: 16px;
  font-weight: 500;
}
.union-header-operator {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  position: relative;

  .select-svg-icon {
    position: absolute;
    left: 12px;
    top: 50%;
    z-index: 2;
    transform: translateY(-50%);
  }
}
.union-selector {
  width: 180px;
  ::v-deep.el-input__inner {
    padding-left: 32px;
  }
}
.union-add {
  margin-left: 12px;
}
.union-body {
  height: 240px;
  width: 100%;
}
.union-body-header {
  height: 46px;
  align-items: center;
  justify-content: space-between;
  display: flex;
  font-size: 14px;
  font-weight: 500;
  color: var(--deTextSecondary, #646a73);
}
.union-body-header .column {
  width: 336px;
  display: inline-block;
  margin-right: 9px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  margin-left: 10px;
}
.union-body-header .column-last {
  width: 40px;
  text-align: center;
}
.union-body-container {
  height: 180px;
  overflow-y: auto;
}
.select-field {
  width: 352px;
  display: inline-block;
}
.union-body-item {
  height: 32px;
  align-items: center;
  justify-content: space-between;
  display: flex;
  margin-bottom: 10px;
}
.union-body-item .column {
  width: 352px;
  display: inline-block;
  margin-right: 5px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}
.union-body-item .column-last {
  width: 40px;
  text-align: center;
  ::v-deep i {
    font-size: 16px;
    color: var(--deTextSecondary, #646a73);
  }
}
</style>
