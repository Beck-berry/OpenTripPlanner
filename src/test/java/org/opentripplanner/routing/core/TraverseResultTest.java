package org.opentripplanner.routing.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.opentripplanner.routing.api.request.RouteRequest;
import org.opentripplanner.routing.api.request.StreetMode;

public class TraverseResultTest {

  @Test
  public void testAddToExistingResultChain() {
    State resultChain = null;

    /* note: times are rounded to seconds toward zero */

    for (int i = 0; i < 4; i++) {
      State r = new State(
        null,
        Instant.ofEpochSecond(i * 1000),
        StateData.getInitialStateData(new RouteRequest(), StreetMode.WALK)
      );
      resultChain = r.addToExistingResultChain(resultChain);
    }

    assertEquals(3000, resultChain.getTimeSeconds(), 0.0);

    resultChain = resultChain.getNextResult();
    assertEquals(2000, resultChain.getTimeSeconds(), 0.0);

    resultChain = resultChain.getNextResult();
    assertEquals(1000, resultChain.getTimeSeconds(), 0.0);

    resultChain = resultChain.getNextResult();
    assertEquals(0000, resultChain.getTimeSeconds(), 0.0);

    resultChain = resultChain.getNextResult();
    assertNull(resultChain);
  }
}
