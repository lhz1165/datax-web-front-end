package com.wugui.datax.admin.tool.datax.reader;

import java.util.Map;

/**
 * mysql debezium reader 构建类（用于增量同步）
 *
 * @author zhouhongfa@gz-yibo.com
 * @ClassName MySQLDebeziumReader
 * @Version 1.0
 * @since 2024/01/01
 */
public class MySQLDebeziumReader extends BaseReaderPlugin implements DataxReaderInterface {
    @Override
    public String getName() {
        return "mysqldebeziumreader";
    }

    @Override
    public Map<String, Object> sample() {
        return null;
    }
}

