package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import us.dot.its.jpo.ode.api.models.postgres.tables.RsuCredential;

import java.util.List;
import java.util.Optional;

@Repository
public interface RsuCredentialRepository extends JpaRepository<RsuCredential, Integer> {
    @Query("SELECT rc.nickname FROM RsuCredential rc ORDER BY rc.nickname ASC")
    List<String> findAllNicknames();

    Optional<RsuCredential> findByNickname(String nickname);

    boolean existsByNickname(String nickname);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM RsuCredential r " +
            "JOIN r.ownerOrganization ro " +
            "WHERE r.nickname = :nickname AND ro.name IN :organizations")
    boolean existsByNicknameAndOrganizations(String nickname, List<String> qualifiedOrgList);
}
