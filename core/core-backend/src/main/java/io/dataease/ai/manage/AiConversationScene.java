package io.dataease.ai.manage;

import java.util.List;

/** 业务上下文与权限边界。共享引擎不解析或执行 SQL，不持有数据源凭据。 */
public interface AiConversationScene {
    record Resource(String id, String name) { }
    record Context(String resourceId, String resourceName, String revision, String instruction) { }
    String code();
    List<Resource> resources();
    Context resolve(String resourceId, String input);
    /** 经过当前业务权限链复核操作、资源范围及版本；可在受控身份作用域内重复调用。 */
    void authorize(Context context);
}
