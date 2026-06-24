<!-- 设备分布饼图 -->
<template>
  <ElCard class="art-card mb-5" shadow="never">
    <template #header>
      <span class="font-medium">设备分布</span>
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

  const deviceLabels: Record<string, string> = {
    PC: '桌面端',
    Mobile: '移动端',
    Tablet: '平板端'
  }

  function renderChart() {
    if (!chartRef.value || !props.data?.length) return

    if (!chartInstance) {
      chartInstance = echarts.init(chartRef.value)
    }

    const seriesData = props.data.map((d) => ({
      name: deviceLabels[d.deviceType] || d.deviceType,
      value: Number(d.uv) || 0
    }))

    chartInstance.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0 },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          label: { show: true, formatter: '{b}\n{d}%' },
          data: seriesData
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
