import io.dataease.visualization.server.StaticResourceContentGuard;
import io.dataease.visualization.server.StaticResourceServer;
import io.dataease.config.StaticResourceSecurityInterceptor;
import io.dataease.i18n.DeReloadableResourceBundleMessageSource;
import io.dataease.i18n.Translator;
import org.springframework.mock.web.*;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.*;
import java.util.*;

public class Regression {
    static int count;
    interface Work { void run() throws Exception; }
    static void check(boolean b, String name) { if (!b) throw new AssertionError(name); count++; System.out.println("PASS "+name); }
    static void denied(Work w, String name) throws Exception {try { w.run(); } catch (Exception e) {check(true,name);return;} throw new AssertionError(name);}
    static byte[] svg(String body) {return ("<svg xmlns='http://www.w3.org/2000/svg'>"+body+"</svg>").getBytes(java.nio.charset.StandardCharsets.UTF_8);}
    public static void main(String[] args) throws Exception {
        var messages=new DeReloadableResourceBundleMessageSource();messages.setBasename("classpath:i18n/core");new Translator().setMessageSource(messages);
        byte[] png=null;
        for (String format: List.of("png","jpg","gif")) {
            var out=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(2,2,BufferedImage.TYPE_INT_RGB),format,out);
            byte[] data=out.toByteArray();StaticResourceContentGuard.validate("test."+format,data);check(true,"valid "+format);
            if(format.equals("png")) png=data;
            denied(()->StaticResourceContentGuard.validate("evil.html",data),format+" magic with html rejected");
        }
        final byte[] image=png;
        denied(()->StaticResourceContentGuard.validate("fake.jpg",image),"extension mismatch rejected");
        denied(()->StaticResourceContentGuard.validate("fake.gif","GIF89a<script>alert(1)</script>".getBytes()),"magic-only polyglot rejected");
        byte[] safe=svg("<defs><linearGradient id='g'><stop offset='0' stop-color='red'/></linearGradient></defs><rect width='10' height='10' fill='url(#g)'/>");
        StaticResourceContentGuard.validate("safe.svg",safe);check(true,"static gradient SVG allowed");
        StaticResourceContentGuard.validate("prefix.svg","<s:svg xmlns:s='http://www.w3.org/2000/svg'><s:path d='M0 0L1 1'/></s:svg>".getBytes());check(true,"safe namespace prefix allowed");
        for(String payload: List.of("<script>alert(1)</script>","<s:script xmlns:s='http://www.w3.org/2000/svg'>alert(1)</s:script>","<foreignObject><div xmlns='http://www.w3.org/1999/xhtml'>bad</div></foreignObject>","<rect onload='alert(1)'/>","<rect xmlns:s='http://www.w3.org/2000/svg' s:onload='alert(1)'/>","<use href='javascript:alert(1)'/>","<use href='https://example.test/a.svg'/>","<animate attributeName='href' values='javascript:alert(1)'/>","<set attributeName='onload' to='alert(1)'/>","<style>@import url(https://example.test);</style>","<rect style='fill:url(https://example.test)'/>","<?xml-stylesheet href='https://example.test/x'?>")) {
            denied(()->StaticResourceContentGuard.validate("evil.svg",svg(payload)),"active SVG rejected "+payload.substring(0,Math.min(35,payload.length())));
        }
        denied(()->StaticResourceContentGuard.validate("dtd.svg","<!DOCTYPE svg [<!ENTITY x SYSTEM 'file:///etc/passwd'>]><svg xmlns='http://www.w3.org/2000/svg'>&x;</svg>".getBytes()),"DOCTYPE rejected");
        denied(()->StaticResourceContentGuard.validate("large.png",new byte[StaticResourceContentGuard.MAX_BYTES+1]),"size bounded");
        var dir=Files.createTempDirectory("static-resource-regression-");var server=new StaticResourceServer();var field=StaticResourceServer.class.getDeclaredField("staticDir");field.setAccessible(true);field.set(server,dir.toString());
        try {
            server.upload("uploaded",new MockMultipartFile("file","test.png","image/png",image));check(Files.exists(dir.resolve("uploaded.png")),"valid upload written");
            denied(()->server.upload("bad",new MockMultipartFile("file","test.html","image/png",image)),"upload html rejected");
            check(!Files.exists(dir.resolve("bad.html")),"rejected upload not persisted");
            server.saveSingleFileToServe("template.png",Base64.getEncoder().encodeToString(image));check(Files.exists(dir.resolve("template.png")),"template image written");
            server.saveSingleFileToServe("snapshot.svg",Base64.getEncoder().encodeToString(safe));check(Files.exists(dir.resolve("snapshot.svg")),"static SVG snapshot written");
            denied(()->server.saveSingleFileToServe("evil.html",Base64.getEncoder().encodeToString(image)),"template arbitrary extension rejected");
            denied(()->server.saveSingleFileToServe("../outside.png",Base64.getEncoder().encodeToString(image)),"template traversal rejected");
            denied(()->server.saveFilesToServe("{\"/static-resource/evil.svg\":\""+Base64.getEncoder().encodeToString(svg("<script/>"))+"\"}"),"template batch propagates rejection");
            check(!Files.exists(dir.resolve("evil.svg")),"rejected template not persisted");
            var interceptor=new StaticResourceSecurityInterceptor();var handler=new ResourceHttpRequestHandler();
            var req=new MockHttpServletRequest("GET","/de2api/static-resource/old.svg");var res=new MockHttpServletResponse();check(interceptor.preHandle(req,res,handler),"existing SVG still served");check(res.getHeader("Content-Security-Policy").contains("sandbox;") && res.getHeader("Content-Security-Policy").contains("script-src 'none'"),"legacy SVG sandbox and script block");check("nosniff".equals(res.getHeader("X-Content-Type-Options")),"nosniff header");
            res=new MockHttpServletResponse();check(!interceptor.preHandle(new MockHttpServletRequest("GET","/de2api/static-resource/old.html"),res,handler)&&res.getStatus()==404,"legacy HTML blocked");
            res=new MockHttpServletResponse();check(interceptor.preHandle(req,res,new Object())&&res.getHeader("Content-Security-Policy")==null,"non-resource controller unchanged");
        } finally {try(var files=Files.walk(dir)){for(var f:files.sorted(Comparator.reverseOrder()).toList())Files.delete(f);}}
        System.out.println("RESULT PASS="+count);
    }
}
