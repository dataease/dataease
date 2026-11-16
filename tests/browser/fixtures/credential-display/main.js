import { createApp, h, reactive } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import CustomPassword from '@/components/custom-password/src/CustomPassword.vue'
import { clearCopiedCredentials } from '@/utils/copiedCredentials'
const source = {
  password: '******',
  sshPassword: '******',
  sshKey: '******',
  sshKeyPassword: '******',
  passwd: '******',
  sslKey: '******',
  host: 'fixture.invalid',
  headers: [{ name: 'Authorization', value: '******' }],
  optional: '',
  absent: null
}
const mode = new URLSearchParams(location.search).get('mode') || 'edit'
const configuration = reactive(
  mode === 'copy' ? clearCopiedCredentials(source) : { ...source }
)
const types = [
  'DATASOURCE_PASSWORD',
  'DATASOURCE_SSH_PASSWORD',
  'DATASOURCE_SSH_KEY_PASSWORD',
  'DATASOURCE_REMOTE_PASSWORD',
  'ENGINE_PASSWORD',
  'SYNC_PASSWORD'
]
window.credentialFixture = { configuration, source, clearCopiedCredentials }
createApp({
  render: () =>
    h(
      'div',
      types.map((type, index) =>
        h(CustomPassword, {
          modelValue:
            mode === 'empty'
              ? undefined
              : configuration[
                  index === 3
                    ? 'passwd'
                    : index === 1
                    ? 'sshPassword'
                    : index === 2
                    ? 'sshKeyPassword'
                    : 'password'
                ],
          'onUpdate:modelValue': (value) => {
            configuration[
              index === 3
                ? 'passwd'
                : index === 1
                ? 'sshPassword'
                : index === 2
                ? 'sshKeyPassword'
                : 'password'
            ] = value
          },
          'aria-label': type,
          type: 'password',
          'show-password': true,
          revealResourceId: mode === 'edit' ? '101' : undefined,
          revealSecretType: type
        })
      )
    )
})
  .use(ElementPlus)
  .mount('#app')
