package com.deweydatasystem.ro;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import com.deweydatasystem.model.SelectStatement;

import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RunQueryTemplateRequest {

    /**
     * The name of the SelectStatement.
     */
    @JsonProperty(value = "queryTemplateName", required = true)
    @Size(max = 50, message = "Select Statement name cannot exceed 50 characters")
    private String queryTemplateName;

    /**
     * The version of the SelectStatement.
     */
    @JsonProperty(value = "version", required = true)
    private int version;

    /**
     * The query's optional property overrides.  These differ from {@link SelectStatement#criteriaArguments} in that every {@link SelectStatement}
     * has these optional property overrides whereas not all {@link SelectStatement} have the same criteria parameters and arguments.
     */
    @JsonProperty(value = "overrides")
    private SelectStatement.PropertyOverrides overrides = new SelectStatement.PropertyOverrides();

    /**
     * The query's criteria runtime arguments.  The key is the name of the parameter to find in the query criteria.  The
     * value is what will be passed into the query criteria.
     */
    @JsonProperty(value = "arguments")
    private Map<String, List<String>> criteriaArguments = new HashMap<>();

}
