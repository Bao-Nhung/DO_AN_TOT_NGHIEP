package com.zestia.datn.zestia.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LuckyWheelIconServiceTests {
    @TempDir
    Path tempDirectory;

    @Test
    void validPngIsStoredUnderManagedDirectory() throws Exception {
        byte[] png = new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0, 0, 0};
        MockMultipartFile file = new MockMultipartFile("file", "gift.png", "image/png", png);
        LuckyWheelIconService service = new LuckyWheelIconService(tempDirectory);

        Map<String, String> result = service.store(file);

        String url = result.get("url");
        assertNotNull(url);
        assertTrue(url.matches("^/images/lucky-wheel/prize_[a-f0-9]{32}\\.png$"));
        assertTrue(Files.exists(tempDirectory.resolve(Path.of(url).getFileName())));
    }

    @Test
    void fileExtensionCannotDisguiseNonImageContent() {
        MockMultipartFile file = new MockMultipartFile("file", "gift.png", "image/png",
                "not a real image".getBytes());
        LuckyWheelIconService service = new LuckyWheelIconService(tempDirectory);

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.store(file));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertFalse(Files.exists(tempDirectory.resolve("gift.png")));
    }
}
