package me.povisenko.jacob_within_spring_boot_2.services.impl;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Variant;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import me.povisenko.jacob_within_spring_boot_2.services.DllApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.util.function.Function;

import static java.lang.Integer.valueOf;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static me.povisenko.jacob_within_spring_boot_2.services.DllApiService.COM_API_Method.*;
import static me.povisenko.jacob_within_spring_boot_2.utils.LogUtil.logNext;
import static reactor.util.function.Tuples.of;

@Slf4j
@Service
@Primary
@Profile({"local_windows"})
@RequiredArgsConstructor
public class DllApiServiceImpl implements DllApiService {

    @Value("${DLL_API_ADDRESS}")
    private String address;

    private final ActiveXComponent dll;

    @Override
    public Mono<Tuple3<Integer, String, String>> initialiseWithID(final String accountId) {

        return Mono.just(format("Calling %s(%s, %s, %s, %s, %s, %s)",//
                                InitialiseWithID, address, accountId, "loginId/out", "error/out", "outcome/out", "sessionId/out"))
                   .doOnEach(logNext(log::info))
                   //invoke COM interface method and extract the result mapping it onto corresponding *Out inner class
                   .map(msg -> invoke(InitialiseWithID, vars -> InitialiseWithIDOut.builder()
                                                                                   .loginId(vars[3].toString())
                                                                                   .error(vars[4].toString())
                                                                                   .outcome(valueOf(vars[5].toString()))
                                                                                   .sessionId(vars[6].toString())
                                                                                   .build(), //
                                      new Variant(address), new Variant(accountId), initRef(), initRef(), initRef(), initRef()))
                   //Handle the response according to the documentation
                   .map(out -> {

                       final String errorVal;

                       switch (out.outcome) {
                           case 2:
                               errorVal = "InitialiseWithID method call failed. DLL API request outcome (response code from server via DLL) = 2 " +//
                                       "(Unable to connect to server due to absent server, or incorrect details)";
                               break;
                           case 3:
                               errorVal = "InitialiseWithID method call failed. DLL API request outcome (response code from server via DLLe) = 3 (Unmatched AccountID)";
                               break;
                           default:
                               errorVal = handleOutcome(out.outcome, out.error, InitialiseWithID);
                       }

                       return of(out, errorVal);
                   })
                   .doOnEach(logNext(t2 -> {
                       InitialiseWithIDOut out = t2.getT1();
                       log.info("{} API call result:\noutcome: {}\nsessionId: {}\nerror: {}\nloginId: {}",//
                                InitialiseWithID, out.outcome, out.sessionId, t2.getT2(), out.loginId);
                   }))
                   .map(t2 -> {
                       InitialiseWithIDOut out = t2.getT1();
                       //out.outcome == 4 auto-login successful, SessionID is retrieved
                       return of(out.outcome, out.outcome == 4 ? out.sessionId : out.loginId, t2.getT2());
                   });
    }

    @Override
    public Mono<Tuple3<Integer, String, String>> logon(final String loginId, final String username, final String password) {
        return Mono.just(format("Calling %s(%s, %s, %s, %s, %s, %s)", //
                                Logon, loginId, username, "password", "sessionID/out", "error/out", "outcome/out"))
                   .doOnEach(logNext(log::info))
                   .map(msg -> invoke(Logon, vars -> LogonOut.builder()
                                                             .sessionId(vars[3].toString())
                                                             .error(vars[4].toString())
                                                             .outcome(valueOf(vars[5].toString()))
                                                             .build(), //
                                      new Variant(loginId), new Variant(username), new Variant(password), initRef(), initRef(), initRef()))
                   .map(out -> {

                       final String errorVal;

                       switch (out.outcome) {
                           case 2:
                               errorVal = "Logon method call failed. DLL API request outcome (response code from server via DLL) = 2 (Expired)";
                               break;
                           case 3:
                               errorVal = "Logon method call failed. DLL API request outcome (response code from server via DLL) = 2 (unsuccessful)";
                               break;
                           case 4:
                               errorVal = "Logon method call failed. DLL API request outcome (response code from server via DLL) = 4 " + //
                                       "(Invalid login ID or login ID does not have access to this DLL API product)";
                               break;
                           default:
                               errorVal = handleOutcome(out.outcome, out.error, Logon);
                       }
                       return of(out, errorVal);
                   })
                   .map(t2 -> of(t2.getT1().outcome, t2.getT1().sessionId, t2.getT2()))
                   .doOnEach(logNext(t3 -> log.info("{} API call result:\noutcome: {}\nsessionId: {}\nerror: {}", //
                                                    Logon, t3.getT1(), t3.getT2(), t3.getT3())));
    }

