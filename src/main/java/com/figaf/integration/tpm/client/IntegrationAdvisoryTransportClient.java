package com.figaf.integration.tpm.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.entity.integrationadvisory.transport.ExportRequest;
import com.figaf.integration.tpm.entity.integrationadvisory.transport.ImportArchiveResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.springframework.http.*;

import static com.figaf.integration.tpm.utils.TpmUtils.PATH_FOR_TOKEN;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class IntegrationAdvisoryTransportClient extends TpmBaseClient {

    private static final String EXPORTER_RESOURCE = "/api/1.0/exporter";
    private static final String CONSISTENCY_CHECK_RESOURCE = "/api/1.0/consistencycheck";
    private static final String IMPORTER_RESOURCE = "/api/1.0/importer";

    public IntegrationAdvisoryTransportClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public byte[] export(RequestContext requestContext, ExportRequest exportRequest) {
        log.debug("#export: requestContext = {}, exportRequest = {}", requestContext, exportRequest);
        return executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            EXPORTER_RESOURCE,
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<ExportRequest> requestEntity = new HttpEntity<>(exportRequest, httpHeaders);
                ResponseEntity<byte[]> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, byte[].class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't execute export request. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }

                return responseEntity.getBody();
            }
        );
    }

    public byte[] consistencyCheck(RequestContext requestContext, String request) {
        log.debug("#consistencyCheck: requestContext = {}, request = {}", requestContext, request);
        return executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            CONSISTENCY_CHECK_RESOURCE,
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> requestEntity = new HttpEntity<>(request, httpHeaders);
                ResponseEntity<byte[]> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, byte[].class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't execute consistency check request. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }

                return responseEntity.getBody();
            }
        );
    }

    public ImportArchiveResult importIntegrationAdvisoryObjects(RequestContext requestContext, byte[] zipFile) {
        log.debug("#importIntegrationAdvisoryObjects: requestContext = {}", requestContext);
        return executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            IMPORTER_RESOURCE,
            (url, token, restTemplateWrapper) -> {
                try {
                    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                        .setMode(HttpMultipartMode.LEGACY)
                        .addBinaryBody("artifact", zipFile, ContentType.APPLICATION_OCTET_STREAM, "export.zip")
                        .addTextBody("name", "artifact", ContentType.TEXT_PLAIN);

                    HttpPost post = new HttpPost(url);
                    post.setHeader("X-CSRF-Token", token);
                    post.setEntity(entityBuilder.build());

                    HttpClient client = restTemplateWrapper.getHttpClient();

                    return client.execute(post, response -> {
                        String responseBody = IOUtils.toString(response.getEntity().getContent(), UTF_8);
                        if (is2xxSuccessful(response.getCode())) {
                            return jsonMapper.readValue(responseBody, ImportArchiveResult.class);
                        } else {
                            throw new ClientIntegrationException(format("Can't import integration advisory objects. Code: %s, Message: %s", response.getCode(), responseBody));
                        }
                    });
                } catch (ClientIntegrationException ex) {
                    throw ex;
                } catch (Exception ex) {
                    log.error("Error occurred while importing integration advisory objects {}", ex.getMessage(), ex);
                    throw new ClientIntegrationException("Error occurred while importing integration advisory objects: " + ex.getMessage(), ex);
                }
            }
        );
    }

    private static boolean is2xxSuccessful(int code) {
        return code >= 200 && code < 300;
    }

}
