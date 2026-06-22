<!-- 系统设置 - 轮播图管理 -->
<template>
  <div class="site-banner art-full-height">
    <ElCard>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>轮播图管理</span>
          <ElSpace>
            <ElButton type="primary" @click="addBanner" v-ripple>添加轮播图</ElButton>
            <ElButton type="success" @click="handleSave" :loading="saving" v-ripple>保存</ElButton>
          </ElSpace>
        </div>
      </template>

      <div v-loading="loading">
        <ElEmpty v-if="banners.length === 0" description="暂无轮播图" />

        <div v-else style="display: flex; flex-wrap: wrap; gap: 16px">
          <ElCard v-for="(banner, index) in banners" :key="index" style="width: 320px" shadow="hover">
            <ElImage :src="banner.image" style="width: 100%; height: 160px" fit="cover" />
            <div style="margin-top: 12px">
              <ElInput v-model="banner.title" placeholder="标题(可选)" size="small" style="margin-bottom: 8px" />
              <ElInput v-model="banner.link" placeholder="链接(可选)" size="small" style="margin-bottom: 8px" />
              <div style="display: flex; justify-content: space-between; align-items: center">
                <ElUpload :http-request="(opt: any) => handleBannerUpload(opt, index)" :show-file-list="false"
                  accept="image/*">
                  <ElButton size="small" type="primary" link>更换图片</ElButton>
                </ElUpload>
                <ElButton size="small" type="danger" link @click="banners.splice(index, 1)">删除</ElButton>
              </div>
            </div>
          </ElCard>
        </div>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { fetchGetSiteConfig, fetchUpdateSiteConfig } from '@/api/site-config'
import { uploadImage } from '@/api/upload'

defineOptions({ name: 'SiteBanner' })

const loading = ref(false)
const saving = ref(false)

interface Banner {
  image: string
  title: string
  link: string
}

const banners = ref<Banner[]>([])

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchGetSiteConfig()
    if (data?.banner_images) {
      try {
        banners.value = JSON.parse(data.banner_images)
      } catch {
        banners.value = []
      }
    }
  } finally {
    loading.value = false
  }
}

function addBanner() {
  banners.value.push({ image: '', title: '', link: '' })
}

async function handleBannerUpload(options: any, index: number) {
  try {
    banners.value[index].image = await uploadImage(options.file)
    ElMessage.success('上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
}

async function handleSave() {
  saving.value = true
  try {
    await fetchUpdateSiteConfig({
      banner_images: JSON.stringify(banners.value)
    })
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(() => loadData())
</script>
