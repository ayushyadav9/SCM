package com.contact.manager.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.contact.manager.entities.PaymentOrder;

public interface OrderRepository extends JpaRepository<PaymentOrder, Long> {
	public PaymentOrder findByOrderId(String orderId);
}
