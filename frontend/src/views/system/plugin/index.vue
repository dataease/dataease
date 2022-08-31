<template>
  <de-layout-content
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
  >
    <div class="top-install">
      <el-input
        :placeholder="$t('components.by_plugin_name')"
        size="small"
        prefix-icon="el-icon-search"
        v-model="name"
        clearable
        @blur="search"
      >
      </el-input>
      <el-upload
        v-permission="['plugin:upload']"
        :action="baseUrl + 'api/plugin/upload'"
        :multiple="false"
        :show-file-list="false"
        :file-list="fileList"
        accept=".zip"
        :before-upload="beforeUpload"
        :on-success="uploadSuccess"
        :on-error="uploadFail"
        name="file"
        :headers="headers"
      >
        <deBtn
          :icon="!uploading ? 'el-icon-upload2' : 'el-icon-loading'"
          type="primary"
          :disabled="uploading"
        >
          {{ $t(!uploading ? "plugin.local_install" : "dataset.uploading") }}
        </deBtn>
      </el-upload>
    </div>
    <div v-if="!data.length" class="plugin-cont">
      <el-empty style="width: 100%" description="没有找到相关内容"></el-empty>
    </div>
    <div v-else class="plugin-cont">
      <div v-for="ele in data" :key="ele.pluginId" class="de-card-plugin">
        <div class="card-info">
          <div class="info-top">
            <el-image
              :src="`/api/pluginCommon/staticInfo/${ele.moduleName}/svg`"
            >
              <div slot="error" class="image-slot">
                <img  :src="imgDefault"/>
              </div>
            </el-image>
            <p class="title">{{ ele.descript }}</p>
            <el-tooltip
              class="item"
              effect="dark"
              :content="ele.descript"
              placement="top"
            >
              <p class="tips">{{ ele.name }}</p>
            </el-tooltip>
          </div>
          <div class="info-left">
            <p class="list name" v-for="item in listName" :key="item">
              {{ $t(`components.${item}`) }}
            </p>
          </div>
          <div class="info-right">
            <p class="list value" v-for="item in listValue" :key="item">
              <template v-if="item === 'cost' && !ele.cost">
                <el-tag size="mini" type="success">{{ $t('components.free')}}</el-tag>
              </template>
              <template v-else>
                {{ ele[item] }}
              </template>
            </p>
          </div>
        </div>
        <div class="card-method" :class="`btn-${numPlugin}`">
          <el-upload
            v-permission="['plugin:upload']"
            :action="baseUrl + 'api/plugin/update/' + ele.pluginId"
            :multiple="false"
            :show-file-list="false"
            :file-list="fileList"
            accept=".zip"
            :before-upload="beforeUpload"
            :on-success="uploadSuccess"
            :on-error="uploadFail"
            name="file"
            :headers="headers"
          >
            <div class="btn-plugin update">
              <i class="el-icon-more"></i>{{ $t('dataset.update')}}
            </div>
          </el-upload>
          <el-divider v-if="numPlugin === 2" direction="vertical"></el-divider>
          <el-tooltip
            class="item"
            effect="dark"
            :content="$t('components.unable_to_uninstall')"
            placement="top"
          >
            <div
              :class="[{ 'is-disable': btnDisabled(ele) }]"
              v-if="checkPermission(['plugin:uninstall']) && btnDisabled(ele)"
              @click="del(ele)"
              class="btn-plugin uninstall"
            >
              <i class="el-icon-more"></i> {{$t('components.uninstall')}}
            </div>
          </el-tooltip>
          <div
            :class="[{ 'is-disable': btnDisabled(ele) }]"
            v-if="checkPermission(['plugin:uninstall']) && !btnDisabled(ele)"
            @click="del(ele)"
            class="btn-plugin uninstall"
          >
            <i class="el-icon-more"></i>{{$t('components.uninstall')}}
          </div>
        </div>
      </div>
    </div>
  </de-layout-content>
</template>

<script>
import DeLayoutContent from "@/components/business/DeLayoutContent";

