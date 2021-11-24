package io.dataease.plugins.server;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.*;
import io.dataease.plugins.common.entity.GlobalTaskEntity;
import io.dataease.plugins.common.entity.GlobalTaskInstance;
import io.dataease.plugins.common.entity.XpackGridRequest;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.email.dto.request.XpackEmailCreate;
import io.dataease.plugins.xpack.email.dto.request.XpackEmailTaskRequest;
import io.dataease.plugins.xpack.email.dto.request.XpackEmailViewRequest;
import io.dataease.plugins.xpack.email.dto.request.XpackPixelEntity;
import io.dataease.plugins.xpack.email.dto.response.XpackTaskGridDTO;
import io.dataease.plugins.xpack.email.dto.response.XpackTaskInstanceDTO;
import io.dataease.plugins.xpack.email.service.EmailXpackService;
import io.dataease.service.ScheduleService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "xpack：定时报告")
@RequestMapping("/plugin/task")
@RestController
public class XEmailTaskServer {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping("/queryTasks/{goPage}/{pageSize}")
    public Pager<List<XpackTaskGridDTO>> queryTask(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody XpackGridRequest request) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<XpackTaskGridDTO> tasks = emailXpackService.taskGrid(request);
        Pager<List<XpackTaskGridDTO>> listPager = PageUtils.setPageInfo(page, tasks);
        return listPager;
    }

    @PostMapping("/save")
    public void save(@RequestBody XpackEmailCreate param) throws Exception {
        XpackEmailTaskRequest request = param.fillContent();
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        request.setCreator(AuthUtils.getUser().getUserId());
        emailXpackService.save(request);
        GlobalTaskEntity globalTask = BeanUtils.copyBean(new GlobalTaskEntity(), request);
        scheduleService.addSchedule(globalTask);
    }

    @PostMapping("/queryForm/{taskId}")
    public XpackEmailCreate queryForm(@PathVariable Long taskId) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);

        XpackEmailTaskRequest taskForm = emailXpackService.taskForm(taskId);
        XpackEmailCreate xpackEmailCreate = new XpackEmailCreate();
        byte[] bytes = taskForm.getContent();

        if (ObjectUtils.isNotEmpty(bytes)) {
            String emailContent;
            try {
                emailContent = new String(bytes, "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            taskForm.setContent(null);
            xpackEmailCreate.setEmailContent(emailContent);
        }
        xpackEmailCreate.setRequest(taskForm);
        return xpackEmailCreate;
    }

    @PostMapping("/preview")
    public String preview(@RequestBody XpackEmailViewRequest request) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        String panelId = request.getPanelId();
        String content = request.getContent();

        String url = ServletUtils.domain() + "/#/preview/" + panelId;

        String token = ServletUtils.getToken();
        String fileId = null;
        try {
            fileId = emailXpackService.print(url, token, buildPixel(request.getPixel()));
        } catch (Exception e) {
            LogUtil.error(e);
            DEException.throwException("预览失败，请联系管理员");
        }
        String imageUrl = "/system/ui/image/" + fileId;
        String html = "<div>" +
                "<h2>" + content + "</h2>" +
                "<img style='width: 100%;' id='" + panelId + "' src='" + imageUrl + "' />" +
                "</div>";

        return html;

    }

    @PostMapping("/delete/{taskId}")
    public void delete(@PathVariable Long taskId) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        try {
            XpackEmailTaskRequest request = emailXpackService.taskForm(taskId);
            GlobalTaskEntity globalTaskEntity = BeanUtils.copyBean(new GlobalTaskEntity(), request);
            scheduleService.deleteSchedule(globalTaskEntity);
            emailXpackService.delete(taskId);
        } catch (Exception e) {
            LogUtil.error(e);
            DEException.throwException(e);
        }
    }

    @PostMapping("/queryInstancies/{goPage}/{pageSize}")
    public Pager<List<XpackTaskInstanceDTO>> instancesGrid(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody XpackGridRequest request) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<XpackTaskInstanceDTO> instances = emailXpackService.taskInstanceGrid(request);
        Pager<List<XpackTaskInstanceDTO>> listPager = PageUtils.setPageInfo(page, instances);
        return listPager;
    }

    @PostMapping("/execInfo/{instanceId}")
    public String execInfo(@PathVariable Long instanceId) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        GlobalTaskInstance instanceForm = emailXpackService.instanceForm(instanceId);
        return instanceForm.getInfo();
    }

    private XpackPixelEntity buildPixel(String pixel) {

        if (StringUtils.isBlank(pixel)) return null;
        String[] arr = pixel.split("\\*");
        if (arr.length != 2) return null;
        try {
            XpackPixelEntity result = new XpackPixelEntity();
            int x = Integer.parseInt(arr[0]);
            int y = Integer.parseInt(arr[1]);
            result.setX(String.valueOf(x));
            result.setY(String.valueOf(y));
            return result;
        } catch (Exception e) {
            return null;
        }
    }

}
