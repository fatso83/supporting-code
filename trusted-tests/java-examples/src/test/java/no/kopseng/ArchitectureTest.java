package no.kopseng;


import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses
class ArchitectureTest {

    @ArchTest
    static final ArchRule onlyUseSuppliedClock = noClasses()
            .should().callMethod(LocalDateTime.class, "now")
            .orShould().callMethod(LocalDateTime.class, "now", ZoneId.class)
            .orShould().callMethod(OffsetDateTime.class, "now")
            .orShould().callMethod(OffsetDateTime.class, "now", ZoneId.class)
            // etc
            ;
}