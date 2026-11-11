import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.chart.dao.auto.entity.CoreChartView;
import io.dataease.chart.dao.auto.mapper.CoreChartViewMapper;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.chart.manage.ChartViewManege;
import io.dataease.constant.AuthEnum;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableFieldMapper;
import io.dataease.exception.DEException;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.i18n.Translator;
import io.dataease.system.manage.CorePermissionManage;
import io.dataease.utils.AuthUtils;
import io.dataease.visualization.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import org.springframework.context.support.StaticMessageSource;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/** Exercises both production read entry points with isolated persistence and authorization services. */
public class Regression {
    private static final Long SCENE = 100L;
    private static final Long CHART = 200L;
    private static final Long USER = 20L;
    private static final Long ORG = 30L;
    private static DataVisualizationInfo visualization;
    private static boolean authorized;
    private static int authCalls;
    private static int passed;
    private static Long persistedScene = SCENE;
    private static final RuntimeException DOWNSTREAM = new RuntimeException("downstream reached");

    public static void main(String[] args) throws Exception {
        new Translator().setMessageSource(new StaticMessageSource());
        ChartViewManege manage = new ChartViewManege() {
            @Override
            public ChartViewDTO getDetails(Long id, String resourceTable) {
                ChartViewDTO view = new ChartViewDTO();
                view.setId(CHART);
                view.setSceneId(persistedScene);
                return view;
            }
        };
        inject(manage, "visualizationInfoMapper", proxy(DataVisualizationInfoMapper.class, (method, values) -> {
            if (!method.equals("selectById") || !SCENE.equals(values[0])) throw new AssertionError(method);
            return visualization;
        }));
        inject(manage, "coreChartViewMapper", proxy(CoreChartViewMapper.class, (method, values) -> {
            if (!method.equals("selectById")) throw new AssertionError(method);
            CoreChartView chart = new CoreChartView();
            chart.setSceneId(persistedScene);
            // Source chart ownership must not authorize a copied canvas.
            chart.setCreateBy("999");
            return chart;
        }));
        inject(manage, "corePermissionManage", new CorePermissionManage() {
            @Override
            public boolean checkAuth(BusiPerCheckDTO dto) {
                if (!SCENE.equals(dto.getId()) || dto.getAuthEnum() != AuthEnum.READ) throw new AssertionError();
                authCalls++;
                return authorized;
            }
        });
        inject(manage, "coreDatasetTableFieldMapper", proxy(CoreDatasetTableFieldMapper.class, (method, values) -> {
            throw DOWNSTREAM;
        }));
        inject(manage, "chartDataManage", new ChartDataManage() {
            @Override
            public ChartViewDTO calcData(ChartViewDTO view) {
                throw DOWNSTREAM;
            }
        });
        try {
            reset(); check(manage, true, false); // Owner in current organization, unregistered draft.
            reset(); visualization.setStatus(1); check(manage, true, false); // Published source status is inherited.
            reset(); visualization.setCreateBy("21"); check(manage, false, false);
            reset(); visualization.setOrgId(31L); check(manage, false, false);
            reset(); visualization.setCreateBy(null); check(manage, false, false);
            reset(); visualization.setOrgId(null); check(manage, false, false);
            reset(); AuthUtils.remove(); check(manage, false, false);
            reset(); AuthUtils.setUser(new TokenUserBO(null, ORG)); check(manage, false, false);
            reset(); AuthUtils.setUser(new TokenUserBO(USER, null)); check(manage, false, false);
            reset(); visualization.setDeleteFlag(null); check(manage, false, false);
            reset(); visualization.setDeleteFlag(true); authorized = true; check(manage, false, false);
            reset(); visualization = null; authorized = true; check(manage, false, false);
            reset(); visualization.setPid(0L); check(manage, false, true); // Creator is not sufficient after save.
            reset(); visualization.setPid(0L); authorized = true; check(manage, true, true);
            reset(); visualization.setPid(400L); visualization.setCreateBy("21"); authorized = true; check(manage, true, true);
            reset(); visualization.setPid(400L); visualization.setCreateBy("21"); check(manage, false, true);
            reset(); // Saving and revoking must immediately stop the draft-owner allowance.
            check(manage, true, false);
            visualization.setPid(400L);
            authorized = true;
            check(manage, true, true);
            authorized = false;
            check(manage, false, true);
            reset(); persistedScene = null; check(manage, true, false); // Existing scene-less chart behavior.
        } finally {
            AuthUtils.remove();
        }
        System.out.println("PASS: " + passed + " checks across both chart read entry points");
    }

    private static void reset() {
        visualization = new DataVisualizationInfo();
        visualization.setId(SCENE);
        visualization.setPid(-1L);
        visualization.setCreateBy(USER.toString());
        visualization.setOrgId(ORG);
        visualization.setDeleteFlag(false);
        visualization.setStatus(0);
        persistedScene = SCENE;
        authorized = false;
        AuthUtils.setUser(new TokenUserBO(USER, ORG));
    }

    private static void check(ChartViewManege manage, boolean allowed, boolean formalCheck) throws Exception {
        for (boolean fields : new boolean[]{true, false}) {
            authCalls = 0;
            try {
                if (fields) {
                    ChartViewDTO forged = new ChartViewDTO();
                    forged.setSceneId(999L); // Request scene must not replace the persisted chart scene.
                    manage.listByDQ(300L, CHART, forged);
                } else {
                    manage.getChart(CHART, "core");
                }
                throw new AssertionError("Expected rejection or downstream processing");
            } catch (RuntimeException error) {
                if (allowed ? error != DOWNSTREAM : !(error instanceof DEException)
                        || !"i18n_no_permission".equals(error.getMessage())) {
                    throw new AssertionError("Unexpected permission result", error);
                }
            }
            if (authCalls != (formalCheck ? 1 : 0)) throw new AssertionError("Formal authorization changed");
            passed++;
        }
    }

    private interface Handler {
        Object call(String method, Object[] values);
    }

    private static <T> T proxy(Class<T> type, Handler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (instance, method, values) -> handler.call(method.getName(), values)));
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        Field field = ChartViewManege.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
