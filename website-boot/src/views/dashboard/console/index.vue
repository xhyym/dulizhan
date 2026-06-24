<!-- 工作台页面 -->
<template>
  <div class="console-page">
    <!-- 统计卡片 -->
    <CardList :data="dashboardData" />

    <!-- 图表区域 -->
    <ElRow :gutter="20">
      <ElCol :sm="24" :lg="16">
        <VisitTrend :data="dashboardData?.visitTrend ?? []" />
      </ElCol>
      <ElCol :sm="24" :lg="8">
        <DeviceChart :data="dashboardData?.deviceDistribution ?? []" />
      </ElCol>
    </ElRow>

    <ElRow :gutter="20">
      <ElCol :sm="24" :lg="12">
        <CountryChart :data="dashboardData?.countryTop10 ?? []" />
      </ElCol>
      <ElCol :sm="24" :lg="12">
        <RecentInquiries :data="dashboardData?.recentInquiries ?? []" />
      </ElCol>
    </ElRow>
  </div>
</template>

<script setup lang="ts">
  import { fetchGetDashboard } from '@/api/dashboard'
  import CardList from './modules/card-list.vue'
  import VisitTrend from './modules/visit-trend.vue'
  import CountryChart from './modules/country-chart.vue'
  import DeviceChart from './modules/device-chart.vue'
  import RecentInquiries from './modules/recent-inquiries.vue'

  defineOptions({ name: 'Console' })

  const dashboardData = ref<Api.Dashboard.DashboardVO | null>(null)

  async function loadData() {
    try {
      dashboardData.value = await fetchGetDashboard()
    } catch (error) {
      console.error('加载仪表盘数据失败', error)
    }
  }

  onMounted(() => {
    loadData()
  })
</script>
