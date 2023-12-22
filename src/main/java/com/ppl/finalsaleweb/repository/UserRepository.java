package com.ppl.finalsaleweb.repository;

import com.ppl.finalsaleweb.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);



    Optional<User> findByToken(String token);

    User findByUsername(String username);
}

