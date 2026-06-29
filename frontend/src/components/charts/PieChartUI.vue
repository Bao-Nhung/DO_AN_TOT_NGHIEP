<template>
  <Pie :data="chartData" :options="chartOptions" :plugins="chartPlugins" />
</template>

<script setup>
import { Pie } from "vue-chartjs";
import { Chart as ChartJS, Title, Tooltip, Legend, ArcElement } from "chart.js";
import ChartDataLabels from 'chartjs-plugin-datalabels';

ChartJS.register(Title, Tooltip, Legend, ArcElement);

const chartPlugins = [ChartDataLabels];


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
        datalabels: {
          color: '#ffffff',
          font: {
            weight: 'bold',
            size: 13
          },
          formatter: (value, context) => {
            const dataset = context.chart.data.datasets[0].data;
            const total = dataset.reduce((acc, current) => acc + Number(current), 0);
            if (total === 0) return '';
            const percentage = ((Number(value) * 100) / total).toFixed(1);
            if (percentage < 3) return ''; // Ẩn % nếu phần quá nhỏ để tránh đè chữ
            return percentage + '%';
          }
        },
        tooltip: {
          callbacks: {
            label: function (context) {
              let label = context.label || '';
              if (label) {
                label += ': ';
              }
              
              // Lấy toàn bộ mảng dữ liệu để tính tổng
              const dataset = context.dataset.data;
              const total = dataset.reduce((acc, current) => acc + Number(current), 0);
              const value = Number(context.raw);
              
              // Tính phần trăm (làm tròn 1 chữ số thập phân)
              const percentage = total > 0 ? ((value * 100) / total).toFixed(1) + '%' : '0%';
              
              // Format lại số cho dễ nhìn (ngăn cách hàng nghìn)
              const formattedValue = value.toLocaleString('vi-VN');
              
              return label + formattedValue + ' (' + percentage + ')';
            }
          }
        }
      },
    }),
  },
});
</script>
