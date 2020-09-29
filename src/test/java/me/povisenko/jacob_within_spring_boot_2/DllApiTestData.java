package me.povisenko.jacob_within_spring_boot_2;

import me.povisenko.jacob_within_spring_boot_2.data.Account;

public abstract class DllApiTestData {
    //API User
    public static final String API_USER = "TestUser1";
    public static final String API_PWD = "Password3";
    public static final String API_PWD_ENCRYPTED = "3+OnD2eDNTiBGktKT/EOFpmHilL2FqCPP0+XHqTF6HLqIjCaiR4Kd+4=";


    public static final String ACCOUNT_ID = "28963";

    public static Account TEST_ACCOUNT = Account.builder()
                                                .accountId(ACCOUNT_ID)
                                                .apiUsername(API_USER)
                                                .apiPassword(API_PWD_ENCRYPTED)
                                                .build();

}