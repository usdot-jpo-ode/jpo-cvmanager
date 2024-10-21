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
@Table(name = "rsu_organization")
public class RsuOrganization {

    @Id
    private int rsu_organization_id;
    private UUID rsu_id;
    private int organization_id;
    
}