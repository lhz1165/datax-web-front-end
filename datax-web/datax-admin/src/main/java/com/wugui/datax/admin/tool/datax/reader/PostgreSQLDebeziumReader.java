package com.wugui.datax.admin.tool.datax.reader;

import java.util.Map;

/**
 * postgresql debezium reader 构建类（用于增量同步）
 *
 * @author zhouhongfa@gz-yibo.com
 * @version 1.0
 * @since 2024/01/01
 */
public class PostgreSQLDebeziumReader extends BaseReaderPlugin implements DataxReaderInterface {
    @Override
    public String getName() {
        return "postgresqldebeziumreader";
    }

    @Override
    public Map<String, Object> sample() {
        return null;
    }
}

