package org.opentripplanner.routing.algorithm.raptoradapter.transit.cost;

import java.util.BitSet;
import java.util.function.DoubleFunction;
import org.opentripplanner.routing.api.request.preference.AccessibilityPreferences;

/**
 * Mutable version of the {@link GeneralizedCostParameters}.
 */
@SuppressWarnings("UnusedReturnValue")
public class GeneralizedCostParametersBuilder {

  private int boardCost;
  private int transferCost;
  private double[] transitReluctanceFactors;
  private double waitReluctanceFactor;
  private boolean wheelchairEnabled;
  private AccessibilityPreferences wheelchairAccessibility;
  private BitSet unpreferredPatterns;
  private DoubleFunction<Double> unpreferredCost;

  public GeneralizedCostParametersBuilder() {
    this(GeneralizedCostParameters.DEFAULTS);
  }

  private GeneralizedCostParametersBuilder(GeneralizedCostParameters other) {
    this.boardCost = other.boardCost();
    this.transferCost = other.transferCost();
    this.transitReluctanceFactors = other.transitReluctanceFactors();
    this.waitReluctanceFactor = other.waitReluctanceFactor();
    this.wheelchairEnabled = other.wheelchairEnabled();
    this.wheelchairAccessibility = other.wheelchairAccessibility();
    this.unpreferredPatterns = other.unpreferredPatterns();
  }

  public int boardCost() {
    return boardCost;
  }

  public GeneralizedCostParametersBuilder boardCost(int boardCost) {
    this.boardCost = boardCost;
    return this;
  }

  public int transferCost() {
    return transferCost;
  }

  public GeneralizedCostParametersBuilder transferCost(int transferCost) {
    this.transferCost = transferCost;
    return this;
  }

  public double[] transitReluctanceFactors() {
    return transitReluctanceFactors;
  }

  public GeneralizedCostParametersBuilder transitReluctanceFactors(
    double[] transitReluctanceFactors
  ) {
    this.transitReluctanceFactors = transitReluctanceFactors;
    return this;
  }

  public double waitReluctanceFactor() {
    return waitReluctanceFactor;
  }

  public GeneralizedCostParametersBuilder waitReluctanceFactor(double waitReluctanceFactor) {
    this.waitReluctanceFactor = waitReluctanceFactor;
    return this;
  }

  public boolean wheelchairEnabled() {
    return wheelchairEnabled;
  }

  public AccessibilityPreferences wheelchairAccessibility() {
    return wheelchairAccessibility;
  }

  public GeneralizedCostParametersBuilder wheelchairEnabled(boolean wheelchairEnabled) {
    this.wheelchairEnabled = wheelchairEnabled;
    return this;
  }

  public GeneralizedCostParametersBuilder wheelchairAccessibility(
    AccessibilityPreferences wheelchairAccessibility
  ) {
    this.wheelchairAccessibility = wheelchairAccessibility;
    return this;
  }

  public BitSet unpreferredPatterns() {
    return unpreferredPatterns;
  }

  public GeneralizedCostParametersBuilder unpreferredPatterns(BitSet patterns) {
    this.unpreferredPatterns = patterns;
    return this;
  }

  public DoubleFunction<Double> unpreferredCost() {
    return unpreferredCost;
  }

  public GeneralizedCostParametersBuilder unpreferredCost(DoubleFunction<Double> unpreferredCost) {
    this.unpreferredCost = unpreferredCost;
    return this;
  }

  public GeneralizedCostParameters build() {
    return new GeneralizedCostParameters(this);
  }
}
