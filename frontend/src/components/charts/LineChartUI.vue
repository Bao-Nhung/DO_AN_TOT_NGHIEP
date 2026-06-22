<template>
  <Line :data="chartData" :options="chartOptions" />
</template>

<script setup>
import { Line } from "vue-chartjs";

import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  LineElement,
  CategoryScale,
  LinearScale,
  PointElement,
} from "chart.js";

ChartJS.register(
  Title,

  Tooltip,

  Legend,

  LineElement,

  CategoryScale,

  LinearScale,

  PointElement,
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

          position: "top",
        },
      },

      scales: {
        y: {
          beginAtZero: true,
        },
      },
    }),
  },
});
</script>
