package com.theokanning.openai.dashboard.credit;

import lombok.Data;

import java.io.Serializable;

@Data
public class DashboardCreditSummary implements Serializable {

    /**
     * {
     *   "object": "credit_summary",
     *   "total_granted": 18.0,
     *   "total_used": 0.03,
     *   "total_available": 17.97,
     *   "grants": {
     *     "object": "list",
     *     "data": [
     *       {
     *         "object": "credit_grant",
     *         "id": "6bc529bc-8161-4946-a546-5776a2871a0b",
     *         "grant_amount": 18.0,
     *         "used_amount": 0.03,
     *         "effective_at": 1675900800.0,
     *         "expires_at": 1685577600.0
     *       }
     *     ]
     *   }
     * }
     */
    private String object;
    private String total_granted;
    private String total_used;
    private CreditGrants grants;
}
