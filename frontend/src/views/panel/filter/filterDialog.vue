<template>

  <de-container class="de-dialog-container">
    <de-aside-container class="ms-aside-container">
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane :lazy="true" class="de-tab" label="按表选择" name="dataset">按表选择</el-tab-pane>
        <el-tab-pane :lazy="true" class="de-tab" label="按组件选择" name="assembly">按组件选择</el-tab-pane>
      </el-tabs>
    </de-aside-container>

    <!--画布区域-->
    <de-main-container class="ms-main-container">
      <div>
        <el-row>
          <el-col :span="24">
            <div class="filter-field">
              <div class="field-content">
                <div class="field-content-left">
                  <div class="field-content-text">字段</div>
                </div>

                <div class="field-content-right">请拖入左侧字段</div>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <div class="filter-options-left">
              <el-switch
                active-text="单选"
                inactive-text="多选"
              />
            </div>
          </el-col>
          <el-col :span="16"><div class="filter-options-right">
            <el-checkbox disabled>备选项1</el-checkbox>
            <el-checkbox disabled>备选项</el-checkbox>
          </div>

          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <div class="filter-content" />
          </el-col>
        </el-row>
      </div>
    </de-main-container>
  </de-container>

</template>
<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
export default {
  name: 'FilterDialog',
  components: {
    DeMainContainer,
    DeContainer,
    DeAsideContainer
  },
  data() {
    return {
      activeName: 'dataset'
    }
  },
  created() {

  },

  methods: {

  }
}
</script>

<style lang="scss" scoped>
  .de-dialog-container {
    height: 50vh !important;

  }
  .ms-aside-container {
    width: 40% !important;
    min-width: 230px !important;
    max-width: 260px !important;
    height: 100%;
    min-height: 400px;
    padding: 5px;
    border: none;
  }

  .ms-main-container {
    height: 100%;
    min-height: 400px;

  }

  .filter-field {
    //   background: #99a9bf;
      border-radius: 4px;
      height: 45px;
      .field-content {
        position: relative;
        display: table;
        width: 100%;
        height: 100%;
        white-space: nowrap;

        .field-content-left {
            width: 50px;
            max-width: 50px;
            position: relative;
            display: table-cell;
            vertical-align: middle;
            margin: 0px;
            padding: 8px;
            height: 100%;
            border-right: none;
            border: 1px solid #E6E6E6;
            .field-content-text {
                box-sizing: border-box;
                overflow: hidden;
                overflow-x: hidden;
                overflow-y: hidden;
                word-break: break-all;
            }
        }
        .field-content-right {
            border-left: none;
            color: #9ea6b2;
            border: 1px solid #E6E6E6;
            width: 0%;
            max-width: 0%;
            position: relative;
            display: table-cell;
            vertical-align: middle;
            margin: 0px;
            padding: 0 0 0 10px;
            height: 100%;
        }
    }

  }
  .filter-options-left {
      align-items: center;
      display: flex;
      flex-direction: row;
      justify-content: flex-start;
      flex-wrap: nowrap;
      height: 50px;
  }
  .filter-options-right {
      align-items: center;
      display: flex;
      flex-direction: row;
      justify-content: flex-end;
      flex-wrap: nowrap;
      height: 50px;
  }

  .filter-content {
      height: calc(50vh - 150px);
      top: 160px;
      background: #99a9bf;
  }

</style>
