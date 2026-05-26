package com.byd.qrcode.controller;

import com.byd.qrcode.entity.QrcodeRecord;
import com.byd.qrcode.service.QrcodeGeneratorService;
import com.byd.qrcode.service.QrcodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class QrcodeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private QrcodeService qrcodeService;

    @Mock
    private QrcodeGeneratorService qrcodeGeneratorService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new QrcodeController(qrcodeService, qrcodeGeneratorService))
                .build();
    }

    @Test
    void imageReturnsGeneratedPngForPreview() throws Exception {
        QrcodeRecord record = new QrcodeRecord();
        record.setId(123);
        record.setJumpPageUrl("https://example.com/jump?qid=123");

        byte[] png = new byte[] {(byte) 0x89, 'P', 'N', 'G'};

        when(qrcodeService.getById(123)).thenReturn(record);
        when(qrcodeGeneratorService.generate(record.getJumpPageUrl())).thenReturn(png);

        mockMvc.perform(get("/api/qrcodes/123/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(png));
    }

    @Test
    void imageWithDownloadFlagReturnsAttachmentHeader() throws Exception {
        QrcodeRecord record = new QrcodeRecord();
        record.setId(123);
        record.setJumpPageUrl("https://example.com/jump?qid=123");

        byte[] png = new byte[] {(byte) 0x89, 'P', 'N', 'G'};

        when(qrcodeService.getById(123)).thenReturn(record);
        when(qrcodeGeneratorService.generate(record.getJumpPageUrl())).thenReturn(png);

        mockMvc.perform(get("/api/qrcodes/123/image").param("download", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("qrcode-123.png")))
                .andExpect(content().bytes(png));
    }
}
