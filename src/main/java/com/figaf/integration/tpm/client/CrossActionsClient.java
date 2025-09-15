package com.figaf.integration.tpm.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.entity.crossactions.ImportRequest;
import com.figaf.integration.tpm.entity.crossactions.ImportTaskResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.json.JSONObject;
import org.springframework.http.*;

import java.io.File;

import static com.figaf.integration.tpm.utils.TpmUtils.PATH_FOR_TOKEN;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class CrossActionsClient extends TpmBaseClient {

    private static final String X_CSRF_TOKEN = "X-CSRF-Token";

    public CrossActionsClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public String uploadAgreementsArchive(RequestContext requestContext, byte[] zipFile) {
        log.debug("#uploadAgreementsArchive: requestContext = {}", requestContext);
        return executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            CROSS_ACTIONS_UPLOAD_ARCHIVE_RESOURCE,
            (url, token, restTemplateWrapper) -> {
                try {
                    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                        .setMode(HttpMultipartMode.LEGACY)
                        .addBinaryBody("datum", zipFile, ContentType.create("application/x-zip-compressed"), "archive.zip")
                        .addTextBody("name", "archive.zip", ContentType.TEXT_PLAIN);

                    HttpPost post = new HttpPost(url);
                    post.setHeader(X_CSRF_TOKEN, token);
                    post.setEntity(entityBuilder.build());

                    HttpClient client = restTemplateWrapper.getHttpClient();

                    return client.execute(post, response -> {
                        String responseBody = IOUtils.toString(response.getEntity().getContent(), UTF_8);
                        if (response.getCode() == 201) {
                            JSONObject jsonObject = new JSONObject(responseBody);
                            return jsonObject.getString("id");
                        } else {
                            throw new ClientIntegrationException(format("Can't upload agreements archive. Code: %s, Message: %s", response.getCode(), responseBody));
                        }
                    });
                } catch (ClientIntegrationException ex) {
                    throw ex;
                } catch (Exception ex) {
                    log.error("Error occurred while uploading agreements archive {}", ex.getMessage(), ex);
                    throw new ClientIntegrationException("Error occurred while uploading agreements archive: " + ex.getMessage(), ex);
                }
            });
    }

    public String executeAgreementsArchiveImport(RequestContext requestContext, ImportRequest importRequest) {
        log.debug("#executeAgreementsArchiveImport: requestContext = {}, importRequest = {}", requestContext, importRequest);
        return executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            CROSS_ACTIONS_EXECUTE_IMPORT_RESOURCE,
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ImportRequest> requestEntity = new HttpEntity<>(importRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Can't execute agreements archive import. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }

                JSONObject jsonObject = new JSONObject(responseEntity.getBody());
                return jsonObject.getString("TaskId");
            }
        );
    }

    public ImportTaskResult getTaskStatus(RequestContext requestContext, String taskId) {
        log.debug("#getTaskStatus: requestContext = {}, taskId = {}", requestContext, taskId);
        return executeGet(
            requestContext,
            format(CROSS_ACTIONS_TASK_STATUS_RESOURCE, taskId),
            response -> jsonMapper.readValue(response, ImportTaskResult.class)
        );
    }

}
