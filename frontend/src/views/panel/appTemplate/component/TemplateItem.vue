<template>
  <div
    :style="classBackground"
    class="de-card-model"
  >
    <div
      class="card-img-model"
      :style="classImg"
    >
      <img
        :src="imgSrc"
        alt=""
      >
    </div>
    <div
      v-if="showPositionCheck('system-setting')"
      class="card-info"
    >
      <el-tooltip
        class="item"
        effect="dark"
        :content="model.name"
        placement="top"
      >
        <span class="de-model-text">{{ model.name }}</span>
      </el-tooltip>
      <el-dropdown
        size="medium"
        trigger="click"
        @command="handleCommand"
      >
        <i class="el-icon-more"/>
        <el-dropdown-menu
          slot="dropdown"
          class="de-card-dropdown"
        >
          <slot>
            <el-dropdown-item command="update">
              <i class="el-icon-edit"/>
              {{ $t('commons.update') }}
            </el-dropdown-item>
            <el-dropdown-item command="delete">
              <i class="el-icon-delete"/>
              {{ $t('commons.uninstall') }}
            </el-dropdown-item>
            <el-dropdown-item
              icon="el-icon-right"
              command="move"
            >
              {{ $t('app_template.move') }}
            </el-dropdown-item>
          </slot>
        </el-dropdown-menu>
      </el-dropdown>
    </div>

    <div
      v-if="showPositionCheck('market-manage')"
      class="card-info-apply"
    >
      <el-row>
        <el-row>
          <el-tooltip
            class="item"
            effect="dark"
            :content="model.name"
            placement="top"
          >
            <span class="de-model-text-market">{{ model.name }}</span>
          </el-tooltip>
        </el-row>
        <el-row class="market-button-area">
          <el-button
            size="small"
            style="width: 45%"
            @click="templatePreview"
          >{{ $t('panel.preview') }}
          </el-button>
          <el-button
            size="small"
            style="width: 45%"
            type="primary"
            @click="apply"
          >{{ $t('panel.apply') }}
          </el-button>
        </el-row>
      </el-row>
    </div>
  </div>
</template>
<script>
import { imgUrlTrans } from '@/components/canvas/utils/utils'

export default {
  props: {
    showPosition: {
      type: String,
      required: false,
      default: 'system-setting'
    },
    model: {
      type: Object,
      default: () => {
      }
    },
    width: {
      type: Number
    }
  },
  computed: {
    imgSrc() {
      return imgUrlTrans(this.model.snapshot)
    },
    classBackground() {
      return {
        width: this.width + 'px',
        height: this.width * 0.714 + 'px'
      }
    },
    classImg() {
      return {
        width: (this.width - 2) + 'px',
        height: this.width * 0.576 + 'px'
      }
    }
  },
  methods: {
    templatePreview() {
      this.$emit('previewApp')
    },
    apply() {
      this.$emit('applyNew')
    },
    showPositionCheck(requiredPosition) {
      return this.showPosition === requiredPosition
    },
    handleCommand(key) {
      this.$emit('command', key)
    }
  }
}
</script>

<style lang="scss">
.de-card-model {
  position: relative;
  box-sizing: border-box;
  background: #ffffff;
  border: 1px solid var(--deCardStrokeColor, #dee0e3);
  border-radius: 4px;
  margin: 0 24px 25px 0;

  .card-img-model {
    border-bottom: 1px solid var(--deCardStrokeColor, #dee0e3);
    height: 144px;
    width: 100%;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      border-top-left-radius: 4px;
      border-top-right-radius: 4px;
    }
  }

  .card-info {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px 9px 12px;
    box-sizing: border-box;

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

  &:hover .card-info-apply {
    height: 92px;
  }

  &:hover .market-button-area {
    display: block;
  }

  .market-button-area {
    text-align: center;
    margin-top: 4px;
    display: none;
  }

  .card-info-apply {
    background: #ffffff;
    width: 100%;
    height: 48px;
    position: absolute;
    bottom: 0px;
    left: 0px;
    transition: height 0.3s ease-out;
    align-items: center;
    justify-content: space-between;
    padding: 12px 12px 12px 12px;
    box-sizing: border-box;
    border-radius: 0 0 4px 4px;
    border-top: 1px solid var(--deCardStrokeColor, #dee0e3);
    overflow-y: hidden;

  }
}

.de-model-text {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1f2329;
  display: inline-block;
  width: 90%;
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
  margin-right: 10px;
}

.de-model-text-market {
  font-family: "PingFang SC";
  font-style: normal;
  color: #1f2329;
  display: inline-block;
  width: 90%;
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
  margin-right: 10px;
  font-weight: 500;
  font-size: 16px;
  line-height: 24px;
}

.de-card-model:hover {
  box-shadow: 0px 6px 24px rgba(31, 35, 41, 0.08);
}

.de-card-dropdown {
  margin-top: 0 !important;

  .popper__arrow {
    display: none !important;
  }
}
</style>
