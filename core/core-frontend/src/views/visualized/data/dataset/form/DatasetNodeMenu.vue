<script setup lang="ts">
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_rename_outlined from '@/assets/svg/icon_rename_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_textBox_outlined from '@/assets/svg/icon_text-box_outlined.svg'
import icon_moreVertical_outlined from '@/assets/svg/icon_more-vertical_outlined.svg'
import { HandleMore } from '@/components/handle-more'
import { useI18n } from '@/hooks/web/useI18n'
import type { Node } from './util'

defineProps<{ nodeType: Node['type'] }>()
const emit = defineEmits<{ command: [value: string] }>()
const { t } = useI18n()
const menuList = [
  {
    svgName: icon_textBox_outlined,
    label: t('data_set.field_selection'),
    command: 'editorField'
  },
  {
    svgName: icon_deleteTrash_outlined,
    label: t('data_set.delete'),
    command: 'del'
  }
]

const sqlMenu = [
  {
    svgName: icon_edit_outlined,
    label: t('data_set.edit_sql'),
    command: 'editorSql'
  },
  {
    svgName: icon_rename_outlined,
    label: t('datasource.field_rename'),
    command: 'rename'
  }
]
</script>

<template>
  <handle-more
    :icon-name="icon_moreVertical_outlined"
    :menu-list="nodeType === 'sql' ? [...sqlMenu, ...menuList] : menuList"
    @handle-command="command => emit('command', command)"
  />
</template>
