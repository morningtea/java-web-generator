package org.mybatis.generator.outputdependence;

import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;

/**
 * ID 生成器
 *
 */
public class IDGenerator {

    private static IdGenerator idGenerator = new AlternativeJdkIdGenerator();

    /**
     * 返回UUID
     * 
     * @return
     */
    public static String generateId() {
        // UUID uuid = UUID.randomUUID();
        return idGenerator.generateId().toString().toUpperCase();
    }

}
