package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import us.dot.its.jpo.ode.api.models.postgres.tables.SnmpCredential;

import java.util.List;
import java.util.Optional;

@Repository
public interface SnmpCredentialRepository extends JpaRepository<SnmpCredential, Integer> {
    @Query("SELECT sc.nickname FROM SnmpCredential sc ORDER BY sc.nickname ASC")
    List<String> findAllNicknames();

    Optional<SnmpCredential> findByNickname(String nickname);

    boolean existsByNickname(String nickname);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM SnmpCredential s " +
            "JOIN s.ownerOrganization ro " +
            "WHERE s.nickname = :nickname AND ro.name IN :organizations")
    boolean existsByNicknameAndOrganizations(String nickname, List<String> qualifiedOrgList);
}
