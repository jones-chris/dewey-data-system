package net.querybuilder4j.dao.query_template;

import net.querybuilder4j.config.QbConfig;
import net.querybuilder4j.config.QueryTemplateRepositoryType;
import net.querybuilder4j.exceptions.QueryTemplateRepositoryTypeNotRecognizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class QueryTemplateDaoFactory extends AbstractFactoryBean<QueryTemplateDao> {

    private final QbConfig qbConfig;

    @Autowired
    public QueryTemplateDaoFactory(QbConfig qbConfig) {
        this.qbConfig = qbConfig;
        this.setSingleton(true);
    }

    @Override
    public Class<?> getObjectType() {
        return QueryTemplateDao.class;
    }

    @Override
    protected QueryTemplateDao createInstance() {
        QueryTemplateRepositoryType repositoryType = this.qbConfig.getQueryTemplateDataSource().getRepositoryType();

        if (QueryTemplateRepositoryType.IN_MEMORY.equals(repositoryType)) {
            return new InMemoryQueryTemplateDaoImpl();
        }
        else if (QueryTemplateRepositoryType.SQL_DATABASE.equals(repositoryType)) {
            DataSource dataSource = this.qbConfig.getQueryTemplateDataSource().getDataSource();
            return new SqlDatabaseQueryTemplateDaoImpl(dataSource);
        }
        else {
            throw new QueryTemplateRepositoryTypeNotRecognizedException(repositoryType.toString());
        }
    }

}