    @Override
    public Mono<Tuple3<Integer, String, String>> getMatchedUsers(final String sessionId, final String matchTerm) {

        return Mono.just(format("Calling %s(%s, %s, %s, %s, %s)", getMatchedUsers, sessionId, "matchTerm", "matchedList/out", "error/out", "outcome/out"))
                   .doOnEach(logNext(log::info))

                   .map(msg -> invoke(getMatchedUsers, vars -> MatchedUsersOut.builder()
                                                                              .matchedList(vars[2].toString())
                                                                              .error(vars[3].toString())
                                                                              .outcome(valueOf(vars[4].toString()))
                                                                              .build(),//
                                      new Variant(sessionId), new Variant(matchTerm), initRef(), initRef(), initRef()))
                   .map(out -> {
                       final String errorVal;

                       if (out.outcome == 3) {
                           errorVal = "No users";
                       } else {
                           errorVal = handleOutcome(out.outcome, out.error, getMatchedUsers);

                       }
                       return of(out, errorVal);
                   })
                   .map(t2 -> of(t2.getT1().outcome, t2.getT1().matchedList, t2.getT2()))
                   .doOnEach(logNext(t3 -> {
                       log.info("{} API call result:\noutcome: {}\nerror: {}", getMatchedUsers, t3.getT1(), t3.getT3());
                       if (Integer.valueOf(1)
                                  .equals(t3.getT1())) {
                           log.debug("Retrieved Matched Users XML: {}", t3.getT2());
                       }
                   }));

    }

    private static Variant initRef() {
        return new Variant("", true);
    }

    private static String handleOutcome(Integer outcome, String error, COM_API_Method method) {
        switch (outcome) {
            case 1:
                return "no error";
            case 2:
                return format("%s method call failed. DLL API request outcome (response code from server via DLL) = 2 (Access denied)", method);
            default:
                return format("%s method call failed. DLL API request outcome (response code from server via DLL) = %s (server technical error). " + //
                                      "DLL API is temporary unavailable (server behind is down), %s", method, outcome, error);
        }

    }

    /**
     * @param method     to be called in COM interface
     * @param returnFunc maps Variants (references) array onto result object that is to be returned by the method
     * @param vars       arguments required for calling COM interface method
     * @param <T>        type of the result object that is to be returned by the method
     * @return result of the COM API method invocation in defined format
     */
    private <T extends Out> T invoke(COM_API_Method method, Function<Variant[], T> returnFunc, Variant... vars) {
        dll.invoke(method.name(), vars);
        T res = returnFunc.apply(vars);
        asList(vars).forEach(Variant::safeRelease);
        return res;
    }

    @SuperBuilder
    private static abstract class Out {
        final Integer outcome;
        final String error;
    }

    @SuperBuilder
    private static class InitialiseWithIDOut extends Out {
        final String loginId;
        final String sessionId;
    }

    @SuperBuilder
    private static class LogonOut extends Out {
        final Integer outcome;
        final String error;
        final String sessionId;
    }

    @SuperBuilder
    private static class MatchedUsersOut extends Out {
        final String matchedList;
    }
}