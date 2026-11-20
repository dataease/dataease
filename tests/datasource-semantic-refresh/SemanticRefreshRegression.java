package io.dataease.datasource.manage;
import java.util.*;
import io.dataease.api.ds.DatasourceSemanticApi.*;
import io.dataease.datasource.dao.auto.entity.*;
import io.dataease.datasource.dao.auto.repository.*;
public class SemanticRefreshRegression {
 @SuppressWarnings("unchecked") static <T> T proxy(Class<T> type, java.util.function.Function<String,Object> result) {return (T)java.lang.reflect.Proxy.newProxyInstance(type.getClassLoader(),new Class[]{type},(p,m,a)->result.apply(m.getName()));}
 public static void main(String[] args) {
  var table=new CoreDatasourceTableSemantic();table.setId(1L);table.setTableName("orders");table.setActive(true);table.setAvailable(true);table.setDescription("old");
  var field=new CoreDatasourceFieldSemantic();field.setFieldName("status");field.setBusinessName("old");field.setConfirmed(true);
  var access=new DatasourceMetadataAccess(null,null,null,null){@Override io.dataease.dao.auto.entity.CoreDatasource lock(Long id){return null;}};
  var manage=new DatasourceSemanticManage(access,null,proxy(CoreDatasourceTableSemanticRepository.class,n->Optional.of(table)),proxy(CoreDatasourceFieldSemanticRepository.class,n->List.of(field)));
  var snapshot=new TableSemantic(1L,"orders",null,null,true,null,true,false,1,0);
  var suggestion=new DatasourceSemanticManage.SemanticSuggestion("new",null,List.of(new DatasourceSemanticManage.FieldSuggestion("status","new","new description","new aliases")));
  manage.applySuggestion(1L,snapshot,suggestion,Set.of("status"));
  if(!"new".equals(table.getDescription()) || !"old".equals(field.getBusinessName()) || field.getDescription()!=null)throw new AssertionError("unconfirmed refresh / confirmed empty protection");
  table.setConfirmed(true);table.setDescription(null);field.setConfirmed(false);
  manage.applySuggestion(1L,snapshot,suggestion,Set.of("status"));
  if(table.getDescription()!=null || !"new".equals(field.getBusinessName()) || field.isConfirmed())throw new AssertionError("confirmation gate");
  System.out.println("PASS: unconfirmed overwrite, confirmed empty protection, pending state preserved");
 }
}
