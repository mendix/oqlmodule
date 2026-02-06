package com.mendix.oqlmodule.test;

import com.mendix.systemwideinterfaces.MendixRuntimeException;
import oql.actions.ExecuteDMLStatement;
import oqlexample.proxies.ExampleData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExecuteDMLStatementTest extends OQLStatementTestSkeleton {

    @Test
    public void executeValidUpdateDMLStatement() throws Exception {
        ExampleData example = new ExampleData(this.context);
        example.setContents("Before");
        example.commit();

        String updateStmt =
            "UPDATE OQLExample.ExampleData SET Contents = 'After' WHERE id = '" +
                example.getMendixObject().getId().toLong() + "'";

        Long result = new ExecuteDMLStatement(this.context, updateStmt)
            .executeAction();

        assertEquals(1L, result);
    }

    @Test
    public void executeValidDeleteDMLStatement() throws Exception {
        ExampleData example = new ExampleData(this.context);
        example.setContents("ToDelete");
        example.commit();

        String deleteStmt =
            "DELETE FROM OQLExample.ExampleData WHERE Contents = 'ToDelete'";

        java.lang.Long result = new ExecuteDMLStatement(this.context, deleteStmt)
            .executeAction();

        assertEquals(1L, result);
    }

    @Test
    public void executeDMLWithNullStatement() {
        assertThrows(MendixRuntimeException.class, () ->
            new ExecuteDMLStatement(this.context, null).executeAction()
        );
    }

    @Test
    public void executeDMLWithInvalidStatement() {
        assertThrows(Exception.class, () ->
            new ExecuteDMLStatement(this.context, "BAD OQL").executeAction()
        );
    }
}

