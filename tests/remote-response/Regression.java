import io.dataease.utils.*;
import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.datasource.provider.ApiUtils;
import io.dataease.extensions.datasource.dto.*;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class Regression {
    static int passed;
    static void check(boolean b,String m){if(!b)throw new AssertionError(m);passed++;System.out.println("PASS "+m);}
    interface Work {void run() throws Exception;}
    static void reject(Work w,String name)throws Exception{try{w.run();}catch(Exception e){passed++;System.out.println("PASS "+name+" "+e.getClass().getSimpleName());return;}throw new AssertionError(name);}
    public static void main(String[] args)throws Exception{
        var messages=new io.dataease.i18n.DeReloadableResourceBundleMessageSource();messages.setBasename("classpath:i18n/core");new io.dataease.i18n.Translator().setMessageSource(messages);
        try(var g=new RemoteTransfer(8,1000,()->{});var in=g.wrap(new ByteArrayInputStream(new byte[8]))){check(in.readAllBytes().length==8,"exact byte limit");}
        reject(()->{try(var g=new RemoteTransfer(8,1000,()->{});var in=g.wrap(new ByteArrayInputStream(new byte[9]))){in.skip(9);}},"skip counts bytes");
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.createContext("/",e->{try{
            String p=e.getRequestURI().getPath();
            if(p.equals("/headers"))Thread.sleep(1500);
            if(p.equals("/declared")){e.sendResponseHeaders(200,RemoteTransfer.RESPONSE_BYTES+1);return;}
            if(p.equals("/gzip"))e.getResponseHeaders().add("Content-Encoding","gzip");
            e.sendResponseHeaders(p.equals("/error")?500:200,0);
            OutputStream out=e.getResponseBody();
            if(p.equals("/gzip"))out=new GZIPOutputStream(out);
            if(p.equals("/chunked")||p.equals("/gzip")||p.equals("/large.csv")){
                byte[] chunk=new byte[65536];Arrays.fill(chunk,(byte)'a');
                int count=p.equals("/large.csv")?1601:257;
                for(int i=0;i<count;i++)out.write(chunk);
            }else if(p.equals("/slow")){for(int i=0;i<100;i++){out.write('a');out.flush();Thread.sleep(50);}}
            else out.write("hello".getBytes());out.close();
        }catch(Exception ignored){}finally{e.close();}});
        server.start();String base="http://127.0.0.1:"+server.getAddress().getPort();
        try{
            check("hello".equals(HttpClientUtil.get(base+"/ok",null)),"normal HTTP");
            for(String p:List.of("declared","chunked","gzip","error"))reject(()->HttpClientUtil.get(base+"/"+p,null),p);
            for(String p:List.of("slow","headers")){HttpClientConfig c=new HttpClientConfig();c.setResponseTimeout(200);long start=System.nanoTime();reject(()->HttpClientUtil.get(base+"/"+p,c),p);check(System.nanoTime()-start<2_000_000_000L,p+" deadline");}
            HttpClientConfig expanded=new HttpClientConfig();expanded.setMaxResponseSizeMb(32);
            check(HttpClientUtil.get(base+"/chunked",expanded).length()==257*65536,"configured API limit above default");
            ApiDefinition api=new ApiDefinition();api.setUrl(base+"/chunked");api.setRequest(new ApiDefinitionRequest());api.setMaxResponseSizeMb(1);
            reject(()->ApiUtils.execHttpRequest(true,api,10,List.of()),"API provider configured limit");
            api.setMaxResponseSizeMb(32);
            check(ApiUtils.execHttpRequest(true,api,10,List.of()).length()==257*65536,"API provider larger limit");
            reject(()->expanded.setMaxResponseSizeMb(0),"zero limit rejected");
            reject(()->expanded.setMaxResponseSizeMb(65),"excessive API limit rejected");
            reject(()->RemoteTransfer.timeoutMillis(-1),"negative timeout rejected");
            reject(()->RemoteTransfer.timeoutMillis(1801),"excessive timeout rejected");
            check(RemoteTransfer.timeoutMillis(null)==120000,"old config timeout default");
            check(RemoteTransfer.sizeBytes(null,100,1024)==RemoteTransfer.FILE_BYTES,"old config file default");
            Path dir=Files.createTempDirectory("remote-download-");
            var names=HttpClientUtil.downloadFile(base+"/ok.csv",new HttpClientConfig(),dir.toString());
            check(Files.readString(dir.resolve(names.get("tranName"))).equals("hello"),"file download");Files.delete(dir.resolve(names.get("tranName")));
            HttpClientConfig smallFile=new HttpClientConfig();smallFile.setMaxFileSizeMb(1);
            reject(()->HttpClientUtil.downloadFile(base+"/large.csv",smallFile,dir.toString()),"configured file limit");
            reject(()->HttpClientUtil.downloadFile(base+"/large.csv",new HttpClientConfig(),dir.toString()),"file byte limit");
            try(var paths=Files.list(dir)){check(paths.count()==0,"partial download deleted");}Files.delete(dir);
        }finally{server.stop(0);}
        var model=LocalModelUtils.class.getDeclaredField("modelValue");model.setAccessible(true);model.set(null,"standalone");
        String csv="a,b\n".repeat(120);
        check(ExcelUtils.csvData(new BufferedReader(new StringReader(csv)),true,2).size()==100,"CSV preview");
        check(ExcelUtils.csvData(new BufferedReader(new StringReader(csv)),false,2).size()==120,"CSV full import");
        var row=ExcelUtils.csvData(new BufferedReader(new StringReader("\"a,b\",\"x\"\"y\",\n")),true,3).get(0);
        check(Arrays.equals(row,new String[]{"a,b","x\"y",""}),"CSV quoting");
        var method=ExcelUtils.class.getDeclaredMethod("parseExcel",String.class,InputStream.class,boolean.class,String.class);method.setAccessible(true);
        for(boolean xlsx:List.of(true,false)){
            byte[] bytes;try(Workbook wb=xlsx?new XSSFWorkbook():new HSSFWorkbook();var out=new ByteArrayOutputStream()){
                for(int s=0;s<2;s++){var sh=wb.createSheet("sheet"+s);sh.createRow(0).createCell(0).setCellValue("field"+s);for(int r=1;r<=120;r++)sh.createRow(r).createCell(0).setCellValue(r);}wb.write(out);bytes=out.toByteArray();
            }
            var fetch=ExcelUtils.class.getDeclaredMethod("fetchExcelDataList",String.class,InputStream.class);fetch.setAccessible(true);
            var imported=(List<?>)fetch.invoke(new ExcelUtils(),"sheet0",new ByteArrayInputStream(bytes));
            check(imported.size()==120,(xlsx?"xlsx":"xls")+" full sync reader");
            for(boolean preview:List.of(true,false)){
                var result=(List<?>)method.invoke(new ExcelUtils(),xlsx?"test.xlsx":"test.xls",new ByteArrayInputStream(bytes),preview,"test");
                check(result.size()==2,(xlsx?"xlsx":"xls")+" sheet count");
                for(Object sheet:result){var data=(List<?>)sheet.getClass().getMethod("getData").invoke(sheet);check(data.size()==(preview?100:120),(xlsx?"xlsx":"xls")+" rows preview="+preview);}
            }
        }
        var zipCheck=ExcelUtils.class.getDeclaredMethod("validateRemoteWorkbook",Path.class);zipCheck.setAccessible(true);
        Path bomb=Files.createTempFile("remote-zip-", ".xlsx");try(var zip=new ZipOutputStream(Files.newOutputStream(bomb))){zip.putNextEntry(new ZipEntry("xl/sharedStrings.xml"));zip.write(new byte[2*1024*1024]);zip.closeEntry();}
        reject(()->zipCheck.invoke(null,bomb),"ZIP expansion ratio");Files.delete(bomb);
        System.out.println("RESULT PASS="+passed);
    }
}
