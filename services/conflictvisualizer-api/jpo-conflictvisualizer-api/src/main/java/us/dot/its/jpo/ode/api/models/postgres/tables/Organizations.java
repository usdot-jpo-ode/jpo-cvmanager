package us.dot.its.jpo.ode.api.models.postgres.tables;

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
@Table(name = "organizations")
public class Organizations {

    @Id
    private int organization_id;
    private String name;
    private String email;

    
}