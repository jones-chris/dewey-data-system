package com.deweydatasystem.model;

import java.util.List;

public interface Constraint {

    boolean isSatisfied(List<String> arguments);

}
