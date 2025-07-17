package com.example.dop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DopApplication {

    public static void main(String[] args) {
        SpringApplication.run(DopApplication.class, args);
    }

}

// sealed types
// pattern matching
// smart switch expressions
// records

class Loans {

    String messageFor(Loan loan) {
        return switch (loan) {
            case UnsecuredLoan(var interest) -> "ouch! that " + interest + "% is gonig to hurt!";
            case SecuredLoan _ -> "good job. nice loan";
        };
    }
}

sealed interface Loan permits SecuredLoan, UnsecuredLoan {
}

final class SecuredLoan implements Loan {
}

record UnsecuredLoan(float interest) implements Loan {
}

// tuples + name = record
