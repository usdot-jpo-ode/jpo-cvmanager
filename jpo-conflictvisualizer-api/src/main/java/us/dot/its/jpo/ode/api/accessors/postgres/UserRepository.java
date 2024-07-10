package us.dot.its.jpo.ode.api.accessors.postgres;


import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import us.dot.its.jpo.ode.api.models.postgres.tables.Users;

public interface UserRepository extends JpaRepository<Users, UUID> {

}