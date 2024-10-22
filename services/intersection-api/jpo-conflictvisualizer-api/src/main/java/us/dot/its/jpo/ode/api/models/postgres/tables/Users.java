package us.dot.its.jpo.ode.api.models.postgres.tables;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@ToString
@Setter
@EqualsAndHashCode
@Getter
@Entity
@Table(name = "users")
public class Users {

    @Id
    private int user_id;
    private UUID keycloak_id;
    private String email;
    private String first_name;
    private String last_name;
    private long created_timestamp;
    private boolean super_user;
    
}