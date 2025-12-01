package com.figaf.integration.tpm.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.entity.agreement_tasks.AgreementTaskResponse;
import com.figaf.integration.tpm.entity.agreement_tasks.AgreementTasksRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.figaf.integration.tpm.utils.TpmUtils.PATH_FOR_TOKEN;
import static java.lang.String.format;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Client for executing agreement task operations (deploy, undeploy, redeploy, checking task status) via TPM API.
 * <p>
 * Uses atomic task operations for managing agreement lifecycle states.
 */
@Slf4j
public class AgreementTasksClient extends TpmBaseClient {

    private static final String AGREEMENT_TASKS_ATOMIC_RESOURCE = "/itspaces/tpm/api/2.0/tasks.atomic";
    private static final String AGREEMENT_TASKS_ATOMIC_RESOURCES = "/itspaces/tpm/api/2.0/tasks.atomic/%s";

    public AgreementTasksClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    /**
     * Retrieves the current status of an agreement task by its task ID.
     *
     * @param requestContext the context containing request authentication and configuration
     * @param taskId         the unique identifier of the task to check
     * @return AgreementTaskResponse containing the current task status and details
     * @throws ClientIntegrationException if the API request fails or response parsing fails
     * @apiNote Makes a GET request to: /itspaces/tpm/api/2.0/tasks.atomic/{taskId}
     */
    public AgreementTaskResponse checkAgreementStatus(RequestContext requestContext, String taskId) {
        log.debug("#checkAgreementStatus: requestContext = {}, taskId = {}", requestContext, taskId);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            AGREEMENT_TASKS_ATOMIC_RESOURCES.formatted(taskId),
            response -> jsonMapper.readValue(response, AgreementTaskResponse.class)
        );
    }

    /**
     * Deploys agreement task (activation operation).
     * Typically used when activating an agreement in the system.
     *
     * @param requestContext the context containing request authentication and configuration
     * @param agreementId    the ID of the agreement being deployed
     * @return AgreementTaskResponse containing the task execution result
     * @throws ClientIntegrationException if the API request fails or response parsing fails
     * @apiNote Corresponds to ACTIVATE button in SAP SUIT UI → DEPLOY_V2 operation
     * @apiNote Makes a POST request to: /itspaces/tpm/api/2.0/tasks.atomic
     */
    public AgreementTaskResponse deployAgreement(RequestContext requestContext, String agreementId) {
        log.debug("#deployAgreement: requestContext = {}, agreementId = {}", requestContext, agreementId);

        return executeAgreementTasks(requestContext, AgreementTasksRequest.createDeployRequest(agreementId));
    }

    /**
     * Undeploys agreement task (deactivation operation).
     * Typically used when deactivating an agreement in the system.
     *
     * @param requestContext the context containing request authentication and configuration
     * @param agreementId    the ID of the agreement being undeployed
     * @return AgreementTaskResponse containing the task execution result
     * @throws ClientIntegrationException if the API request fails or response parsing fails
     * @apiNote Corresponds to DEACTIVATE button in SAP SUIT UI → UNDEPLOY_V2 operation
     * @apiNote Makes a POST request to: /itspaces/tpm/api/2.0/tasks.atomic
     */
    public AgreementTaskResponse undeployAgreement(RequestContext requestContext, String agreementId) {
        log.debug("#undeployAgreement: requestContext = {}, agreementId = {}", requestContext, agreementId);

        return executeAgreementTasks(requestContext, AgreementTasksRequest.createUndeployRequest(agreementId));
    }

    /**
     * Redeploys agreement task (update operation).
     * Typically used when updating an existing agreement in the system.
     *
     * @param requestContext the context containing request authentication and configuration
     * @param agreementId    the ID of the agreement being redeployed
     * @param transactionIds list of transaction IDs affected by the redeploy operation
     * @return AgreementTaskResponse containing the task execution result
     * @throws ClientIntegrationException if the API request fails or response parsing fails
     * @apiNote Corresponds to UPDATE button in SAP SUIT UI → REDEPLOY_V2 operation
     * @apiNote Makes a POST request to: /itspaces/tpm/api/2.0/tasks.atomic
     */
    public AgreementTaskResponse redeployAgreement(RequestContext requestContext, String agreementId, List<String> transactionIds) {
        log.debug("#redeployAgreement: requestContext = {},  agreementId = {}, transactionIds={}", requestContext, agreementId, transactionIds);

        return executeAgreementTasks(requestContext, AgreementTasksRequest.createRedeployRequest(agreementId, transactionIds));
    }

    /**
     * Executes agreement task by sending a payload to the atomic tasks endpoint.
     * This is the underlying method used by deploy, undeploy, and redeploy operations.
     *
     * @param requestContext        the context containing request authentication and configuration
     * @param agreementTasksRequest the agreement task request object
     * @return AgreementTaskResponse containing the task execution result
     * @throws ClientIntegrationException if the API request fails or response parsing fails
     * @apiNote Internal workhorse method for all task execution operations
     * @apiNote Makes a POST request to: /itspaces/tpm/api/2.0/tasks.atomic
     */
    private AgreementTaskResponse executeAgreementTasks(RequestContext requestContext, AgreementTasksRequest agreementTasksRequest) {
        log.debug("#executeAgreementTasks: requestContext = {}, agreementTasksRequest = {}", requestContext, agreementTasksRequest);

        return executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            AGREEMENT_TASKS_ATOMIC_RESOURCE,
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(APPLICATION_JSON);
                HttpEntity<AgreementTasksRequest> requestEntity = new HttpEntity<>(agreementTasksRequest, httpHeaders);
                ResponseEntity<AgreementTaskResponse> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, POST, requestEntity, AgreementTaskResponse.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format("Couldn't process agreement tasks. Code: %d, Message: %s", responseEntity.getStatusCode().value(), requestEntity.getBody()));
                }
                return responseEntity.getBody();
            }
        );
    }
}
