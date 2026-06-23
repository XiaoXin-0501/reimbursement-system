package com.wtu.syserver.common.exception;

import com.wtu.syserver.common.enums.MessageEnum;
import com.wtu.syserver.common.enums.StateCodeEnum;

public class ReimbursementException extends BaseException {
    public ReimbursementException(MessageEnum message) {
        super(StateCodeEnum.REIMBURSE_EXCEPTION, message);
    }

    public ReimbursementException(StateCodeEnum code, MessageEnum message) {
        super(code, message);
    }
}
