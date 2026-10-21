import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceRepository;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.utils.CommonThreadPool;
import io.dataease.utils.EncryptUtils;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class Regression {
    private static final String MYSQL_CONFIG = "{\"host\":\"localhost\",\"port\":3306,\"dataBase\":\"regression\"}";
    private static int scenarios;

    public static void main(String[] args) throws Exception {
        for (String type : new String[] {"mysql", "oracle", "pg", "sqlServer", "kingbase", "h2"}) {
            if (!ProviderFactory.isCalciteDatasource(type)) throw new AssertionError(type);
        }
        for (String type : new String[] {"dm", "es", "ES", "missing-plugin", "", null}) {
            if (ProviderFactory.isCalciteDatasource(type)) throw new AssertionError(type);
        }
        pluginOnly();
        emptyPool();
        mixedDatasources();
        builtinEngine();
        missingPlugin();
        System.out.println("PASS: " + scenarios + " datasource pool routing scenarios");
    }

    private static void pluginOnly() throws Exception {
        try (Fixture fixture = new Fixture(List.of(datasource(1L, "dm")), datasource(2L, "dm"))) {
            fixture.provider.initConnectionPool();
            verify(fixture.provider).take();
            verify(fixture.provider, never()).initConnection(anyMap());
            verifyNoInteractions(fixture.threadPool);
            fixture.decrypt.verifyNoInteractions();
            fixture.schemas.verifyNoInteractions();
            scenarios++;
        }
    }

    private static void emptyPool() throws Exception {
        try (Fixture fixture = new Fixture(List.of(), null)) {
            fixture.provider.initConnectionPool();
            verify(fixture.provider).take();
            verifyNoInteractions(fixture.threadPool);
            scenarios++;
        }
    }

    @SuppressWarnings("unchecked")
    private static void mixedDatasources() throws Exception {
        try (Fixture fixture = new Fixture(List.of(datasource(1L, "dm"), datasource(3L, "mysql"),
                datasource(4L, "es")), datasource(2L, "dm"))) {
            fixture.provider.initConnectionPool();
            ArgumentCaptor<Map<Long, DatasourceSchemaDTO>> requests = ArgumentCaptor.forClass(Map.class);
            verify(fixture.provider).initConnection(requests.capture());
            if (!requests.getValue().keySet().equals(java.util.Set.of(3L))) {
                throw new AssertionError("Only the builtin datasource should enter the Calcite pool");
            }
            verify(fixture.provider).take();
            fixture.decrypt.verify(() -> EncryptUtils.aesDecrypt(MYSQL_CONFIG));
            fixture.schemas.verifyNoInteractions();
            scenarios++;
        }
    }

    private static void builtinEngine() throws Exception {
        try (Fixture fixture = new Fixture(List.of(datasource(1L, "dm")), datasource(2L, "mysql"))) {
            fixture.provider.initConnectionPool();
            verify(fixture.provider, times(2)).take();
            fixture.schemas.verify(() -> JdbcSchema.create(eq(fixture.root), anyString(), any(), isNull(), eq("regression")));
            verify(fixture.root).add(anyString(), any(JdbcSchema.class));
            verifyNoInteractions(fixture.threadPool);
            scenarios++;
        }
    }

    private static void missingPlugin() throws Exception {
        try (Fixture fixture = new Fixture(List.of(datasource(5L, "missing-plugin"), datasource(3L, "mysql")), null)) {
            fixture.provider.initConnectionPool();
            verify(fixture.provider).initConnection(anyMap());
            verify(fixture.provider).take();
            scenarios++;
        }
    }

    private static CoreDatasource datasource(Long id, String type) {
        CoreDatasource datasource = new CoreDatasource();
        datasource.setId(id);
        datasource.setName("regression-" + id);
        datasource.setType(type);
        datasource.setConfiguration(type.equals("mysql") ? MYSQL_CONFIG : "plugin-config-must-not-be-decrypted");
        return datasource;
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        Field field = CalciteProvider.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static class Fixture implements AutoCloseable {
        final CalciteProvider provider = spy(new CalciteProvider());
        final CommonThreadPool threadPool = mock(CommonThreadPool.class);
        final SchemaPlus root = mock(SchemaPlus.class);
        final MockedStatic<EncryptUtils> decrypt = mockStatic(EncryptUtils.class);
        final MockedStatic<JdbcSchema> schemas = mockStatic(JdbcSchema.class);

        Fixture(List<CoreDatasource> datasources, CoreDatasource engine) throws Exception {
            CoreDatasourceRepository repository = mock(CoreDatasourceRepository.class);
            when(repository.findAll()).thenReturn(datasources);
            EngineManage engineManage = mock(EngineManage.class);
            when(engineManage.deEngine()).thenReturn(engine);
            inject(provider, "coreDatasourceRepository", repository);
            inject(provider, "engineManage", engineManage);
            inject(provider, "commonThreadPool", threadPool);
            CalciteConnection connection = mock(CalciteConnection.class);
            when(connection.unwrap(CalciteConnection.class)).thenReturn(connection);
            when(connection.getRootSchema()).thenReturn(root);
            doReturn(connection).when(provider).take();
            doReturn(connection).when(provider).initConnection(anyMap());
            doAnswer(invocation -> { invocation.getArgument(0, Runnable.class).run(); return null; })
                    .when(threadPool).addTask(any(Runnable.class));
            decrypt.when(() -> EncryptUtils.aesDecrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
            schemas.when(() -> JdbcSchema.create(any(), anyString(), any(), any(), any()))
                    .thenReturn(mock(JdbcSchema.class));
        }

        @Override
        public void close() {
            schemas.close();
            decrypt.close();
        }
    }
}
