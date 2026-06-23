package com.wtu.syserver.common.exception;

import com.wtu.syserver.common.enums.MessageEnum;
import com.wtu.syserver.common.enums.StateCodeEnum;

public class SubsidInfoException extends BaseException {
    public SubsidInfoException(MessageEnum message) {
        super(StateCodeEnum.SUBSIDY_INFO_EXCEPTION, message);
    }

    public SubsidInfoException(StateCodeEnum code, MessageEnum message) {
        super(code, message);
    }
}
