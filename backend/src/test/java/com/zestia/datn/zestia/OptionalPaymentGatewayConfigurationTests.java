package com.zestia.datn.zestia;

import com.zestia.datn.zestia.config.JwtUtil;
import com.zestia.datn.zestia.controller.GatewayPaymentController;
import com.zestia.datn.zestia.repository.HoaDonRepository;
import com.zestia.datn.zestia.repository.LichSuThanhToanRepository;
import com.zestia.datn.zestia.service.GatewayPaymentResultService;
import com.zestia.datn.zestia.service.PaymentRefundService;
import com.zestia.datn.zestia.service.RequestRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Map;

class OptionalPaymentGatewayConfigurationTests {

    @Test
    void applicationBeansStartWhenOptionalZaloPayAppIdIsBlank() {
        new ApplicationContextRunner()
                .withUserConfiguration(GatewayPaymentController.class, PaymentRefundService.class)
                .withBean(HoaDonRepository.class, () -> mock(HoaDonRepository.class))
                .withBean(JwtUtil.class, () -> mock(JwtUtil.class))
                .withBean(GatewayPaymentResultService.class, () -> mock(GatewayPaymentResultService.class))
                .withBean(RequestRateLimiter.class, () -> mock(RequestRateLimiter.class))
                .withBean(LichSuThanhToanRepository.class, () -> mock(LichSuThanhToanRepository.class))
                .withPropertyValues("payment.zalopay.app-id=")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(GatewayPaymentController.class);
                    assertThat(context).hasSingleBean(PaymentRefundService.class);
                    Map<String, Object> methods = context.getBean(GatewayPaymentController.class).paymentMethods();
                    assertThat(available(methods, "MOMO")).isFalse();
                    assertThat(available(methods, "ZALOPAY")).isFalse();
                    assertThat(available(methods, "COD")).isTrue();
                });
    }

    @Test
    void capabilitiesExposeConfiguredGatewaysWithoutExposingSecrets() {
        runner()
                .withPropertyValues(
                        "payment.momo.enabled=true",
                        "payment.momo.partner-code=MOMO",
                        "payment.momo.access-key=test-access",
                        "payment.momo.secret-key=test-secret",
                        "payment.momo.endpoint=https://test-payment.momo.vn/create",
                        "payment.zalopay.enabled=true",
                        "payment.zalopay.app-id=2553",
                        "payment.zalopay.key1=test-key-1",
                        "payment.zalopay.key2=test-key-2",
                        "payment.zalopay.endpoint=https://sb-openapi.zalopay.vn/create"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    Map<String, Object> methods = context.getBean(GatewayPaymentController.class).paymentMethods();
                    assertThat(available(methods, "MOMO")).isTrue();
                    assertThat(available(methods, "ZALOPAY")).isTrue();
                    assertThat(methods.toString()).doesNotContain("test-access", "test-secret", "test-key-1", "test-key-2");
                });
    }

    private ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
                .withUserConfiguration(GatewayPaymentController.class, PaymentRefundService.class)
                .withBean(HoaDonRepository.class, () -> mock(HoaDonRepository.class))
                .withBean(JwtUtil.class, () -> mock(JwtUtil.class))
                .withBean(GatewayPaymentResultService.class, () -> mock(GatewayPaymentResultService.class))
                .withBean(RequestRateLimiter.class, () -> mock(RequestRateLimiter.class))
                .withBean(LichSuThanhToanRepository.class, () -> mock(LichSuThanhToanRepository.class));
    }

    @SuppressWarnings("unchecked")
    private boolean available(Map<String, Object> methods, String method) {
        return Boolean.TRUE.equals(((Map<String, Object>) methods.get(method)).get("available"));
    }
}
