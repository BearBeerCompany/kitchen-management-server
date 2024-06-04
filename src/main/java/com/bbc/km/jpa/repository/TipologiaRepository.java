package com.bbc.km.jpa.repository;

import com.bbc.km.jpa.entity.Tipologia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipologiaRepository extends JpaRepository<Tipologia, Integer> {
}
