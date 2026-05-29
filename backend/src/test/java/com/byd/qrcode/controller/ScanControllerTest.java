package com.byd.qrcode.controller;

import com.byd.qrcode.entity.ScanRecord;
import com.byd.qrcode.security.RateLimitService;
import com.byd.qrcode.service.ScanRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ScanControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ScanRecordService scanRecordService;

    @BeforeEach
    void setUp() {
        ScanController controller = new ScanController(scanRecordService, new RateLimitService());
        ReflectionTestUtils.setField(controller, "scanMaxRequests", 1);
        ReflectionTestUtils.setField(controller, "scanWindowSeconds", 60L);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void blocksRepeatedH5ScanEventsFromSameClientWithinWindow() throws Exception {
        when(scanRecordService.recordFromH5(eq(123), any(), any())).thenReturn(new ScanRecord());

        mockMvc.perform(post("/api/scans/h5")
                        .param("qid", "123")
                        .header("X-Real-IP", "1.2.3.4"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/scans/h5")
                        .param("qid", "123")
                        .header("X-Real-IP", "1.2.3.4"))
                .andExpect(status().isTooManyRequests());
    }
}
