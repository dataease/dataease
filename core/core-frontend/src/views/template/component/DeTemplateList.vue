<template xmlns:el-col="http://www.w3.org/1999/html">
  <div class="de-template-list">
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
        <span class="text-template-overflow" :title="ele.name">{{ ele.name }}</span>
        <span class="more" @click.stop>
          <el-dropdown trigger="click" size="small" @command="type => clickMore(type, ele)">
            <el-icon class="el-icon-more"><MoreFilled /></el-icon>
            <template #dropdown>
              <el-dropdown-menu class="de-template-dropdown">
                <el-dropdown-item command="edit">
                  <el-icon><EditPen /></el-icon>
                  {{ t('visualization.rename') }}
                </el-dropdown-item>
                <el-dropdown-item command="delete">
                  <el-icon><Delete /></el-icon>
                  {{ t('visualization.delete') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </span>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from '@/hooks/web/useI18n'
import { computed, reactive } from 'vue'
import NoneImage from '@/assets/none.png'
import NothingImage from '@/assets/nothing.png'
import { ElMessageBox } from 'element-plus-secondary'
const { t } = useI18n()

const emits = defineEmits([
  'showCurrentTemplate',
  'showTemplateEditDialog',
  'categoryDelete',
  'categoryEdit',
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
  return props.templateList.filter(ele => ele['id'] !== '1')
})

const clickMore = (type, data) => {
  switch (type) {
    case 'edit':
      categoryEdit(data)
      break
    case 'delete':
      categoryDelete(data)
      break
    case 'import':
      templateImport(data)
      break
  }
}
const nodeClick = ({ id, name }) => {
  state.activeTemplate = id
  emits('showCurrentTemplate', id, name)
}

const categoryDelete = template => {
  ElMessageBox.confirm('确定删除该分类吗？', {
    tip: '删除后不可恢复，是否继续？',
    confirmButtonType: 'danger',
    confirmButtonText: t('common.delete'),
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    emits('categoryDelete', template.id)
  })
}
const categoryEdit = template => {
  emits('categoryEdit', template)
}
const templateImport = template => {
  emits('templateImport', template.id)
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
    margin: 0px 0 0 0;
    padding: 0;
    overflow-y: auto;
    max-height: 100%;
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
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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

.custom-line {
  margin: 4px;
  background: rgba(31, 35, 41, 0.15);
  border: 0;
  height: 1px;
}
</style>
