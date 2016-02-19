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
    private String path;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    private Long fileSize;

    public long getBackupId() {
        return backupId;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Date getDate() {
        return new Date(date.getTime());
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDate(Date date) {
        this.date = new Date(date.getTime());
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}
