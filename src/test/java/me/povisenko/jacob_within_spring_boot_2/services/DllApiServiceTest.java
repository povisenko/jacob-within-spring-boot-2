package me.povisenko.jacob_within_spring_boot_2.services;

import me.povisenko.jacob_within_spring_boot_2.DllApiTestData;
import me.povisenko.jacob_within_spring_boot_2.repo.AccountRepo;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.util.function.Tuple3;

import static me.povisenko.jacob_within_spring_boot_2.DllApiTestData.ACCOUNT_ID;
import static me.povisenko.jacob_within_spring_boot_2.DllApiTestData.TEST_ACCOUNT;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.util.StringUtils.hasText;

@Tag("local_windows")
@SpringBootTest
@TestInstance(PER_CLASS)
class DllApiServiceTest {

    @Autowired
    private DllApiService service;

    @Autowired
    private SessionIDService sessionService;

    @Autowired
    private AccountRepo accountRepo;

    @BeforeAll
    void init() {
        accountRepo.insert(TEST_ACCOUNT)
                   .block();
    }

    @AfterAll
    void clean() {
        accountRepo.deleteAll()
                   .block();
    }

    @Test
    void testInitWithId() {
        Tuple3<Integer, String, String> t3 = service.initialiseWithID(ACCOUNT_ID)
                                                    .block();

        Assertions.assertEquals(1, t3.getT1()
                                     .intValue());
        Assertions.assertTrue(hasText(t3.getT2()));
        Assertions.assertEquals("no error", t3.getT3());
    }

    @Test
    @Disabled("as should be used in combination with initWithId method")
    void testLogon() {
        Tuple3<Integer, String, String> t3 = service.logon(ACCOUNT_ID, DllApiTestData.API_USER, DllApiTestData.API_PWD)
                                                    .block();

        Assertions.assertEquals(1, t3.getT1()
                                     .intValue());
        Assertions.assertTrue(hasText(t3.getT2()));
        Assertions.assertEquals("no error", t3.getT3());
    }

    @Test
    void test_retrieve_session_id_synced() {
        Tuple3<Integer, String, String> t3 = service.initialiseWithID(ACCOUNT_ID)
                                                    .block();

        Tuple3<Integer, String, String> sessionId = service.logon(t3.getT2(), DllApiTestData.API_USER, DllApiTestData.API_PWD)
                                                           .block();

        Assertions.assertEquals((Integer) 1, sessionId.getT1());
    }


    @Test
    void test_getMatchedUsers() {
        String sessionId = sessionService.getSessionId(ACCOUNT_ID, DllApiTestData.API_USER, DllApiTestData.API_PWD)
                                         .block()
                                         .getT2();

        Tuple3<Integer, String, String> t3 = service.getMatchedUsers(sessionId, "4505577104")
                                                    .block();
        Assertions.assertEquals(1, t3.getT1()
                                     .intValue());
        Assertions.assertTrue(hasText(t3.getT2()));
        Assertions.assertEquals("no error", t3.getT3());
    }
}