<!-- 国家分布图 -->
<template>
  <ElCard class="art-card mb-5" shadow="never">
    <template #header>
      <span class="font-medium">访客国家 Top 10</span>
    </template>
    <div ref="chartRef" style="height: 320px"></div>
  </ElCard>
</template>

<script setup lang="ts">
  import * as echarts from 'echarts'

  type ChartInstance = ReturnType<typeof echarts.init>

  const props = defineProps<{
    data: Record<string, any>[]
  }>()

  const chartRef = ref<HTMLElement>()
  let chartInstance: ChartInstance | null = null

  function renderChart() {
    if (!chartRef.value || !props.data?.length) return

    const chart = chartInstance ?? echarts.init(chartRef.value)
    chartInstance = chart

    const countries = props.data.map((d) => d.country)
    const uvData = props.data.map((d) => Number(d.uv) || 0)

    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: countries, axisLabel: { rotate: 30 } },
      yAxis: { type: 'value' },
      series: [
        {
          type: 'bar',
          data: uvData,
          barMaxWidth: 30,
          itemStyle: {
            borderRadius: [4, 4, 0, 0]
          }
        }
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
