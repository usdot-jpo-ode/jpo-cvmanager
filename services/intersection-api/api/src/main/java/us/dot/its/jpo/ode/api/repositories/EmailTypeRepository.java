package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import us.dot.its.jpo.ode.api.models.postgres.tables.EmailType;

import java.util.List;

@Repository
public interface EmailTypeRepository extends JpaRepository<EmailType, Integer> {
    List<EmailType> findAll();

    EmailType findByEmailType(String emailType);
}