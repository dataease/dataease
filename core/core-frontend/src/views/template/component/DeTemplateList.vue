<template xmlns:el-col="http://www.w3.org/1999/html">
  <div class="de-template-list">
    <el-input
      v-model="state.templateFilterText"
      :placeholder="'搜索关键字'"
      size="small"
      class="de-input-search"
      clearable
    >
      <template #prefix>
        <el-icon>
          <Icon name="de-search" />
        </el-icon>
      </template>
    </el-input>
    <el-empty
      v-if="!templateListComputed.length && state.templateFilterText === ''"
      :image="NoneImage"
      :description="'当前无分类'"
    />
    <el-empty
      v-if="!templateListComputed.length && state.templateFilterText !== ''"
      :image="NothingImage"
      :description="'没有找到相关内容'"
    />
    <ul>
      <li
        v-for="ele in templateListComputed"
        :key="ele.name"
        :class="[{ select: state.activeTemplate === ele.id }]"
        @click="nodeClick(ele)"
      >
        <el-icon class="de-icon-sense">
          <Icon name="scene" />
        </el-icon>
        <span class="text-template-overflow" :title="ele.name">{{ ele.name }}</span>
        <span class="more" @click.stop>
          <el-dropdown trigger="click" size="small" @command="type => clickMore(type, ele)">
            <span class="el-dropdown-link">
              <i class="el-icon-more" />
            </span>
            <template #dropdown>
              <el-dropdown-menu class="de-template-dropdown">
                <el-dropdown-item icon="el-icon-upload2" command="import">
                  {{ t('visualization.import') }}
                </el-dropdown-item>
                <el-dropdown-item icon="el-icon-edit" command="edit">
                  {{ t('visualization.rename') }}
                </el-dropdown-item>
                <el-dropdown-item icon="el-icon-delete" command="delete">
                  {{ t('visualization.delete') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </span>
      </li>
    </ul>
    <el-button
      v-if="state.templateFilterText === ''"
      style="width: 100%"
      icon="el-icon-plus"
      secondary
      @click="add()"
    >
      {{ t('visualization.add_category') }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from '@/hooks/web/useI18n'
import { computed, reactive } from 'vue'
import NoneImage from '@/assets/none.png'
import NothingImage from '@/assets/nothing.png'
const { t } = useI18n()

const emits = defineEmits([
  'showCurrentTemplate',
  'showTemplateEditDialog',
  'templateDelete',
  'templateEdit',
  'templateImport'
])

const props = defineProps({
  templateType: {
    type: String,
    default: ''
  },
  templateList: {
    type: Array,
    default: function () {
      return []
    }
  }
})

const state = reactive({
  templateFilterText: '',
  activeTemplate: '',
  noneImg: '@/assets/none.png',
  nothingImg: '@/assets/nothing.png'
})

const templateListComputed = computed(() => {
  if (!state.templateFilterText) return [...props.templateList]
  return props.templateList.filter(ele => ele['name']?.includes(state.templateFilterText))
})

const clickMore = (type, data) => {
  switch (type) {
    case 'edit':
      templateEdit(data)
      break
    case 'delete':
      templateDelete(data)
      break
    case 'import':
      templateImport(data)
      break
  }
}
const nodeClick = ({ id, label }) => {
  state.activeTemplate = id
  emits('showCurrentTemplate', id, label)
}
const add = () => {
  emits('showTemplateEditDialog', 'new')
}
const templateDelete = template => {
  const options = {
    title: 'system_parameter_setting.delete_this_category',
    content: 'system_parameter_setting.also_be_deleted',
    type: 'primary',
    cb: () => emits('templateDelete', template.id)
  }
  handlerConfirm(options)
}
const templateEdit = template => {
  emits('templateEdit', template)
}
const templateImport = template => {
  emits('templateImport', template.id)
}

const handlerConfirm = options => {
  // do handlerConfirm
}

defineExpose({
  nodeClick
})
</script>

<style scoped lang="less">
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
    font-family: 'PingFang SC';
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

.sense {
  width: 20px;
  height: 20px;
}
</style>
