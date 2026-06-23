package com.wtu.syserver.common.exception;

import com.wtu.syserver.common.enums.MessageEnum;
import com.wtu.syserver.common.enums.StateCodeEnum;

public class ExpenseDetailException extends BaseException {
    public ExpenseDetailException(MessageEnum message) {
        super(StateCodeEnum.EXPENSES_DETAIL_EXCEPTION, message);
    }

    public ExpenseDetailException(StateCodeEnum code, MessageEnum message) {
        super(code, message);
    }
}
