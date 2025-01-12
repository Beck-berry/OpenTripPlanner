package org.opentripplanner.routing.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opentripplanner.routing.api.request.RouteRequest;
import org.opentripplanner.routing.api.request.StreetMode;

public class StateEditorTest {

  @Test
  public final void testIncrementTimeInSeconds() {
    StateEditor stateEditor = new StateEditor(new RouteRequest(), StreetMode.WALK, null);

    stateEditor.setTimeSeconds(0);
    stateEditor.incrementTimeInSeconds(999999999);

    assertEquals(999999999, stateEditor.child.getTimeSeconds());
  }

  @Test
  public final void testWeightIncrement() {
    StateEditor stateEditor = new StateEditor(new RouteRequest(), StreetMode.WALK, null);

    stateEditor.setTimeSeconds(0);
    stateEditor.incrementWeight(10);

    assertNotNull(stateEditor.makeState());
  }

  @Test
  public final void testNanWeightIncrement() {
    StateEditor stateEditor = new StateEditor(new RouteRequest(), StreetMode.WALK, null);

    stateEditor.setTimeSeconds(0);
    stateEditor.incrementWeight(Double.NaN);

    assertNull(stateEditor.makeState());
  }

  @Test
  public final void testInfinityWeightIncrement() {
    StateEditor stateEditor = new StateEditor(new RouteRequest(), StreetMode.WALK, null);

    stateEditor.setTimeSeconds(0);
    stateEditor.incrementWeight(Double.NEGATIVE_INFINITY);

    assertNull(stateEditor.makeState(), "Infinity weight increment");
  }
}
