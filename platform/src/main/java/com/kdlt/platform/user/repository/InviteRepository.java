package com.kdlt.platform.user.repository;

import com.kdlt.platform.user.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {
    Optional<Invite> findByToken(String token);
    java.util.List<Invite> findAllByOrderByDateCreationDesc();
}
