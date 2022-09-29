<template>
  <div
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
    class="de-ds-container"
    :class="[{ 'is-driver-mgm': currentMgm === 'driverMgm' }]"
  >
    <div v-if="currentMgm === 'driverMgm'" class="dsr-route-title">
      <div>
        <i class="el-icon-arrow-left back-button" @click="jump" />
        <span>{{ $t('driver.mgm') }}</span>
      </div>
      <deBtn
        type="primary"
        icon="el-icon-plus"
        @click="addDriver"
      >{{ $t("driver.add") }}
      </deBtn>
    </div>
    <de-aside-container
      style="padding: 0 0"
      type="datasource"
    >
      <ds-tree
        ref="dsTree"
        :datasource="datasource"
        @switch-mgm="switchMgm"
        @switch-main="switchMain"
      />
    </de-aside-container>
    <de-main-container>
      <component
        :is="component"
        v-if="!!component"
        :params="param"
        @DataUpdate="dataUpdate"
        :t-data="tData"
        :ds-types="dsTypes"
        @refresh-type="refreshType"
        @switch-component="switchMain"
      />
      <el-empty v-else :image-size="125" :description="$t(`datasource.${swTips}`)" :image="image" />
    </de-main-container>
  </div>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import DsTree from './DsTree'
import DsForm from './DsForm'
import dsTable from './dsTable'
import DriverForm from './DriverFormDetail'
export default {
  name: 'DsMain',
  components: { DeMainContainer, DeAsideContainer, DsTree },
  data() {
    return {
      image: require('@/assets/None_Select_ds.png'),
      component: '',
      datasource: {},
      param: null,
      tData: null,
      currentMgm: 'dsMgm',
      dsTypes: []
    }
  },
  computed: {
    swTips() {
      return this.currentMgm === 'driverMgm' ? 'diver_on_the_left' : 'on_the_left'
    }
  },
  methods: {
    dataUpdate(row) {
      this.$refs.dsTree.dataUpdate(row)
    },
    jump() {
      this.$refs.dsTree.dsMgm()
      this.switchMgm('dsMgm')
    },
    switchMgm(type) {
      this.currentMgm = type
    },
    addDriver() {
      this.$refs.dsTree.addDriver()
    },
    // 切换main区内容
    switchMain(param) {
      const { component, componentParam, tData, dsTypes } = param
      this.component = ''
      this.param = null
      this.$nextTick(() => {
        switch (component) {
          case 'DsForm':
            this.component = DsForm
            this.param = componentParam
            this.tData = tData
            this.dsTypes = dsTypes
            break
          case 'DriverForm':
            this.component = DriverForm
            this.param = componentParam
            this.tData = tData
            this.dsTypes = dsTypes
            break
          case 'dsTable':
            this.component = dsTable
            this.param = componentParam
            break
          default:
            this.component = ''
            this.param = null
            break
        }
      })
    },
    refreshType(datasource) {
      this.datasource = datasource
      this.$refs.dsTree && this.$refs.dsTree.refreshType(datasource)
    },
    msg2Current(sourceParam) {
      this.$refs.dsTree && this.$refs.dsTree.markInvalid(sourceParam)
    }
  }
}
</script>

<style scoped lang="scss">
.de-ds-container {
  height: 100%;
  width: 100%;
  overflow: hidden;
  display: flex;
  flex-wrap: nowrap;
  box-sizing: border-box;
  .el-empty {
    height: 100%;
  }
}
.ms-aside-container {
  height: calc(100vh - 56px);
  padding: 0px;
  min-width: 260px;
  max-width: 460px;
}
.dsr-route-title {
  width: 100%;
  margin: -2px 0 22px 0;
  display: flex;
  justify-content: space-between;
  align-content: center;
}
.is-driver-mgm {
  height: calc(100vh - 56px);
  background-color: var(--MainBG, #f5f6f7);
  padding: 24px;
  flex-wrap: wrap;
  .ms-aside-container,
  .ms-main-container {
    height: calc(100vh - 170px);
    background-color: var(--ContentBG, #ffffff);
    .tree-style {
      padding-top: 24px;
    }
  }
  .ms-main-container {
    flex: 1;
    position: relative;
    padding: 24px 0 70px 24px;
  }
}
</style>
