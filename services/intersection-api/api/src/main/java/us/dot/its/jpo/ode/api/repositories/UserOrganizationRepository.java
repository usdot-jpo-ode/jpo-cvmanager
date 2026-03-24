package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import us.dot.its.jpo.ode.api.models.postgres.tables.User;
import us.dot.its.jpo.ode.api.models.postgres.tables.UserOrganization;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganization, Integer> {

    @Modifying
    @Transactional
    @Query("DELETE FROM UserOrganization uo WHERE uo.user.email = :email")
    void removeUserOrganizationByEmail(@Param("email") String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserOrganization uo WHERE uo.user.email IN :emails")
    void removeMultipleUserOrganizationsByEmail(@Param("emails") List<String> emails);

    @Query("SELECT uo FROM UserOrganization uo WHERE uo.user.email = :email")
    List<UserOrganization> findAllByEmail(@Param("email") String email);

    Optional<UserOrganization> findByOrganization_Name(String organizationName);

    Optional<UserOrganization> findByUserAndOrganization_Name(User user, String organizationName);

    @Query("SELECT uo.user.email FROM UserOrganization uo WHERE uo.organization.name = :organizationName")
    List<String> findAllUserEmailsByOrganizationName(@Param("organizationName") String organizationName);

    @Query("SELECT DISTINCT u FROM User u WHERE NOT EXISTS " +
            "(SELECT 1 FROM UserOrganization uo WHERE uo.user.id = u.id AND uo.organization.name = :organizationName)")
    List<User> findAllUserEmailsNotInOrganizationName(
            @Param("organizationName") String organizationName);
}
