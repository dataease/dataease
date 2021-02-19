package io.dataease.commons.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeneratorApi {

    @Autowired
    private CodeGenerator codeGenerator;
    @GetMapping("/generator/{moduleName}/{basePackage}/{tableName}")
    public void generator(@PathVariable("moduleName") String moduleName, @PathVariable("basePackage") String basePackage, @PathVariable("tableName") String tableName){
        codeGenerator.generator(moduleName, basePackage, tableName);
    }
}
