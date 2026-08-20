package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import us.dot.its.jpo.ode.api.models.postgres.tables.UserEmailNotification;

import java.net.InetAddress;
import java.util.List;

@Repository
public interface UserEmailNotificationRepository extends JpaRepository<UserEmailNotification, Integer> {

    @Query("SELECT DISTINCT uen.user.email " +
            "FROM UserEmailNotification uen " +
            "WHERE uen.emailType.emailType = :notificationType " +
            "AND ((:frequency = 'IMMEDIATE' AND uen.immediate = true) " +
            "OR (:frequency = 'HOURLY' AND uen.hourly = true) " +
            "OR (:frequency = 'ONCE_PER_DAY' AND uen.daily = true) " +
            "OR (:frequency = 'ONCE_PER_WEEK' AND uen.weekly = true) " +
            "OR (:frequency = 'ONCE_PER_MONTH' AND uen.monthly = true))")
    List<String> findUsersByNotificationType(@Param("notificationType") String notificationType,
            @Param("frequency") String frequency);

    @Query("SELECT DISTINCT uen.user.email " +
            "FROM UserEmailNotification uen " +
            "JOIN UserOrganization uo ON uen.user.id = uo.user.id " +
            "JOIN RsuOrganization ro ON uo.organization.id = ro.organization.id " +
            "JOIN Rsu r ON ro.rsu.id = r.id " +
            "WHERE uen.emailType.emailType = :notificationType " +
            "AND r.ipv4Address = :rsuIp " +
            "AND ((:frequency = 'IMMEDIATE' AND uen.immediate = true) " +
            "OR (:frequency = 'HOURLY' AND uen.hourly = true) " +
            "OR (:frequency = 'ONCE_PER_DAY' AND uen.daily = true) " +
            "OR (:frequency = 'ONCE_PER_WEEK' AND uen.weekly = true) " +
            "OR (:frequency = 'ONCE_PER_MONTH' AND uen.monthly = true))")
    List<String> findUsersByNotificationTypeAndRsu(@Param("notificationType") String notificationType,
            @Param("frequency") String frequency, @Param("rsuIp") InetAddress rsuIp);

    @Query("SELECT DISTINCT uen.user.email " +
            "FROM UserEmailNotification uen " +
            "JOIN UserOrganization uo ON uen.user.id = uo.user.id " +
            "JOIN Organization o ON uo.organization.id = o.id " +
            "WHERE uen.emailType.emailType = :notificationType " +
            "AND o.name = :organizationName " +
            "AND ((:frequency = 'IMMEDIATE' AND uen.immediate = true) " +
            "OR (:frequency = 'HOURLY' AND uen.hourly = true) " +
            "OR (:frequency = 'ONCE_PER_DAY' AND uen.daily = true) " +
            "OR (:frequency = 'ONCE_PER_WEEK' AND uen.weekly = true) " +
            "OR (:frequency = 'ONCE_PER_MONTH' AND uen.monthly = true))")
    List<String> findUsersByNotificationTypeAndOrganization(@Param("notificationType") String notificationType,
            @Param("frequency") String frequency, @Param("organizationName") String organizationName);

    @Query("SELECT uen " +
            "FROM UserEmailNotification uen " +
            "WHERE uen.user.email = :userEmail")
    List<UserEmailNotification> findNotificationsByUser(@Param("userEmail") String userEmail);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserEmailNotification uen " +
            "WHERE uen.emailType.emailType IN :emailType " +
            "AND uen.user.email = :userEmail")
    void deleteByTypeAndUserEmail(@Param("emailType") List<String> emailTypes, @Param("userEmail") String userEmail);
}