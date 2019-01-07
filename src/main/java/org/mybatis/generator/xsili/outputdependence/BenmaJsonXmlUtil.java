package org.mybatis.generator.xsili.outputdependence;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.mybatis.generator.xsili.outputdependence.exception.SystemException;
import org.mybatis.generator.xsili.outputdependence.exception.SystemExceptionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * json, xml 转换器
 */
public class BenmaJsonXmlUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenmaJsonXmlUtil.class);

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        // 序列化时支持空对象
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 反序列化时忽略掉多余的属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 默认从毫秒数转成Bean
        // 需要和 spring-mvc 日期参数转换类兼容 {@link com.benma.context.transform.DateConverter}
        // mapper.setDateFormat(new SimpleDateFormat(DateUtil.FORMAT_YMDHMS));
    }

    /**
     * 把bean转换成json字符串
     * 
     * @param object
     * @return
     * @throws JsonProcessingException
     */
    public static String toJson(Object object) throws SystemException {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SystemException(SystemExceptionEnum.to_json_convert, e);
        }
    }

    /**
     * 把json字符串转换成bean
     * 
     * @param json
     * @param clazz
     * @return
     * @throws Exception
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws SystemException {
        try {
            return (T) mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new SystemException(SystemExceptionEnum.from_json_convert, e);
        }
    }

    /**
     * 把json转换成list
     * 
     * @param json
     * @param clazz
     * @return
     */
    public static <T> List<T> fromJsonList(String json, Class<T> clazz) throws SystemException {
        JavaType t = mapper.getTypeFactory().constructParametricType(List.class, clazz);
        try {
            return mapper.readValue(json, t);
        } catch (Exception e) {
            throw new SystemException(SystemExceptionEnum.from_json_convert, e);
        }
    }

    /**
     * 把json转换成map
     * 
     * @param json
     * @param clazzk Map中key的类型
     * @param clazzv Map中value的类型
     * @return
     */
    public static <K, V> Map<K, V> fromJsonMap(String json, Class<K> clazzk, Class<V> clazzv) throws SystemException {
        JavaType t = mapper.getTypeFactory().constructParametricType(Map.class, clazzk, clazzv);
        try {
            return mapper.readValue(json, t);
        } catch (Exception e) {
            throw new SystemException(SystemExceptionEnum.from_json_convert, e);
        }
    }

    /**
     * 将bean转换成xml
     * 
     * @param object
     * @return
     * @throws SystemException
     */
    public static String toXml(Object object) throws SystemException {
        StringWriter out = new StringWriter();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(object, out);
        } catch (JAXBException e) {
            throw new SystemException(SystemExceptionEnum.to_xml_convert, e);
        }
        return out.toString();
    }

    /**
     * 把xml转换成bean
     * 
     * @param xml
     * @param clazz
     * @return
     * @throws SystemException
     * @throws XMLStreamException
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromXml(String xml, Class<T> clazz) throws SystemException {
        InputStream is = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
            // 避免XXE攻击, disable external entities
            xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            // 避免XXE攻击, disable DTDs entirely for the factory
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            XMLStreamReader xsr = xmlInputFactory.createXMLStreamReader(new StringReader(xml));
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (T) unmarshaller.unmarshal(xsr);
        } catch (JAXBException | XMLStreamException e) {
            throw new SystemException(SystemExceptionEnum.from_json_convert, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("xmlToBean转换关闭流失败", e);
                }
            }
        }
    }

}
