<template xmlns:el-col="http://www.w3.org/1999/html">
  <div class="de-template-list">
    <el-input
      v-model="templateFilterText"
      :placeholder="$t('system_parameter_setting.search_keywords')"
      size="small"
      class="de-input-search"
      clearable
    >
      <svg-icon
        slot="prefix"
        icon-class="de-search"
      />
    </el-input>
    <el-empty
      v-if="!templateListComputed.length && templateFilterText === ''"
      :image="noneImg"
      :description="$t('components.no_classification')"
    />
    <el-empty
      v-if="!templateListComputed.length && templateFilterText !== ''"
      :image="nothingImg"
      :description="$t('components.relevant_content_found')"
    />
    <ul>
      <li
        v-for="ele in templateListComputed"
        :key="ele.name"
        :class="[{ select: activeTemplate === ele.id }]"
        @click="nodeClick(ele)"
      >
        <img
          :src="iconImgRul(ele.icon)"
          style="margin-right: 8px;border-radius: 4px"
          width="24"
          height="24"
        >
        <span>{{ ele.name }}</span>
      </li>
    </ul>
    <el-row class="de-root-class">
      <deBtn
        secondary
        @click="cancel()"
      >{{
          $t('commons.cancel')
        }}
      </deBtn>
      <deBtn
        type="primary"
        @click="save()"
        :disabled="!activeTemplate"
      >{{
          $t('commons.confirm')
        }}
      </deBtn>
    </el-row>
  </div>
</template>

<script>
import msgCfm from '@/components/msgCfm/index'
import { imgUrlTrans } from '@/components/canvas/utils/utils'
import { move } from '@/api/system/appTemplate'

export default {
  name: 'TemplateMoveList',
  components: {},
  mixins: [msgCfm],
  props: {
    sourceTemplateInfo: {
      type: Object
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
      activeTemplate: '',
      noneImg: require('@/assets/None.png'),
      nothingImg: require('@/assets/nothing.png')
    }
  },
  computed: {
    templateListComputed() {
      if (!this.templateFilterText) {
        return this.templateList.filter((ele) =>
          ele.id !== this.sourceTemplateInfo.pid
        )
      }
      return this.templateList.filter((ele) =>
        ele.name.includes(this.templateFilterText) && ele.id !== this.sourceTemplateInfo.pid
      )
    }
  },
  methods: {
    iconImgRul(iconUrl) {
      return imgUrlTrans(iconUrl)
    },
    cancel() {
      this.$emit('closeDialog')
    },
    save() {
      const request = {
        id: this.sourceTemplateInfo.id,
        pid: this.activeTemplate,
        name: this.sourceTemplateInfo.name
      }
      move(request).then((response) => {
        this.$emit('templateMoveClose')
      })
    },
    showPositionCheck(requiredPosition) {
      return this.showPosition === requiredPosition
    },
    nodeClick({ id, name }) {
      this.activeTemplate = id
    }
  }
}
</script>

<style scoped lang="scss">
.de-template-list {
  height: 380px;
  overflow-y: hidden;
  position: relative;

  ul {
    margin: 16px 0 20px 0;
    padding: 0;
    overflow-y: auto;
    max-height: calc(100% - 90px);
  }

  li {
    list-style: none;
    width: 100%;
    box-sizing: border-box;
    height: 40px;
    padding: 0 30px 0 12px;
    display: flex;
    align-items: center;
    border-radius: 4px;
    color: var(--deTextPrimary, #1f2329);
    font-family: "PingFang SC";
    font-style: normal;
    font-weight: 500;
    font-size: 14px;
    cursor: pointer;
    position: relative;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;

    .folder {
      color: #8f959e;
      margin-right: 9px;
    }

    .more {
      position: absolute;
      top: 50%;
      right: 10px;
      transform: translateY(-50%);
      display: none;

      .el-icon-more {
        width: 24px;
        height: 24px;
        line-height: 24px;
        text-align: center;
        font-size: 12px;
        color: #646a73;
        cursor: pointer;
      }

      .el-icon-more:hover {
        background: rgba(31, 35, 41, 0.1);
        border-radius: 4px;
      }

      .el-icon-more:active {
        background: rgba(31, 35, 41, 0.2);
        border-radius: 4px;
      }
    }

    &:hover {
      background: rgba(31, 35, 41, 0.1);

      .more {
        display: block;
      }
    }
  }

  li.select {
    background: var(--deWhiteHover, #ecf5ff) !important;
    color: var(--TextActive, #3370ff) !important;
  }

  .de-btn-fix {
    position: absolute;
    bottom: 0;
    left: 0;
  }
}

.de-template-dropdown {
  margin-top: 0 !important;

  .popper__arrow {
    display: none !important;
  }
}

.de-root-class {
  bottom: 0;
  width: 100%;
  position: absolute;
  text-align: right;
}
</style>
