package io.dataease.share.manage;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.api.system.vo.ShareBaseVO;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.xpack.share.request.XpackShareProxyRequest;
import io.dataease.api.xpack.share.request.XpackSharePwdValidator;
import io.dataease.api.xpack.share.request.XpackShareUuidEditor;
import io.dataease.api.xpack.share.vo.TicketValidVO;
import io.dataease.api.xpack.share.vo.XpackShareGridVO;
import io.dataease.api.xpack.share.vo.XpackShareProxyVO;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.constant.AuthConstant;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.dao.auto.entity.QDataVisualizationInfo;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.license.config.XpackInteract;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.share.dao.auto.entity.QXpackShare;
import io.dataease.share.dao.auto.entity.XpackShare;
import io.dataease.share.dao.auto.mapper.XpackShareRepository;
import io.dataease.share.dao.ext.po.XpackSharePO;
import io.dataease.share.util.LinkTokenUtil;
import io.dataease.system.manage.SysParameterManage;
import io.dataease.utils.*;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component("xpackShareManage")
public class XpackShareManage {

    @Resource
    private JPAQueryFactory queryFactory;

    @Resource
    private XpackShareRepository xpackShareRepository;

    @Resource
    private ShareTicketManage shareTicketManage;

    @Resource
    private SysParameterManage sysParameterManage;

