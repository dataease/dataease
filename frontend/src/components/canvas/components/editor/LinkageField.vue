<template>

  <el-popover
    width="400"
    trigger="click"
    style="max-height: 400px;overflow-y: auto"
  >
    <el-row>
      <el-col :span="11">
        <div class="ellip">{{ sourceLinkageInfo.targetViewName }}</div>
      </el-col>
      <el-col :span="11">
        <div class="ellip">{{ linkageInfo.targetViewName }}</div>
      </el-col>
    </el-row>

    <el-row style="height: 120px;overflow-y: auto">
      <el-row
        v-for="(itemLinkage, index) in linkageInfo.linkageFields"
        :key="index"
      >
        <el-col :span="11">
          <div class="select-filed">
            <el-select
              v-model="itemLinkage.sourceField"
              size="mini"
              placeholder="请选择"
            >
              <el-option
                v-for="item in sourceLinkageInfo.targetViewFields"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              >
                <span style="float: left">
                  <svg-icon
                    v-if="item.deType === 0"
                    icon-class="field_text"
                    class="field-icon-text"
                  />
                  <svg-icon
                    v-if="item.deType === 1"
                    icon-class="field_time"
                    class="field-icon-time"
                  />
                  <svg-icon
                    v-if="item.deType === 2 || item.deType === 3"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                  <svg-icon
                    v-if="item.deType === 5"
                    icon-class="field_location"
                    class="field-icon-location"
                  />
                </span>
                <span style="float: left; color: #8492a6; font-size: 12px">{{ item.name }}</span>
              </el-option>
            </el-select>
          </div>
        </el-col>
        <el-col :span="11">
          <div class="select-filed">
            <el-select
              v-model="itemLinkage.targetField"
              size="mini"
              placeholder="请选择"
            >
              <el-option
                v-for="item in linkageInfo.targetViewFields"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              >
                <span style="float: left">
                  <svg-icon
                    v-if="item.deType === 0"
                    icon-class="field_text"
                    class="field-icon-text"
                  />
                  <svg-icon
                    v-if="item.deType === 1"
                    icon-class="field_time"
                    class="field-icon-time"
                  />
                  <svg-icon
                    v-if="item.deType === 2 || item.deType === 3"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                  <svg-icon
                    v-if="item.deType === 5"
                    icon-class="field_location"
                    class="field-icon-location"
                  />
                </span>
                <span style="float: left; color: #8492a6; font-size: 12px">{{ item.name }}</span>
              </el-option>
            </el-select>
          </div>
        </el-col>
        <el-col :span="2">
          <div>
            <el-button
              icon="el-icon-delete"
              type="text"
              size="small"
              style="float: left"
              @click="deleteLinkageField(index)"
            />
          </div>
        </el-col>
      </el-row>
    </el-row>

    <el-row class="bottom">
      <el-button
        size="mini"
        type="success"
        icon="el-icon-plus"
        round
        @click="addLinkageField(null,null)"
      >追加联动依赖字段</el-button>
    </el-row>

    <!--    <el-button slot="reference">T</el-button>-->
    <i
      slot="reference"
      class="icon iconfont icon-edit slot-class"
    />
  </el-popover>
</template>

<script>
import { mapState } from 'vuex'
import { checkSameDataSet } from '@/api/chart/chart'
export default {

  props: {
    element: {
      type: Object,
      required: true
    },
    active: {
      type: Boolean,
      required: false,
      default: false
    },
    // 当前模式 preview 预览 edit 编辑，
    activeModel: {
      type: String,
      required: false,
      default: 'preview'
    }
  },
  data() {
    return {
      componentType: null,
      linkageActiveStatus: false,
      editFilter: [
        'view',
        'custom'
      ]
    }
  },
  computed: {
    linkageInfo() {
      return this.targetLinkageInfo[this.element.propValue.viewId]
    },
    sourceLinkageInfo() {
      return this.targetLinkageInfo[this.curLinkageView.propValue.viewId]
    },
    ...mapState([
      'menuTop',
      'menuLeft',
      'menuShow',
      'curComponent',
      'componentData',
      'canvasStyleData',
      'linkageSettingStatus',
      'targetLinkageInfo',
      'curLinkageView'
    ])
  },
  mounted() {
    // 初始化映射关系 如果当前是相同的数据集且没有关联关系，则自动补充映射关系
    checkSameDataSet(this.curLinkageView.propValue.viewId, this.element.propValue.viewId).then(res => {
      if (res.data === 'YES' && this.linkageInfo.linkageFields.length === 0) {
        this.sourceLinkageInfo.targetViewFields.forEach(item => {
          this.addLinkageField(item.id, item.id)
        })
      }
    })
  },
  methods: {
    showViewDetails() {
      this.$emit('showViewDetails')
    },
    edit() {

    },
    linkageEdit() {

    },
    deleteLinkageField(index) {
      this.linkageInfo.linkageFields.splice(index, 1)
    },
    addLinkageField(sourceFieldId, targetFieldId) {
      const linkageFieldItem = {
        sourceField: sourceFieldId,
        targetField: targetFieldId
      }
      this.linkageInfo.linkageFields.push(linkageFieldItem)
    }
  }
}
</script>

<style lang="scss" scoped>
  .slot-class{
    color: white;
  }

  .bottom {
    margin-top: 20px;
    text-align: center;

  }
  .ellip{
    /*width: 100%;*/
    margin-left: 10px;
    margin-right: 10px;
    overflow: hidden;/*超出部分隐藏*/
    white-space: nowrap;/*不换行*/
    text-overflow:ellipsis;/*超出部分文字以...显示*/
    background-color: #f7f8fa;
    color: #3d4d66;
    font-size: 12px;
    line-height: 24px;
    height: 24px;
    border-radius: 3px;
  }

  .select-filed{
    /*width: 100%;*/
    margin-left: 10px;
    margin-right: 10px;
    overflow: hidden;/*超出部分隐藏*/
    white-space: nowrap;/*不换行*/
    text-overflow:ellipsis;/*超出部分文字以...显示*/
    color: #3d4d66;
    font-size: 12px;
    line-height: 35px;
    height: 35px;
    border-radius: 3px;
  }
  ::v-deep .el-popover{
    height: 200px;
    overflow: auto;
  }

</style>
