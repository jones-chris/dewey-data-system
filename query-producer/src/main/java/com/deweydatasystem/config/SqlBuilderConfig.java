package com.deweydatasystem.config;

import com.deweydatasystem.SqlBuilderFactory;
import com.deweydatasystem.dao.database.DatabaseMetadataCacheDao;
import com.deweydatasystem.model.validator.*;
import com.deweydatasystem.service.QueryTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SqlBuilderConfig {

    private final DatabaseMetadataCacheDao databaseMetadataCacheDao;

    private final QueryTemplateService queryTemplateService;

    private final QbConfig qbConfig;

    @Autowired
    public SqlBuilderConfig(
            final DatabaseMetadataCacheDao databaseMetadataCacheDao,
            final QueryTemplateService queryTemplateService,
            final QbConfig qbConfig
    ) {
        this.databaseMetadataCacheDao = databaseMetadataCacheDao;
        this.queryTemplateService = queryTemplateService;
        this.qbConfig = qbConfig;
    }

    // todo:  Consider making each of these dependent validators beans, so that they can be mocked.
    @Bean
    public SelectStatementValidator buildSelectStatementValidator() {
        return new SelectStatementValidator(
                qbConfig,
                new ColumnValidator(this.databaseMetadataCacheDao),
                new TableValidator(),
                new CommonTableExpressionValidator(),
                new CriterionValidator(),
                new JoinValidator()
        );
    }

    @Bean
    public SqlBuilderFactory buildSqlBuilderFactory() {
        return new SqlBuilderFactory(
                this.databaseMetadataCacheDao,
                this.queryTemplateService,
                this.buildSelectStatementValidator()
        );
    }

}
