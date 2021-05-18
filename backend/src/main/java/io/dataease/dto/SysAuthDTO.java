package io.dataease.dto;

import io.dataease.base.domain.SysAuth;
import io.dataease.base.domain.SysAuthDetail;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-05-12
 * Description:
 */
@Data
public class SysAuthDTO extends SysAuth {

     private List<SysAuthDetail> sysAuthDetails;

//    private List<BaseAuthDetail> baseAuthDetails;
//
//    private String authDetails;
//
//    @Override
//    public void setAuthDetails(String authDetails) {
//        this.authDetails = authDetails;
//        if(StringUtils.isNotEmpty(authDetails)){
//            try{
//                baseAuthDetails = JSON.parseArray(authDetails,BaseAuthDetail.class);
//            }catch (Exception e){
//                e.printStackTrace();
//                //ignored
//            }
//        }
//    }
//
//    public List<BaseAuthDetail> getBaseAuthDetails() {
//        return baseAuthDetails;
//    }
//
//    public void setBaseAuthDetails(List<BaseAuthDetail> baseAuthDetails) {
//        this.baseAuthDetails = baseAuthDetails;
//    }
//
//    @Override
//    public String getAuthDetails() {
//        return authDetails;
//    }
}
