package com.kronos.config.table;

import com.kronos.config.Config;

/**
 * 元数据
 * catalog 管理多个catalog database
 * catelog database 管理多个 catalog table
 * @Author: jackila
 * @Date: 11:45 AM 2022-6-22
 */
public interface Catalog {
    void register(Config config);
}
