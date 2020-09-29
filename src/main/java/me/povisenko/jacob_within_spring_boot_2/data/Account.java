package me.povisenko.jacob_within_spring_boot_2.data;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("Account")
@Document(collection = "accounts")
public class Account {

    @Version
    private Long version;

    /**
     * unique account ID for API, provided by supplier
     * defines restriction for data domain visibility
     * i.e. data from one account is not visible for another
     */
    @Id
    private String accountId;

    /**
     * COM API username, provided by supplier
     */
    private String apiUsername;

    /**
     * COM API password, provided by supplier
     */
    private String apiPassword;


    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedOn;
}