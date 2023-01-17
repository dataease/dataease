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
        <svg-icon
          icon-class="scene"
          class="de-icon-sense"
        />
        <span
          class="text-template-overflow"
          :title="ele.name"
        >{{ ele.name }}</span>
        <span
          class="more"
          @click.stop
        >
          <el-dropdown
            trigger="click"
            size="small"
            @command="(type) => clickMore(type, ele)"
          >
            <span class="el-dropdown-link">
              <i class="el-icon-more"/>
            </span>
            <el-dropdown-menu
              slot="dropdown"
              class="de-template-dropdown"
            >
              <el-dropdown-item
                icon="el-icon-upload2"
                command="import"
              >
                {{ $t('panel.import') }}
              </el-dropdown-item>
              <el-dropdown-item
                icon="el-icon-edit"
                command="edit"
              >
                {{ $t('panel.rename') }}
              </el-dropdown-item>
              <el-dropdown-item
                icon="el-icon-delete"
                command="delete"
              >
                {{ $t('panel.delete') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </span>
      </li>
    </ul>
    <deBtn
      v-if="templateFilterText === ''"
      style="width: 100%"
      icon="el-icon-plus"
      secondary
      @click="add()"
    >
      {{ $t('panel.add_category') }}
    </deBtn>
  </div>
</template>

<script>
import msgCfm from '@/components/msgCfm/index'

export default {
  name: 'TemplateList',
  components: {},
  mixins: [msgCfm],
  props: {
    templateType: {
      type: String,
      default: ''
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
      // if (!this.templateFilterText)
      //   return [
      //     ...this.templateList,
      //     ...this.templateList,
      //     ...this.templateList,
      //     ...this.templateList,
      //   ];
      if (!this.templateFilterText) return [...this.templateList]
      return this.templateList.filter((ele) =>
        ele.name.includes(this.templateFilterText)
      )
    }
  },
  methods: {
    clickMore(type, data) {
      switch (type) {
        case 'edit':
          this.templateEdit(data)
          break
        case 'delete':
          this.templateDelete(data)
          break
        case 'import':
          this.templateImport(data)
          break
      }
    },
    nodeClick({ id, label }) {
      this.activeTemplate = id
      this.$emit('showCurrentTemplate', id, label)
    },
    add() {
      this.$emit('showTemplateEditDialog', 'new')
    },
    templateDelete(template) {
      const options = {
        title: 'system_parameter_setting.delete_this_category',
        content: 'system_parameter_setting.also_be_deleted',
        type: 'primary',
        cb: () => this.$emit('templateDelete', template.id)
      }
      this.handlerConfirm(options)
    },
    templateEdit(template) {
      this.$emit('templateEdit', template)
    },
    templateImport(template) {
      this.$emit('templateImport', template.id)
    }
  }
}
</script>

<style scoped lang="scss">
.de-template-list {
  height: 100%;
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

    .text-template-overflow {
      display: inline-block;
      max-width: 87%;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }

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
    background: var(--deWhiteHover, #e0eaff) !important;
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
</style>
