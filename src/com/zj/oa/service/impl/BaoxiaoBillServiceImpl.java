package com.zj.oa.service.impl;

import com.zj.oa.mapper.BaoxiaoBillMapper;
import com.zj.oa.pojo.BaoxiaoBill;
import com.zj.oa.pojo.BaoxiaoBillExample;
import com.zj.oa.service.BaoxiaoBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaoxiaoBillServiceImpl implements BaoxiaoBillService {

    @Autowired
    private BaoxiaoBillMapper baoxiaoBillMapper;

    @Override
    public void saveBaoxiaoBill(BaoxiaoBill baoxiaoBill) {
        this.baoxiaoBillMapper.insert(baoxiaoBill);
    }

    @Override
    public BaoxiaoBill findBaoxiaoBillById(long id) {
        BaoxiaoBill bill = baoxiaoBillMapper.selectByPrimaryKey(id);
        return bill;
    }

    @Override
    public BaoxiaoBill findBaoxiaoBillById(Long id) {
        BaoxiaoBill bill = baoxiaoBillMapper.selectByPrimaryKey(id);
        return bill;
    }

    @Override
    public List<BaoxiaoBill> findLeaveBillListByUser(long id) {
        BaoxiaoBillExample example = new BaoxiaoBillExample();
        BaoxiaoBillExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(id);
        return baoxiaoBillMapper.selectByExample(example);
    }

    @Override
    public void deleteBaoxiaoBillById(long id) {
        baoxiaoBillMapper.deleteByPrimaryKey(id);
    }
}
