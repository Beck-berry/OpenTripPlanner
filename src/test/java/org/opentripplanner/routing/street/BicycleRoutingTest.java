package org.opentripplanner.routing.street;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opentripplanner.test.support.PolylineAssert.assertThatPolylinesAreEqual;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.opentripplanner.ConstantsForTests;
import org.opentripplanner.TestOtpModel;
import org.opentripplanner.model.GenericLocation;
import org.opentripplanner.model.plan.StreetLeg;
import org.opentripplanner.routing.algorithm.mapping.GraphPathToItineraryMapper;
import org.opentripplanner.routing.api.request.RouteRequest;
import org.opentripplanner.routing.api.request.StreetMode;
import org.opentripplanner.routing.core.BicycleOptimizeType;
import org.opentripplanner.routing.core.TemporaryVerticesContainer;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.GraphPathFinder;
import org.opentripplanner.util.PolylineEncoder;

public class BicycleRoutingTest {

  static final Instant dateTime = Instant.now();
  private final Graph herrenbergGraph;

  {
    TestOtpModel model = ConstantsForTests.buildOsmGraph(ConstantsForTests.HERRENBERG_OSM);
    herrenbergGraph = model.graph();

    model.transitModel().index();
    herrenbergGraph.index(model.transitModel().getStopModel());
  }

  /**
   * https://www.openstreetmap.org/way/22392895 is access=destination which means that both bicycles
   * and motor vehicles must not pass through.
   */
  @Test
  public void shouldRespectGeneralNoThroughTraffic() {
    var mozartStr = new GenericLocation(48.59713, 8.86107);
    var fritzLeharStr = new GenericLocation(48.59696, 8.85806);

    var polyline1 = computePolyline(herrenbergGraph, mozartStr, fritzLeharStr);
    assertThatPolylinesAreEqual(polyline1, "_srgHutau@h@B|@Jf@BdAG?\\JT@jA?DSp@_@fFsAT{@DBpC");

    var polyline2 = computePolyline(herrenbergGraph, fritzLeharStr, mozartStr);
    assertThatPolylinesAreEqual(polyline2, "{qrgH{aau@CqCz@ErAU^gFRq@?EAkAKUeACg@A_AM_AEDQF@H?");
  }

  /**
   * Tests that https://www.openstreetmap.org/way/35097400 is allowed for cars due to
   * motor_vehicle=destination being meant for cars only.
   */
  @Test
  public void shouldNotRespectMotorCarNoThru() {
    var schiessmauer = new GenericLocation(48.59737, 8.86350);
    var zeppelinStr = new GenericLocation(48.59972, 8.86239);

    var polyline1 = computePolyline(herrenbergGraph, schiessmauer, zeppelinStr);
    assertThatPolylinesAreEqual(polyline1, "otrgH{cbu@S_AU_AmAdAyApAGDs@h@_@\\_ClBe@^?S");

    var polyline2 = computePolyline(herrenbergGraph, zeppelinStr, schiessmauer);
    assertThatPolylinesAreEqual(polyline2, "ccsgH{|au@?Rd@_@~BmB^]r@i@FExAqAlAeAT~@R~@");
  }

  private static String computePolyline(Graph graph, GenericLocation from, GenericLocation to) {
    RouteRequest request = new RouteRequest();
    request.setDateTime(dateTime);
    request.setFrom(from);
    request.setTo(to);
    request.preferences().withBike(it -> it.setOptimizeType(BicycleOptimizeType.QUICK));

    request.journey().direct().setMode(StreetMode.BIKE);
    var temporaryVertices = new TemporaryVerticesContainer(
      graph,
      request,
      request.journey().direct().mode(),
      request.journey().direct().mode()
    );
    var gpf = new GraphPathFinder(null, Duration.ofSeconds(5));
    var paths = gpf.graphPathFinderEntryPoint(request, temporaryVertices);

    GraphPathToItineraryMapper graphPathToItineraryMapper = new GraphPathToItineraryMapper(
      ZoneId.of("Europe/Berlin"),
      graph.streetNotesService,
      graph.ellipsoidToGeoidDifference
    );

    var itineraries = graphPathToItineraryMapper.mapItineraries(paths);
    temporaryVertices.close();

    // make sure that we only get BICYCLE legs
    itineraries.forEach(i ->
      i
        .getLegs()
        .forEach(l -> {
          if (l instanceof StreetLeg stLeg) {
            assertEquals(TraverseMode.BICYCLE, stLeg.getMode());
          } else {
            fail("Expected StreetLeg (BICYCLE): " + l);
          }
        })
    );
    Geometry legGeometry = itineraries.get(0).getLegs().get(0).getLegGeometry();
    return PolylineEncoder.encodeGeometry(legGeometry).points();
  }
}
