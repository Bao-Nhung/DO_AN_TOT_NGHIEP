package com.zestia.datn.zestia;

import com.zestia.datn.zestia.repository.AiChatLogRepository;
import com.zestia.datn.zestia.repository.GiamGiaRepository;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.HuongDanKichThuocRepository;
import com.zestia.datn.zestia.repository.KhachHangRepository;
import com.zestia.datn.zestia.repository.SanPhamChiTietRepository;
import com.zestia.datn.zestia.repository.SanPhamRepository;
import com.zestia.datn.zestia.service.AiChatService;
import com.zestia.datn.zestia.service.PromotionPricingService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiChatServiceProviderFallbackTests {

    @Test
    void providerRejectionKeepsConfiguredTrueAndReportsInternalFallback() throws Exception {
        HttpClient client = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(403);
        when(response.body()).thenReturn("{\"error\":{\"type\":\"unsupported_country_region_territory\",\"code\":\"unsupported_country_region_territory\"}}");
        when(response.headers()).thenReturn(HttpHeaders.of(Map.of("x-request-id", List.of("req_test")), (a, b) -> true));
        when(client.send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        AiChatService service = service(client, "test-api-key");
        Map<String, Object> result = service.reply("xin chao", List.of(), new AiChatService.ChatUser(null, null), "assistant");

        assertThat(result).containsEntry("configured", true)
                .containsEntry("providerAvailable", false)
                .containsEntry("fallback", true);
    }

    @Test
    void blankKeyUsesInternalModeWithoutCallingProvider() throws Exception {
        HttpClient client = mock(HttpClient.class);
        AiChatService service = service(client, "");

        Map<String, Object> result = service.reply("xin chao", List.of(), new AiChatService.ChatUser(null, null), "assistant");

        assertThat(result).containsEntry("configured", false);
        verify(client, never()).send(any(HttpRequest.class), org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    private AiChatService service(HttpClient client, String apiKey) {
        AiChatService service = new AiChatService(
                mock(SanPhamRepository.class),
                mock(SanPhamChiTietRepository.class),
                mock(GiamGiaRepository.class),
                mock(HoaDonRepository.class),
                mock(PromotionPricingService.class),
                mock(AiChatLogRepository.class),
                mock(HuongDanKichThuocRepository.class),
                mock(KhachHangRepository.class)
        );
        ReflectionTestUtils.setField(service, "httpClient", client);
        ReflectionTestUtils.setField(service, "apiKey", apiKey);
        ReflectionTestUtils.setField(service, "model", "gpt-4.1-mini");
        ReflectionTestUtils.setField(service, "endpoint", "https://api.openai.com/v1/responses");
        ReflectionTestUtils.setField(service, "timeoutSeconds", 30L);
        return service;
    }
}
