package com.wtu.syserver.common.exception;

import com.wtu.syserver.common.enums.MessageEnum;
import com.wtu.syserver.common.enums.StateCodeEnum;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final StateCodeEnum code;
    private final MessageEnum messageEnum;

    public BaseException(StateCodeEnum code, MessageEnum messageEnum) {
        super(messageEnum.getMessage());
        this.code = code;
        this.messageEnum = messageEnum;
    }
}
