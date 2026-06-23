package com.wtu.syserver.common.exception;

import com.wtu.syserver.common.enums.MessageEnum;
import com.wtu.syserver.common.enums.StateCodeEnum;

public class ParamInvalidException extends BaseException {

    public ParamInvalidException(MessageEnum message) {
        super(StateCodeEnum.REQUEST_PARAM_ERROR, message);
    }

    public ParamInvalidException(StateCodeEnum code, MessageEnum message) {
        super(code, message);
    }
}
