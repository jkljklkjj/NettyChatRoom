package com.example.repository;

import org.springframework.stereotype.Repository;
import com.example.model.mongo.MongoGroup;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface MongoGroupRepository extends MongoRepository<MongoGroup, ObjectId> {
    MongoGroup findByGroupid(int groupId);
}
