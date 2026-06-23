package com.wtu.syserver.common.exception;

import com.wtu.syserver.common.enums.MessageEnum;
import com.wtu.syserver.common.enums.StateCodeEnum;

public class CacheException extends BaseException {

    public CacheException(MessageEnum message) {
        super(StateCodeEnum.CACHE_EXCEPTION, message);
    }

    public CacheException(StateCodeEnum code, MessageEnum message) {
        super(code, message);
    }
}
