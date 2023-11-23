package net.querybuilder4j.model.validator;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.querybuilder4j.model.join.Join;

import java.util.List;

@Slf4j
public class JoinValidator extends SqlValidator<List<Join>> {

    @Override
    public void isValid(@NonNull List<Join> joins) {
        joins.forEach(join -> {
            if (join.getParentJoinColumns().size() != join.getTargetJoinColumns().size()) {
                final String message = "Each join must have at least 1 pair of columns to join on";

                log.error(message);
                throw new IllegalStateException(message);
            }

            if (join.getParentTable().getFullyQualifiedName().equals(join.getTargetTable().getFullyQualifiedName())) {
                final String message = "A join cannot be performed on the same table, " + join.getParentTable().getFullyQualifiedName();

                log.error(message);
                throw new IllegalStateException(message);
            }
        });
    }

}
