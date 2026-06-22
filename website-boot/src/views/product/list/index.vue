<!-- 商品列表页面 -->
<template>
  <div class="product-page art-full-height">
    <!-- 搜索栏 -->
    <ElCard class="art-search-card">
      <ElForm :model="searchForm" inline>
        <ElFormItem label="商品名称">
          <ElInput v-model="searchForm.name" placeholder="请输入商品名称" clearable style="width: 200px"
            @keyup.enter="loadData" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <ElOption label="上架" :value="1" />
            <ElOption label="下架" :value="0" />
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
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="loadData">
        <template #left>
          <ElButton type="primary" @click="goDetail()" v-ripple>新增商品</ElButton>
        </template>
      </ArtTableHeader>

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
import { ElMessageBox, ElMessage, ElTag, ElImage } from 'element-plus'
import { useTableColumns } from '@/hooks/core/useTableColumns'
import { fetchGetProductList, fetchDeleteProduct } from '@/api/product'

defineOptions({ name: 'ProductList' })

const router = useRouter()
const loading = ref(false)
const tableData = ref<any[]>([])

const searchForm = ref({ name: '', status: undefined as number | undefined })
const pagination = ref({ current: 1, size: 10, total: 0 })

// 列配置
const { columns, columnChecks } = useTableColumns(() => [
  { prop: 'id', label: 'ID', width: 80 },
  {
    prop: 'mainImage',
    label: '商品图片',
    width: 100,
    formatter: (row: any) =>
      h(ElImage, {
        src: row.mainImage,
        style: 'width: 60px; height: 60px',
        fit: 'cover',
        previewSrcList: [row.mainImage],
        previewTeleported: true
      })
  },
  { prop: 'name', label: '商品名称', minWidth: 200, showOverflowTooltip: true },
  {
    prop: 'price',
    label: '原价',
    width: 100,
    align: 'right',
    formatter: (row: any) => `¥${row.price}`
  },
  {
    prop: 'discountPrice',
    label: '折后价',
    width: 100,
    align: 'right',
    formatter: (row: any) =>
      row.discountPrice
        ? h('span', { style: 'color: #f56c6c' }, `¥${row.discountPrice}`)
        : h('span', {}, '-')
  },
  {
    prop: 'status',
    label: '状态',
    width: 80,
    align: 'center',
    formatter: (row: any) =>
      h(ElTag, { type: row.status === 1 ? 'success' : 'info' }, () =>
        row.status === 1 ? '上架' : '下架'
      )
  },
  { prop: 'createTime', label: '创建时间', width: 180 },
  {
    prop: 'operation',
    label: '操作',
    width: 180,
    fixed: 'right',
    formatter: (row: any) =>
      h('div', [
        h('button', {
          class: 'el-button el-button--primary is-link',
          onClick: () => goDetail(row.id)
        }, '编辑'),
        h('button', {
          class: 'el-button el-button--danger is-link',
          onClick: () => handleDelete(row)
        }, '删除')
      ])
  }
])

async function loadData() {
  loading.value = true
  try {
    const { data } = await fetchGetProductList({
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
  searchForm.value = { name: '', status: undefined }
  pagination.value.current = 1
  loadData()
}

function goDetail(id?: number) {
  if (id) {
    router.push({ path: '/product/detail', query: { id: String(id) } })
  } else {
    router.push('/product/detail')
  }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(`确定删除商品「${row.name}」吗？`, '提示', { type: 'warning' })
  await fetchDeleteProduct(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadData())
</script>
