package com.deweydatasystem.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RunnableSql {

    private final String sql;

    private final List<SqlParameter> sqlParameters = new ArrayList<>();

    private final String database;

}
