<template>
  <el-container>
    <!-- <de-header>Header</de-header> -->
    <span style="line-height: 35px; position: absolute; top:10px;right: 10px">
      <el-button
        size="mini"
        @click="toDir"
      >
        {{ $t('commons.close') }}
      </el-button>
    </span>
    <de-container>

      <de-main-container class="ms-main-container">
        <!--        <drawing-board :disabled="true" />-->
        this is test
      </de-main-container>
    </de-container>
  </el-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
// import DrawingBoard from '../DrawingBoard'
export default {
  components: {
    DeMainContainer,
    DeContainer
  },
  data() {
    return {

    }
  },

  methods: {

    toDir() {
      this.$router.replace('/panel/index')
    }

  }
}
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 91px);
    padding: 15px;
    min-width: 60px;
    max-width: 60px;
    border: none;
  }

  .ms-main-container {
    height: calc(100vh - 91px);
  }

  .de-header {
    height: 35px !important;
    border-bottom: 1px solid #E6E6E6;
  }

  .showLeftPanel {
  overflow: hidden;
  position: relative;
  width: calc(100% - 15px);
}
</style>

<style lang="scss" scoped>
.leftPanel-background {
  position: fixed;
  top: 0;
  left: 0;
  opacity: 0;
  transition: opacity .3s cubic-bezier(.7, .3, .1, 1);
  background: rgba(0, 0, 0, .2);
  z-index: -1;
}

.leftPanel {
  width: 100%;
  max-width: 260px;
  height: calc(100vh - 91px);
  position: fixed;
  top: 91px;
  left: 60px;
  box-shadow: 0px 0px 15px 0px rgba(0, 0, 0, .05);
  transition: all .25s cubic-bezier(.7, .3, .1, 1);
  transform: translate(100%);
  background: #fff;
  z-index: 1003;
}

.show {
  transition: all .3s cubic-bezier(.7, .3, .1, 1);

  .leftPanel-background {
    z-index: 1002;
    opacity: 1;
    width: 100%;
    height: 100%;
  }

  .leftPanel {
    transform: translate(0);
  }

}

</style>
