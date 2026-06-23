<!-- 系统设置 - 邮件配置 -->
<template>
  <div class="site-email art-full-height">
    <ElCard>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>邮件配置</span>
          <ElSpace>
            <ElButton @click="handleTest" :loading="testing" v-ripple>发送测试邮件</ElButton>
            <ElButton type="primary" @click="handleSave" :loading="saving" v-ripple>保存</ElButton>
          </ElSpace>
        </div>
      </template>

      <ElForm :model="formData" label-width="120px" style="max-width: 700px" v-loading="loading">
        <ElDivider content-position="left">SMTP 设置</ElDivider>

        <ElFormItem label="SMTP 服务器">
          <ElInput v-model="formData.smtp_host" placeholder="如: smtp.gmail.com" />
        </ElFormItem>
        <ElFormItem label="SMTP 端口">
          <ElInputNumber v-model="formData.smtp_port" :min="1" :max="65535" style="width: 200px" />
          <span style="margin-left: 12px; color: #909399">常用端口: 25, 465, 587</span>
        </ElFormItem>
        <ElFormItem label="加密方式">
          <ElRadioGroup v-model="formData.smtp_encryption">
            <ElRadio value="none">无</ElRadio>
            <ElRadio value="ssl">SSL</ElRadio>
            <ElRadio value="tls">TLS</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="用户名">
          <ElInput v-model="formData.smtp_username" placeholder="SMTP 登录用户名" />
        </ElFormItem>
        <ElFormItem label="密码">
          <ElInput v-model="formData.smtp_password" type="password" show-password placeholder="SMTP 登录密码" />
        </ElFormItem>

        <ElDivider content-position="left">发件人设置</ElDivider>

        <ElFormItem label="发件人名称">
          <ElInput v-model="formData.from_name" placeholder="如: Indie Station" />
        </ElFormItem>
        <ElFormItem label="发件人邮箱">
          <ElInput v-model="formData.from_email" placeholder="如: noreply@example.com" />
        </ElFormItem>

        <ElDivider content-position="left">测试邮件</ElDivider>

        <ElFormItem label="测试邮箱">
          <ElInput v-model="testEmail" placeholder="输入接收测试邮件的邮箱地址" style="width: 300px" />
        </ElFormItem>
      </ElForm>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { fetchGetSiteConfig, fetchUpdateSiteConfig } from '@/api/site-config'
import request from '@/utils/http'

defineOptions({ name: 'SiteEmail' })

const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const testEmail = ref('')

const formData = ref({
  smtp_host: '',
  smtp_port: 587,
  smtp_encryption: 'tls',
  smtp_username: '',
  smtp_password: '',
  from_name: '',
  from_email: ''
})

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchGetSiteConfig()
    if (data?.email_config) {
      try {
        const config = JSON.parse(data.email_config)
        Object.assign(formData.value, config)
      } catch { }
    }
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    await fetchUpdateSiteConfig({
      email_config: JSON.stringify(formData.value)
    })
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

async function handleTest() {
  if (!testEmail.value) {
    ElMessage.warning('请输入测试邮箱地址')
    return
  }
  testing.value = true
  try {
    await request.post({
      url: '/api/admin/site-config/test-email',
      data: { email: testEmail.value }
    })
    ElMessage.success('测试邮件已发送，请检查邮箱')
  } catch (e: any) {
    ElMessage.error(e.message || '发送失败')
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
</style>
