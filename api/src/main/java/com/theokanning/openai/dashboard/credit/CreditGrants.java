package com.theokanning.openai.dashboard.credit;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CreditGrants implements Serializable {
    private String object;
    private List<CreditGrantsData> data;
}
