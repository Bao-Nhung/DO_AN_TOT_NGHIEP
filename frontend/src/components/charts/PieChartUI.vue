<template>
  <Pie :data="chartData" :options="chartOptions" />
</template>

<script setup>
import { Pie } from "vue-chartjs";

import { Chart as ChartJS, Title, Tooltip, Legend, ArcElement } from "chart.js";

ChartJS.register(
  Title,

  Tooltip,

  Legend,

  ArcElement,
);

defineProps({
  chartData: {
    type: Object,

    required: true,
  },

  chartOptions: {
    type: Object,

    default: () => ({
      responsive: true,

      maintainAspectRatio: false,

      plugins: {
        legend: {
          display: true,

          position: "right",
        },
      },
    }),
  },
});
</script>
