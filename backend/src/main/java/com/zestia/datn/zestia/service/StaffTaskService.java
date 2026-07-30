package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.SupportConversationRepository;
import com.zestia.datn.zestia.repository.VayChiTietRepository;
import com.zestia.datn.zestia.repository.YeuCauDoiTraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StaffTaskService {
    private static final List<String> OPEN_CHAT_STATUSES = List.of(
            SupportChatService.WAITING,
            SupportChatService.ACTIVE
    );
    private static final List<String> ACTIONABLE_RETURN_STATUSES = List.of(
            ReturnExchangeService.PENDING,
            ReturnExchangeService.WAITING_FOR_GOODS,
            ReturnExchangeService.READY_TO_COMPLETE,
            ReturnExchangeService.REFUND_PENDING
    );

    private final HoaDonRepository orderRepository;
    private final SupportConversationRepository conversationRepository;
    private final YeuCauDoiTraRepository returnRepository;
    private final VayChiTietRepository variantRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getTasks(Integer employeeId, boolean admin) {
        List<Map<String, Object>> items = new ArrayList<>();

        addTask(
                items,
                "orders",
                "Đơn hàng chờ xử lý",
                "Xác nhận các đơn hợp lệ và chuẩn bị hàng.",
                orderRepository.countActionablePendingOrders(),
                "/admin/orders",
                "high"
        );

        long chatCount = admin
                ? conversationRepository.countByTrangThaiIn(OPEN_CHAT_STATUSES)
                : conversationRepository.countActionableForEmployee(employeeId, OPEN_CHAT_STATUSES);
        addTask(
                items,
                "support-chat",
                "Khách đang cần hỗ trợ",
                "Tiếp nhận hoặc phản hồi cuộc trò chuyện đang mở.",
                chatCount,
                "/admin/support-chat",
                "high"
        );

        addTask(
                items,
                "returns",
                "Yêu cầu đổi trả",
                "Kiểm tra các yêu cầu đang chờ thao tác tiếp theo.",
                returnRepository.countByTrangThaiIn(ACTIONABLE_RETURN_STATUSES),
                "/admin/returns",
                "medium"
        );

        if (admin) {
            addTask(
                    items,
                    "low-stock",
                    "Biến thể sắp hết hàng",
                    "Kiểm tra các biến thể còn tối đa 5 sản phẩm.",
                    variantRepository.countLowStockVariants(),
                    "/admin/products",
                    "medium"
            );
        }

        long total = items.stream()
                .mapToLong(item -> ((Number) item.get("count")).longValue())
                .sum();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("generatedAt", LocalDateTime.now());
        response.put("total", total);
        response.put("items", items);
        return response;
    }

    private void addTask(List<Map<String, Object>> items,
                         String key,
                         String title,
                         String description,
                         long count,
                         String route,
                         String priority) {
        if (count <= 0) return;
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", key);
        item.put("title", title);
        item.put("description", description);
        item.put("count", count);
        item.put("route", route);
        item.put("priority", priority);
        items.add(item);
    }
}
