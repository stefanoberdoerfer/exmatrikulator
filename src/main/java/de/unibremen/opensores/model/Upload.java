package de.unibremen.opensores.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity bean for the Upload class.
 */
@Entity
@Table(name = "UPLOADS")
public class Upload {

    @Id
    @GeneratedValue
    private Long uploadId;

    @Column(nullable = false)
    private Date time;

    private String comment;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String path;

    @ManyToOne
    @JoinColumn(name = "groupId")
    private Group group;

    @ManyToMany
    @JoinTable(name = "UPLOAD_STUDENT",
            joinColumns = {@JoinColumn(name = "uploadId")},
            inverseJoinColumns = {@JoinColumn(name = "studentId")})
    private List<Student> uploaders = new ArrayList<>();

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public List<Student> getUploaders() {
        return uploaders;
    }

    public void setUploaders(List<Student> uploaders) {
        this.uploaders = uploaders;
    }

    public Long getUploadId() {
        return uploadId;
    }
}
