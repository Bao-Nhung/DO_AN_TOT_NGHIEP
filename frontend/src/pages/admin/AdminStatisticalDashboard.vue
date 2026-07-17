<template>
  <AdminLayout>
    <h1 class="title">Thống kê bán hàng</h1>

    <div class="filter-box">
      <template v-if="filter.timeType === 'ngay'">
        <div>
          <label>Từ ngày</label>
          <input type="date" v-model="filter.startDate" />
        </div>

        <div>
          <label>Đến ngày</label>
          <input type="date" v-model="filter.endDate" />
        </div>
      </template>

      <template v-else-if="filter.timeType === 'thang'">
        <div>
          <label>Từ tháng</label>
          <input type="month" v-model="filter.startMonth" />
        </div>

        <div>
          <label>Đến tháng</label>
          <input type="month" v-model="filter.endMonth" />
        </div>
      </template>

      <div>
        <label>Kiểu thống kê</label>
        <select v-model="filter.timeType">
          <option value="ngay">Theo ngày</option>
          <option value="thang">Theo tháng</option>
        </select>
      </div>

      <button @click="loadThongKe">Xem thống kê</button>
    </div>

    <div class="row g-3 mb-4">
      <div class="col-md-3" v-for="item in cards" :key="item.label">
        <div class="card-box">
          <span>{{ item.label }}</span>
          <h2>{{ item.value }}</h2>
        </div>
      </div>
    </div>

    <div class="chart-box">
      <h3>Doanh thu & Lợi nhuận</h3>
      <div class="chart">
        <LineChartUI
          v-if="lineRevenue.labels.length"
          :chartData="lineRevenue"
        />
      </div>
    </div>

    <div class="row mt-4 g-4">
      <div class="col-lg-6">
        <div class="chart-box">
          <h3>Trạng thái đơn hàng</h3>
          <div class="chart">
            <PieChartUI v-if="pieOrder.labels.length" :chartData="pieOrder" />
          </div>
        </div>
      </div>

      <div class="col-lg-6">
        <div class="chart-box">
          <h3>Khách hàng đăng ký mới</h3>
          <div class="chart">
            <LineChartUI
              v-if="lineCustomer.labels.length"
              :chartData="lineCustomer"
            />
          </div>
        </div>
      </div>
    </div>

    <div class="chart-box mt-4">
      <h3>Top 5 sản phẩm bán chạy</h3>
      <div class="chart">
        <BarChartUI v-if="barProduct.labels.length" :chartData="barProduct" />
      </div>
    </div>

    <div class="row mt-4 g-4">
      <div class="col-lg-6">
        <div class="chart-box">
          <h3>Size được mua nhiều nhất</h3>
          <div class="chart">
            <BarChartUI v-if="barSize.labels.length" :chartData="barSize" />
          </div>
        </div>
      </div>

      <div class="col-lg-6">
        <div class="chart-box">
          <h3>Màu sắc bán chạy</h3>
          <div class="chart">
            <BarChartUI v-if="barColor.labels.length" :chartData="barColor" />
          </div>
        </div>
      </div>
    </div>

    <div class="chart-box mt-4">
      <h3>Tỷ lệ doanh thu danh mục</h3>
      <div class="chart small">
        <PieChartUI v-if="pieCategory.labels.length" :chartData="pieCategory" />
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from "vue";
import AdminLayout from "@/components/layout/AdminLayout.vue";
import { api } from "@/composables/useApi";
import { useToast } from "@/composables/useToast";
import LineChartUI from "@/components/charts/LineChartUI.vue";
import BarChartUI from "@/components/charts/BarChartUI.vue";
import PieChartUI from "@/components/charts/PieChartUI.vue";

const { showToast } = useToast();

// 1. TỰ ĐỘNG TÍNH NGÀY ĐẦU THÁNG ĐẾN HIỆN TẠI
const initDates = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const date = String(now.getDate()).padStart(2, "0");

  return {
    startDate: `${year}-${month}-01`,
    endDate: `${year}-${month}-${date}`,
    startMonth: `${year}-${month}`,
    endMonth: `${year}-${month}`,
    timeType: "ngay",
  };
};

