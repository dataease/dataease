<template>
  <div>
    <ms-database-from :config="currentConfig" :callback="saveConfig" ref="databaseFrom" :is-read-only="isReadOnly"/>
    <ms-database-config-list @rowSelect="rowSelect" v-if="configs.length > 0" :table-data="configs"/>
  </div>
</template>

<script>
    import MsDatabaseConfigList from "./DatabaseConfigList";
    import {DatabaseConfig} from "../../../model/ScenarioModel";
    import MsDatabaseFrom from "./DatabaseFrom";
    import {getUUID} from "@/common/js/utils";

    export default {
      name: "MsDatabaseConfig",
      components: {MsDatabaseFrom, MsDatabaseConfigList},
      props: {
        configs: Array,
        isReadOnly: {
          type: Boolean,
          default: false
        },
      },
      data() {
        return {
          drivers: DatabaseConfig.DRIVER_CLASS,
          currentConfig: new DatabaseConfig()
        }
      },
      watch: {
        configs() {
          this.currentConfig = new DatabaseConfig();
        }
      },
      methods: {
        saveConfig(config) {
          for (let item of this.configs) {
            if (item.name === config.name && item.id !== config.id) {
              this.$warning(this.$t('commons.already_exists'));
              return;
            }
          }
          if (config.id) {
            this.updateConfig(config);
          } else {
            this.addConfig(config);
          }
        },
        addConfig(config) {
          config.id = getUUID();
          let item = {};
          Object.assign(item, config);
          this.configs.push(item);
          this.currentConfig = new DatabaseConfig();
        },
        updateConfig(config) {
          Object.assign(this.currentConfig, config);
          this.currentConfig = new DatabaseConfig();
        },
        rowSelect(config) {
          //防止清除后再点击该行无响应
          this.currentConfig = {};
          this.$nextTick(() => {
            this.currentConfig = config;
          });
        }
      }
    }
</script>

<style scoped>

  .addButton {
    float: right;
  }

  .database-from {
    padding: 10px;
    border: #DCDFE6 solid 1px;
    margin: 5px 0;
    border-radius: 5px;
  }

</style>
