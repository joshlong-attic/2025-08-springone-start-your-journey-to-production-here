package com.example.data_oriented_programming;

public class DataOrientedProgrammingApplication {

    public static void main(String[] args) {


    }

}

// 1. sealed types
// 2. pattern matching
// 3. smart switch
// 4. records

sealed interface Loan permits SecuredLoan, UnsecuredLoan {
}


final class SecuredLoan implements Loan {
}


record UnsecuredLoan(float interest) implements Loan {
}

class Loans {

    String message(Loan loan) {
        return switch (loan) {
            case UnsecuredLoan(var interest) -> "ouch! that " + interest + "% rate is going to hurt!";
            case SecuredLoan _ -> "good job. you have a secured loan.";
        };
    }
}