package io.dataease.share.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.license.config.XpackInteract;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.share.dao.auto.entity.XpackShare;
import io.dataease.share.dao.auto.mapper.XpackShareMapper;
import io.dataease.share.dao.ext.mapper.XpackShareExtMapper;
import io.dataease.share.dao.ext.po.XpackSharePO;
import io.dataease.share.util.LinkTokenUtil;
import io.dataease.system.manage.SysParameterManage;
import io.dataease.utils.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
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

    @Resource(name = "xpackShareMapper")
    private XpackShareMapper xpackShareMapper;

    @Resource(name = "xpackShareExtMapper")
    private XpackShareExtMapper xpackShareExtMapper;

    @Resource
    private ShareTicketManage shareTicketManage;

    @Resource
    private SysParameterManage sysParameterManage;

    public XpackShare queryByResource(Long resourceId) {
        Long userId = AuthUtils.getUser().getUserId();
        QueryWrapper<XpackShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("creator", userId);
        queryWrapper.eq("resource_id", resourceId);
        return xpackShareMapper.selectOne(queryWrapper);
    }

    public String queryPwd(Long resourceId, Long userId) {
        QueryWrapper<XpackShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("creator", userId);
        queryWrapper.eq("resource_id", resourceId);
        XpackShare xpackShare = xpackShareMapper.selectOne(queryWrapper);
        if (ObjectUtils.isEmpty(xpackShare)) return null;
        return xpackShare.getPwd();
    }

    @Transactional
    public void switcher(Long resourceId) {
        XpackShare originData = queryByResource(resourceId);
        if (ObjectUtils.isNotEmpty(originData)) {
            xpackShareMapper.deleteById(originData.getId());
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
        String dType = xpackShareExtMapper.visualizationType(resourceId);
        xpackShare.setType(StringUtils.equalsIgnoreCase("dataV", dType) ? 2 : 1);
        xpackShareMapper.insert(xpackShare);
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
        QueryWrapper<XpackShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid);
        if (xpackShareMapper.selectCount(queryWrapper) > 0) {
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
        xpackShareMapper.updateById(originData);
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
        xpackShareMapper.updateById(originData);
    }

    public void editPwd(Long resourceId, String pwd, Boolean autoPwd) {
        XpackShare originData = queryByResource(resourceId);
        if (ObjectUtils.isEmpty(originData)) {
            DEException.throwException("share instance not exist");
        }
        originData.setPwd(pwd);
        originData.setAutoPwd(ObjectUtils.isEmpty(autoPwd) || autoPwd);
        xpackShareMapper.updateById(originData);
    }


    public IPage<XpackSharePO> querySharePage(int goPage, int pageSize, VisualizationWorkbranchQueryRequest request) {
        Long uid = AuthUtils.getUser().getUserId();
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("s.creator", uid);
        if (StringUtils.isNotBlank(request.getType())) {
            BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(request.getType().toUpperCase());
            if (ObjectUtils.isEmpty(busiResourceEnum)) {
                DEException.throwException("type is invalid");
            }
            String resourceType = convertResourceType(request.getType());
            if (StringUtils.isNotBlank(resourceType)) {
                queryWrapper.eq("v.type", resourceType);
            }
        }
        if (StringUtils.isNotBlank(request.getKeyword())) {
            queryWrapper.like("v.name", request.getKeyword());
        }
        String info = CommunityUtils.getInfo();
        if (StringUtils.isNotBlank(info)) {
            queryWrapper.notExists(String.format(info, "s.resource_id"));
        }
        queryWrapper.orderBy(true, request.isAsc(), "s.time");
        Page<XpackSharePO> page = new Page<>(goPage, pageSize);
        return xpackShareExtMapper.query(page, queryWrapper);
    }

    private String convertResourceType(String busiFlag) {
        return switch (busiFlag) {
            case "panel" -> "dashboard";
            case "screen" -> "dataV";
            default -> null;
        };
    }

    @XpackInteract(value = "perFilterShareManage", recursion = true, invalid = true)
    public IPage<XpackShareGridVO> query(int pageNum, int pageSize, VisualizationWorkbranchQueryRequest request) {
        IPage<XpackSharePO> poiPage = proxy().querySharePage(pageNum, pageSize, request);
        List<XpackShareGridVO> vos = proxy().formatResult(poiPage.getRecords());
        if (!org.springframework.util.CollectionUtils.isEmpty(vos)) {
            vos.forEach(item -> {
                item.setCreator(StringUtils.equals(item.getCreator(), "1") ? Translator.get("i18n_sys_admin") : item.getCreator());
            });
        }
        IPage<XpackShareGridVO> ipage = new Page<>();
        ipage.setSize(poiPage.getSize());
        ipage.setCurrent(poiPage.getCurrent());
        ipage.setPages(poiPage.getPages());
        ipage.setTotal(poiPage.getTotal());
        ipage.setRecords(vos);
        return ipage;
    }

    public List<XpackShareGridVO> formatResult(List<XpackSharePO> pos) {
        if (CollectionUtils.isEmpty(pos)) return new ArrayList<>();
        return pos.stream().map(po ->
                new XpackShareGridVO(
                        po.getShareId(), po.getResourceId(), po.getName(), po.getCreator().toString(),
                        po.getTime(), po.getExp(), 9, po.getExtFlag(), po.getType())).toList();
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
        QueryWrapper<XpackShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", request.getUuid());
        XpackShare xpackShare = xpackShareMapper.selectOne(queryWrapper);
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
        QueryWrapper<XpackShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid);
        XpackShare xpackShare = xpackShareMapper.selectOne(queryWrapper);
        return StringUtils.equals(xpackShare.getUuid(), uuid) && StringUtils.equals(xpackShare.getPwd(), pwd);
    }

    public Map<String, String> queryRelationByUserId(Long uid) {
        QueryWrapper<XpackShare> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("creator", uid);
        List<XpackShare> result = xpackShareMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(result)) {
            return result.stream()
                    .collect(Collectors.toMap(xpackShare -> String.valueOf(xpackShare.getResourceId()), XpackShare::getUuid));
        }
        return new HashMap<>();
    }
}
