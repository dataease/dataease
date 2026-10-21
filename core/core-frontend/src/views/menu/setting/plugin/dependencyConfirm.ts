/*
 * 同步插件驱动冲突的展示与确认，只提交用户选择，不在前端决定文件版本
 */
import { h } from 'vue'
import { ElMessageBox } from 'element-plus-secondary'
import request from '@/config/axios'

/*
 * 上传预检查响应，名称仅用于展示，operation 用于确认或取消暂存安装
 */
export interface InstallCheck {
  operation: string
  databaseName?: string
  databaseType?: string
  pluginType?: 'sync-source' | 'sync-sink'
  conflicts: {
    name: string
    currentVersion: string
    uploadedVersion: string
    locations?: { service: 'DE' | 'EXECUTOR'; version: string }[]
  }[]
}

/*
 * 同步插件上传逐项确认驱动选择，关闭弹窗取消准备中的操作
 */
export async function confirmDependencies(
  check: InstallCheck,
  t: (key: string) => string
): Promise<boolean> {
  const choices: Record<string, 'KEEP' | 'REPLACE'> = {}
  for (const conflict of check.conflicts) {
    const context: string[] = []
    const databaseName = check.databaseName?.trim() || check.databaseType
    if (databaseName) context.push(databaseName)
    if (check.pluginType === 'sync-source' || check.pluginType === 'sync-sink') {
      context.push(t(`plugin.flag-${check.pluginType}`))
    }
    const versionsByService = (['DE', 'EXECUTOR'] as const)
      .map(service => ({
        service,
        versions: [
          ...new Set(
            conflict.locations
              ?.filter(item => item.service === service)
              .map(item => item.version) || []
          )
        ]
          .sort()
          .join(' / ')
      }))
      .filter(item => item.versions)
    const distinctVersions = [...new Set(versionsByService.map(item => item.versions))]
    const current =
      distinctVersions.length > 1
        ? versionsByService
            .map(
              item =>
                `${t(item.service === 'DE' ? 'system.driver_de' : 'system.driver_executor')}: ${
                  item.versions
                }`
            )
            .join('\n')
        : distinctVersions[0] || conflict.currentVersion
    const rows = [
      ['system.driver_uploaded_file', conflict.name],
      ['system.driver_current', current],
      ['system.driver_uploaded', conflict.uploadedVersion]
    ]
    const message = h('div', { style: { lineHeight: '1.6' } }, [
      ...(context.length
        ? [
            h(
              'div',
              { style: { marginBottom: '16px', color: 'var(--el-text-color-secondary)' } },
              context.join(' · ')
            )
          ]
        : []),
      h(
        'div',
        {
          style: {
            display: 'grid',
            gridTemplateColumns: 'max-content minmax(0, 1fr)',
            gap: '8px 16px'
          }
        },
        rows.flatMap(([label, value]) => [
          h('span', { style: { color: 'var(--el-text-color-secondary)' } }, t(label)),
          h('span', { style: { whiteSpace: 'pre-line', overflowWrap: 'anywhere' } }, value)
        ])
      ),
      h('div', { style: { marginTop: '20px' } }, t('system.driver_replace_question')),
      h(
        'div',
        { style: { marginTop: '8px', color: 'var(--el-text-color-secondary)' } },
        t('system.driver_impact')
      )
    ])
    try {
      await ElMessageBox.confirm(message, t('system.driver_exists'), {
        confirmButtonText: t('system.driver_replace'),
        cancelButtonText: t('system.driver_keep'),
        distinguishCancelAndClose: true,
        closeOnClickModal: false,
        customStyle: {
          whiteSpace: 'pre-line',
          overflowWrap: 'anywhere',
          maxWidth: '90vw',
          width: '520px'
        }
      })
      choices[conflict.name] = 'REPLACE'
    } catch (action) {
      if (action === 'cancel') choices[conflict.name] = 'KEEP'
      else {
        await request.post({ url: `/plugin/cancel/${check.operation}` })
        return false
      }
    }
  }
  await request.post({ url: '/plugin/confirm', data: { operation: check.operation, choices } })
  return true
}
