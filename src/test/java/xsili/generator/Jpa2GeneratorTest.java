package xsili.generator;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mybatis.generator.JavaCodeGenerationTest;
import org.mybatis.generator.SqlScriptRunner;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

public class Jpa2GeneratorTest {

    @Test
    public void generatorTest() throws Exception {
        // create database
        SqlScriptRunner scriptRunner = new SqlScriptRunner(JavaCodeGenerationTest.class.getResourceAsStream("/xsili-hsqldb-test.sql"), "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:dbname", "sa", "");
        scriptRunner.executeScript();

        // generate code
        List<String> warnings = new ArrayList<String>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(this.getClass().getClassLoader().getResourceAsStream("xsili-jpa-generator.xml"));

        DefaultShellCallback shellCallback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);
        myBatisGenerator.generate(null);
    }

}
