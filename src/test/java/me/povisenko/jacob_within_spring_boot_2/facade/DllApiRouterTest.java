package me.povisenko.jacob_within_spring_boot_2.facade;

import me.povisenko.jacob_within_spring_boot_2.DllApiTestData;
import me.povisenko.jacob_within_spring_boot_2.repo.AccountRepo;
import me.povisenko.jacob_within_spring_boot_2.services.DllApiService;
import me.povisenko.jacob_within_spring_boot_2.services.SessionIDService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ActiveProfiles("local_unix")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestInstance(PER_CLASS)
class DllApiRouterTest {

    private static final String SESSION_ID = randomUUID().toString();

    @Autowired
    private WebTestClient webTestClient;

    @SpyBean
    private DllApiService dllApiService;

    @SpyBean
    private SessionIDService sessionIDService;

    @Autowired
    private AccountRepo accountRepo;

    @BeforeAll
    void init() {
        StepVerifier.create(accountRepo.insert(DllApiTestData.TEST_ACCOUNT))
                    .expectNextCount(1)
                    .verifyComplete();
    }

    @AfterAll
    void clean() {
        StepVerifier.create(accountRepo.deleteAll())
                    .expectNextCount(0)
                    .verifyComplete();
    }

    @Test
    void get_session_id_requires_valid_account_id() {
        webTestClient.get()
                     .uri("/api/session/1")
                     .accept(APPLICATION_JSON)
                     .exchange()
                     .expectStatus()
                     .isBadRequest();

        verify(sessionIDService, times(0)).getSessionId(anyString(), anyString(), anyString());
    }

    @Test
    void get_session_id_success() {
        when(sessionIDService.getSessionId(anyString(), anyString(), anyString())).thenReturn(Mono.just(Tuples.of(true, "1", HttpStatus.CREATED)));

        webTestClient.get()
                     .uri("/api/session/" + DllApiTestData.ACCOUNT_ID)
                     .accept(APPLICATION_JSON)
                     .exchange()
                     .expectStatus()
                     .isCreated()
                     .expectBody(String.class)
                     .value(s -> Assertions.assertEquals("1", s));

        verify(sessionIDService, times(1)).getSessionId(anyString(), anyString(), anyString());
    }

    @Test
    void get_users_requires_session_id() {
        webTestClient.get()
                     .uri("/api/users/1")
                     .accept(APPLICATION_JSON)
                     .exchange()
                     .expectStatus()
                     .isBadRequest();

        verify(dllApiService, times(0)).getMatchedUsers(any(), any());
    }

    @Test
    void get_users_success() {
        webTestClient.get()
                     .uri("/api/users/Smith")
                     .header("SESSION_ID", SESSION_ID)
                     .accept(APPLICATION_JSON)
                     .exchange()
                     .expectStatus()
                     .isOk();

        verify(dllApiService, times(1)).getMatchedUsers(any(), any());
    }

    @Test
    void get_users_not_found() {
        webTestClient.get()
                     .uri("/api/users/1")
                     .header("SESSION_ID", SESSION_ID)
                     .accept(APPLICATION_JSON)
                     .exchange()
                     .expectStatus()
                     .isNotFound();

        verify(dllApiService, times(1)).getMatchedUsers(any(), any());
    }
}