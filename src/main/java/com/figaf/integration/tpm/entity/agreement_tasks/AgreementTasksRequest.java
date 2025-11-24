package com.figaf.integration.tpm.entity.agreement_tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.figaf.integration.tpm.entity.agreement_tasks.enums.AgreementTasksAction;

import java.util.List;

import static com.figaf.integration.tpm.entity.agreement_tasks.enums.AgreementTasksAction.DEPLOY_V2;
import static com.figaf.integration.tpm.entity.agreement_tasks.enums.AgreementTasksAction.REDEPLOY_V2;
import static com.figaf.integration.tpm.entity.agreement_tasks.enums.AgreementTasksAction.UNDEPLOY_V2;
import static com.figaf.integration.tpm.entity.agreement_tasks.enums.AgreementTasksArtifactType.TRADING_PARTNER_AGREEMENT_V2;

public record AgreementTasksRequest(
    @JsonProperty("Action") AgreementTasksAction action,
    @JsonProperty("Description") String description,
    @JsonProperty("TaskParameters") TaskParameters taskParameters,
    @JsonProperty("TaskInput") TaskInput taskInput
) {
    public static AgreementTasksRequest createDeployRequest(String agreementId) {
        return new AgreementTasksRequest(
            DEPLOY_V2,
            "",
            new TaskParameters(List.of()),
            new TaskInput(agreementId, "", TRADING_PARTNER_AGREEMENT_V2, "", "2.0")
        );
    }

    public static AgreementTasksRequest createUndeployRequest(String agreementId) {
        return new AgreementTasksRequest(
            UNDEPLOY_V2,
            "",
            new TaskParameters(List.of()),
            new TaskInput(agreementId, "", TRADING_PARTNER_AGREEMENT_V2, "", "2.0")
        );
    }

    public static AgreementTasksRequest createRedeployRequest(String agreementId, List<String> btList) {
        return new AgreementTasksRequest(
            REDEPLOY_V2,
            "",
            new TaskParameters(btList),
            new TaskInput(agreementId, "", TRADING_PARTNER_AGREEMENT_V2, "", "2.0")
        );
    }

}
