package me.povisenko.jacob_within_spring_boot_2.services;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

public interface DllApiService {

    /**
     * @param accountId identifier for which we trigger initialisation
     * @return Tuple3 from values of Outcome, SessionID/LoginID, error
     * where by the first argument you can understand what is the result of the API call
     */
    Mono<Tuple3<Integer, String, String>> initialiseWithID(String accountId);

    /**
     * @param loginId  is retrieved before using {@link DllApiService#initialiseWithID(String)} call
     * @param username
     * @param password
     * @return Tuple3 from values of Outcome, SessionID, Error
     * where by the first argument you can understand what is the result of the API call
     */
    Mono<Tuple3<Integer, String, String>> logon(String loginId, String username, String password);

    /**
     * @param sessionId is retrieved before using either
     *                  {@link DllApiService#initialiseWithID(String)} or
     *                  {@link DllApiService#logon(String, String, String)} calls
     * @param matchTerm
     * @return Tuple3 from values of Outcome, MatchedList, Error
     * where by the first argument you can understand what is the result of the API call
     */
    Mono<Tuple3<Integer, String, String>> getMatchedUsers(String sessionId, String matchTerm);

    enum COM_API_Method {
        InitialiseWithID, Logon, getMatchedUsers
    }
}