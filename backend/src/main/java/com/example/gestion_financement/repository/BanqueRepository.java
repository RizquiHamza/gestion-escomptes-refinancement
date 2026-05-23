package com.example.gestion_financement.repository;

import com.example.gestion_financement.entity.Banque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BanqueRepository extends JpaRepository<Banque, Long> {

    Optional<Banque> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByNom(String nom);
}
