package com.zj.oa.service;

import com.zj.oa.pojo.BaoxiaoBill;

import java.util.List;

public interface BaoxiaoBillService {
    void saveBaoxiaoBill(BaoxiaoBill baoxiaoBill);
    BaoxiaoBill findBaoxiaoBillById(long id);
    public BaoxiaoBill findBaoxiaoBillById(Long id);

    List<BaoxiaoBill> findLeaveBillListByUser(long id);

    void deleteBaoxiaoBillById(long id);
}
