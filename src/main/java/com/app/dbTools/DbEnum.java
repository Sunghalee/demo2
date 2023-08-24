package com.app.dbTools;

import com.zaxxer.hikari.HikariDataSource;


/**
 * 连接池开启 关闭
 */
public class DbEnum {

    //连接池对象
    public static HikariDataSource dataSource = null;

    /**
     * 关闭连接池
     */
    public static void shutdownDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
