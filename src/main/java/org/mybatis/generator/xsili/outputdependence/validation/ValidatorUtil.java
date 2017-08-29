package org.mybatis.generator.xsili.outputdependence.validation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;

import org.apache.commons.collections.CollectionUtils;
import org.mybatis.generator.xsili.outputdependence.I18NUtils;
import org.mybatis.generator.xsili.outputdependence.exception.BusinessException;
import org.mybatis.generator.xsili.outputdependence.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ValidatorUtil {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatorUtil.class);

    /** hibernate校验器 */
    private static javax.validation.Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 校验对象的属性
     * 
     * @param object 校验的对象
     * @param strings
     * @return
     */
    public static List<Validator> validate(Object object, String... strings) {
        String nameSuffix = "";
        if (strings.length > 0) {
            nameSuffix = strings[0];
        }
        List<Validator> validatorList = new ArrayList<>();
        Set<ConstraintViolation<Object>> set = validator.validate(object);
        for (ConstraintViolation<Object> v : set) {
            Validator validator = new Validator();
            validator.setName(v.getPropertyPath().toString() + nameSuffix);
            String value = null;
            if (v.getInvalidValue() != null) {
                value = v.getInvalidValue().toString();
            }
            validator.setValue(value);
            validator.setMsg(I18NUtils.getText(v.getMessage(), replaceArgs(v)));
            validatorList.add(validator);
        }

        validatorList.sort(new Comparator<Validator>() {
            @Override
            public int compare(Validator o1, Validator o2) {
                int length1 = o1.getMsg() == null ? 0 : o1.getMsg().length();
                int length2 = o2.getMsg() == null ? 0 : o2.getMsg().length();
                return length1 < length2 ? -1 : 1;
            }
        });

        return validatorList;
    }

    /**
     * 根据校验结果构造 result, 如果validatorList 为空, 则返回null
     * 
     * @param validatorList
     * @return
     */
    public static Result getResultModel(List<Validator> validatorList) {
        if (CollectionUtils.isEmpty(validatorList)) {
            return null;
        }

        // 构建ResultModel对象
        // String msg = CommonUtil.getText("validation.entity.param.error", validatorList.size()); // 返回总错误数
        String msg = validatorList.get(0).getMsg(); // 返回第一个错误
        Result resultModel = Result.error(msg);
        resultModel.setData(validatorList);
        return resultModel;
    }

    /**
     * 校验对象的属性
     * 
     * @param object 校验的对象
     * @param strings
     * @return
     */
    public static void checkParams(Object object, String... strings) throws BusinessException {
        List<Validator> validatorList = validate(object, strings);
        Result result = getResultModel(validatorList);
        if (result != null) {
            BusinessException businessException = new BusinessException();
            businessException.setResult(result);
            throw businessException;
        }
    }

    /**
     * 替换占位符
     * 
     * @param v
     * @return
     */
    private static Object[] replaceArgs(ConstraintViolation<Object> v) {
        Map<String, Object> map = v.getConstraintDescriptor().getAttributes();
        if (map.containsKey("value")) {
            return new Object[] {map.get("value").toString()};
        } else if (map.containsKey("min") && map.containsKey("max")) {
            return new Object[] {map.get("min").toString(), map.get("max").toString()};
        } else if (map.containsKey("max")) {
            return new Object[] {map.get("max").toString()};
        } else {
            return null;
        }
    }

}
