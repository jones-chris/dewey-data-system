package com.deweydatasystem.model;

public interface SqlRepresentation {

    String toSql(char beginningDelimiter, char endingDelimiter);

}
