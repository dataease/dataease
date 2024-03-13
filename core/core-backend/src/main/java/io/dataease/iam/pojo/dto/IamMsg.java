package io.dataease.iam.pojo.dto;

import lombok.*;

/**
 * Description: IamMsg
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class IamMsg {

    /**
     * 响应状态码
     */
    private int errorNumber;

    /**
     * 响应信息
     */
    private String errors;

    /**
     * 静态方法方便通过类名调用,返回值为Msg当前对象，便于链式操作
     */
    public static IamMsg success() {
        IamMsg msg = new IamMsg();
        msg.setErrorNumber(0);
        msg.setErrors("操作成功");
        return msg;
    }

    public static IamMsg fail(String message) {
        IamMsg msg = new IamMsg();
        msg.setErrorNumber(500);
        msg.setErrors("处理失败:"+message);
        return msg;
    }

    public static IamMsg failUser() {
        IamMsg msg = new IamMsg();
        msg.setErrorNumber(500);
        msg.setErrors("用户信息已存在");
        return msg;
    }
}
