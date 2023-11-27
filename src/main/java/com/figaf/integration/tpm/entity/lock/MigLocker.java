package com.figaf.integration.tpm.entity.lock;

import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.tpm.entity.lock.exception.MigEntityIsLockedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.function.BiFunction;

import static com.figaf.integration.tpm.client.TpmBaseClient.MIG_RESOURCE_BY_ID;


@Slf4j
public class MigLocker {

    public static void lockMigObject(
        RequestContext requestContext,
        String migVersionId,
        BiFunction<RequestContext, String, String> requestContextStringStringBiFunction
    ) throws MigEntityIsLockedException {
        try {
            lockOrUnlockMigObject(requestContext, migVersionId, "LOCK", true, false, requestContextStringStringBiFunction);
        } catch (HttpClientErrorException ex) {
            if (HttpStatus.LOCKED.equals(ex.getStatusCode())) {
                LockInfo lockInfo = MigLocker.requestLockInfoForCpiObject(requestContext, migVersionId, requestContextStringStringBiFunction);
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
        RequestContext requestContext,
        String migVersionId,
        String webdav,
        boolean forceLock,
        boolean lockinfo,
        BiFunction<RequestContext, String, String> requestContextStringStringBiFunction
    ) {
        log.debug("#lockOrUnlockPackage(RequestContext requestContext, String migVersionId, String webdav, boolean forceLock, boolean lockinfo): " +
            "{}, {}, {}, {}, {}", requestContext.getConnectionProperties(), migVersionId, webdav, forceLock, lockinfo);

        Assert.notNull(requestContext.getConnectionProperties(), "connectionProperties must be not null!");
        Assert.notNull(migVersionId, "migId must be not null!");

        StringBuilder uriBuilder = new StringBuilder();
        String baseUri = String.format(MIG_RESOURCE_BY_ID, migVersionId);

        uriBuilder.append(baseUri);

        if(forceLock){
            uriBuilder.append("?forcelock=").append(forceLock);
        }
        uriBuilder.append("&webdav=").append(webdav);
        if (lockinfo) {
            uriBuilder.append("&lockinfo=true");
        }
        String finalUri = uriBuilder.toString();

        String rawResponse = requestContextStringStringBiFunction.apply(requestContext, finalUri);
        return rawResponse;
    }

    private static LockInfo requestLockInfoForCpiObject(
        RequestContext requestContext,
        String migVersionId,
        BiFunction<RequestContext, String, String> requestContextStringStringBiFunction
    ) {
        String lockInfoString = lockOrUnlockMigObject(requestContext, migVersionId, "LOCK", true, true, requestContextStringStringBiFunction);
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
}
