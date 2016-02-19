package de.unibremen.opensores.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Backup entity bean.
 *
 * @author Lorenz Huether
 */
@Entity
@Table(name = "BACKUPS")
public class Backup {

    @Id
    @GeneratedValue
    private Long backupId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    private Long fileSize;

    private String getName() {
        return name;
    }

    private Date getDate() {
        return date;
    }

    private Long getFileSize() {
        return fileSize;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void set(Date date) {
        this.date = date;
    }

    private void set(Long fileSize) {
        this.fileSize = fileSize;
    }
}
