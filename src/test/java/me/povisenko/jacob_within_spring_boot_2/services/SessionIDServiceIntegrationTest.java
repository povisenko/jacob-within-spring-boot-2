package me.povisenko.jacob_within_spring_boot_2.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import reactor.util.function.Tuple3;

import static me.povisenko.jacob_within_spring_boot_2.DllApiTestData.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.util.StringUtils.hasText;

@SpringBootTest
@TestInstance(PER_CLASS)
class SessionIDServiceIntegrationTest {

    @SpyBean
    private SessionIDService sessionService;

    @SpyBean
    private DllApiService nativeService;

    @Test
    void test_retrieve_session_id() {
        Tuple3<Boolean, String, HttpStatus> t3 = sessionService.getSessionId(ACCOUNT_ID, API_USER, API_PWD)
                                                               .block();

        verify(nativeService, times(1)).initialiseWithID(ACCOUNT_ID);
        verify(nativeService, times(1)).logon(any(), eq(API_USER), eq(API_PWD));

        Assertions.assertTrue(t3.getT1());
        Assertions.assertTrue(hasText(t3.getT2()));
        Assertions.assertEquals(OK, t3.getT3());
    }
}