package org.opentripplanner.openstreetmap.model;

import java.time.ZoneId;
import org.opentripplanner.graph_builder.module.osm.WayPropertySet;
import org.opentripplanner.graph_builder.module.osm.WayPropertySetSource;

public interface OSMProvider {
  ZoneId getZoneId();

  WayPropertySetSource getWayPropertySetSource();

  WayPropertySet getWayPropertySet();
}
