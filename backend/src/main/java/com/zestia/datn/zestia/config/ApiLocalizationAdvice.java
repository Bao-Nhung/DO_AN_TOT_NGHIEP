package com.zestia.datn.zestia.config;

import com.zestia.datn.zestia.service.ApiLocalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.*;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiLocalizationAdvice implements ResponseBodyAdvice<Object> {
    private static final Set<String> TRANSLATABLE_KEYS = Set.of(
            "error", "message", "thongBao",
            "loaiVay", "tenLoaiVay",
            "chatLieu", "tenChatLieu",
            "mauSac", "tenMauSac",
            "kichThuoc", "tenKichThuoc",
            "moTaPhom", "trangThaiText", "statusLabel", "caLam",
            "hinhThucThanhToan", "hinhThucNhanHang"
    );
    private static final Set<String> CONTENT_KEYS = Set.of(
            "tenVay", "tenSanPham", "moTa",
            "tieuDe", "noiDung", "tomTat",
            "tenGiamGia", "tenDot", "giamGia",
            "ghiChu", "lyDo", "tinhTrangHang",
            "noiDungDanhGia", "moTaTracking"
    );

    private final ApiLocalizationService localizationService;

    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        String language = request.getHeaders().getFirst("Accept-Language");
        if (language == null || !language.toLowerCase(Locale.ROOT).startsWith("en")) {
            response.getHeaders().set("Content-Language", "vi");
            return body;
        }
        response.getHeaders().set("Content-Language", "en");
        return localize(body, null);
    }

    private Object localize(Object value, String parentKey) {
        if (value == null) return null;
        if (value instanceof String text) {
            if (CONTENT_KEYS.contains(parentKey)) {
                return localizationService.translateKnown(text);
            }
            return TRANSLATABLE_KEYS.contains(parentKey)
                    ? localizationService.translate(text)
                    : text;
        }
        if (value instanceof Map<?, ?> source) {
            Map<Object, Object> localized = new LinkedHashMap<>();
            source.forEach((key, child) -> {
                String keyText = String.valueOf(key);
                localized.put(key, localize(child, keyText));
            });
            return localized;
        }
        if (value instanceof Collection<?> collection) {
            List<Object> localized = new ArrayList<>(collection.size());
            for (Object child : collection) {
                localized.add(localize(child, parentKey));
            }
            return localized;
        }
        if (value.getClass().isArray() && value instanceof Object[] array) {
            return Arrays.stream(array)
                    .map(child -> localize(child, parentKey))
                    .toArray();
        }
        return value;
    }
}
