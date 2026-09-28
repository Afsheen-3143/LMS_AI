package com.afsheen.aiassistant.lms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Backs human-readable id generation (e.g. "S000003", "CRS004") - MySQL has no native SEQUENCE. */
@Entity
@Table(name = "id_sequence")
public class IdSequence {

    @Id
    @Column(name = "seq_name", length = 30)
    private String seqName;

    @Column(name = "next_val", nullable = false)
    private Long nextVal;

    public String getSeqName() { return seqName; }
    public void setSeqName(String seqName) { this.seqName = seqName; }

    public Long getNextVal() { return nextVal; }
    public void setNextVal(Long nextVal) { this.nextVal = nextVal; }
}
