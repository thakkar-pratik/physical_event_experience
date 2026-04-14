package com.example.stadiumflow.repository;

import com.example.stadiumflow.domain.ConcessionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<ConcessionOrder, Long> {
}
