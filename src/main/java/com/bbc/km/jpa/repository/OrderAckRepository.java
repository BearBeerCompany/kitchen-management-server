package com.bbc.km.jpa.repository;

import com.bbc.km.jpa.entity.OrderAck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderAckRepository extends JpaRepository<OrderAck, Integer> {

    List<OrderAck> findByAckFalse();

}

