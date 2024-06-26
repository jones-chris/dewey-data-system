package com.deweydatasystem.service.query_template;

import com.deweydatasystem.dao.database.DatabaseMetadataCacheDao;
import com.deweydatasystem.dao.query_template.QueryTemplateDao;
import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.model.cte.CommonTableExpression;
import com.deweydatasystem.service.QueryTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class QueryTemplateServiceImpl implements QueryTemplateService {

    private QueryTemplateDao queryTemplateDao;

    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @Autowired
    public QueryTemplateServiceImpl(QueryTemplateDao queryTemplateDao, DatabaseMetadataCacheDao databaseMetadataCacheDao) {
        this.queryTemplateDao = queryTemplateDao;
        this.databaseMetadataCacheDao = databaseMetadataCacheDao;
    }

    @Override
    public boolean save(SelectStatement selectStatement) {
        // Set metadata.
        Objects.requireNonNull(selectStatement, "selectStatement is null");
        Objects.requireNonNull(selectStatement.getMetadata(), "selectStatement.metadata is null");
        Objects.requireNonNull(selectStatement.getColumns(), "selectStatement.columns is null");

        long limit = 10L; // todo:  This is the default limit.  Make this configurable in the future.
        if (selectStatement.getLimit() != null) {
            limit = selectStatement.getLimit();
        }

        selectStatement.getMetadata().setNumberOfColumnsReturned(selectStatement.getColumns().size());
        selectStatement.setLimit(limit);
        selectStatement.getMetadata().setMaxNumberOfRowsReturned(limit);
        selectStatement.getMetadata().setColumns(selectStatement.getColumns());

        // Create metadata for criteria parameters.
//        this.setSelectStatementCriteriaParameters(selectStatement);

        // Get the newest version number in the database.
        this.queryTemplateDao.getNewestVersion(selectStatement.getMetadata().getName())
                .ifPresentOrElse(
                        (currentVersion) -> selectStatement.getMetadata().setVersion(currentVersion + 1), // If version exists, increment by 1.
                        () -> selectStatement.getMetadata().setVersion(0) // If version does not exist, set to 0.
                );

        // Save the Select Statement.
        return queryTemplateDao.save(selectStatement);
    }

    // todo:  add this method back after producing an MVP.
//    @Override
//    public Map<String, SelectStatement> findByNames(List<String> names) {
//        return this.queryTemplateDao.findByNames(names);
//    }

    @Override
    public SelectStatement findByName(String name, int version) {
        return queryTemplateDao.findByName(name, version);
    }

    @Override
    public Set<String> getNames(String databaseName) {
        return queryTemplateDao.listNames(databaseName);
    }

    /**
     * This method is responsible for retrieving a {@link SelectStatement} for each of the
     * {@link CommonTableExpression}s in the parameter, commonTableExpressions.
     *
     * @param commonTableExpressions {@link List<CommonTableExpression>}
     */
    @Override
    public void getCommonTableExpressionSelectStatement(List<CommonTableExpression> commonTableExpressions) {
        if (! commonTableExpressions.isEmpty()) {
            commonTableExpressions.forEach(commonTableExpression -> {
                SelectStatement selectStatement = this.queryTemplateDao.findByName(
                        commonTableExpression.getQueryName(),
                        commonTableExpression.getVersion()
                );

                // Pass parameters, arguments, and overrides from CTE to related select statement.
                selectStatement.setCriteriaArguments(commonTableExpression.getParametersAndArguments());
                selectStatement.setOverrides(commonTableExpression.getOverrides());

//                commonTableExpression.setSelectStatement(selectStatement);
            });
        }
    }

    @Override
    public List<Integer> getVersions(String name) {
        return this.queryTemplateDao.getVersions(name);
    }

    @Override
    public SelectStatement.Metadata getMetadata(String name, int version) {
        return this.queryTemplateDao.getMetadata(name, version);
    }

//    /**
//     * A private helper method for setting the {@param selectStatement}'s {@link List<CriterionParameter>}.
//     *
//     * @param selectStatement {@link SelectStatement}
//     */
//    private void setSelectStatementCriteriaParameters(SelectStatement selectStatement) {
//        selectStatement.getFlattenedCriteria().forEach(criterion -> {
//            // Check that the column exists.
//            if (this.databaseMetadataCacheDao.columnExists(criterion.getColumn())) {
//                // If so, create a CriterionParameter for each parameter and add it to the SelectStatement's Metadata.
//                criterion.getFilter().getParameters().forEach(parameter -> {
//                    selectStatement.getMetadata().getCriteriaParameters().add(
//                            new CriterionParameter(
//                                    parameter,
//                                    criterion.getColumn(),
//                                    criterion.hasMultipleValuesOperator())
//                    );
//                });
//            } else {
//                throw new CacheMissException("Did not recognize column, " + criterion.getColumn().getColumnName());
//            }
//        });
//    }

}
