package piotrholda.portfoliomanager;

import lombok.Setter;

@Setter
public class Calculator {

    private double value1;
    private double value2;
    private String operator;

    public double calculate() {
        return 10.0;
    }
}
