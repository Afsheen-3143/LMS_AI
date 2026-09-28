package com.afsheen.aiassistant.lms.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "program_fee_setting")
public class ProgramFeeSetting {

    /** Same value as the owning program's id - one current fee setting per program. */
    @Id
    @Column(name = "program_id", length = 10)
    private String programId;

    @Column(name = "duration", length = 50)
    private String duration;

    @Column(name = "fee", precision = 12, scale = 2)
    private BigDecimal fee;

    @Column(name = "discount", precision = 12, scale = 2)
    private BigDecimal discount;

    public String getProgramId() { return programId; }
    public void setProgramId(String programId) { this.programId = programId; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
}
