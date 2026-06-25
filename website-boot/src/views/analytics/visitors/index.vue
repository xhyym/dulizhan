<!-- 访客统计页面 -->
<template>
  <div class="visitors-page art-full-height">
    <!-- 搜索栏 -->
    <ElCard class="art-search-card">
      <ElForm inline class="search-form-compact">
        <ElFormItem label="日期范围">
          <ElDatePicker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
          />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="loadData" v-ripple>查询</ElButton>
          <ElButton @click="setDateRange('week')" v-ripple>近7天</ElButton>
          <ElButton @click="setDateRange('month')" v-ripple>近30天</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- 概览卡片 -->
    <ElRow :gutter="20" class="mb-5">
      <ElCol :span="12">
        <div class="art-card flex items-center h-28 px-6">
          <div class="size-12 rounded-xl flex-cc bg-blue-50 mr-4">
            <ArtSvgIcon icon="ri:eye-line" class="text-xl text-blue-500" />
          </div>
          <div>
            <span class="text-g-500 text-sm">总 PV</span>
            <ArtCountTo class="text-2xl font-medium block mt-1" :target="statsData?.totalPv ?? 0" />
          </div>
        </div>
      </ElCol>
      <ElCol :span="12">
        <div class="art-card flex items-center h-28 px-6">
          <div class="size-12 rounded-xl flex-cc bg-green-50 mr-4">
            <ArtSvgIcon icon="ri:group-line" class="text-xl text-green-500" />
          </div>
          <div>
            <span class="text-g-500 text-sm">总 UV</span>
            <ArtCountTo class="text-2xl font-medium block mt-1" :target="statsData?.totalUv ?? 0" />
          </div>
        </div>
      </ElCol>
    </ElRow>

    <!-- 每日趋势 -->
    <ElCard class="art-card mb-5" shadow="never">
      <template #header>
        <span class="font-medium">访客趋势</span>
      </template>
      <div ref="trendChartRef" style="height: 350px"></div>
    </ElCard>

    <!-- 国家/设备 分布 -->
    <ElRow :gutter="20" class="mb-5">
      <ElCol :sm="24" :lg="12">
        <ElCard class="art-card" shadow="never">
          <template #header>
            <span class="font-medium">国家 Top 10</span>
          </template>
          <div ref="countryChartRef" style="height: 350px"></div>
        </ElCard>
      </ElCol>
      <ElCol :sm="24" :lg="12">
        <ElCard class="art-card" shadow="never">
          <template #header>
            <span class="font-medium">设备分布</span>
          </template>
          <div ref="deviceChartRef" style="height: 350px"></div>
        </ElCard>
      </ElCol>
    </ElRow>

    <!-- 城市/热门页面 -->
    <ElRow :gutter="20" class="mb-5">
      <ElCol :sm="24" :lg="12">
        <ElCard class="art-card" shadow="never">
          <template #header>
            <span class="font-medium">城市 Top 10</span>
          </template>
          <ElTable :data="statsData?.cityTop10 ?? []" stripe size="small">
            <ElTableColumn type="index" label="#" width="50" />
            <ElTableColumn prop="city" label="城市" min-width="100" />
            <ElTableColumn prop="country" label="国家" min-width="100" />
            <ElTableColumn prop="pv" label="PV" width="80" align="right" />
            <ElTableColumn prop="uv" label="UV" width="80" align="right" />
          </ElTable>
        </ElCard>
      </ElCol>
      <ElCol :sm="24" :lg="12">
        <ElCard class="art-card" shadow="never">
          <template #header>
            <span class="font-medium">热门页面 Top 10</span>
          </template>
          <ElTable :data="statsData?.topPages ?? []" stripe size="small">
            <ElTableColumn type="index" label="#" width="50" />
            <ElTableColumn prop="pageUrl" label="页面" min-width="200" show-overflow-tooltip />
            <ElTableColumn prop="pv" label="PV" width="80" align="right" />
            <ElTableColumn prop="uv" label="UV" width="80" align="right" />
          </ElTable>
        </ElCard>
      </ElCol>
    </ElRow>

    <!-- 时段热力图 -->
    <ElCard class="art-card mb-5" shadow="never">
      <template #header>
        <span class="font-medium">时段分布</span>
      </template>
      <div ref="heatmapChartRef" style="height: 280px"></div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import * as echarts from 'echarts'
  import { fetchGetVisitorStats } from '@/api/dashboard'

  defineOptions({ name: 'VisitorStats' })

  type ChartInstance = ReturnType<typeof echarts.init>

  const dateRange = ref<string[]>([])
  const statsData = ref<Api.Dashboard.VisitorStatsVO | null>(null)

  const trendChartRef = ref<HTMLElement>()
  const countryChartRef = ref<HTMLElement>()
  const deviceChartRef = ref<HTMLElement>()
  const heatmapChartRef = ref<HTMLElement>()

  let trendChart: ChartInstance | null = null
  let countryChart: ChartInstance | null = null
  let deviceChart: ChartInstance | null = null
  let heatmapChart: ChartInstance | null = null

  const deviceLabels: Record<string, string> = { PC: '桌面端', Mobile: '移动端', Tablet: '平板端' }
  const dayLabels = ['', '周日', '周一', '周二', '周三', '周四', '周五', '周六']

  function setDateRange(type: 'week' | 'month') {
    const end = new Date()
    const start = new Date()
    start.setDate(start.getDate() - (type === 'week' ? 6 : 29))
    dateRange.value = [
      start.toISOString().slice(0, 10),
      end.toISOString().slice(0, 10)
    ]
    loadData()
  }

  async function loadData() {
    if (!dateRange.value || dateRange.value.length !== 2) return
    try {
      statsData.value = await fetchGetVisitorStats(dateRange.value[0], dateRange.value[1])
      renderAllCharts()
    } catch (error) {
      console.error('加载访客统计数据失败', error)
    }
  }

  function renderAllCharts() {
    renderTrendChart()
    renderCountryChart()
    renderDeviceChart()
    renderHeatmapChart()
  }

  function renderTrendChart() {
    if (!trendChartRef.value || !statsData.value?.dailyTrend?.length) return
    const chart = trendChart ?? echarts.init(trendChartRef.value)
    trendChart = chart

    const data = statsData.value.dailyTrend
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['PV', 'UV'], top: 0 },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: data.map((d: any) => d.date), boundaryGap: false },
      yAxis: { type: 'value' },
      series: [
        { name: 'PV', type: 'line', data: data.map((d: any) => Number(d.pv) || 0), smooth: true, areaStyle: { opacity: 0.15 } },
        { name: 'UV', type: 'line', data: data.map((d: any) => Number(d.uv) || 0), smooth: true, areaStyle: { opacity: 0.15 } }
      ]
    })
  }

  function renderCountryChart() {
    if (!countryChartRef.value || !statsData.value?.countryTop10?.length) return
    const chart = countryChart ?? echarts.init(countryChartRef.value)
    countryChart = chart

    const data = statsData.value.countryTop10
    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: data.map((d: any) => d.country), axisLabel: { rotate: 30 } },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: data.map((d: any) => Number(d.uv) || 0), barMaxWidth: 30, itemStyle: { borderRadius: [4, 4, 0, 0] } }]
    })
  }

  function renderDeviceChart() {
    if (!deviceChartRef.value || !statsData.value?.deviceDistribution?.length) return
    const chart = deviceChart ?? echarts.init(deviceChartRef.value)
    deviceChart = chart

    chart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        label: { show: true, formatter: '{b}\n{d}%' },
        data: statsData.value.deviceDistribution.map((d: any) => ({
          name: deviceLabels[d.deviceType] || d.deviceType,
          value: Number(d.uv) || 0
        }))
      }]
    })
  }

  function renderHeatmapChart() {
    if (!heatmapChartRef.value || !statsData.value?.hourlyHeatmap?.length) return
    const chart = heatmapChart ?? echarts.init(heatmapChartRef.value)
    heatmapChart = chart

    const rawData = statsData.value.hourlyHeatmap
    const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
    const days = dayLabels.slice(1)

    const heatData: [number, number, number][] = []
    for (const item of rawData) {
      const hour = Number(item.hour)
      const day = Number(item.dayOfWeek) - 1
      heatData.push([hour, day, Number(item.pv) || 0])
    }

    chart.setOption({
      tooltip: { formatter: (p: any) => `${days[p.value[1]]} ${hours[p.value[0]]}<br/>PV: ${p.value[2]}` },
      grid: { left: '8%', right: '4%', bottom: '15%', top: '3%' },
      xAxis: { type: 'category', data: hours, splitArea: { show: true } },
      yAxis: { type: 'category', data: days },
      visualMap: { min: 0, max: Math.max(...heatData.map((d) => d[2]), 1), calculable: true, orient: 'horizontal', left: 'center', bottom: 0 },
      series: [{ type: 'heatmap', data: heatData, label: { show: false }, emphasis: { itemStyle: { shadowBlur: 10 } } }]
    })
  }

  onMounted(() => {
    setDateRange('week')
    window.addEventListener('resize', () => {
      trendChart?.resize()
      countryChart?.resize()
      deviceChart?.resize()
      heatmapChart?.resize()
    })
  })

  onUnmounted(() => {
    trendChart?.dispose()
    countryChart?.dispose()
    deviceChart?.dispose()
    heatmapChart?.dispose()
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
</style>
