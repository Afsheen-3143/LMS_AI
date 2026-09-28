package com.afsheen.aiassistant.lms.dto.response;

import java.math.BigDecimal;

public class CourseFeeHistoryResponse {

    private BigDecimal fee;
    private BigDecimal discount;

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
}
