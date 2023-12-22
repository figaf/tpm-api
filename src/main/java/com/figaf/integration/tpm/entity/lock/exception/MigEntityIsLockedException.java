package com.figaf.integration.tpm.entity.lock.exception;

import com.figaf.integration.tpm.entity.lock.LockInfo;
import lombok.Getter;

@Getter
public class MigEntityIsLockedException extends Exception {

    private final LockInfo lockInfo;
    private final String migId;

    public MigEntityIsLockedException(String message, LockInfo lockInfo, String migId) {
        super(message);
        this.lockInfo = lockInfo;
        this.migId = migId;
    }

    public MigEntityIsLockedException(String message, Throwable cause, LockInfo lockInfo, String migId) {
        super(message, cause);
        this.lockInfo = lockInfo;
        this.migId = migId;
    }
}
