package com.zestia.datn.zestia.service;

import com.zestia.datn.zestia.entity.KhachHang;
import com.zestia.datn.zestia.entity.LichLamViec;
import com.zestia.datn.zestia.entity.NhanVien;
import com.zestia.datn.zestia.entity.SupportConversation;
import com.zestia.datn.zestia.entity.SupportMessage;
import com.zestia.datn.zestia.repository.LichLamViecRepository;
import com.zestia.datn.zestia.repository.NhanVienRepository;
import com.zestia.datn.zestia.repository.SupportConversationRepository;
import com.zestia.datn.zestia.repository.SupportMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SupportChatService {
    public static final String WAITING = "WAITING";
    public static final String ACTIVE = "ACTIVE";
    public static final String CLOSED = "CLOSED";
    private static final List<String> OPEN_STATUSES = List.of(WAITING, ACTIVE);

    private final SupportConversationRepository conversationRepository;
    private final SupportMessageRepository messageRepository;
    private final LichLamViecRepository scheduleRepository;
    private final NhanVienRepository employeeRepository;

    @Transactional
    public Map<String, Object> requestSupport(String rawMessage, KhachHang customer) {
        String message = validateMessage(rawMessage);
        LocalDateTime now = LocalDateTime.now();
        SupportConversation conversation = SupportConversation.builder()
                .publicToken(UUID.randomUUID().toString().replace("-", ""))
                .khachHang(customer)
                .tieuDe(abbreviate(message, 120))
                .trangThai(WAITING)
                .ngayTao(now)
                .ngayCapNhat(now)
                .build();
        conversationRepository.save(conversation);
        saveMessage(conversation, "CUSTOMER", customerName(customer), message, now);
        if (!tryAssign(conversation, now)) {
            saveMessage(conversation, "SYSTEM", "Zestia",
                    "Yêu cầu của bạn đã được đưa vào hàng chờ. Admin hoặc nhân viên trong ca sẽ tiếp nhận sớm nhất.",
                    now);
        }
        return customerSnapshot(conversation);
    }

    @Transactional
    public Map<String, Object> getCustomerConversation(String token) {
        SupportConversation conversation = requireByTokenForUpdate(token);
        refreshAssignment(conversation, LocalDateTime.now());
        return customerSnapshot(conversation);
    }

    @Transactional
    public Map<String, Object> sendCustomerMessage(String token, String rawMessage) {
        SupportConversation conversation = requireByTokenForUpdate(token);
        if (CLOSED.equals(conversation.getTrangThai())) {
            throw new IllegalStateException("Phiên hỗ trợ đã kết thúc");
        }
        LocalDateTime now = LocalDateTime.now();
        saveMessage(conversation, "CUSTOMER", customerName(conversation.getKhachHang()), validateMessage(rawMessage), now);
        conversation.setNgayCapNhat(now);
        refreshAssignment(conversation, now);
        conversationRepository.save(conversation);
        return customerSnapshot(conversation);
    }

    @Transactional
    public Map<String, Object> getStaffConversations(Integer employeeId, boolean admin, int page, int size) {
        List<SupportConversation> conversations = conversationRepository.findByTrangThaiInOrderByNgayCapNhatDesc(OPEN_STATUSES);
        LocalDateTime now = LocalDateTime.now();
        rebalanceAssignments(conversations, now);

        int safePage = Math.max(0, page);
        int safeSize = Math.min(50, Math.max(1, size));
        var result = conversationRepository.findOpenForStaff(
                OPEN_STATUSES,
                employeeId,
                admin,
                PageRequest.of(safePage, safeSize,
                        Sort.by(Sort.Direction.DESC, "ngayCapNhat").and(Sort.by(Sort.Direction.DESC, "id")))
        );
        Map<Integer, String> latestMessages = latestMessages(result.getContent());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", result.getContent().stream()
                .map(conversation -> staffSummary(conversation,
                        latestMessages.getOrDefault(conversation.getId(), "")))
                .toList());
        response.put("page", result.getNumber());
        response.put("size", result.getSize());
        response.put("totalElements", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStaffConversation(Integer conversationId, Integer employeeId, boolean admin) {
        SupportConversation conversation = requireStaffAccess(conversationId, employeeId, admin, false);
        return staffSnapshot(conversation);
    }

    @Transactional
    public Map<String, Object> claim(Integer conversationId, Integer employeeId, boolean admin) {
        SupportConversation conversation = conversationRepository.findByIdForUpdate(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phiên hỗ trợ"));
        if (CLOSED.equals(conversation.getTrangThai())) {
            throw new IllegalStateException("Phiên hỗ trợ đã kết thúc");
        }
        if (conversation.getNhanVien() != null
                && !Objects.equals(conversation.getNhanVien().getId(), employeeId)
                && !admin) {
            throw new IllegalStateException("Phiên hỗ trợ đã được nhân viên khác tiếp nhận");
        }
        NhanVien employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nhân viên"));
        LocalDateTime now = LocalDateTime.now();
        if (conversation.getNhanVien() == null) {
            conversation.setNhanVien(employee);
            conversation.setNgayNhan(now);
            saveMessage(conversation, "SYSTEM", "Zestia", employee.getHoVaTen() + " đã tiếp nhận cuộc trò chuyện.", now);
        }
        conversation.setTrangThai(ACTIVE);
        conversation.setNgayCapNhat(now);
        conversationRepository.save(conversation);
        return staffSnapshot(conversation);
    }

    @Transactional
    public Map<String, Object> sendStaffMessage(Integer conversationId, Integer employeeId,
                                                boolean admin, String rawMessage, String senderName) {
        SupportConversation conversation = requireStaffAccess(conversationId, employeeId, admin, true);
        LocalDateTime now = LocalDateTime.now();
        if (conversation.getNhanVien() == null) {
            NhanVien employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nhân viên"));
            conversation.setNhanVien(employee);
            conversation.setNgayNhan(now);
        }
        conversation.setTrangThai(ACTIVE);
        conversation.setNgayCapNhat(now);
        saveMessage(conversation, "EMPLOYEE", cleanName(senderName), validateMessage(rawMessage), now);
        conversationRepository.save(conversation);
        return staffSnapshot(conversation);
    }

    @Transactional
    public Map<String, Object> close(Integer conversationId, Integer employeeId, boolean admin, String senderName) {
        SupportConversation conversation = requireStaffAccess(conversationId, employeeId, admin, true);
        LocalDateTime now = LocalDateTime.now();
        conversation.setTrangThai(CLOSED);
        conversation.setNgayDong(now);
        conversation.setNgayCapNhat(now);
        saveMessage(conversation, "SYSTEM", "Zestia", "Phiên hỗ trợ đã được " + cleanName(senderName) + " kết thúc.", now);
        conversationRepository.save(conversation);
        return staffSnapshot(conversation);
    }

    private boolean tryAssign(SupportConversation conversation, LocalDateTime now) {
        NhanVien employee = findAvailableEmployee(now);
        if (employee == null) return false;
        conversation.setNhanVien(employee);
        conversation.setTrangThai(ACTIVE);
        conversation.setNgayNhan(now);
        conversation.setNgayCapNhat(now);
        conversationRepository.save(conversation);
        saveMessage(conversation, "SYSTEM", "Zestia", employee.getHoVaTen() + " đang trực và đã tiếp nhận yêu cầu.", now);
        return true;
    }

    private void refreshAssignment(SupportConversation conversation, LocalDateTime now) {
        if (CLOSED.equals(conversation.getTrangThai())) return;
        if (conversation.getNhanVien() != null && !isEmployeeAvailable(conversation.getNhanVien().getId(), now)) {
            String previousName = conversation.getNhanVien().getHoVaTen();
            conversation.setNhanVien(null);
            conversation.setTrangThai(WAITING);
            conversation.setNgayNhan(null);
            conversation.setNgayCapNhat(now);
            conversationRepository.save(conversation);
            saveMessage(conversation, "SYSTEM", "Zestia",
                    previousName + " đã kết thúc ca. Zestia đang chuyển yêu cầu cho nhân viên khác.", now);
        }
        if (WAITING.equals(conversation.getTrangThai()) && conversation.getNhanVien() == null) {
            tryAssign(conversation, now);
        }
    }

    private void rebalanceAssignments(List<SupportConversation> conversations, LocalDateTime now) {
        Map<Integer, NhanVien> availableEmployees = checkedInShifts(now).stream()
                .map(LichLamViec::getNhanVien)
                .filter(Objects::nonNull)
                .filter(employee -> Byte.valueOf((byte) 1).equals(employee.getTinhTrangLamViec()))
                .filter(this::isCustomerSupportRole)
                .collect(java.util.stream.Collectors.toMap(
                        NhanVien::getId,
                        employee -> employee,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        Map<Integer, Long> assignedCounts = new HashMap<>();
        for (SupportConversation conversation : conversations) {
            if (conversation.getNhanVien() != null) {
                assignedCounts.merge(conversation.getNhanVien().getId(), 1L, Long::sum);
            }
        }

        for (SupportConversation conversation : conversations) {
            NhanVien assigned = conversation.getNhanVien();
            boolean assignedAvailable = assigned != null && (
                    isActiveAdmin(assigned) || availableEmployees.containsKey(assigned.getId())
            );
            if (assigned != null && !assignedAvailable) {
                String previousName = assigned.getHoVaTen();
                conversation.setNhanVien(null);
                conversation.setTrangThai(WAITING);
                conversation.setNgayNhan(null);
                conversation.setNgayCapNhat(now);
                conversationRepository.save(conversation);
                saveMessage(conversation, "SYSTEM", "Zestia",
                        previousName + " đã kết thúc ca. Zestia đang chuyển yêu cầu cho nhân viên khác.", now);
                assignedCounts.computeIfPresent(assigned.getId(), (id, count) -> Math.max(0L, count - 1));
                assigned = null;
            }

            if (assigned == null && !WAITING.equals(conversation.getTrangThai())) {
                conversation.setTrangThai(WAITING);
                conversation.setNgayNhan(null);
                conversation.setNgayCapNhat(now);
                conversationRepository.save(conversation);
            }
            if (assigned == null && !availableEmployees.isEmpty()) {
                NhanVien selected = availableEmployees.values().stream()
                        .min(Comparator.comparingLong(employee -> assignedCounts.getOrDefault(employee.getId(), 0L)))
                        .orElse(null);
                if (selected != null) {
                    conversation.setNhanVien(selected);
                    conversation.setTrangThai(ACTIVE);
                    conversation.setNgayNhan(now);
                    conversation.setNgayCapNhat(now);
                    conversationRepository.save(conversation);
                    saveMessage(conversation, "SYSTEM", "Zestia",
                            selected.getHoVaTen() + " đang trực và đã tiếp nhận yêu cầu.", now);
                    assignedCounts.merge(selected.getId(), 1L, Long::sum);
                }
            } else if (assigned != null && !ACTIVE.equals(conversation.getTrangThai())) {
                conversation.setTrangThai(ACTIVE);
                conversation.setNgayCapNhat(now);
                conversationRepository.save(conversation);
            }
        }
    }

    private boolean isActiveAdmin(NhanVien employee) {
        return Byte.valueOf((byte) 1).equals(employee.getTinhTrangLamViec())
                && employee.getVaiTro() != null
                && "Admin".equalsIgnoreCase(employee.getVaiTro().getTenVaiTro());
    }

    private boolean isEmployeeAvailable(Integer employeeId, LocalDateTime now) {
        NhanVien assigned = employeeRepository.findById(employeeId).orElse(null);
        if (assigned == null || !Byte.valueOf((byte) 1).equals(assigned.getTinhTrangLamViec())) {
            return false;
        }
        if (assigned.getVaiTro() != null
                && "Admin".equalsIgnoreCase(assigned.getVaiTro().getTenVaiTro())) {
            return true;
        }
        return checkedInShifts(now).stream()
                .map(LichLamViec::getNhanVien)
                .filter(Objects::nonNull)
                .anyMatch(employee -> Objects.equals(employee.getId(), employeeId) && isCustomerSupportRole(employee));
    }

    private NhanVien findAvailableEmployee(LocalDateTime now) {
        return checkedInShifts(now).stream()
                .map(LichLamViec::getNhanVien)
                .filter(Objects::nonNull)
                .filter(employee -> Byte.valueOf((byte) 1).equals(employee.getTinhTrangLamViec()))
                .filter(this::isCustomerSupportRole)
                .min(Comparator.comparingLong(employee -> conversationRepository
                        .countByNhanVienIdAndTrangThaiIn(employee.getId(), OPEN_STATUSES)))
                .orElse(null);
    }

    private List<LichLamViec> checkedInShifts(LocalDateTime now) {
        return scheduleRepository.findCheckedInShifts(now.toLocalDate()).stream()
                .filter(shift -> shift.getGioBatDau() != null && shift.getGioKetThuc() != null)
                .filter(shift -> !now.toLocalTime().isBefore(shift.getGioBatDau()))
                .filter(shift -> !now.toLocalTime().isAfter(shift.getGioKetThuc()))
                .toList();
    }

    private boolean isCustomerSupportRole(NhanVien employee) {
        if (employee.getVaiTro() == null || employee.getVaiTro().getTenVaiTro() == null) return false;
        String role = employee.getVaiTro().getTenVaiTro();
        return "NhanVien".equalsIgnoreCase(role) || "Nhân viên".equalsIgnoreCase(role);
    }

    private SupportConversation requireByTokenForUpdate(String token) {
        if (token == null || !token.matches("[A-Za-z0-9]{16,64}")) {
            throw new NoSuchElementException("Phiên hỗ trợ không hợp lệ");
        }
        return conversationRepository.findByPublicTokenForUpdate(token)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phiên hỗ trợ"));
    }

    private SupportConversation requireStaffAccess(Integer id, Integer employeeId, boolean admin, boolean lock) {
        SupportConversation conversation = (lock
                ? conversationRepository.findByIdForUpdate(id)
                : conversationRepository.findById(id))
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phiên hỗ trợ"));
        if (CLOSED.equals(conversation.getTrangThai()) && lock) {
            throw new IllegalStateException("Phiên hỗ trợ đã kết thúc");
        }
        if (!admin && conversation.getNhanVien() != null
                && !Objects.equals(conversation.getNhanVien().getId(), employeeId)) {
            throw new SecurityException("Bạn không được truy cập phiên hỗ trợ của nhân viên khác");
        }
        return conversation;
    }

    private void saveMessage(SupportConversation conversation, String senderType, String senderName,
                             String content, LocalDateTime createdAt) {
        messageRepository.save(SupportMessage.builder()
                .conversation(conversation)
                .senderType(senderType)
                .senderName(senderName)
                .content(content)
                .createdAt(createdAt)
                .build());
    }

    private Map<String, Object> customerSnapshot(SupportConversation conversation) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("token", conversation.getPublicToken());
        map.put("status", conversation.getTrangThai());
        map.put("employeeName", conversation.getNhanVien() != null ? conversation.getNhanVien().getHoVaTen() : null);
        map.put("messages", messages(conversation));
        return map;
    }

    private Map<String, Object> staffSummary(SupportConversation conversation, String lastMessage) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", conversation.getId());
        map.put("title", conversation.getTieuDe());
        map.put("status", conversation.getTrangThai());
        map.put("customerName", customerName(conversation.getKhachHang(), conversation.getId()));
        map.put("employeeId", conversation.getNhanVien() != null ? conversation.getNhanVien().getId() : null);
        map.put("employeeName", conversation.getNhanVien() != null ? conversation.getNhanVien().getHoVaTen() : null);
        map.put("createdAt", conversation.getNgayTao());
        map.put("updatedAt", conversation.getNgayCapNhat());
        map.put("lastMessage", lastMessage);
        return map;
    }

    private Map<String, Object> staffSnapshot(SupportConversation conversation) {
        List<SupportMessage> messages = messageRepository.findByConversationIdOrderByIdAsc(conversation.getId());
        String lastMessage = messages.isEmpty() ? "" : messages.get(messages.size() - 1).getContent();
        Map<String, Object> map = staffSummary(conversation, lastMessage);
        map.put("messages", messageMaps(messages));
        map.put("closedAt", conversation.getNgayDong());
        return map;
    }

    private List<Map<String, Object>> messages(SupportConversation conversation) {
        return messageMaps(messageRepository.findByConversationIdOrderByIdAsc(conversation.getId()));
    }

    private List<Map<String, Object>> messageMaps(List<SupportMessage> messages) {
        return messages.stream().map(message -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", message.getId());
            map.put("senderType", message.getSenderType());
            map.put("senderName", message.getSenderName());
            map.put("content", message.getContent());
            map.put("createdAt", message.getCreatedAt());
            return map;
        }).toList();
    }

    private Map<Integer, String> latestMessages(List<SupportConversation> conversations) {
        if (conversations.isEmpty()) return Map.of();
        return messageRepository.findLatestByConversationIds(
                        conversations.stream().map(SupportConversation::getId).toList())
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        message -> message.getConversation().getId(),
                        SupportMessage::getContent
                ));
    }

    private String validateMessage(String value) {
        String message = value == null ? "" : value.trim();
        if (message.length() < 2 || message.length() > 1000) {
            throw new IllegalArgumentException("Tin nhắn phải từ 2 đến 1000 ký tự");
        }
        return message;
    }

    private String customerName(KhachHang customer) {
        return customer != null && customer.getHoVaTen() != null ? customer.getHoVaTen() : "Khách hàng";
    }

    private String customerName(KhachHang customer, Integer conversationId) {
        return customer != null && customer.getHoVaTen() != null
                ? customer.getHoVaTen()
                : "Khách vãng lai #" + conversationId;
    }

    private String cleanName(String value) {
        String name = value == null ? "Nhân viên Zestia" : value.trim();
        return name.isEmpty() ? "Nhân viên Zestia" : abbreviate(name, 150);
    }

    private String abbreviate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }
}
