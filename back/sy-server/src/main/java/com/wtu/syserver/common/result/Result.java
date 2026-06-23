package com.wtu.syserver.common.result;

import com.wtu.syserver.common.enums.MessageEnum;
import com.wtu.syserver.common.enums.StateCodeEnum;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(StateCodeEnum.SUCCESS.getCode());
        result.setMessage(StateCodeEnum.SUCCESS.getMessage());
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(StateCodeEnum.SUCCESS.getCode());
        result.setMessage(StateCodeEnum.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(T data, MessageEnum messageEnum) {
        Result<T> result = new Result<>();
        result.setCode(StateCodeEnum.SUCCESS.getCode());
        result.setMessage(messageEnum.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(StateCodeEnum stateCodeEnum, MessageEnum messageEnum) {
        Result<T> result = new Result<>();
        result.setCode(stateCodeEnum.getCode());
        result.setMessage(messageEnum.getMessage());
        return result;
    }

    public static <T> Result<T> error(StateCodeEnum stateCodeEnum, MessageEnum messageEnum, T data) {
        Result<T> result = new Result<>();
        result.setCode(stateCodeEnum.getCode());
        result.setMessage(messageEnum.getMessage());
        result.setData(data);
        return result;
    }
}
