<!-- 仪表盘统计卡片 -->
<template>
  <ElRow :gutter="20" class="flex">
    <ElCol v-for="(item, index) in dataList" :key="index" :sm="12" :md="6" :lg="4">
      <div class="art-card relative flex flex-col justify-center h-35 px-5 mb-5 max-sm:mb-4">
        <span class="text-g-700 text-sm">{{ item.des }}</span>
        <ArtCountTo class="text-[26px] font-medium mt-2" :target="item.num" :duration="1300" />
        <div class="absolute top-0 bottom-0 right-5 m-auto size-12.5 rounded-xl flex-cc bg-theme/10">
          <ArtSvgIcon :icon="item.icon" class="text-xl text-theme" />
        </div>
      </div>
    </ElCol>
  </ElRow>
</template>

<script setup lang="ts">
  interface CardDataItem {
    des: string
    icon: string
    num: number
  }

  const props = defineProps<{
    data: Api.Dashboard.DashboardVO | null
  }>()

  const dataList = computed<CardDataItem[]>(() => [
    { des: '今日 PV', icon: 'ri:eye-line', num: props.data?.todayPv ?? 0 },
    { des: '今日 UV', icon: 'ri:group-line', num: props.data?.todayUv ?? 0 },
    { des: '已上架商品', icon: 'ri:shopping-bag-line', num: props.data?.productCount ?? 0 },
    { des: '待处理询盘', icon: 'ri:mail-unread-line', num: props.data?.pendingInquiryCount ?? 0 },
    { des: '本月新客户', icon: 'ri:user-add-line', num: props.data?.monthNewCustomerCount ?? 0 },
    { des: '本月询盘', icon: 'ri:file-list-3-line', num: props.data?.monthInquiryCount ?? 0 }
  ])
</script>
