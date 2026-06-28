<!-- 系统设置 - 邮件配置 -->
<template>
  <div class="site-email art-full-height">
    <ElCard>
      <template #header>
        <div class="card-header">
          <span>邮件配置</span>
          <ElSpace>
            <ElButton @click="handleTest" :loading="testing" :disabled="loading" v-ripple>发送测试邮件</ElButton>
            <ElButton type="primary" @click="handleSave" :loading="saving" :disabled="loading" v-ripple>
              保存
            </ElButton>
          </ElSpace>
        </div>
      </template>

      <ElForm
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="email-form"
        v-loading="loading"
      >
        <ElDivider content-position="left">SMTP 设置</ElDivider>

        <ElFormItem label="服务商预设">
          <ElRadioGroup v-model="selectedProvider" size="small" @change="handleProviderChange">
            <ElRadioButton label="qq">QQ邮箱</ElRadioButton>
            <ElRadioButton label="163">网易163</ElRadioButton>
            <ElRadioButton label="gmail">Gmail</ElRadioButton>
            <ElRadioButton label="custom">自定义</ElRadioButton>
          </ElRadioGroup>
          <div class="form-tip">
            预设仅用于快速填充 SMTP 服务器、端口和加密方式，你仍然可以继续手动修改，适配更多邮箱服务商。
          </div>
        </ElFormItem>

        <ElFormItem label="SMTP 服务器" prop="smtp_host">
          <ElInput v-model="formData.smtp_host" placeholder="如: smtp.gmail.com" />
        </ElFormItem>

        <ElFormItem label="SMTP 端口" prop="smtp_port">
          <ElInputNumber v-model="formData.smtp_port" :min="1" :max="65535" class="port-input" />
          <span class="field-tip">常用端口：25、465、587</span>
        </ElFormItem>

        <ElFormItem label="加密方式" prop="smtp_encryption">
          <ElRadioGroup v-model="formData.smtp_encryption">
            <ElRadio value="none">无</ElRadio>
            <ElRadio value="ssl">SSL</ElRadio>
            <ElRadio value="tls">TLS</ElRadio>
          </ElRadioGroup>
        </ElFormItem>

        <ElFormItem label="用户名" prop="smtp_username">
          <ElInput v-model="formData.smtp_username" placeholder="SMTP 登录用户名，一般为完整邮箱地址" />
        </ElFormItem>

        <ElFormItem label="密码 / 授权码" prop="smtp_password">
          <ElInput
            v-model="formData.smtp_password"
            type="password"
            show-password
            placeholder="请输入 SMTP 密码或授权码"
          />
          <div class="form-tip">
            QQ、网易通常填写 SMTP 授权码；Gmail 建议填写 App Password，不建议直接使用邮箱登录密码。
          </div>
        </ElFormItem>

        <ElDivider content-position="left">发件人设置</ElDivider>

        <ElFormItem label="发件人名称">
          <ElInput v-model="formData.from_name" placeholder="如: OSEN FURNITURE" />
        </ElFormItem>

        <ElFormItem label="发件人邮箱" prop="from_email">
          <ElInput v-model="formData.from_email" placeholder="如: noreply@example.com" />
          <div class="form-tip">建议与认证邮箱保持一致，避免被服务商判定为代发而拒绝发送。</div>
        </ElFormItem>

        <ElDivider content-position="left">测试邮件</ElDivider>

        <ElFormItem label="测试邮箱">
          <ElInput v-model="testEmail" placeholder="输入接收测试邮件的邮箱地址" class="test-email-input" />
          <div class="form-tip">
            发送测试邮件前会自动保存当前配置，用于验证 SMTP 连接、认证信息以及发件人配置是否正确。
          </div>
        </ElFormItem>
      </ElForm>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { fetchGetSiteConfig, fetchUpdateSiteConfig } from '@/api/site-config'
import request from '@/utils/http'

defineOptions({ name: 'SiteEmail' })

type EncryptionMode = 'none' | 'ssl' | 'tls'
type ProviderKey = 'qq' | '163' | 'gmail' | 'custom'

interface EmailConfigForm {
  smtp_host: string
  smtp_port: number
  smtp_encryption: EncryptionMode
  smtp_username: string
  smtp_password: string
  from_name: string
  from_email: string
}

interface EmailConfigPayload {
  smtpHost: string
  smtpPort: number
  smtpEncryption: EncryptionMode
  smtpUsername: string
  smtpPassword: string
  fromName: string
  fromEmail: string
}

interface ProviderPreset {
  key: Exclude<ProviderKey, 'custom'>
  smtp_host: string
  smtp_port: number
  smtp_encryption: EncryptionMode
}

const EMAIL_REGEX = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/

const PROVIDER_PRESETS: ProviderPreset[] = [
  { key: 'qq', smtp_host: 'smtp.qq.com', smtp_port: 465, smtp_encryption: 'ssl' },
  { key: '163', smtp_host: 'smtp.163.com', smtp_port: 465, smtp_encryption: 'ssl' },
  { key: 'gmail', smtp_host: 'smtp.gmail.com', smtp_port: 587, smtp_encryption: 'tls' }
]

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const testEmail = ref('')
const selectedProvider = ref<ProviderKey>('custom')

