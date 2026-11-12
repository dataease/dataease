package io.dataease.ai.manage;

import java.util.List;
import java.util.Map;
import io.dataease.ai.support.AiToolProtocol;
import io.dataease.ai.support.AiErrors;
import io.dataease.api.ai.conversation.AiConversationDtos;

/** 业务上下文与权限边界。共享引擎不解析或执行 SQL，不持有数据源凭据。 */
public interface AiConversationScene {
    record Resource(String id, String name) { }
    /** Trusted scene declaration after current business authorization and canonical metadata resolution. */
    record ResourceClaim(String datasourceId, String table, String operation) { }
    default List<ResourceClaim> resourceClaims(Context context) { return List.of(); }
    record Context(String resourceId, String resourceName, String revision, String instruction, List<String> selectedTables) {
        public Context(String resourceId, String resourceName, String revision, String instruction) {
            this(resourceId, resourceName, revision, instruction, null);
        }
    }
    default Context resolveTask(String resourceId, String input, List<String> selectedTables) {
        if (selectedTables != null && !selectedTables.isEmpty()) throw AiErrors.error("unsupported");
        return resolve(resourceId, input);
    }
    default List<AiToolProtocol.Definition> tools() { return List.of(); }
    default List<AiToolProtocol.Definition> tools(Context context) { return tools(); }
    /** Trusted execution strategy; a task-only business cannot be invoked through foreground routes. */
    default boolean backgroundOnly() { return false; }
    default String capability() { return code() + ".assist"; }
    /** Pure business-result projection, reusable by execution lifecycles after their own authority check. */
    default AiConversationDtos.Result result(Context context, String text, List<AiConversationDtos.Tool> tools, String editorId, String draftVersion) {
        return new AiConversationDtos.Result("EXPLANATION", text, null, context.resourceId(), context.resourceName(), context.revision(), editorId, draftVersion);
    }
    /** Parameters were strictly validated; context is always the server-owned scope. */
    default String execute(Context context, String name, Map<String, String> arguments) { throw AiErrors.error("unsupported"); }
    default String execute(Context context, String name, Map<String, String> arguments, Runnable active, long deadline) {
        active.run();
        return execute(context, name, arguments);
    }
    String code();
    List<Resource> resources();
    Context resolve(String resourceId, String input);
    /** 经过当前业务权限链复核操作、资源范围及版本；可在受控身份作用域内重复调用。 */
    void authorize(Context context);
}
