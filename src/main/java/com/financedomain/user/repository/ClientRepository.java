package com.financedomain.user.repository;

import com.financedomain.user.bean.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByNumber(String number);
    boolean existsByNumber(String number);
}
