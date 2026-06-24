<!-- 访客趋势图 -->
<template>
  <ElCard class="art-card mb-5" shadow="never">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="font-medium">访客趋势（近30天）</span>
      </div>
    </template>
    <div ref="chartRef" style="height: 320px"></div>
  </ElCard>
</template>

<script setup lang="ts">
  import * as echarts from 'echarts'
  import type { EChartsType } from 'echarts/core'

  const props = defineProps<{
    data: Record<string, any>[]
  }>()

  const chartRef = ref<HTMLElement>()
  let chartInstance: EChartsType | null = null

  function renderChart() {
    if (!chartRef.value || !props.data?.length) return

    if (!chartInstance) {
      chartInstance = echarts.init(chartRef.value)
    }

    const dates = props.data.map((d) => d.date)
    const pvData = props.data.map((d) => Number(d.pv) || 0)
    const uvData = props.data.map((d) => Number(d.uv) || 0)

    chartInstance.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['PV', 'UV'], top: 0 },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: dates, boundaryGap: false },
      yAxis: { type: 'value' },
      series: [
        { name: 'PV', type: 'line', data: pvData, smooth: true, areaStyle: { opacity: 0.15 } },
        { name: 'UV', type: 'line', data: uvData, smooth: true, areaStyle: { opacity: 0.15 } }
      ]
    })
  }

  watch(() => props.data, renderChart, { deep: true })

  onMounted(() => {
    renderChart()
    window.addEventListener('resize', () => chartInstance?.resize())
  })

  onUnmounted(() => {
    chartInstance?.dispose()
  })
</script>