const formData = ref<EmailConfigForm>({
  smtp_host: '',
  smtp_port: 587,
  smtp_encryption: 'tls',
  smtp_username: '',
  smtp_password: '',
  from_name: '',
  from_email: ''
})

const formRules: FormRules<EmailConfigForm> = {
  smtp_host: [{ required: true, message: '请输入 SMTP 服务器地址', trigger: 'blur' }],
  smtp_port: [{ required: true, message: '请输入 SMTP 端口', trigger: 'change' }],
  smtp_encryption: [{ required: true, message: '请选择加密方式', trigger: 'change' }],
  smtp_username: [{ required: true, message: '请输入 SMTP 登录用户名', trigger: 'blur' }],
  smtp_password: [{ required: true, message: '请输入 SMTP 密码或授权码', trigger: 'blur' }],
  from_email: [
    {
      validator: (_rule, value: string, callback) => {
        if (!value) {
          callback()
          return
        }
        if (!EMAIL_REGEX.test(value)) {
          callback(new Error('发件人邮箱格式不正确'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

async function loadData() {
  loading.value = true
  try {
    const data = await fetchGetSiteConfig()
    if (data?.email_config) {
      try {
        const config = JSON.parse(data.email_config) as Partial<EmailConfigForm & EmailConfigPayload>
        Object.assign(formData.value, {
          smtp_host: config.smtp_host ?? config.smtpHost ?? '',
          smtp_port: config.smtp_port ?? config.smtpPort ?? 587,
          smtp_encryption: config.smtp_encryption ?? config.smtpEncryption ?? 'tls',
          smtp_username: config.smtp_username ?? config.smtpUsername ?? '',
          smtp_password: config.smtp_password ?? config.smtpPassword ?? '',
          from_name: config.from_name ?? config.fromName ?? '',
          from_email: config.from_email ?? config.fromEmail ?? ''
        })
      } catch {
        ElMessage.warning('邮件配置解析失败，已使用默认值')
      }
    }
    selectedProvider.value = detectProvider(formData.value)
  } finally {
    loading.value = false
  }
}

function detectProvider(config: EmailConfigForm): ProviderKey {
  const matchedProvider = PROVIDER_PRESETS.find((provider) => {
    return provider.smtp_host === config.smtp_host
      && provider.smtp_port === config.smtp_port
      && provider.smtp_encryption === config.smtp_encryption
  })

  return matchedProvider?.key ?? 'custom'
}

function isProviderKey(value: string | number | boolean | undefined): value is ProviderKey {
  return value === 'qq' || value === '163' || value === 'gmail' || value === 'custom'
}

function handleProviderChange(providerValue: string | number | boolean | undefined) {
  if (!isProviderKey(providerValue) || providerValue === 'custom') return

  const preset = PROVIDER_PRESETS.find((item) => item.key === providerValue)
  if (!preset) return

  formData.value.smtp_host = preset.smtp_host
  formData.value.smtp_port = preset.smtp_port
  formData.value.smtp_encryption = preset.smtp_encryption
}

function buildConfigPayload() {
  return {
    email_config: JSON.stringify({
      smtpHost: formData.value.smtp_host,
      smtpPort: formData.value.smtp_port,
      smtpEncryption: formData.value.smtp_encryption,
      smtpUsername: formData.value.smtp_username,
      smtpPassword: formData.value.smtp_password,
      fromName: formData.value.from_name,
      fromEmail: formData.value.from_email
    } satisfies EmailConfigPayload)
  }
}

async function validateConfigForm() {
  if (!formRef.value) return false
  await formRef.value.validate()
  return true
}

async function persistConfig(showSuccessMessage: boolean) {
  await validateConfigForm()
  await fetchUpdateSiteConfig(buildConfigPayload())
  if (showSuccessMessage) {
    ElMessage.success('邮件配置保存成功')
  }
}

async function handleSave() {
  saving.value = true
  try {
    await persistConfig(true)
  } finally {
    saving.value = false
  }
}

async function handleTest() {
  if (!EMAIL_REGEX.test(testEmail.value.trim())) {
    ElMessage.warning('请输入格式正确的测试邮箱地址')
    return
  }

  testing.value = true
  try {
    await persistConfig(false)
    const message = await request.post<string>({
      url: '/api/admin/site-config/test-email',
      data: { email: testEmail.value.trim() }
    })
    ElMessage.success(message || '测试邮件已发送，请检查收件箱')
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : '测试邮件发送失败'
    ElMessage.error(errorMessage)
  } finally {
    testing.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.site-email {
  :deep(.el-card) {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }

  :deep(.el-card__body) {
    flex: 1;
    overflow-y: auto;
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.email-form {
  max-width: 700px;
}

.port-input {
  width: 200px;
}

.test-email-input {
  width: 320px;
}

.field-tip {
  margin-left: 12px;
  color: #909399;
}

.form-tip {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.6;
  color: #909399;
}
</style>
