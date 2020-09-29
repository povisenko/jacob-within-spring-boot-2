package me.povisenko.jacob_within_spring_boot_2.services;

import me.povisenko.jacob_within_spring_boot_2.DllApiTestData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import reactor.util.function.Tuple3;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;
import static reactor.core.publisher.Mono.just;
import static reactor.util.function.Tuples.of;

@SpringBootTest
@TestInstance(PER_CLASS)
class SessionIDServiceTest {

    @SpyBean
    private SessionIDService sessionService;

    @MockBean
    private DllApiService nativeService;

    @Test
    void test_init_with_id_tech_error() {
        when(nativeService.initialiseWithID(any())).thenReturn(just(of(-1, "", "tech error")));

        Tuple3<Boolean, String, HttpStatus> t3 = sessionService.getSessionId(DllApiTestData.ACCOUNT_ID, DllApiTestData.API_USER, DllApiTestData.API_PWD)
                                                               .block();

        Assertions.assertFalse(t3.getT1());
        Assertions.assertEquals("tech error", t3.getT2());
        Assertions.assertEquals(SERVICE_UNAVAILABLE, t3.getT3());
    }

    @Test
    void test_init_with_id_bad_request_1() {
        when(nativeService.initialiseWithID(any())).thenReturn(just(of(2, "", "tech error")));

        Tuple3<Boolean, String, HttpStatus> t3 = sessionService.getSessionId(DllApiTestData.ACCOUNT_ID, DllApiTestData.API_USER, DllApiTestData.API_PWD)
                                                               .block();

        Assertions.assertFalse(t3.getT1());
        Assertions.assertEquals("tech error", t3.getT2());
        Assertions.assertEquals(BAD_REQUEST, t3.getT3());
    }

    @Test
    void test_init_with_id_bad_request_2() {
        when(nativeService.initialiseWithID(any())).thenReturn(just(of(3, "", "tech error")));

        Tuple3<Boolean, String, HttpStatus> t3 = sessionService.getSessionId(DllApiTestData.ACCOUNT_ID, DllApiTestData.API_USER, DllApiTestData.API_PWD)
                                                               .block();

        Assertions.assertFalse(t3.getT1());
        Assertions.assertEquals("tech error", t3.getT2());
        Assertions.assertEquals(BAD_REQUEST, t3.getT3());
    }

    @Test
    void test_logon_tech_error() {
        when(nativeService.initialiseWithID(any())).thenReturn(just(of(1, "loginId", "no error")));
        when(nativeService.logon(any(), any(), any())).thenReturn(just(of(-1, "sessionId", "tech error")));

        Tuple3<Boolean, String, HttpStatus> t3 = sessionService.getSessionId(DllApiTestData.ACCOUNT_ID, DllApiTestData.API_USER, DllApiTestData.API_PWD)
                                                               .block();

        Assertions.assertFalse(t3.getT1());
        Assertions.assertEquals("tech error", t3.getT2());
        Assertions.assertEquals(SERVICE_UNAVAILABLE, t3.getT3());
    }

    @Test
    void test_logon_forbidden_1() {
        when(nativeService.initialiseWithID(any())).thenReturn(just(of(1, "loginId", "no error")));
        when(nativeService.logon(any(), any(), any())).thenReturn(just(of(2, "", "tech error")));

        Tuple3<Boolean, String, HttpStatus> t3 = sessionService.getSessionId(DllApiTestData.ACCOUNT_ID, DllApiTestData.API_USER, DllApiTestData.API_PWD)
                                                               .block();

        Assertions.assertFalse(t3.getT1());
        Assertions.assertEquals("tech error", t3.getT2());
        Assertions.assertEquals(FORBIDDEN, t3.getT3());
    }

    @Test
    void test_logon_forbidden_2() {
        when(nativeService.initialiseWithID(any())).thenReturn(just(of(1, "loginId", "no error")));
        when(nativeService.logon(any(), any(), any())).thenReturn(just(of(4, "", "tech error")));

        Tuple3<Boolean, String, HttpStatus> t3 = sessionService.getSessionId(DllApiTestData.ACCOUNT_ID, DllApiTestData.API_USER, DllApiTestData.API_PWD)
                                                               .block();

        Assertions.assertFalse(t3.getT1());
        Assertions.assertEquals("tech error", t3.getT2());
        Assertions.assertEquals(FORBIDDEN, t3.getT3());
    }

    @Test
    void test_logon_bad_request() {
        when(nativeService.initialiseWithID(any())).thenReturn(just(of(1, "loginId", "no error")));
        when(nativeService.logon(any(), any(), any())).thenReturn(just(of(3, "sessionId", "tech error")));

        Tuple3<Boolean, String, HttpStatus> t3 = sessionService.getSessionId(DllApiTestData.ACCOUNT_ID, DllApiTestData.API_USER, DllApiTestData.API_PWD)
                                                               .block();

        Assertions.assertFalse(t3.getT1());
        Assertions.assertEquals("tech error", t3.getT2());
        Assertions.assertEquals(BAD_REQUEST, t3.getT3());
    }
}