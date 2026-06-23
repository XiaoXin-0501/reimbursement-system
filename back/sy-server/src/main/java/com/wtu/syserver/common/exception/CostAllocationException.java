package com.wtu.syserver.common.exception;

import com.wtu.syserver.common.enums.MessageEnum;
import com.wtu.syserver.common.enums.StateCodeEnum;

public class CostAllocationException extends BaseException {
    public CostAllocationException(MessageEnum message) {
        super(StateCodeEnum.COST_ALLOCATION_EXCEPTION, message);
    }

    public CostAllocationException(StateCodeEnum code, MessageEnum message) {
        super(code, message);
    }
}
