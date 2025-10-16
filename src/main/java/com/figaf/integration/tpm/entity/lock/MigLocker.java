package com.figaf.integration.tpm.entity.lock;

import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.tpm.entity.lock.exception.MigEntityIsLockedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static com.figaf.integration.tpm.client.MigClient.MIG_RESOURCE_BY_ID;
import static java.lang.String.format;
import static org.springframework.http.HttpStatus.OK;


@Slf4j
public class MigLocker {

    public static void lockMigObject(
        RequestContext requestContext,
        String userApiCsrfToken,
        RestTemplate restTemplate,
        String migVersionId
    ) throws MigEntityIsLockedException {
        try {
            lockOrUnlockMigObject(requestContext.getConnectionProperties(), "LOCK", false, userApiCsrfToken, restTemplate, format(MIG_RESOURCE_BY_ID, migVersionId));
        } catch (HttpClientErrorException ex) {
            if (HttpStatus.LOCKED.equals(ex.getStatusCode())) {
                LockInfo lockInfo = MigLocker.requestLockInfoForCpiObject(requestContext, userApiCsrfToken, restTemplate, migVersionId);
                if (lockInfo.isLocked() && !lockInfo.isCurrentUserHasLock()) {
                    throw new MigEntityIsLockedException(String.format("Locked by another user: %s", lockInfo.getLockedBy()), lockInfo, migVersionId);
                }
                if (lockInfo.isLocked() && !lockInfo.isCurrentSessionHasLock()) {
                    throw new MigEntityIsLockedException(String.format("Locked by current user (%s) but in another session", lockInfo.getLockedBy()), lockInfo, migVersionId);
                }
                throw new MigEntityIsLockedException(String.format("Can't lock a mig. Lock info: %s", lockInfo), ex, lockInfo, migVersionId);
            } else {
                throw new ClientIntegrationException(String.format("Can't lock a mig: %s", ex.getResponseBodyAsString()));
            }
        }
    }

    private static String lockOrUnlockMigObject(
        ConnectionProperties connectionProperties,
        String webdav,
        boolean lockinfo,
        String userApiCsrfToken,
        RestTemplate restTemplate,
        String urlOfLockOrUnlockCpiObject
    ) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
            .scheme(connectionProperties.getProtocol())
            .host(connectionProperties.getHost())
            .path(urlOfLockOrUnlockCpiObject);
        if (lockinfo) {
            uriBuilder.queryParam("lockinfo", "true");
        }
        uriBuilder.queryParam("webdav", webdav);

        if (StringUtils.isNotEmpty(connectionProperties.getPort())) {
            uriBuilder.port(connectionProperties.getPort());
        }

        URI lockOrUnlockArtifactUri = uriBuilder.build().toUri();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-CSRF-Token", userApiCsrfToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
            lockOrUnlockArtifactUri,
            HttpMethod.PUT,
            requestEntity,
            String.class
        );

        if (!OK.equals(responseEntity.getStatusCode())) {
            throw new RuntimeException("Couldn't lock or unlock mig object\n" + responseEntity.getBody());
        }

        return responseEntity.getBody();
    }

    private static LockInfo requestLockInfoForCpiObject(
        RequestContext requestContext,
        String userApiCsrfToken,
        RestTemplate restTemplate,
        String migVersionId
    ) {
        String lockInfoString = lockOrUnlockMigObject(requestContext.getConnectionProperties(), "LOCK", true, userApiCsrfToken, restTemplate, format(MIG_RESOURCE_BY_ID, migVersionId));
        return getLockInfo(lockInfoString);
    }

    private static LockInfo getLockInfo(String lockInfoString) {
        JSONObject jsonObject = new JSONObject(lockInfoString);
        LockInfo lockInfo = new LockInfo();
        lockInfo.setCurrentSessionHasLock(jsonObject.optBoolean("isCurrentSessionHasLock"));
        lockInfo.setCurrentUserHasLock(jsonObject.optBoolean("isCurrentUserHasLock"));
        lockInfo.setLocked(jsonObject.optBoolean("isLocked"));
        lockInfo.setPublishToCatalogAllowed(jsonObject.optBoolean("isPublishToCatalogAllowed"));
        lockInfo.setResourceEditOpted(jsonObject.optBoolean("isResourceEditOpted"));
        lockInfo.setLockedBy(jsonObject.optString("lockedBy"));
        lockInfo.setLockedTime(jsonObject.optString("lockedTime"));
        return lockInfo;
    }

    public static void unlockMig(
        RequestContext requestContext,
        String userApiCsrfToken,
        RestTemplate restTemplate,
        String migVersionId
    ) {
        lockOrUnlockMigObject(requestContext.getConnectionProperties(), "UNLOCK", false, userApiCsrfToken, restTemplate, format(MIG_RESOURCE_BY_ID, migVersionId));
    }
}
