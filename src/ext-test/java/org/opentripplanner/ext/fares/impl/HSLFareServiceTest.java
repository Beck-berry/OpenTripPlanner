package org.opentripplanner.ext.fares.impl;

import static org.opentripplanner.model.plan.TestItineraryBuilder.newItinerary;
import static org.opentripplanner.transit.model._data.TransitModelForTest.FEED_ID;

import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentripplanner.ext.fares.model.FareAttribute;
import org.opentripplanner.ext.fares.model.FareRuleSet;
import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.model.plan.Place;
import org.opentripplanner.model.plan.PlanTestConstants;
import org.opentripplanner.routing.core.FareType;
import org.opentripplanner.routing.fares.FareService;
import org.opentripplanner.transit.model.framework.FeedScopedId;
import org.opentripplanner.transit.model.site.FareZone;

public class HSLFareServiceTest implements PlanTestConstants {

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("createTestCases")
  public void canCalculateHSLFares(
    String testCaseName, // used to create parameterized test name
    FareService fareService,
    Itinerary i,
    List<String> expectedFareIds
  ) {
    Assertions.assertArrayEquals(
      expectedFareIds.toArray(),
      fareService.getCost(i).getDetails(FareType.regular).stream().map(f -> f.fareId()).toArray()
    );
  }

