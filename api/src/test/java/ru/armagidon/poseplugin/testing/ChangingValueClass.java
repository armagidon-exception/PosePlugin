package ru.armagidon.poseplugin.testing;

import lombok.Getter;
import lombok.Setter;

public class ChangingValueClass
{
    private @Getter @Setter int integer;
    private @Getter @Setter boolean flag;
    private @Getter @Setter String string;

    public ChangingValueClass(int integer, boolean flag, String string) {
        this.integer = integer;
        this.flag = flag;
        this.string = string;
    }
}
