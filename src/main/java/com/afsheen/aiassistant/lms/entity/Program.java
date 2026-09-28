package com.afsheen.aiassistant.lms.entity;

import com.afsheen.aiassistant.entity.base.AuditFields;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "program")
public class Program extends AuditFields {

    @Id
    @Column(name = "program_id", length = 10)
    private String programId;

    @Column(name = "program_title", nullable = false, length = 200)
    private String programTitle;

    @Column(name = "description", columnDefinition = "CLOB")
    private String description;

    public String getProgramId() { return programId; }
    public void setProgramId(String programId) { this.programId = programId; }

    public String getProgramTitle() { return programTitle; }
    public void setProgramTitle(String programTitle) { this.programTitle = programTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