  private static List<Arguments> createTestCases() {
    List<Arguments> args = new LinkedList<>();

    FareZone A = FareZone.of(new FeedScopedId(FEED_ID, "A")).build();
    FareZone B = FareZone.of(new FeedScopedId(FEED_ID, "B")).build();
    FareZone C = FareZone.of(new FeedScopedId(FEED_ID, "C")).build();
    FareZone D = FareZone.of(new FeedScopedId(FEED_ID, "D")).build();

    Place A1 = PlanTestConstants.place("A1", 10.0, 12.0, A);
    Place A2 = PlanTestConstants.place("A2", 10.0, 12.0, A);

    Place B1 = PlanTestConstants.place("B1", 10.0, 12.0, B);
    Place B2 = PlanTestConstants.place("B2", 10.0, 12.0, B);

    Place C1 = PlanTestConstants.place("C1", 10.0, 12.0, C);
    Place C2 = PlanTestConstants.place("C2", 10.0, 12.0, C);

    Place D1 = PlanTestConstants.place("D1", 10.0, 12.0, D);
    Place D2 = PlanTestConstants.place("D2", 10.0, 12.0, D);

    float AB_PRICE = 2.80f;
    float BC_PRICE = 2.80f;
    float CD_PRICE = 3.20f;
    float ABC_PRICE = 4.10f;
    float BCD_PRICE = 4.10f;
    float ABCD_PRICE = 5.70f;
    float D_PRICE = 2.80f;

    HSLFareServiceImpl hslFareService = new HSLFareServiceImpl();
    int fiveMinutes = 60 * 5;

    // Fare attributes

    FareAttribute fareAttributeAB = FareAttribute
      .of(new FeedScopedId(FEED_ID, "AB"))
      .setCurrencyType("EUR")
      .setPrice(AB_PRICE)
      .setTransferDuration(fiveMinutes)
      .build();

    FareAttribute fareAttributeBC = FareAttribute
      .of(new FeedScopedId(FEED_ID, "BC"))
      .setCurrencyType("EUR")
      .setPrice(BC_PRICE)
      .setTransferDuration(fiveMinutes)
      .build();

    FareAttribute fareAttributeCD = FareAttribute
      .of(new FeedScopedId(FEED_ID, "CD"))
      .setCurrencyType("EUR")
      .setPrice(CD_PRICE)
      .setTransferDuration(fiveMinutes)
      .build();

    FareAttribute fareAttributeD = FareAttribute
      .of(new FeedScopedId(FEED_ID, "D"))
      .setCurrencyType("EUR")
      .setPrice(D_PRICE)
      .setTransferDuration(fiveMinutes)
      .build();

    FareAttribute fareAttributeABC = FareAttribute
      .of(new FeedScopedId(FEED_ID, "ABC"))
      .setCurrencyType("EUR")
      .setPrice(ABC_PRICE)
      .setTransferDuration(fiveMinutes)
      .build();

    FareAttribute fareAttributeBCD = FareAttribute
      .of(new FeedScopedId(FEED_ID, "BCD"))
      .setCurrencyType("EUR")
      .setPrice(BCD_PRICE)
      .setTransferDuration(fiveMinutes)
      .build();

    FareAttribute fareAttributeABCD = FareAttribute
      .of(new FeedScopedId(FEED_ID, "ABCD"))
      .setCurrencyType("EUR")
      .setPrice(ABCD_PRICE)
      .setTransferDuration(fiveMinutes)
      .build();

    // Fare rule sets
    FareRuleSet ruleSetAB = new FareRuleSet(fareAttributeAB);
    ruleSetAB.addContains("A");
    ruleSetAB.addContains("B");

    FareRuleSet ruleSetBC = new FareRuleSet(fareAttributeBC);
    ruleSetBC.addContains("B");
    ruleSetBC.addContains("C");

    FareRuleSet ruleSetCD = new FareRuleSet(fareAttributeCD);
    ruleSetCD.addContains("C");
    ruleSetCD.addContains("D");

    FareRuleSet ruleSetABC = new FareRuleSet(fareAttributeABC);
    ruleSetABC.addContains("A");
    ruleSetABC.addContains("B");
    ruleSetABC.addContains("C");

    FareRuleSet ruleSetBCD = new FareRuleSet(fareAttributeBCD);
    ruleSetBCD.addContains("B");
    ruleSetBCD.addContains("C");
    ruleSetBCD.addContains("D");

    FareRuleSet ruleSetABCD = new FareRuleSet(fareAttributeABCD);
    ruleSetABCD.addContains("A");
    ruleSetABCD.addContains("B");
    ruleSetABCD.addContains("C");
    ruleSetABCD.addContains("D");

    FareRuleSet ruleSetD = new FareRuleSet(fareAttributeD);
    ruleSetD.addContains("D");

    hslFareService.addFareRules(
      FareType.regular,
      List.of(ruleSetAB, ruleSetBC, ruleSetCD, ruleSetABC, ruleSetBCD, ruleSetABCD, ruleSetD)
    );

    // Itineraries within zone A
    Itinerary A1_A2 = newItinerary(A1, T11_06).bus(1, T11_06, T11_12, A2).build();

    args.add(
      Arguments.of(
        "Bus ride within zone A",
        hslFareService,
        A1_A2,
        List.of(fareAttributeAB.getId())
      )
    );

    // Itineraries within zone B
    Itinerary B1_B2 = newItinerary(B1, T11_06).bus(1, T11_06, T11_12, B2).build();

    args.add(
      Arguments.of(
        "Bus ride within zone B",
        hslFareService,
        B1_B2,
        List.of(fareAttributeAB.getId())
      )
    );

    // Itineraries within zone C
    Itinerary C1_C2 = newItinerary(C1, T11_06).bus(1, T11_06, T11_12, C2).build();

    args.add(
      Arguments.of(
        "Bus ride within zone C",
        hslFareService,
        C1_C2,
        List.of(fareAttributeBC.getId())
      )
    );

    // Itineraries within zone D
    Itinerary D1_D2 = newItinerary(D1, T11_06).bus(1, T11_06, T11_12, D2).build();

    args.add(
      Arguments.of("Bus ride within zone D", hslFareService, D1_D2, List.of(fareAttributeD.getId()))
    );

    // Itineraries between zones A and B
    Itinerary A1_B1 = newItinerary(A1, T11_06).bus(1, T11_06, T11_12, B1).build();

    args.add(
      Arguments.of(
        "Bus ride between zones A and B",
        hslFareService,
        A1_B1,
        List.of(fareAttributeAB.getId())
      )
    );

    // Itineraries between zones B and C
    Itinerary B1_C1 = newItinerary(B1, T11_06).bus(1, T11_06, T11_12, C1).build();

    args.add(
      Arguments.of(
        "Bus ride between zones B and C",
        hslFareService,
        B1_C1,
        List.of(fareAttributeBC.getId())
      )
    );

    // Itineraries between zones C and D
    Itinerary C1_D1 = newItinerary(C1, T11_06).bus(1, T11_06, T11_12, D1).build();

    args.add(
      Arguments.of(
        "Bus ride between zones C and D",
        hslFareService,
        C1_D1,
        List.of(fareAttributeCD.getId())
      )
    );

    // Itineraries between zones A and D
    Itinerary A1_D1 = newItinerary(A1, T11_06).bus(1, T11_20, T11_30, D1).build();

    args.add(
      Arguments.of(
        "Bus ride between zones A and D",
        hslFareService,
        A1_D1,
        List.of(fareAttributeABCD.getId())
      )
    );

    // Itineraries between zones A and C
    Itinerary A1_C1 = newItinerary(A1, T11_06).bus(1, T11_20, T11_30, C1).build();

    args.add(
      Arguments.of(
        "Bus ride between zones A and C",
        hslFareService,
        A1_C1,
        List.of(fareAttributeABC.getId())
      )
    );

    // Itineraries between zones B and D
    Itinerary B1_D1 = newItinerary(B1, T11_06).bus(1, T11_20, T11_30, D1).build();

    args.add(
      Arguments.of(
        "Bus ride between zones B and D",
        hslFareService,
        B1_D1,
        List.of(fareAttributeBCD.getId())
      )
    );

    Itinerary twoTicketsItinerary = newItinerary(A1, T11_20)
      .bus(1, T11_20, T11_30, B1)
      .bus(1, T11_33, T11_50, C1)
      .build();

    args.add(
      Arguments.of(
        "Ride that needs two tickets",
        hslFareService,
        twoTicketsItinerary,
        List.of(fareAttributeAB.getId(), fareAttributeBC.getId())
      )
    );

    return args;
  }
}
