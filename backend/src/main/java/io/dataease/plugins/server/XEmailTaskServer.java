package io.dataease.plugins.server;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.plugins.common.entity.GlobalTaskEntity;
import io.dataease.plugins.common.entity.XpackGridRequest;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.email.dto.request.XpackEmailCreate;
import io.dataease.plugins.xpack.email.dto.request.XpackEmailTaskRequest;
import io.dataease.plugins.xpack.email.dto.response.XpackTaskGridDTO;
import io.dataease.plugins.xpack.email.service.EmailXpackService;
import io.dataease.service.ScheduleService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.ObjectUtils;
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
    public void save(@RequestBody XpackEmailCreate param) throws Exception{
        XpackEmailTaskRequest request = param.fillContent();
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
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

        if(ObjectUtils.isNotEmpty(bytes)) {
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
}