const filter = ref(initDates());

// 2. STATE DATA
const cards = ref([]);
const lineRevenue = ref({ labels: [], datasets: [] });
const pieOrder = ref({ labels: [], datasets: [] });
const lineCustomer = ref({ labels: [], datasets: [] });
const barProduct = ref({ labels: [], datasets: [] });
const barSize = ref({ labels: [], datasets: [] });
const barColor = ref({ labels: [], datasets: [] });
const pieCategory = ref({ labels: [], datasets: [] });

// 3. COLOR PALETTE MẶC ĐỊNH CHO PIE CHART
const defaultColors = [
  "#2563eb",
  "#16a34a",
  "#f59e0b",
  "#9333ea",
  "#dc2626",
  "#0891b2",
  "#64748b",
  "#e11d48",
  "#0f766e",
  "#7c3aed",
];

function addDays(dateValue, days) {
  const [year, month, day] = dateValue.split("-").map(Number);
  return new Date(Date.UTC(year, month - 1, day + days)).toISOString().slice(0, 10);
}

function firstDayOfNextMonth(monthValue) {
  const [year, month] = monthValue.split("-").map(Number);
  return new Date(Date.UTC(year, month, 1)).toISOString().slice(0, 10);
}

// 4. LOAD API
async function loadThongKe() {
  try {
    const payload = {
      timeType: filter.value.timeType,
    };

    if (filter.value.timeType === "ngay") {
      if (!filter.value.startDate || !filter.value.endDate) {
        showToast("Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc", "error");
        return;
      }
      if (filter.value.startDate > filter.value.endDate) {
        showToast("Ngày bắt đầu không được sau ngày kết thúc", "error");
        return;
      }
      payload.startDate = `${filter.value.startDate}T00:00`;
      payload.endDate = `${addDays(filter.value.endDate, 1)}T00:00`;
    } else {
      if (!filter.value.startMonth || !filter.value.endMonth) {
        showToast("Vui lòng chọn đầy đủ tháng bắt đầu và tháng kết thúc", "error");
        return;
      }
      if (filter.value.startMonth > filter.value.endMonth) {
        showToast("Tháng bắt đầu không được sau tháng kết thúc", "error");
        return;
      }
      payload.startDate = `${filter.value.startMonth}-01T00:00`;
      payload.endDate = `${firstDayOfNextMonth(filter.value.endMonth)}T00:00`;
    }

    const data = await api().getThongKeTongHop(payload);

    // CARD
    const t = data.tongQuan;
    cards.value = [
      { label: "Doanh thu", value: formatMoney(t?.doanhThu) },
      { label: "Lợi nhuận gộp", value: formatMoney(t?.loiNhuanGop) },
      {
        label: "Đơn hàng thành công",
        value: `${t?.donHangThanhCong ?? 0} / ${t?.tongDonHang ?? 0}`,
      },
      { label: "Khách hàng mới", value: t?.khachHangMoi ?? 0 },
    ];

    // LINE REVENUE
    lineRevenue.value = {
      labels: data.doanhThuLoiNhuan?.map((x) => x.thoiGian) || [],
      datasets: [
        {
          label: "Doanh thu",
          data: data.doanhThuLoiNhuan?.map((x) => Number(x.doanhThu)) || [],
          borderWidth: 3,
          borderColor: "#2563eb",
          backgroundColor: "#2563eb",
        },
        {
          label: "Lợi nhuận",
          data: data.doanhThuLoiNhuan?.map((x) => Number(x.loiNhuan)) || [],
          borderWidth: 3,
          borderColor: "#16a34a",
          backgroundColor: "#16a34a",
        },
      ],
    };

    // STATUS PIE (Đã lấy string trực tiếp từ DB)
    pieOrder.value = {
      labels: data.trangThaiDonHang?.map((x) => x.tenTrangThai) || [],
      datasets: [
        {
          data: data.trangThaiDonHang?.map((x) => x.soLuong) || [],
          backgroundColor: defaultColors,
        },
      ],
    };

    // CUSTOMER
    lineCustomer.value = {
      labels: data.tangTruongKhachHang?.map((x) => x.thoiGian) || [],
      datasets: [
        {
          label: "Khách hàng mới",
          data: data.tangTruongKhachHang?.map((x) => x.soLuongKHMoi) || [],
          borderColor: "#9333ea",
          backgroundColor: "#9333ea",
          borderWidth: 3,
        },
      ],
    };

    // TOP 5 SẢN PHẨM
    barProduct.value = {
      labels: data.topSanPham?.map((x) => x.ten) || [],
      datasets: [
        {
          label: "Số lượng bán",
          data: data.topSanPham?.map((x) => x.tongSoLuong) || [],
          backgroundColor: defaultColors,
          borderWidth: 1,
        },
      ],
    };

    // SIZE BÁN CHẠY
    barSize.value = {
      labels: data.theoSize?.map((x) => x.ten) || [],
      datasets: [
        {
          label: "Số lượng bán",
          data: data.theoSize?.map((x) => x.tongSoLuong) || [],
          backgroundColor: [
            "#0ea5e9",
            "#14b8a6",
            "#8b5cf6",
            "#f97316",
            "#ef4444",
          ],
          borderWidth: 1,
        },
      ],
    };

    // MÀU SẮC BÁN CHẠY (Tự động lấy maHex từ CSDL)
    barColor.value = {
      labels: data.theoMau?.map((x) => x.ten) || [],
      datasets: [
        {
          label: "Số lượng bán",
          data: data.theoMau?.map((x) => x.tongSoLuong) || [],
          backgroundColor: data.theoMau?.map((x) => x.maHex || "#cccccc") || [],
          borderColor: "#d1d5db", // Viền xám nhạt đề phòng màu trắng tệp nền
          borderWidth: 1,
        },
      ],
    };

    // DOANH THU DANH MỤC
    pieCategory.value = {
      labels: data.doanhThuDanhMuc?.map((x) => x.tenDanhMuc) || [],
      datasets: [
        {
          data: data.doanhThuDanhMuc?.map((x) => Number(x.tongDoanhThu)) || [],
          backgroundColor: defaultColors,
          borderWidth: 2,
        },
      ],
    };
  } catch (e) {
    console.error("Lỗi thống kê", e);
    showToast(e.error || e.message || "Không thể tải dữ liệu thống kê", "error");
  }
}

