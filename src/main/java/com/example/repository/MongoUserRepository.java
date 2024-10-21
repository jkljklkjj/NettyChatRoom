package com.example.repository;

import com.example.model.mongo.MongoUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoUserRepository extends MongoRepository<MongoUser, Object> {
    MongoUser findByUserId(int id);
}