import { checkPermission } from "@/utils/permission";
import { formatCondition, formatQuickCondition } from "@/utils/index";
import { pluginLists, uninstall } from "@/api/system/plugin";
import { getToken } from "@/utils/auth";
import msgCfm from "@/components/msgCfm/index";
import { log } from '@antv/g2plot/lib/utils';
export default {
  components: { DeLayoutContent },
  mixins: [msgCfm],
  data() {
    return {
      listName: ["cost", "developer", "edition", "installation_time"],
      name: "",
      listValue: ["cost", "creator", "version", "installTime"],
      data: [],
      imgDefault: require('@/assets/icon_nopicture_filled.png'),
      uploading: false,
      baseUrl: process.env.VUE_APP_BASE_API,
      fileList: [],
      numPlugin: 0,
      headers: { Authorization: getToken() },
    };
  },
  mounted() {
    this.search();
    this.bindKey();
    this.authValidate()
  },
  destroyed() {
    this.unBindKey();
  },
  methods: {
    authValidate() {
      this.numPlugin = Number(checkPermission(['plugin:uninstall'])) + Number(checkPermission(['plugin:upload']))
    },
    entryKey(event) {
      const keyCode = event.keyCode;
      if (keyCode === 13) {
        this.search();
      }
    },
    bindKey() {
      document.addEventListener("keypress", this.entryKey);
    },
    unBindKey() {
      document.removeEventListener("keypress", this.entryKey);
    },
    search() {
      const param = {};
      if (this.name) {
        param.conditions = [
          {
            field: "name",
            operator: "like",
            value: this.name,
          },
        ];
      }
      pluginLists(0, 0, param).then((response) => {
        this.data = response.data.listObject;
        this.data.forEach((ele) => {
          if (ele.cost) {
            ele.cost = ele.cost.toLocaleString();
          }
          ele.installTime = ele.installTime
            ? new Date(ele.installTime).format("yyyy-MM-dd hh:mm:ss")
            : "-";
        });
      });
    },
    beforeUpload(file) {
      this.uploading = true;
    },
    uploadFail(response, file, fileList) {
      const msg = (response && response.message) || "安装失败";
      try {
        const result = JSON.parse(msg);
        if (result && result.message) {
          this.$error(result.message);
          this.uploading = false;
        }
        return;
      } catch (e) {
        console.error(e);
      }
      this.$error(msg);
      this.uploading = false;
    },
    uploadSuccess(response, file, fileList) {
      this.uploading = false;
      this.search();
    },

    del(row) {
      if (row.pluginId < 4) return;
      const options = {
        title: "components.uninstall_the_plugin",
        content: "components.it_takes_effect",
        confirmButtonText: this.$t("components.uninstall"),
        type: "primary",
        cb: () => {
          uninstall(row.pluginId)
            .then((res) => {
              this.search();
              this.openMessageSuccess("plugin.un_install_success");
            })
            .catch(() => {
              this.$error(this.$t("plugin.un_install_error"));
            });
        },
      };
      this.handlerConfirm(options);
    },
    btnDisabled(row) {
      return row.pluginId < 4;
    },
  },
};
</script>
<style lang="scss">
.top-install {
  position: absolute;
  top: 24px;
  right: 24px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  .el-input {
    margin-right: 12px;
  }
  .el-input__inner {
    background: var(--ContentBG, #ffffff) !important;
  }
}

.plugin-cont {
  height: 100%;
  display: flex;
  flex-wrap: wrap;
  background-color: var(--MainBG, #f5f6f7);
  overflow-y: auto;
  align-content: flex-start;
}
.de-card-plugin {
  width: 270px;
  min-height: 188px;
  background: var(--ContentBG, #ffffff);
  border: 1px solid var(--deCardStrokeColor, #dee0e3);
  border-radius: 4px;
  margin: 0 24px 24px 0;
  &:hover {
    box-shadow: 0px 6px 24px rgba(31, 35, 41, 0.08);
  }
  .card-method {
    border-top: 1px solid #dee0e3;
    display: flex;
    align-items: center;
    padding: 9px 30px 10px 30px;
    width: 100%;
    justify-content: space-around;
    box-sizing: border-box;

    .btn-plugin {
      font-family: "PingFang SC";
      font-style: normal;
      font-weight: 400;
      font-size: 14px;
      line-height: 22px;
      cursor: pointer;
      /* identical to box height, or 157% */

      display: flex;
      align-items: center;
      letter-spacing: -0.1px;

      /* Neutral/600 */

      color: #646a73;
      i {
        font-size: 13px;
        margin-right: 5.33px;
      }

      &.is-disable {
        cursor: not-allowed;
        color: #bbbfc4;
      }
    }

    .btn-plugin.update:not(.is-disable):hover {
      color: var(--primaryHover, #26acff);
    }

    .btn-plugin.uninstall:not(.is-disable):hover {
      color: var(--deDangerHover, #26acff);
    }
  }

  .btn-0 {
    display: none;
  }

  .card-info {
    width: 100%;
    height: 188px;
    padding: 12px;
    padding-bottom: 4px;
    box-sizing: border-box;

    .info-top {
      margin-bottom: 12px;
      overflow: hidden;

      .el-image {
        float: left;
        box-sizing: border-box;
        width: 40px;
        height: 40px;
        background: #ffffff;
        border: 1px solid #dee0e3;
        border-radius: 4px;
        display: flex;

        .image-slot {
          padding: 4px;
        }
      }
      img {
        width: 100%;
        height: 100%;
      }

      .title {
        width: 190px;
        height: 22px;
        float: left;
        font-family: "PingFang SC";
        font-style: normal;
        font-weight: 500;
        font-size: 14px;
        line-height: 22px;
        color: #000000;
        margin: -2px 0 0 6px;
      }

      .tips {
        max-width: 200px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        float: left;
        height: 20px;
        font-family: "PingFang SC";
        font-style: normal;
        font-weight: 400;
        font-size: 12px;
        line-height: 20px;
        color: #646a73;
        margin: 2px 0 0 6px;
      }
    }
    .list {
      padding-bottom: 8px;
      font-family: "PingFang SC";
      font-style: normal;
      font-weight: 400;
      font-size: 14px;
      line-height: 22px;
      margin: 0;
      color: #646a73;
    }

    .info-left {
      display: inline-block;

      .name {
        color: #646a73;
      }
    }
    .info-right {
      display: inline-block;
      margin-left: 12px;
      .value {
        color: var(--TextPrimary, #1F2329);
      }
    }
  }
}
</style>
