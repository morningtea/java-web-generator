package org.mybatis.generator.xsili.outputdependence.base.jpa;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.mybatis.generator.xsili.outputdependence.NotNullBeanUtils;
import org.mybatis.generator.xsili.outputdependence.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.querydsl.jpa.impl.JPAQueryFactory;

public class BaseServiceImpl<T extends BaseEntity> implements BaseService<T> {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceImpl.class);

    protected BaseRepository<T> baseRepository;

    public BaseServiceImpl(BaseRepository<T> baseRepository) {
        this.baseRepository = baseRepository;
    }

    // 简单用法示例, 如: 按班级查看捐款总额
    // QUser quser = QUser.user;
    // List<Tuple> tupleList = jpaQueryFactory.select(quser.class, quser.donation.sum())
    // .from(quser)
    // .groupBy(quser.class)
    // .orderBy(quser.donation.sum().desc())
    // .fetch();
    // tupleList.forEach(tuple -> {...});
    @Autowired
    protected JPAQueryFactory jpaQueryFactory;
    @Autowired
    protected EntityManager entityManager;

    /**
     * 更新所有字段
     * 
     * @param t
     * @return
     */
    protected final T updateAll(T t) {
        if (t == null) {
            throw new BusinessException("参数t不能为空");
        }
        if (t.getId() == null) {
            throw new BusinessException("id不能为空");
        }

        t.setGmtModified(new Date());
        return this.baseRepository.saveAndFlush(t);
    }

    /**
     * 只更新not null字段
     * 
     * @param t
     * @return
     */
    protected final T updateNotNull(T t) {
        if (t == null) {
            throw new BusinessException("参数t不能为空");
        }
        if (t.getId() == null) {
            throw new BusinessException("id不能为空");
        }

        T exist = this.baseRepository.findById(t.getId()).orElse(null);
        if (exist == null) {
            throw new BusinessException("记录不存在");
        }
        // 把not null的值赋给exist
        NotNullBeanUtils.copyNotNullProperties(t, exist);
        // 更新exist
        exist.setGmtModified(new Date());
        return this.baseRepository.saveAndFlush(exist);
    }

    @Override
    public void deleteById(Long id) {
        T t = this.getById(id);
        if (t != null) {
            t.setIsDeleted(true);
            this.updateAll(t);
        }
    }

    @Override
    public T getById(Long id) {
        if (id == null) {
            return null;
        }

        T t = this.baseRepository.findById(id).orElse(null);
        if (t == null || t.getIsDeleted()) {
            return null;
        }
        return t;
    }

    @Override
    public List<T> list(List<Long> idList) {
        List<T> list = this.baseRepository.findAllById(idList);
        return list.stream().filter(t -> {
            return !t.getIsDeleted();
        }).collect(Collectors.toList());
    }

}