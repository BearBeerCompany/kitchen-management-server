package com.bbc.km.jpa.service;

import com.bbc.km.jpa.entity.OrderAck;
import com.bbc.km.jpa.repository.OrderAckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderAckService {

    private OrderAckRepository orderAckRepository;

    public OrderAckService(OrderAckRepository orderAckRepository) {
        this.orderAckRepository = orderAckRepository;
    }

    public List<OrderAck> getAllOrders() {
        return orderAckRepository.findAll();
    }

    public Optional<OrderAck> getOrderById(int id) {
        return orderAckRepository.findById(id);
    }

    @Transactional
    public OrderAck saveOrder(OrderAck order) {
        return orderAckRepository.save(order);
    }

    public void deleteOrder(int id) {
        orderAckRepository.deleteById(id);
    }

    public List<OrderAck> getUnAckOrders() {
        return orderAckRepository.findByAckFalse();
    }
}

