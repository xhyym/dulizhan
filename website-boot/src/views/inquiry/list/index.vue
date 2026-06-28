<!-- 询盘列表页面 -->
<template>
  <div class="inquiry-page art-full-height">
    <!-- 搜索栏 -->
    <ElCard class="art-search-card">
      <ElForm :model="searchForm" inline class="search-form-compact">
        <ElFormItem label="询盘编号">
          <ElInput v-model="searchForm.inquiryNo" placeholder="请输入询盘编号" clearable style="width: 180px"
            @keyup.enter="loadData" />
        </ElFormItem>
        <ElFormItem label="用户名">
          <ElInput v-model="searchForm.userName" placeholder="请输入用户名" clearable style="width: 150px"
            @keyup.enter="loadData" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <ElOption label="待处理" :value="0" />
            <ElOption label="已联系" :value="1" />
            <ElOption label="已完成" :value="2" />
            <ElOption label="已取消" :value="3" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="loadData" v-ripple>搜索</ElButton>
          <ElButton @click="resetSearch" v-ripple>重置</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- 表格 -->
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="loadData" />

      <ArtTable
        :loading="loading"
        :data="tableData"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <ElDialog v-model="detailVisible" title="询盘详情" width="980px">
      <div v-if="currentInquiry" class="inquiry-detail" v-loading="detailLoading">
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="询盘编号">{{ currentInquiry.inquiryNo }}</ElDescriptionsItem>
          <ElDescriptionsItem label="状态">
            <ElTag :type="statusConfig[currentInquiry.status]?.type ?? 'info'">
              {{ statusConfig[currentInquiry.status]?.text ?? '未知' }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="客户名称">{{ currentInquiry.userName || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="客户邮箱">{{ currentInquiry.userEmail || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="WhatsApp">{{ currentInquiry.userWhatsapp || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="总金额">¥{{ currentInquiry.totalAmount }}</ElDescriptionsItem>
          <ElDescriptionsItem label="创建时间">{{ currentInquiry.createTime }}</ElDescriptionsItem>
          <ElDescriptionsItem label="更新时间">{{ currentInquiry.updateTime }}</ElDescriptionsItem>
          <ElDescriptionsItem label="客户备注" :span="2">{{ currentInquiry.remark || '-' }}</ElDescriptionsItem>
        </ElDescriptions>

        <div class="detail-section">
          <div class="section-title">商品明细</div>
          <ElTable :data="currentInquiry.items || []" border size="small">
            <ElTableColumn prop="productName" label="商品名称" min-width="220" show-overflow-tooltip />
            <ElTableColumn prop="skuSpec" label="规格" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">{{ row.skuSpec || '-' }}</template>
            </ElTableColumn>
            <ElTableColumn prop="price" label="单价" width="120" align="right">
              <template #default="{ row }">¥{{ row.price }}</template>
            </ElTableColumn>
            <ElTableColumn prop="quantity" label="数量" width="90" align="center" />
            <ElTableColumn label="小计" width="140" align="right">
              <template #default="{ row }">¥{{ Number(row.price) * Number(row.quantity) }}</template>
            </ElTableColumn>
          </ElTable>
        </div>

        <div class="detail-section">
          <div class="section-title">管理员备注</div>
          <ElInput
            v-model="remarkForm.adminRemark"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入管理员备注"
          />
        </div>
      </div>

      <template #footer>
        <ElButton @click="detailVisible = false">关闭</ElButton>
        <ElButton
          v-if="currentInquiry"
          type="primary"
          plain
          :loading="actionLoading"
          @click="handleSaveRemark"
        >
          保存备注
        </ElButton>
        <ElButton
          v-if="currentInquiry"
          type="success"
          plain
          @click="handleGeneratePdf(currentInquiry)"
        >
          生成下发单
        </ElButton>
        <ElButton
          v-if="currentInquiry && currentInquiry.status === 0"
          type="primary"
          :loading="actionLoading"
          @click="handleStatusUpdate(1)"
        >
          标记已联系
        </ElButton>
        <ElButton
          v-if="currentInquiry && currentInquiry.status === 1"
          type="success"
          :loading="actionLoading"
          @click="handleStatusUpdate(2)"
        >
          标记已完成
        </ElButton>
        <ElButton
          v-if="currentInquiry && (currentInquiry.status === 0 || currentInquiry.status === 1)"
          type="danger"
          :loading="actionLoading"
          @click="handleStatusUpdate(3)"
        >
          取消询盘
        </ElButton>
      </template>
    </ElDialog>

    <ElDialog v-model="remarkDialogVisible" title="修改备注" width="560px">
      <ElInput
        v-model="remarkForm.adminRemark"
        type="textarea"
        :rows="5"
        maxlength="500"
        show-word-limit
        placeholder="请输入管理员备注"
      />

      <template #footer>
        <ElButton @click="remarkDialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="actionLoading" @click="handleSaveRemark">保存备注</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { h } from 'vue'
import { ElMessageBox, ElMessage, ElTag } from 'element-plus'
import { useTableColumns } from '@/hooks/core/useTableColumns'
import { fetchGetInquiryList, fetchGetInquiryDetail, fetchUpdateInquiryStatus, fetchGenerateInquiryPdf } from '@/api/inquiry'
import FileSaver from 'file-saver'
import { useRouter } from 'vue-router'

defineOptions({ name: 'InquiryList' })

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const loading = ref(false)
const detailLoading = ref(false)
const actionLoading = ref(false)
const tableData = ref<Api.Inquiry.Inquiry[]>([])
const detailVisible = ref(false)
const remarkDialogVisible = ref(false)
const currentInquiry = ref<Api.Inquiry.Inquiry | null>(null)
const remarkForm = ref({ adminRemark: '' })
const router = useRouter()

const searchForm = ref({ inquiryNo: '', userName: '', status: undefined as number | undefined })
const pagination = ref({ current: 1, size: 10, total: 0 })

const statusConfig: Record<number, { type: TagType; text: string }> = {
  0: { type: 'warning', text: '待处理' },
  1: { type: 'primary', text: '已联系' },
  2: { type: 'success', text: '已完成' },
  3: { type: 'info', text: '已取消' }
}

// 列配置
const { columns, columnChecks } = useTableColumns(() => [
  { prop: 'inquiryNo', label: '询盘编号', width: 180 },
  {
    prop: 'userName',
    label: '客户名称',
    width: 120,
    formatter: (row: Api.Inquiry.Inquiry) =>
      h('button', {
        class: 'el-button el-button--primary is-link',
        onClick: () => openCustomerDetail(row.userId)
      }, row.userName || '-')
  },
  { prop: 'userEmail', label: '邮箱', minWidth: 180, showOverflowTooltip: true },
  { prop: 'userWhatsapp', label: 'WhatsApp', width: 140 },
  {
    prop: 'totalQuantity',
    label: '商品总数量',
    width: 110,
    align: 'center',
    formatter: (row: Api.Inquiry.Inquiry) => row.totalQuantity ?? 0
  },
  {
    prop: 'totalAmount',
    label: '总金额',
    width: 100,
    align: 'right',
    formatter: (row: any) => `¥${row.totalAmount}`
  },
  {
    prop: 'remark',
    label: '客户备注',
    minWidth: 180,
    showOverflowTooltip: true,
    formatter: (row: Api.Inquiry.Inquiry) => row.remark?.trim() || '-'
  },
  {
    prop: 'adminRemark',
    label: '管理员备注',
    minWidth: 180,
    showOverflowTooltip: true,
    formatter: (row: Api.Inquiry.Inquiry) => row.adminRemark?.trim() || '-'
  },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    align: 'center',
    formatter: (row: any) => {
      const cfg = statusConfig[row.status]
      return h(ElTag, { type: cfg?.type ?? 'info' }, () => cfg?.text || '未知')
    }
  },
  { prop: 'createTime', label: '创建时间', width: 180 },
  {
    prop: 'operation',
    label: '操作',
    width: 360,
    fixed: 'right',
    formatter: (row: any) => {
      const buttons = []

      buttons.push(
        h('button', {
          class: 'el-button el-button--primary is-link',
          onClick: () => showDetail(row.id)
        }, '查看详情')
      )

      buttons.push(
        h('button', {
          class: 'el-button el-button--warning is-link',
          onClick: () => openRemarkDialog(row)
        }, '修改备注')
      )

      if (row.status === 0) {
        buttons.push(
          h('button', {
            class: 'el-button el-button--primary is-link',
            onClick: () => handleQuickStatusUpdate(row, 1)
          }, '标记已联系')
        )
      }

      if (row.status === 1) {
        buttons.push(
          h('button', {
            class: 'el-button el-button--success is-link',
            onClick: () => handleQuickStatusUpdate(row, 2)
          }, '标记已完成')
        )
      }

      if (row.status === 0 || row.status === 1) {
        buttons.push(
          h('button', {
            class: 'el-button el-button--danger is-link',
            onClick: () => handleQuickStatusUpdate(row, 3)
          }, '取消')
        )
      }

      return h('div', buttons)
    }
  }
])

async function loadData() {
  loading.value = true
  try {
    const data = await fetchGetInquiryList({
      current: pagination.value.current,
      size: pagination.value.size,
      ...searchForm.value
    })
    tableData.value = data.records || []
    pagination.value.total = data.total
  } finally {
    loading.value = false
  }
}

function handleSizeChange(size: number) {
  pagination.value.size = size
  pagination.value.current = 1
  loadData()
}

function handleCurrentChange(current: number) {
  pagination.value.current = current
  loadData()
}

function resetSearch() {
  searchForm.value = { inquiryNo: '', userName: '', status: undefined }
  pagination.value.current = 1
  loadData()
}

async function showDetail(id: number) {
  detailLoading.value = true
  try {
    const data = await fetchGetInquiryDetail(id)
    currentInquiry.value = data
    remarkForm.value.adminRemark = data.adminRemark || ''
    detailVisible.value = true
  } finally {
    detailLoading.value = false
  }
}

function openRemarkDialog(row: Api.Inquiry.Inquiry) {
  currentInquiry.value = row
  remarkForm.value.adminRemark = row.adminRemark || ''
  remarkDialogVisible.value = true
}

async function handleSaveRemark() {
  if (!currentInquiry.value) return

  actionLoading.value = true
  try {
    await fetchUpdateInquiryStatus(currentInquiry.value.id, {
      status: currentInquiry.value.status,
      adminRemark: remarkForm.value.adminRemark.trim() || undefined
    })
    ElMessage.success('备注保存成功')
    remarkDialogVisible.value = false
    if (detailVisible.value) {
      await showDetail(currentInquiry.value.id)
    }
    await loadData()
  } finally {
    actionLoading.value = false
  }
}

function openCustomerDetail(userId: number) {
  router.push(`/inquiry/customer?userId=${userId}`)
}

async function handleStatusUpdate(status: number) {
  if (!currentInquiry.value) return

  const actionTextMap: Record<number, string> = {
    1: '将该询盘标记为已联系',
    2: '将该询盘标记为已完成',
    3: '取消该询盘'
  }

  await ElMessageBox.confirm(`确定${actionTextMap[status]}吗？`, '提示', { type: 'warning' })

  actionLoading.value = true
  try {
    await fetchUpdateInquiryStatus(currentInquiry.value.id, {
      status,
      adminRemark: remarkForm.value.adminRemark.trim() || undefined
    })
    ElMessage.success('状态更新成功')
    await showDetail(currentInquiry.value.id)
    await loadData()
  } finally {
    actionLoading.value = false
  }
}

async function handleQuickStatusUpdate(row: Api.Inquiry.Inquiry, status: number) {
  const actionTextMap: Record<number, string> = {
    1: '将该询盘标记为已联系',
    2: '将该询盘标记为已完成',
    3: '取消该询盘'
  }

  await ElMessageBox.confirm(`确定${actionTextMap[status]}吗？`, '提示', { type: 'warning' })

  actionLoading.value = true
  try {
    await fetchUpdateInquiryStatus(row.id, {
      status,
      adminRemark: row.adminRemark?.trim() || undefined
    })
    ElMessage.success('状态更新成功')
    await loadData()
  } finally {
    actionLoading.value = false
  }
}

// 生成转换单 PDF
async function handleGeneratePdf(row: any) {
  try {
    const blob = await fetchGenerateInquiryPdf(row.id)
    FileSaver.saveAs(blob, `询盘转换单_${row.inquiryNo}.pdf`)
    ElMessage.success('PDF 已下载')
  } catch (error: any) {
    ElMessage.error(error.message || '生成PDF失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.search-form-compact {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 0;

  :deep(.el-form-item) {
    margin-bottom: 8px;
  }

  :deep(.el-input__wrapper) {
    min-height: 32px;
  }

  :deep(.el-select__wrapper) {
    min-height: 32px;
  }

  :deep(.el-button) {
    height: 32px;
    padding: 0 15px;
  }
}

.inquiry-detail {
  .detail-section {
    margin-top: 20px;
  }

  .section-title {
    margin-bottom: 12px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }
}
</style>
