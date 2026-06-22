<!-- 询盘详情页面 -->
<template>
  <div class="inquiry-detail art-full-height">
    <ElCard>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>询盘详情</span>
          <ElButton @click="router.back()">返回</ElButton>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 用户信息 -->
        <ElDescriptions title="用户信息" :column="2" border>
          <ElDescriptionsItem label="询盘编号">{{ inquiry.inquiryNo }}</ElDescriptionsItem>
          <ElDescriptionsItem label="用户名">{{ inquiry.userName }}</ElDescriptionsItem>
          <ElDescriptionsItem label="邮箱">{{ inquiry.userEmail }}</ElDescriptionsItem>
          <ElDescriptionsItem label="WhatsApp">{{ inquiry.userWhatsapp }}</ElDescriptionsItem>
          <ElDescriptionsItem label="总金额">
            <span style="color: #f56c6c; font-weight: bold">¥{{ inquiry.totalAmount }}</span>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="状态">
            <ElTag :type="statusConfig[inquiry.status]?.type">
              {{ statusConfig[inquiry.status]?.text }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="创建时间">{{ inquiry.createTime }}</ElDescriptionsItem>
          <ElDescriptionsItem label="用户备注">{{ inquiry.remark || '-' }}</ElDescriptionsItem>
        </ElDescriptions>

        <!-- 操作区域 -->
        <ElDivider />
        <ElSpace>
          <ElButton type="primary" @click="updateStatus(1)" :disabled="inquiry.status !== 0">标记已联系</ElButton>
          <ElButton type="success" @click="updateStatus(2)" :disabled="inquiry.status === 2 || inquiry.status === 3">标记已完成</ElButton>
          <ElButton type="info" @click="updateStatus(3)" :disabled="inquiry.status === 3">取消</ElButton>
        </ElSpace>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchGetInquiryDetail, fetchUpdateInquiryStatus } from '@/api/inquiry'

defineOptions({ name: 'InquiryDetail' })

const router = useRouter()
const route = useRoute()
const loading = ref(false)

const inquiry = ref<any>({
  id: 0, inquiryNo: '', userName: '', userEmail: '', userWhatsapp: '',
  totalAmount: 0, remark: '', status: 0, adminRemark: '', createTime: ''
})

const statusConfig: Record<number, { type: string; text: string }> = {
  0: { type: 'warning', text: '待处理' },
  1: { type: 'primary', text: '已联系' },
  2: { type: 'success', text: '已完成' },
  3: { type: 'info', text: '已取消' }
}

async function loadData() {
  const id = Number(route.query.id)
  if (!id) return
  loading.value = true
  try {
    const { data } = await fetchGetInquiryDetail(id)
    inquiry.value = data
  } finally {
    loading.value = false
  }
}

async function updateStatus(status: number) {
  await fetchUpdateInquiryStatus(inquiry.value.id, { status })
  ElMessage.success('状态更新成功')
  loadData()
}

onMounted(() => loadData())
</script>
