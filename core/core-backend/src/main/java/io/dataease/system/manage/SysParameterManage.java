package io.dataease.system.manage;

import io.dataease.api.system.request.OnlineMapEditor;
import io.dataease.api.system.vo.SettingItemVO;
import io.dataease.api.system.vo.ShareBaseVO;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.exception.DEException;
import io.dataease.license.config.XpackInteract;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.SystemSettingUtils;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SysParameterManage {

    @Value("${dataease.show-demo-tips:false}")
    private boolean showDemoTips;

    @Value("${dataease.demo-tips-content:#{null}}")
    private String demoTipsContent;

    private static final String MAP_KEY_PREFIX = "map.";
    private static final String CUSTOM_TILE_MAP_TYPE = "customTile";
    private static final String RASTER_SERVICE_TYPE = "raster";
    private static final String VECTOR_SERVICE_TYPE = "vector";
    private static final int CUSTOM_MAP_FIELD_MAX_LENGTH = 255;
    private static final int CUSTOM_MAP_URL_MAX_LENGTH = 2048;
    private static final Set<String> ONLINE_MAP_TYPES = Set.of("gaode", "tianditu", "qq", CUSTOM_TILE_MAP_TYPE);
    private static final List<String> LEGACY_MAP_FIELDS = List.of("key", "securityCode");
    private static final Set<String> CUSTOM_MAP_URL_FIELDS = Set.of("tileUrl", "styleUrl");
    private static final List<String> CUSTOM_MAP_FIELDS = List.of(
            "serviceType",
            "tileUrl",
            "styleUrl",
            "tileScheme",
            "tileSize",
            "tileMinZoom",
            "tileMaxZoom",
            "tileAttribution",
            "styleAttribution",
            "tileAttributionEnabled",
            "styleAttributionEnabled"
    );
    private static final Pattern RESOURCE_URL_PATTERN = Pattern.compile("^(https?://|/(?!/)|\\.{1,2}/).+", Pattern.CASE_INSENSITIVE);
    private static final Pattern ZOOM_PATTERN = Pattern.compile("^\\d+(?:\\.\\d)?$");

    @Resource
    private CoreSysSettingRepository coreSysSettingRepository;

    @Resource
    private DatasourceServer datasourceServer;

    public String singleVal(String key) {
        Specification<CoreSysSetting> spec = (root, query, cb) -> cb.equal(root.get("pkey"), key);
        List<CoreSysSetting> list = coreSysSettingRepository.findAll(spec, PageRequest.of(0, 1)).toList();
        if (CollectionUtils.isNotEmpty(list)) {
            return list.getFirst().getPval();
        }
        return null;
    }

    public OnlineMapEditor queryOnlineMap(String mapType) {
        if (StringUtils.isBlank(mapType)) {
            List<CoreSysSetting> typeList = groupList(MAP_KEY_PREFIX + "mapType");
            mapType = "gaode";
            if (!CollectionUtils.isEmpty(typeList)) {
                mapType = typeList.getFirst().getPval();
            }
        }
        String prefix;
        if (!StringUtils.equals(mapType, "gaode")) {
            prefix = mapType + "." + MAP_KEY_PREFIX;
        } else {
            prefix = MAP_KEY_PREFIX;
        }
        var editor = new OnlineMapEditor();
        List<String> fields = BeanUtils.getFieldNames(OnlineMapEditor.class);
        Map<String, String> mapVal = groupVal(prefix);
        fields.forEach(field -> {
            String val = mapVal.get(prefix + field);
            if (StringUtils.isNotBlank(val)) {
                BeanUtils.setFieldValueByName(editor, field, val, String.class);
            }
        });

        editor.setMapType(mapType);
        if (StringUtils.equals(mapType, CUSTOM_TILE_MAP_TYPE)) {
            applyLegacyCustomMapConfig(editor);
            editor.setKey("");
            editor.setSecurityCode("");
        }

        return editor;
    }

    @Transactional
    public void saveOnlineMap(OnlineMapEditor editor) {
        String mapType = editor.getMapType();
        if (StringUtils.isBlank(mapType)) {
            List<CoreSysSetting> typeList = groupList(MAP_KEY_PREFIX + "mapType");
            mapType = "gaode";
            if (!CollectionUtils.isEmpty(typeList)) {
                mapType = typeList.getFirst().getPval();
            }
        }
        validateMapType(mapType);
        if (StringUtils.equals(mapType, CUSTOM_TILE_MAP_TYPE)) {
            normalizeAndValidateCustomMap(editor);
            String prefix = CUSTOM_TILE_MAP_TYPE + "." + MAP_KEY_PREFIX;
            CUSTOM_MAP_FIELDS.forEach(field -> saveMapSetting(
                    prefix + field,
                    (String) BeanUtils.getFieldValueByName(field, editor)
            ));
            saveMapSetting(prefix + "key", "");
            saveMapSetting(prefix + "securityCode", "");
        } else {
            String prefix = StringUtils.equals(mapType, "gaode") ? MAP_KEY_PREFIX : mapType + "." + MAP_KEY_PREFIX;
            LEGACY_MAP_FIELDS.forEach(field -> {
                String val = (String) BeanUtils.getFieldValueByName(field, editor);
                if (val != null) {
                    saveMapSetting(prefix + field, val);
                }
            });
        }
        // 地图配置完整写入后再切换全局地图类型
        saveMapSetting(MAP_KEY_PREFIX + "mapType", mapType);
    }

    private void applyLegacyCustomMapConfig(OnlineMapEditor editor) {
        if (isCustomMapConfigComplete(editor) || StringUtils.isBlank(editor.getKey())) {
            return;
        }
        OnlineMapEditor legacyEditor = JsonUtil.parseObject(editor.getKey(), OnlineMapEditor.class);
        if (legacyEditor == null || !isCustomMapConfigComplete(legacyEditor)) {
            return;
        }
        CUSTOM_MAP_FIELDS.forEach(field -> BeanUtils.setFieldValueByName(
                editor,
                field,
                BeanUtils.getFieldValueByName(field, legacyEditor),
                String.class
        ));
    }

    private boolean isCustomMapConfigComplete(OnlineMapEditor editor) {
        if (StringUtils.equals(editor.getServiceType(), VECTOR_SERVICE_TYPE)) {
            return StringUtils.isNotBlank(editor.getStyleUrl());
        }
        return StringUtils.equals(editor.getServiceType(), RASTER_SERVICE_TYPE)
                && StringUtils.isNotBlank(editor.getTileUrl());
    }

    private void normalizeAndValidateCustomMap(OnlineMapEditor editor) {
        editor.setServiceType(StringUtils.defaultIfBlank(editor.getServiceType(), RASTER_SERVICE_TYPE));
        editor.setTileUrl(StringUtils.trimToEmpty(editor.getTileUrl()));
        editor.setStyleUrl(StringUtils.trimToEmpty(editor.getStyleUrl()));
        editor.setTileScheme(StringUtils.equals(editor.getTileScheme(), "tms") ? "tms" : "xyz");
        editor.setTileSize(StringUtils.equals(editor.getTileSize(), "512") ? "512" : "256");
        editor.setTileMinZoom(StringUtils.defaultIfBlank(StringUtils.trim(editor.getTileMinZoom()), "0"));
        editor.setTileMaxZoom(StringUtils.defaultIfBlank(
                StringUtils.trim(editor.getTileMaxZoom()),
                StringUtils.equals(editor.getServiceType(), VECTOR_SERVICE_TYPE) ? "22" : "18"
        ));
        editor.setTileAttribution(StringUtils.defaultString(editor.getTileAttribution()));
        editor.setStyleAttribution(StringUtils.defaultString(editor.getStyleAttribution()));
        editor.setTileAttributionEnabled(normalizeBoolean(editor.getTileAttributionEnabled()));
        editor.setStyleAttributionEnabled(normalizeBoolean(editor.getStyleAttributionEnabled()));

        if (!Set.of(RASTER_SERVICE_TYPE, VECTOR_SERVICE_TYPE).contains(editor.getServiceType())) {
            DEException.throwException("自定义地图服务类型无效");
        }
        String resourceUrl = StringUtils.equals(editor.getServiceType(), VECTOR_SERVICE_TYPE)
                ? editor.getStyleUrl()
                : editor.getTileUrl();
        if (!isValidResourceUrl(resourceUrl)) {
            DEException.throwException("自定义地图服务地址无效");
        }
        if (StringUtils.equals(editor.getServiceType(), RASTER_SERVICE_TYPE)
                && !(resourceUrl.contains("{z}") && resourceUrl.contains("{x}") && resourceUrl.contains("{y}"))) {
            DEException.throwException("瓦片地址必须包含 {z}、{x}、{y}");
        }
        BigDecimal minZoom = parseZoom(editor.getTileMinZoom());
        BigDecimal maxZoom = parseZoom(editor.getTileMaxZoom());
        if (minZoom.compareTo(maxZoom) > 0) {
            DEException.throwException("地图最小缩放层级不能大于最大缩放层级");
        }
        // 统一缩放值格式，避免同一数值产生多种持久化结果
        editor.setTileMinZoom(normalizeZoom(minZoom));
        editor.setTileMaxZoom(normalizeZoom(maxZoom));
        CUSTOM_MAP_FIELDS.forEach(field -> validateSettingLength(
                field,
                (String) BeanUtils.getFieldValueByName(field, editor)
        ));
    }

    private String normalizeBoolean(String value) {
        return StringUtils.equals(value, "false") ? "false" : "true";
    }

    private BigDecimal parseZoom(String value) {
        if (!ZOOM_PATTERN.matcher(value).matches()) {
            DEException.throwException("地图缩放层级必须是 0-24 的数字，最多保留 1 位小数");
        }
        try {
            BigDecimal zoom = new BigDecimal(value);
            if (zoom.compareTo(BigDecimal.ZERO) < 0 || zoom.compareTo(BigDecimal.valueOf(24)) > 0) {
                DEException.throwException("地图缩放层级必须是 0-24 的数字，最多保留 1 位小数");
            }
            return zoom;
        } catch (NumberFormatException e) {
            DEException.throwException("地图缩放层级必须是 0-24 的数字，最多保留 1 位小数");
            return BigDecimal.ZERO;
        }
    }

    private String normalizeZoom(BigDecimal zoom) {
        return zoom.stripTrailingZeros().toPlainString();
    }

    private boolean isValidResourceUrl(String value) {
        return StringUtils.isNotBlank(value)
                && value.length() <= CUSTOM_MAP_URL_MAX_LENGTH
                && RESOURCE_URL_PATTERN.matcher(value.trim()).matches();
    }

    private void validateSettingLength(String field, String value) {
        int maxLength = CUSTOM_MAP_URL_FIELDS.contains(field)
                ? CUSTOM_MAP_URL_MAX_LENGTH
                : CUSTOM_MAP_FIELD_MAX_LENGTH;
        if (value != null && value.length() > maxLength) {
            DEException.throwException("自定义地图配置长度不能超过 " + maxLength + " 个字符");
        }
    }

    private void validateMapType(String mapType) {
        if (!ONLINE_MAP_TYPES.contains(mapType)) {
            DEException.throwException("在线地图类型无效");
        }
    }

    private void saveMapSetting(String key, String value) {
        Specification<CoreSysSetting> spec = (root, query, cb) -> cb.equal(root.get("pkey"), key);
        CoreSysSetting sysSetting = coreSysSettingRepository.findOne(spec).orElseGet(() -> {
            CoreSysSetting setting = new CoreSysSetting();
            setting.setId(IDUtils.snowID());
            setting.setPkey(key);
            setting.setType("text");
            setting.setSort(1);
            return setting;
        });
        sysSetting.setPval(StringUtils.defaultString(value));
        coreSysSettingRepository.save(sysSetting);
    }


    public Map<String, String> groupVal(String groupKey) {
        Specification<CoreSysSetting> spec = (root, query, cb) -> {
            Predicate likeRight = cb.like(root.get("pkey"), groupKey + "%");
            query.orderBy(cb.asc(root.get("sort")));
            return likeRight;
        };

        List<CoreSysSetting> sysSettings = coreSysSettingRepository.findAll(spec);
        if (!CollectionUtils.isEmpty(sysSettings)) {
            return sysSettings.stream().collect(Collectors.toMap(CoreSysSetting::getPkey, CoreSysSetting::getPval));
        }
        return new HashMap<>();
    }

    public List<CoreSysSetting> groupList(String groupKey) {
        Specification<CoreSysSetting> spec = (root, query, cb) -> {
            Predicate likeRight = cb.like(root.get("pkey"), groupKey + "%");
            query.orderBy(cb.asc(root.get("sort")));
            return likeRight;
        };

        return coreSysSettingRepository.findAll(spec);
    }

    @XpackInteract(value = "perSetting")
    public List<SettingItemVO> convert(List<CoreSysSetting> sysSettings) {
        return sysSettings.stream().sorted(Comparator.comparing(CoreSysSetting::getSort)).map(item -> BeanUtils.copyBean(new SettingItemVO(), item)).toList();
    }

    @XpackInteract(value = "perSetting", replace = true)
    public List<Object> getUiList() {
        List<Object> result = new ArrayList<>();
        result.add(buildSettingItem("community", true));
        result.add(buildSettingItem("showDemoTips", showDemoTips));
        result.add(buildSettingItem("demoTipsContent", demoTipsContent));
        return result;
    }

    @XpackInteract(value = "perSetting", replace = true)
    public Integer defaultLogin() {
        return 0;
    }

    private Map<String, Object> buildSettingItem(String pkey, Object pval) {
        Map<String, Object> item = new HashMap<>();
        item.put("pkey", pkey);
        item.put("pval", pval);
        return item;
    }


    @Transactional
    public void saveGroup(List<SettingItemVO> vos, String groupKey) {
        List<CoreSysSetting> sysSettings = vos.stream().filter(vo -> !SystemSettingUtils.xpackSetting(vo.getPkey())).map(item -> {
            CoreSysSetting sysSetting = BeanUtils.copyBean(new CoreSysSetting(), item);
            sysSetting.setId(IDUtils.snowID());
            return sysSetting;
        }).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(sysSettings)) {
            sysSettings.forEach(sysSetting -> {
                coreSysSettingRepository.deleteByPkey(sysSetting.getPkey());
            });

            int batchSize = 1000;
            for (int i = 0; i < sysSettings.size(); i += batchSize) {
                int end = Math.min(i + batchSize, sysSettings.size());
                coreSysSettingRepository.saveAll(sysSettings.subList(i, end));
                coreSysSettingRepository.flush();
            }
        }
        datasourceServer.addJob(sysSettings);
    }


    @XpackInteract(value = "perSetting", before = false)
    @Transactional
    public void saveBasic(List<SettingItemVO> vos) {
        String key = "basic.";
        proxy().saveGroup(vos, key);
    }

    private SysParameterManage proxy() {
        return CommonBeanFactory.getBean(SysParameterManage.class);
    }

    public ShareBaseVO shareBase() {
        String disableText = singleVal("basic.shareDisable");
        String requireText = singleVal("basic.sharePeRequire");
        ShareBaseVO vo = new ShareBaseVO();
        if (StringUtils.isNotBlank(disableText) && StringUtils.equals("true", disableText)) {
            vo.setDisable(true);
        }
        if (StringUtils.isNotBlank(requireText) && StringUtils.equals("true", requireText)) {
            vo.setPeRequire(true);
        }
        return vo;
    }

    public void insert(CoreSysSetting coreSysSetting) {
        coreSysSettingRepository.saveAndFlush(coreSysSetting);
    }

}
