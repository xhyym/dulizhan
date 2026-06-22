<!-- 询盘列表页面 -->
<template>
  <div class="inquiry-page art-full-height">
    <!-- 搜索栏 -->
    <ElCard class="art-search-card">
      <ElForm :model="searchForm" inline>
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

    <ElDivider style="margin: 0" />

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
  </div>
</template>

<script setup lang="ts">
import { h } from 'vue'
import { useRouter } from 'vue-router'
import { ElTag } from 'element-plus'
import { useTableColumns } from '@/hooks/core/useTableColumns'
import { fetchGetInquiryList } from '@/api/inquiry'

defineOptions({ name: 'InquiryList' })

const router = useRouter()
const loading = ref(false)
const tableData = ref<any[]>([])

const searchForm = ref({ inquiryNo: '', userName: '', status: undefined as number | undefined })
const pagination = ref({ current: 1, size: 10, total: 0 })

const statusConfig: Record<number, { type: string; text: string }> = {
  0: { type: 'warning', text: '待处理' },
  1: { type: 'primary', text: '已联系' },
  2: { type: 'success', text: '已完成' },
  3: { type: 'info', text: '已取消' }
}

// 列配置
const { columns, columnChecks } = useTableColumns(() => [
  { prop: 'inquiryNo', label: '询盘编号', width: 180 },
  { prop: 'userName', label: '用户名', width: 120 },
  { prop: 'userEmail', label: '邮箱', minWidth: 180, showOverflowTooltip: true },
  { prop: 'userWhatsapp', label: 'WhatsApp', width: 140 },
  {
    prop: 'totalAmount',
    label: '总金额',
    width: 100,
    align: 'right',
    formatter: (row: any) => `¥${row.totalAmount}`
  },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    align: 'center',
    formatter: (row: any) => {
      const cfg = statusConfig[row.status]
      return h(ElTag, { type: cfg?.type }, () => cfg?.text || '未知')
    }
  },
  { prop: 'createTime', label: '创建时间', width: 180 },
  {
    prop: 'operation',
    label: '操作',
    width: 120,
    fixed: 'right',
    formatter: (row: any) =>
      h('button', {
        class: 'el-button el-button--primary is-link',
        onClick: () => goDetail(row.id)
      }, '查看')
  }
])

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchGetInquiryList({
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

function goDetail(id: number) {
  router.push({ path: '/inquiry/detail', query: { id: String(id) } })
}

onMounted(() => loadData())
</script>
