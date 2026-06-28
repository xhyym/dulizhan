<!-- 系统设置 - Footer 设置 -->
<template>
  <div class="site-footer art-full-height">
    <ElCard>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>Footer 设置</span>
          <ElButton type="primary" @click="handleSave" :loading="saving" v-ripple>保存</ElButton>
        </div>
      </template>

      <ElForm :model="formData" label-width="120px" style="max-width: 700px" v-loading="loading">
        <ElFormItem label="版权信息">
          <ElInput v-model="formData.copyright" placeholder="© 2026 Indie Station. All rights reserved." />
        </ElFormItem>

        <ElFormItem label="底部链接">
          <div style="width: 100%">
            <ElButton size="small" @click="addLink" style="margin-bottom: 8px">添加链接</ElButton>
            <ElTable :data="formData.links" v-if="formData.links.length" border size="small">
              <ElTableColumn label="链接名称" min-width="150">
                <template #default="{ row }">
                  <ElInput v-model="row.name" size="small" placeholder="如: About Us" />
                </template>
              </ElTableColumn>
              <ElTableColumn label="链接地址" min-width="250">
                <template #default="{ row }">
                  <ElInput v-model="row.url" size="small" placeholder="https://..." />
                </template>
              </ElTableColumn>
              <ElTableColumn label="操作" width="80">
                <template #default="{ $index }">
                  <ElButton type="danger" link size="small" @click="formData.links.splice($index, 1)">删除</ElButton>
                </template>
              </ElTableColumn>
            </ElTable>
          </div>
        </ElFormItem>

        <ElFormItem label="社交媒体">
          <div style="width: 100%">
            <ElInput v-model="formData.social_facebook" placeholder="Facebook 链接" style="margin-bottom: 8px">
              <template #prepend>Facebook</template>
            </ElInput>
            <ElInput v-model="formData.social_instagram" placeholder="Instagram 链接" style="margin-bottom: 8px">
              <template #prepend>Instagram</template>
            </ElInput>
            <ElInput v-model="formData.social_twitter" placeholder="Twitter/X 链接" style="margin-bottom: 8px">
              <template #prepend>Twitter</template>
            </ElInput>
            <ElInput v-model="formData.social_youtube" placeholder="YouTube 链接">
              <template #prepend>YouTube</template>
            </ElInput>
          </div>
        </ElFormItem>
      </ElForm>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { fetchGetSiteConfig, fetchUpdateSiteConfig } from '@/api/site-config'

defineOptions({ name: 'SiteFooter' })

const loading = ref(false)
const saving = ref(false)

const formData = ref({
  copyright: '',
  links: [] as { name: string; url: string }[],
  social_facebook: '',
  social_instagram: '',
  social_twitter: '',
  social_youtube: ''
})

async function loadData() {
  loading.value = true
  try {
    const data = await fetchGetSiteConfig()
    if (data) {
      if (data.footer_info) {
        try {
          const footer = JSON.parse(data.footer_info)
          formData.value.copyright = footer.copyright || ''
          formData.value.links = footer.links || []
        } catch { }
      }
      if (data.social_links) {
        try {
          const social = JSON.parse(data.social_links)
          formData.value.social_facebook = social.facebook || ''
          formData.value.social_instagram = social.instagram || ''
          formData.value.social_twitter = social.twitter || ''
          formData.value.social_youtube = social.youtube || ''
        } catch { }
      }
    }
  } finally {
    loading.value = false
  }
}

function addLink() {
  formData.value.links.push({ name: '', url: '' })
}

async function handleSave() {
  saving.value = true
  try {
    const footerInfo = JSON.stringify({
      copyright: formData.value.copyright,
      links: formData.value.links
    })
    const socialLinks = JSON.stringify({
      facebook: formData.value.social_facebook,
      instagram: formData.value.social_instagram,
      twitter: formData.value.social_twitter,
      youtube: formData.value.social_youtube
    })
    await fetchUpdateSiteConfig({ footer_info: footerInfo, social_links: socialLinks })
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.site-footer {
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
