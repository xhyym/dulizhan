<!-- 客户管理页面 -->
<template>
  <div class="customer-page art-full-height">
    <!-- 搜索栏 -->
    <ElCard class="art-search-card">
      <ElForm :model="searchForm" inline class="search-form-compact">
        <ElFormItem label="用户名">
          <ElInput v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 150px"
            @keyup.enter="loadData" />
        </ElFormItem>
        <ElFormItem label="邮箱">
          <ElInput v-model="searchForm.email" placeholder="请输入邮箱" clearable style="width: 180px"
            @keyup.enter="loadData" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchForm.status" placeholder="全部" clearable style="width: 100px">
            <ElOption label="正常" :value="1" />
            <ElOption label="禁用" :value="0" />
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

    <!-- 客户详情弹窗 -->
    <ElDialog v-model="dialogVisible" title="客户详情" width="800px">
      <div v-if="currentCustomer" class="customer-detail">
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="用户名">{{ currentCustomer.username }}</ElDescriptionsItem>
          <ElDescriptionsItem label="邮箱">{{ currentCustomer.email }}</ElDescriptionsItem>
          <ElDescriptionsItem label="WhatsApp">{{ currentCustomer.whatsapp || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="状态">
            <ElTag :type="currentCustomer.status === 1 ? 'success' : 'info'">
              {{ currentCustomer.status === 1 ? '正常' : '禁用' }}
            </ElTag>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="注册时间">{{ formatDateText(currentCustomer.createTime, true) }}</ElDescriptionsItem>
        </ElDescriptions>

        <h4 style="margin: 20px 0 12px">询盘记录</h4>
        <ElTable :data="customerInquiries" border size="small" v-loading="inquiriesLoading">
          <ElTableColumn prop="inquiryNo" label="询盘编号" width="180" />
          <ElTableColumn prop="totalAmount" label="总金额" width="100" align="right">
            <template #default="{ row }">¥{{ row.totalAmount }}</template>
          </ElTableColumn>
          <ElTableColumn prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <ElTag :type="statusConfig[row.status]?.type" size="small">
                {{ statusConfig[row.status]?.text }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createTime" label="创建时间">
            <template #default="{ row }">{{ formatDateText(row.createTime, true) }}</template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="100" align="center">
            <template #default="{ row }">
              <ElButton type="primary" link @click="openInquiryDetail(row.id)">查看详情</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </div>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { h } from 'vue'
import { ElTag } from 'element-plus'
import { useTableColumns } from '@/hooks/core/useTableColumns'
import { fetchGetUserList, fetchGetUserDetail, fetchGetUserInquiries } from '@/api/user'
import { useRoute, useRouter } from 'vue-router'

defineOptions({ name: 'CustomerList' })

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const loading = ref(false)
const inquiriesLoading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const currentCustomer = ref<any>(null)
const customerInquiries = ref<any[]>([])
const router = useRouter()
const route = useRoute()

const searchForm = ref({ username: '', email: '', status: undefined as number | undefined })
const pagination = ref({ current: 1, size: 10, total: 0 })

const statusConfig: Record<number, { type: TagType; text: string }> = {
  0: { type: 'warning', text: '待处理' },
  1: { type: 'primary', text: '已联系' },
  2: { type: 'success', text: '已完成' },
  3: { type: 'info', text: '已取消' }
}

function formatDateText(dateValue?: string | null, includeTime = false) {
  if (!dateValue) {
    return '-'
  }

  const normalizedDateValue = dateValue.replace('T', ' ').replace(/\//g, '-')
  if (includeTime) {
    return normalizedDateValue
  }

  return normalizedDateValue.length >= 10 ? normalizedDateValue.slice(0, 10) : normalizedDateValue
}

// 列配置
const { columns, columnChecks } = useTableColumns(() => [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'username', label: '用户名', width: 120 },
  { prop: 'email', label: '邮箱', minWidth: 200, showOverflowTooltip: true },
  { prop: 'whatsapp', label: 'WhatsApp', width: 140 },
  {
    prop: 'status',
    label: '状态',
    width: 80,
    align: 'center',
    formatter: (row: any) =>
      h(ElTag, { type: row.status === 1 ? 'success' : 'info' }, () =>
        row.status === 1 ? '正常' : '禁用'
      )
  },
  {
    prop: 'createTime',
    label: '注册时间',
    width: 180,
    formatter: (row: any) => formatDateText(row.createTime, true)
  },
  {
    prop: 'operation',
    label: '操作',
    width: 120,
    fixed: 'right',
    formatter: (row: any) =>
      h('button', {
        class: 'el-button el-button--primary is-link',
        onClick: () => showDetail(row)
      }, '查看详情')
  }
])

async function loadData() {
  loading.value = true
  try {
    const data = await fetchGetUserList({
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
  searchForm.value = { username: '', email: '', status: undefined }
  pagination.value.current = 1
  loadData()
}

async function showDetail(row: any) {
  currentCustomer.value = row
  dialogVisible.value = true
  inquiriesLoading.value = true
  try {
    const data = await fetchGetUserInquiries(row.id)
    customerInquiries.value = data || []
  } finally {
    inquiriesLoading.value = false
  }
}

async function openCustomerDetailById(userId: number) {
  const user = await fetchGetUserDetail(userId)
  await showDetail(user)
}

function openInquiryDetail(inquiryId: number) {
  dialogVisible.value = false
  router.push(`/inquiry/list?inquiryId=${inquiryId}`)
}

onMounted(async () => {
  await loadData()

  const userIdParam = route.query.userId
  if (!userIdParam) return

  const userId = Number(userIdParam)
  if (!Number.isFinite(userId)) return

  await openCustomerDetailById(userId)
})
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

.customer-detail {
  h4 {
    color: #303133;
    font-size: 15px;
  }
}
</style>
