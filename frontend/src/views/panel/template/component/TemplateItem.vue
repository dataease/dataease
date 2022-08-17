<template>
  <div class="de-card-model">
    <div class="card-img-model">
      <img :src="model.snapshot" alt="" />
    </div>
    <div class="card-info">
      <el-tooltip
        class="item"
        effect="dark"
        :content="model.name"
        placement="top"
      >
        <span class="de-model-text">{{ model.name }}</span>
      </el-tooltip>
      <el-dropdown size="medium" trigger="click" @command="handleCommand">
        <i class="el-icon-more"></i>
        <el-dropdown-menu class="de-card-dropdown" slot="dropdown">
          <slot>
            <el-dropdown-item command="rename">
              <i class="el-icon-edit"></i>
              重命名
            </el-dropdown-item>
            <el-dropdown-item command="delete">
              <i class="el-icon-delete"></i>
              删除
            </el-dropdown-item>
          </slot>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    model: {
      type: Object,
      default: () => {},
    },
  },
  methods: {
    handleCommand(key) {
      this.$emit("command", key);
    },
  },
};
</script>

<style lang="scss">
.de-card-model {
  width: 258px;
  height: 184px;
  box-sizing: border-box;
  background: #ffffff;
  border: 1px solid var(--deCardStrokeColor, #dee0e3);
  border-radius: 4px;
  margin: 0 25px 25px 0;
  .card-img-model {
    border-bottom: 1px solid var(--deCardStrokeColor, #dee0e3);
    height: 144px;
    width: 100%;

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
