package io.dataease.ai.engine.conversation;

import io.dataease.ai.engine.runtime.AiToolProtocol;
import io.dataease.ai.support.AiErrors;

import java.util.List;
import java.util.Map;
import io.dataease.api.ai.conversation.AiConversationDtos;

/** 业务上下文与权限边界。共享引擎不解析或执行 SQL，不持有数据源凭据。 */
public interface AiConversationScene {
    record Resource(String id, String name) { }
    /** Trusted scene declaration after current business authorization and canonical metadata resolution. */
    record ResourceClaim(String datasourceId, String table, String operation) { }
    default List<ResourceClaim> resourceClaims(Context context) { return List.of(); }
    record ResourceRevision(String id, String revision, boolean isCross) {}
    interface SceneState { }
    record Context(String resourceId, String resourceName, String revision, String instruction, List<String> selectedTables, boolean isCross, ResourceRevision editorResource, SceneState sceneState) {
        public Context(String resourceId, String resourceName, String revision, String instruction, List<String> selectedTables, boolean isCross, ResourceRevision editorResource) {
            this(resourceId, resourceName, revision, instruction, selectedTables, isCross, editorResource, null);
        }
        public Context(String resourceId, String resourceName, String revision, String instruction, List<String> selectedTables, boolean isCross) {
            this(resourceId, resourceName, revision, instruction, selectedTables, isCross, null);
        }
        public Context(String resourceId, String resourceName, String revision, String instruction, List<String> selectedTables) {
            this(resourceId, resourceName, revision, instruction, selectedTables, false);
        }
        public Context(String resourceId, String resourceName, String revision, String instruction) {
            this(resourceId, resourceName, revision, instruction, null);
        }
    }
    default Context resolve(AiConversationDtos.Open editor) {
        if (editor.preview() != null) throw AiErrors.error("invalid");
        return resolve(java.util.Objects.toString(editor.resourceId(), ""), java.util.Objects.toString(editor.sql(), ""));
    }
    default Context resolveTask(String resourceId, String input, List<String> selectedTables) {
        if (selectedTables != null && !selectedTables.isEmpty()) throw AiErrors.error("unsupported");
        return resolve(resourceId, input);
    }
    default List<AiToolProtocol.Definition> tools() { return List.of(); }
    default List<AiToolProtocol.Definition> tools(Context context) { return tools(); }
    default AiToolProtocol.StepLimit toolStepLimit() { return AiToolProtocol.StepLimit.DEFAULT; }
    default boolean retainEveryTurn() { return false; }
    /** A failed business validation may be delivered as a reference-only suggestion. */
    default boolean retainToolFailure(String name, String result) { return false; }
    default boolean applicable(AiConversationDtos.Result result) { return "SQL_SUGGESTION".equals(result.type()); }
    default void authorizeCopyTarget(String target) { throw AiErrors.error("invalid"); }
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
    @FunctionalInterface
    interface ToolExecution {
        String execute(String name, Map<String, String> arguments, Runnable active, long deadline);
    }
    default ToolExecution beginTurn(Context context) {
        return (name, arguments, active, deadline) -> execute(context, name, arguments, active, deadline);
    }
    String code();
    List<Resource> resources();
    Context resolve(String resourceId, String input);
    /** 经过当前业务权限链复核操作、资源范围及版本；可在受控身份作用域内重复调用。 */
    void authorize(Context context);
}
