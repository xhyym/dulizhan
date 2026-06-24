<!-- 最新询盘列表 -->
<template>
  <ElCard class="art-card mb-5" shadow="never">
    <template #header>
      <span class="font-medium">最新询盘</span>
    </template>
    <ElTable :data="data" stripe size="small">
      <ElTableColumn prop="userName" label="客户" min-width="100" show-overflow-tooltip />
      <ElTableColumn prop="inquiryNo" label="询盘号" min-width="140" show-overflow-tooltip />
      <ElTableColumn prop="totalAmount" label="金额" width="100" align="right">
        <template #default="{ row }"> ¥{{ row.totalAmount }} </template>
      </ElTableColumn>
      <ElTableColumn prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <ElTag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="createTime" label="时间" width="160" />
    </ElTable>
  </ElCard>
</template>

<script setup lang="ts">
  defineProps<{
    data: Record<string, any>[]
  }>()

  function statusText(status: number) {
    const map: Record<number, string> = { 0: '待处理', 1: '已联系', 2: '已完成', 3: '已取消' }
    return map[status] ?? '未知'
  }

  function statusType(status: number) {
    const map: Record<number, string> = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
    return map[status] ?? 'info'
  }
</script>
