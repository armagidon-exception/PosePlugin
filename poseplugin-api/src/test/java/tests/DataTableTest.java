package tests;

import org.junit.Test;
import ru.armagidon.poseplugin.api.utils.misc.DataTable;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class DataTableTest
{
    @Test
    public void test() {
        DataTable<Integer, String> table = new DataTable<>();
        table.define(1, "TEST");
        assertTrue(table.getDirty().isEmpty());
        table.set(1, "");
        assertFalse(table.getDirty().isEmpty());
        table.set(1, "LOL");
        System.out.println(table.getDirty().toString());
    }
}
