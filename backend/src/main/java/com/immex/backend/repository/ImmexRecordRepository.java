package com.immex.backend.repository;

import com.immex.backend.model.ImmexRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ImmexRecordRepository extends MongoRepository<ImmexRecord, String> {}