// FORMAT MONEY
function formatMoney(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value || 0);
}

onMounted(() => {
  loadThongKe();
});
</script>

<style scoped>
/* Toàn bộ style cũ của bạn giữ nguyên, vì nó đã lên giao diện rất chuẩn */
.title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 20px;
}
.filter-box {
  background: white;
  padding: 20px;
  border-radius: 16px;
  display: flex;
  gap: 20px;
  align-items: end;
  margin-bottom: 25px;
}
.filter-box label {
  display: block;
  font-size: 13px;
  margin-bottom: 5px;
  color: #666;
}
.filter-box input,
.filter-box select {
  padding: 9px 12px;
  border-radius: 8px;
  border: 1px solid #ddd;
}
.filter-box button {
  background: #111;
  color: white;
  border: none;
  padding: 10px 25px;
  border-radius: 8px;
  cursor: pointer;
}
.card-box {
  background: white;
  padding: 22px;
  border-radius: 16px;
  border: 1px solid #eee;
}
.card-box span {
  font-size: 14px;
  color: #666;
}
.card-box h2 {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 700;
  color: #111;
}
.chart-box {
  background: white;
  padding: 22px;
  border-radius: 16px;
  border: 1px solid #eee;
}
.chart-box h3 {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 20px;
}
.chart {
  height: 350px;
}
.small {
  height: 300px;
}
</style>
