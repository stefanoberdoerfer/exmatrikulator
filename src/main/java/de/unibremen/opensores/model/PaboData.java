package de.unibremen.opensores.model;

import de.unibremen.opensores.model.Student;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PABODATA")
public class PaboData {

    @Id
    @GeneratedValue
    private Long paboDataId;

    public void setPaboData(Long paboDataId) {
        this.paboDataId = paboDataId;
    }

    public Long getPaboDataId() {
        return paboDataId;
    }
}