    public XpackShare queryByResource(Long resourceId) {
        Long userId = AuthUtils.getUser().getUserId();
        Specification<XpackShare> xpackShareSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("creator"), userId));
            predicates.add(cb.equal(root.get("resourceId"), resourceId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return xpackShareRepository.findOne(xpackShareSpec).orElse(null);
    }

    public String queryPwd(Long resourceId, Long userId) {
        Specification<XpackShare> xpackShareSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("creator"), userId));
            predicates.add(cb.equal(root.get("resourceId"), resourceId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        XpackShare xpackShare = xpackShareRepository.findOne(xpackShareSpec).orElse(null);
        if (ObjectUtils.isEmpty(xpackShare)) return null;
        return xpackShare.getPwd();
    }

    @Transactional
    public void switcher(Long resourceId) {
        XpackShare originData = queryByResource(resourceId);
        if (ObjectUtils.isNotEmpty(originData)) {
            xpackShareRepository.deleteById(originData.getId());
            shareTicketManage.deleteByShare(originData.getUuid());
            return;
        }
        TokenUserBO user = AuthUtils.getUser();
        Long userId = user.getUserId();
        XpackShare xpackShare = new XpackShare();
        xpackShare.setId(IDUtils.snowID());
        xpackShare.setCreator(userId);
        xpackShare.setTime(System.currentTimeMillis());
        xpackShare.setResourceId(resourceId);
        xpackShare.setUuid(RandomStringUtils.randomAlphanumeric(8));
        xpackShare.setOid(user.getDefaultOid());


        QDataVisualizationInfo qVisualization = QDataVisualizationInfo.dataVisualizationInfo;
        String dType = queryFactory
                .select(qVisualization.type)
                .from(qVisualization)
                .where(qVisualization.id.eq(resourceId))
                .fetchOne();
        xpackShare.setType(StringUtils.equalsIgnoreCase("dataV", dType) ? 2 : 1);
        xpackShareRepository.saveAndFlush(xpackShare);
    }

    @Transactional
    public String editUuid(XpackShareUuidEditor editor) {
        Long resourceId = editor.getResourceId();
        String uuid = editor.getUuid();
        XpackShare originData = queryByResource(resourceId);
        if (ObjectUtils.isEmpty(originData)) {
            return "公共链接不存在，请先创建！";
        }
        if (StringUtils.isBlank(uuid)) {
            return "不能为空！";
        }
        if (StringUtils.equals(uuid, originData.getUuid())) {
            return "";
        }

        Specification<XpackShare> xpackShareSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("uuid"), uuid));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        if (xpackShareRepository.count(xpackShareSpec) > 0) {
            return "已存在相同的链接，请重新输入！";
        }
        String regex = "^[a-zA-Z0-9]{8,16}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(uuid);
        if (!matcher.matches()) {
            return "仅支持8-16位(字母数字)，请重新输入！";
        }
        shareTicketManage.updateByUuidChange(originData.getUuid(), uuid);
        originData.setUuid(uuid);
        xpackShareRepository.saveAndFlush(originData);
        return "";
    }

    public void editExp(Long resourceId, Long exp) {
        XpackShare originData = queryByResource(resourceId);
        if (ObjectUtils.isEmpty(originData)) {
            DEException.throwException("share instance not exist");
        }
        originData.setExp(exp);
        if (ObjectUtils.isEmpty(exp)) {
            originData.setExp(0L);
        }
        xpackShareRepository.saveAndFlush(originData);
    }

    public void editPwd(Long resourceId, String pwd, Boolean autoPwd) {
        XpackShare originData = queryByResource(resourceId);
        if (ObjectUtils.isEmpty(originData)) {
            DEException.throwException("share instance not exist");
        }
        originData.setPwd(pwd);
        originData.setAutoPwd(ObjectUtils.isEmpty(autoPwd) || autoPwd);
        xpackShareRepository.saveAndFlush(originData);
    }


    public Page<XpackSharePO> querySharePage(int goPage, int pageSize, VisualizationWorkbranchQueryRequest request) {
        Long uid = AuthUtils.getUser().getUserId();
        QXpackShare xpackShare = QXpackShare.xpackShare;
        QDataVisualizationInfo dataVisualizationInfo = QDataVisualizationInfo.dataVisualizationInfo;
        JPAQuery<XpackSharePO> query = queryFactory.select(
                        Projections.fields(XpackSharePO.class,
                                xpackShare.id.as("shareId"),
                                dataVisualizationInfo.id.as("resourceId"),
                                dataVisualizationInfo.mobileLayout.as("extFlag"),
                                dataVisualizationInfo.status.as("extFlag1"),
                                dataVisualizationInfo.type.as("type"),
                                xpackShare.creator.as("creator"),
                                xpackShare.time.as("time"),
                                xpackShare.exp.as("exp"),
                                dataVisualizationInfo.name.as("name")
                        )

                ).from(xpackShare)
                .join(dataVisualizationInfo).on(dataVisualizationInfo.id.eq(xpackShare.resourceId))
                .where(xpackShare.creator.eq(uid));
        if (StringUtils.isNotBlank(request.getType())) {
            BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(request.getType().toUpperCase());
            if (ObjectUtils.isEmpty(busiResourceEnum)) {
                DEException.throwException("type is invalid");
            }
            String resourceType = convertResourceType(request.getType());
            if (StringUtils.isNotBlank(resourceType)) {
                query.where(dataVisualizationInfo.type.eq(resourceType));
            }
        }

        if (StringUtils.isNotBlank(request.getKeyword())) {
            query.where(dataVisualizationInfo.name.like("%" + request.getKeyword() + "%"));
        }

        //TODO CommunityUtils.getInfo
//        String info = CommunityUtils.getInfo();
//        if (StringUtils.isNotBlank(info)) {
//            queryWrapper.notExists(String.format(info, "s.resource_id"));
//        }
        query.orderBy(request.isAsc() ? xpackShare.time.asc() : xpackShare.time.desc());
        Pageable pageable = PageRequest.of(goPage - 1, pageSize);
        long total = query.fetchCount();
        return new PageImpl<>(query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch(), pageable, total);
    }

    private String convertResourceType(String busiFlag) {
        return switch (busiFlag) {
            case "panel" -> "dashboard";
            case "screen" -> "dataV";
            default -> null;
        };
    }

    @XpackInteract(value = "perFilterShareManage", recursion = true, invalid = true)
    public List<XpackShareGridVO> query(int pageNum, int pageSize, VisualizationWorkbranchQueryRequest request) {
        Page<XpackSharePO> poiPage = proxy().querySharePage(pageNum, pageSize, request);
        List<XpackShareGridVO> vos = proxy().formatResult(poiPage.getContent());
        if (!org.springframework.util.CollectionUtils.isEmpty(vos)) {
            vos.forEach(item -> {
                item.setCreator(StringUtils.equals(item.getCreator(), "1") ? Translator.get("i18n_sys_admin") : item.getCreator());
            });
        }
        return vos;
    }

    public List<XpackShareGridVO> formatResult(List<XpackSharePO> pos) {
        if (CollectionUtils.isEmpty(pos)) return new ArrayList<>();
        return pos.stream().map(po ->
                new XpackShareGridVO(
                        po.getShareId(), po.getResourceId(), po.getName(), po.getCreator().toString(),
                        po.getTime(), po.getExp(), 9, po.getExtFlag(), po.getExtFlag1(), po.getType())).toList();
    }

    private XpackShareManage proxy() {
        return CommonBeanFactory.getBean(this.getClass());
    }

    private boolean peRequireValid(ShareBaseVO sharedBase, XpackShare share) {
        if (ObjectUtils.isEmpty(sharedBase) || !sharedBase.isPeRequire()) return true;
        Long exp = share.getExp();
        String pwd = share.getPwd();
        return StringUtils.isNotBlank(pwd) && ObjectUtils.isNotEmpty(exp) && exp > 0L;
    }

    public XpackShareProxyVO proxyInfo(XpackShareProxyRequest request) {
        ShareBaseVO sharedBase = sysParameterManage.shareBase();
        if (ObjectUtils.isNotEmpty(sharedBase) && sharedBase.isDisable()) {
            XpackShareProxyVO vo = new XpackShareProxyVO();
            vo.setShareDisable(true);
            return vo;
        }
        boolean inIframeError = request.isInIframe() && !LicenseUtil.licenseValid();
        if (inIframeError) {
            return new XpackShareProxyVO();
        }
        Specification<XpackShare> xpackShareSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("uuid"), request.getUuid()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        XpackShare xpackShare = xpackShareRepository.findOne(xpackShareSpec).orElse(null);
        if (ObjectUtils.isEmpty(xpackShare))
            return null;
        if (!peRequireValid(sharedBase, xpackShare)) {
            XpackShareProxyVO vo = new XpackShareProxyVO();
            vo.setPeRequireValid(false);
            vo.setInIframeError(false);
            return vo;
        }
        String linkToken = LinkTokenUtil.generate(xpackShare.getCreator(), xpackShare.getResourceId(), xpackShare.getExp(), xpackShare.getPwd(), xpackShare.getOid());
        HttpServletResponse response = ServletUtils.response();
        response.addHeader(AuthConstant.LINK_TOKEN_KEY, linkToken);
        Integer type = xpackShare.getType();
        String typeText = (ObjectUtils.isNotEmpty(type) && type == 1) ? "dashboard" : "dataV";
        TicketValidVO validVO = shareTicketManage.validateTicket(request.getTicket(), xpackShare);
        return new XpackShareProxyVO(xpackShare.getResourceId(), xpackShare.getCreator(), linkExp(xpackShare), pwdValid(xpackShare, request.getCiphertext()), typeText, inIframeError, false, true, validVO);
    }

    private boolean linkExp(XpackShare xpackShare) {
        if (ObjectUtils.isEmpty(xpackShare.getExp()) || xpackShare.getExp().equals(0L)) return false;
        return System.currentTimeMillis() > xpackShare.getExp();
    }

    private boolean pwdValid(XpackShare xpackShare, String ciphertext) {
        if (StringUtils.isBlank(xpackShare.getPwd())) return true;
        if (StringUtils.isBlank(ciphertext)) return false;
        String text = RsaUtils.decryptStr(ciphertext);
        int splitIndex = text.indexOf(",");
        String pwd;
        if (splitIndex == -1) {
            splitIndex = 8;
            pwd = text.substring(splitIndex);
        } else {
            pwd = text.substring(splitIndex + 1);
        }
        String uuid = text.substring(0, splitIndex);
        return StringUtils.equals(xpackShare.getUuid(), uuid) && StringUtils.equals(xpackShare.getPwd(), pwd);
    }

    public boolean validatePwd(XpackSharePwdValidator validator) {
        String ciphertext = RsaUtils.decryptStr(validator.getCiphertext());
        String pwd;
        int splitIndex = ciphertext.indexOf(",");
        if (splitIndex == -1) {
            splitIndex = 8;
            pwd = ciphertext.substring(splitIndex);
        } else {
            pwd = ciphertext.substring(splitIndex + 1);
        }
        String uuid = ciphertext.substring(0, splitIndex);
        Specification<XpackShare> xpackShareSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("uuid"), uuid));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        XpackShare xpackShare = xpackShareRepository.findOne(xpackShareSpec).orElse(null);
        return StringUtils.equals(xpackShare.getUuid(), uuid) && StringUtils.equals(xpackShare.getPwd(), pwd);
    }

    public Map<String, String> queryRelationByUserId(Long uid) {
        Specification<XpackShare> xpackShareSpec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("creator"), uid));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<XpackShare> result = xpackShareRepository.findAll(xpackShareSpec);
        if (CollectionUtils.isNotEmpty(result)) {
            return result.stream()
                    .collect(Collectors.toMap(xpackShare -> String.valueOf(xpackShare.getResourceId()), XpackShare::getUuid));
        }
        return new HashMap<>();
    }
}
