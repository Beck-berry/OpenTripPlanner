package org.opentripplanner.routing.algorithm.astar;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentripplanner.common.pqueue.BinHeap;
import org.opentripplanner.routing.algorithm.astar.strategies.RemainingWeightHeuristic;
import org.opentripplanner.routing.algorithm.astar.strategies.SearchTerminationStrategy;
import org.opentripplanner.routing.algorithm.astar.strategies.SkipEdgeStrategy;
import org.opentripplanner.routing.api.request.RouteRequest;
import org.opentripplanner.routing.api.request.StreetMode;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.DominanceFunction;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.opentripplanner.util.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Find the shortest path between graph vertices using A*. A basic Dijkstra search is a special case
 * of AStar where the heuristic is always zero.
 */
public class AStar {

  private static final Logger LOG = LoggerFactory.getLogger(AStar.class);

  private static final boolean verbose = LOG.isDebugEnabled();

  private final boolean arriveBy;
  private final Set<Vertex> fromVertices;
  private final Set<Vertex> toVertices;
  private final RemainingWeightHeuristic heuristic;
  private final SkipEdgeStrategy skipEdgeStrategy;
  private final SearchTerminationStrategy terminationStrategy;
  private final TraverseVisitor traverseVisitor;
  private final Duration timeout;

  private final ShortestPathTree spt;
  private final BinHeap<State> pq;
  private final List<State> targetAcceptedStates;

  private State u;
  private int nVisited;

  protected AStar(
    RemainingWeightHeuristic heuristic,
    SkipEdgeStrategy skipEdgeStrategy,
    TraverseVisitor traverseVisitor,
    boolean arriveBy,
    Set<Vertex> fromVertices,
    Set<Vertex> toVertices,
    SearchTerminationStrategy terminationStrategy,
    DominanceFunction dominanceFunction,
    Duration timeout,
    Collection<State> initialStates
  ) {
    this.heuristic = heuristic;
    this.skipEdgeStrategy = skipEdgeStrategy;
    this.traverseVisitor = traverseVisitor;
    this.fromVertices = fromVertices;
    this.toVertices = toVertices;
    this.arriveBy = arriveBy;
    this.terminationStrategy = terminationStrategy;
    this.timeout = timeout;

    this.spt = new ShortestPathTree(dominanceFunction);

    // Initialized with a reasonable size, see #4445
    this.pq = new BinHeap<>(1000);
    this.nVisited = 0;
    this.targetAcceptedStates = new ArrayList<>();

    for (State initialState : initialStates) {
      spt.add(initialState);
      pq.insert(initialState, initialState.weight);
    }
  }

  protected ShortestPathTree getShortestPathTree() {
    runSearch();

    return spt;
  }

  protected List<GraphPath> getPathsToTarget() {
    runSearch();

    return targetAcceptedStates
      .stream()
      .filter(State::isFinal)
      .map(GraphPath::new)
      .collect(Collectors.toList());
  }

  private boolean iterate() {
    // print debug info
    if (verbose) {
      double w = pq.peek_min_key();
      LOG.debug("pq min key = {}", w);
    }

    // get the lowest-weight state in the queue
    u = pq.extract_min();

    // check that this state has not been dominated
    // and mark vertex as visited
    if (!spt.visit(u)) {
      // state has been dominated since it was added to the priority queue, so it is
      // not in any optimal path. drop it on the floor and try the next one.
      return false;
    }

    if (traverseVisitor != null) {
      traverseVisitor.visitVertex(u);
    }

    nVisited += 1;

    Vertex u_vertex = u.getVertex();

    if (verbose) {
      LOG.debug("   vertex {}", u_vertex);
    }

    Collection<Edge> edges = arriveBy ? u_vertex.getIncoming() : u_vertex.getOutgoing();
    for (Edge edge : edges) {
      if (skipEdgeStrategy != null && skipEdgeStrategy.shouldSkipEdge(u, edge)) {
        continue;
      }

      // Iterate over traversal results. When an edge leads nowhere (as indicated by
      // returning NULL), the iteration is over.
      for (State v = edge.traverse(u); v != null; v = v.getNextResult()) {
        // Could be: for (State v : traverseEdge...)

        if (traverseVisitor != null) {
          traverseVisitor.visitEdge(edge);
        }

        double remaining_w = heuristic.estimateRemainingWeight(v);

        if (remaining_w < 0 || Double.isInfinite(remaining_w)) {
          continue;
        }
        double estimate = v.getWeight() + remaining_w;

        if (verbose) {
          LOG.debug("      edge {}", edge);
          LOG.debug(
            "      {} -> {}(w) + {}(heur) = {} vert = {}",
            u.getWeight(),
            v.getWeight(),
            remaining_w,
            estimate,
            v.getVertex()
          );
        }

        // spt.add returns true if the state is hopeful; enqueue state if it's hopeful
        if (spt.add(v)) {
          // report to the visitor if there is one
          if (traverseVisitor != null) {
            traverseVisitor.visitEnqueue();
          }
          pq.insert(v, estimate);
        }
      }
    }

    return true;
  }

  private void runSearch() {
    long abortTime = DateUtils.absoluteTimeout(timeout);

    /* the core of the A* algorithm */
    while (!pq.empty()) { // Until the priority queue is empty:
      /*
       * Terminate based on timeout?
       */
      if (timeout != null && System.currentTimeMillis() > abortTime) {
        LOG.warn("Search timeout. origin={} target={}", fromVertices, toVertices);
        // Rather than returning null to indicate that the search was aborted/timed out, we instead
        // set a flag in the SPT and return it anyway. This allows returning a partial list results
        // even when a timeout occurs.
        spt.setAborted();

        break;
      }

      /*
       * Get next best state and, if it hasn't already been dominated, add adjacent states to queue.
       * If it has been dominated, the iteration is over; don't bother checking for termination condition.
       *
       * Note that termination is checked after adjacent states are added. This presents the negligible inefficiency
       * that adjacent states are generated for a state which could be the last one you need to check. The advantage
       * of this is that the algorithm is always left in a restartable state, which is useful for debugging or
       * potential future variations.
       */
      if (!iterate()) {
        continue;
      }

      if (terminationStrategy != null) {
        if (terminationStrategy.shouldSearchTerminate(u)) {
          break;
        }
      }
      if (toVertices != null && toVertices.contains(u.getVertex()) && u.isFinal()) {
        targetAcceptedStates.add(u);

        // Break out of the search if we've found the requested number of paths.
        // Currently,  we can only find one path per search.
        LOG.debug("total vertices visited {}", nVisited);
        break;
      }
    }
  }
}
