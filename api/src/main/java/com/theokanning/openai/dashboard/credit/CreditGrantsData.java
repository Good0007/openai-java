package com.theokanning.openai.dashboard.credit;

import lombok.Data;

import java.io.Serializable;

/**
 *    "object": "credit_grant",
 *    "id": "6bc529bc-8161-4946-a546-5776a2871a0b",
 *    "grant_amount": 18.0,
 *    "used_amount": 0.03,
 *    "effective_at": 1675900800.0,
 *    "expires_at": 1685577600.0
 */
@Data
public class CreditGrantsData implements Serializable {
    private String object;
    private String id;
    private String  grant_amount;
    private Long  effective_at;
    private Long expires_at;
}
