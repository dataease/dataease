package io.dataease.plugins.xpack.user.service;


import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import io.dataease.plugins.common.service.PluginComponentService;

public abstract class UserXpackService extends PluginComponentService {
  
  public abstract void templateDown(HttpServletResponse response);

  public abstract void upload(MultipartFile multipartFile, HttpServletResponse response);
  
  
}
