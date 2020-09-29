package me.povisenko.jacob_within_spring_boot_2.repo;

import me.povisenko.jacob_within_spring_boot_2.data.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepo extends ReactiveMongoRepository<Account, String> {}
