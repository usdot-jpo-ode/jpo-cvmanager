package us.dot.its.jpo.ode.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
            "OR (:frequency = 'DAILY' AND uen.daily = true) " +
            "OR (:frequency = 'WEEKLY' AND uen.weekly = true) " +
            "OR (:frequency = 'MONTHLY' AND uen.monthly = true))")
    List<String> findUsersByNotificationType(@Param("notificationType") String notificationType,
            @Param("frequency") String frequency);

    @Query("SELECT DISTINCT uen.user.email " +
            "FROM UserEmailNotification uen " +
            "JOIN UserOrganization uo " +
            "JOIN RsuOrganization ro " +
            "JOIN Rsu r " +
            "WHERE uen.emailType.emailType = :notificationType " +
            "AND r.ipv4Address = :rsuIp " +
            "AND ((:frequency = 'IMMEDIATE' AND uen.immediate = true) " +
            "OR (:frequency = 'HOURLY' AND uen.hourly = true) " +
            "OR (:frequency = 'DAILY' AND uen.daily = true) " +
            "OR (:frequency = 'WEEKLY' AND uen.weekly = true) " +
            "OR (:frequency = 'MONTHLY' AND uen.monthly = true))")
    List<String> findUsersByNotificationTypeAndRsu(@Param("notificationType") String notificationType,
            @Param("frequency") String frequency, @Param("rsuIp") InetAddress rsuIp);

    @Query("SELECT DISTINCT uen.user.email " +
            "FROM UserEmailNotification uen " +
            "JOIN UserOrganization uo " +
            "JOIN Organization o " +
            "WHERE uen.emailType.emailType = :notification_type " +
            "AND o.name = :organizationName " +
            "AND ((:frequency = 'IMMEDIATE' AND uen.immediate = true) " +
            "OR (:frequency = 'HOURLY' AND uen.hourly = true) " +
            "OR (:frequency = 'DAILY' AND uen.daily = true) " +
            "OR (:frequency = 'WEEKLY' AND uen.weekly = true) " +
            "OR (:frequency = 'MONTHLY' AND uen.monthly = true))")
    List<String> findUsersByNotificationTypeAndOrganization(@Param("notificationType") String notificationType,
            @Param("frequency") String frequency, @Param("organizationName") String organizationName);
}