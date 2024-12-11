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
@Table(name = "intersection_organization")
public class IntersectionOrganization {

    @Id
    private int intersection_organization_id;
    private int intersection_id;
    private int organization_id;
    
}