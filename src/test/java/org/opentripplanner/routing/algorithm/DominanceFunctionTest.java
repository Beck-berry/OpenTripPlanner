package org.opentripplanner.routing.algorithm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.opentripplanner.routing.api.request.RouteRequest;
import org.opentripplanner.routing.api.request.StreetMode;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.DominanceFunction;
import org.opentripplanner.routing.vertextype.TransitStopVertex;

public class DominanceFunctionTest {

  @Test
  public void testGeneralDominanceFunction() {
    DominanceFunction minimumWeightDominanceFunction = new DominanceFunction.MinimumWeight();
    Vertex fromVertex = mock(TransitStopVertex.class);
    Vertex toVertex = mock(TransitStopVertex.class);
    RouteRequest request = new RouteRequest();

    // Test if domination works in the general case

    State stateA = new State(
      fromVertex,
      Instant.EPOCH,
      StateData.getInitialStateData(request, StreetMode.WALK)
    );
    State stateB = new State(
      toVertex,
      Instant.EPOCH,
      StateData.getInitialStateData(request, StreetMode.WALK)
    );
    stateA.weight = 1;
    stateB.weight = 2;

    assertTrue(minimumWeightDominanceFunction.betterOrEqualAndComparable(stateA, stateB));
    assertFalse(minimumWeightDominanceFunction.betterOrEqualAndComparable(stateB, stateA));
  }
  // TODO: Make unit tests for rest of dominance functionality
  // TODO: Make functional tests for concepts covered by dominance with current algorithm
  // (Specific transfers, bike rental, park and ride, turn restrictions)
}
