package org.opentripplanner.routing.api.request.preference;

import static org.opentripplanner.util.lang.DoubleUtils.doubleEquals;

import java.io.Serializable;
import java.util.Objects;
import org.opentripplanner.util.lang.DoubleUtils;
import org.opentripplanner.util.lang.ToStringBuilder;

/**
 * The walk preferences contain all speed, reluctance, cost and factor preferences for walking
 * related to street and transit routing. The values are normalized(rounded) so the class
 * can used as a cache key.
 * <p>
 * THIS CLASS IS IMMUTABLE AND THREAD SAFE.
 */
public class WalkPreferences implements Serializable {

  public static final WalkPreferences DEFAULT = new WalkPreferences();

  private final double speed;
  private final double reluctance;
  private final int boardCost;
  private final double stairsReluctance;
  private final double stairsTimeFactor;
  private final double safetyFactor;

  private WalkPreferences() {
    this.speed = 1.33;
    this.reluctance = 2.0;
    this.boardCost = 60 * 10;
    this.stairsReluctance = 2.0;
    this.stairsTimeFactor = 3.0;
    this.safetyFactor = 1.0;
  }

  private WalkPreferences(Builder builder) {
    this.speed = DoubleUtils.roundTo2Decimals(builder.speed);
    this.reluctance = DoubleUtils.roundTo2Decimals(builder.reluctance);
    this.boardCost = builder.boardCost;
    this.stairsReluctance = DoubleUtils.roundTo2Decimals(builder.stairsReluctance);
    this.stairsTimeFactor = DoubleUtils.roundTo2Decimals(builder.stairsTimeFactor);
    this.safetyFactor = DoubleUtils.roundTo2Decimals(builder.safetyFactor);
  }

  public static Builder of() {
    return new Builder(DEFAULT);
  }

  public Builder copyOf() {
    return new Builder(this);
  }

  /**
   * Human walk speed along streets, in meters per second.
   * <p>
   * Default: 1.33 m/s ~ 3mph, <a href="http://en.wikipedia.org/wiki/Walking">avg. human walk
   * speed</a>
   */
  public double speed() {
    return speed;
  }

  /**
   * A multiplier for how bad walking is, compared to being in transit for equal
   * lengths of time. Empirically, values between 2 and 4 seem to correspond
   * well to the concept of not wanting to walk too much without asking for
   * totally ridiculous itineraries, but this observation should in no way be
   * taken as scientific or definitive. Your mileage may vary. See
   * https://github.com/opentripplanner/OpenTripPlanner/issues/4090 for impact on
   * performance with high values. Default value: 2.0
   */
  public double reluctance() {
    return reluctance;
  }

  /**
   * This prevents unnecessary transfers by adding a cost for boarding a vehicle. This is in
   * addition to the cost of the transfer(walking) and waiting-time. It is also in addition to the
   * {@link TransferPreferences#cost()}.
   */
  public int boardCost() {
    return boardCost;
  }

  /** Used instead of walk reluctance for stairs */
  public double stairsReluctance() {
    return stairsReluctance;
  }

  /**
   * How much more time does it take to walk a flight of stairs compared to walking a similar
   * horizontal length
   * <p>
   * Default value is based on: Fujiyama, T., & Tyler, N. (2010). Predicting the walking speed of
   * pedestrians on stairs. Transportation Planning and Technology, 33(2), 177–202.
   */
  public double stairsTimeFactor() {
    return stairsTimeFactor;
  }

  /**
   * Factor for how much the walk safety is considered in routing. Value should be between 0 and 1.
   * If the value is set to be 0, safety is ignored.
   */
  public double safetyFactor() {
    return safetyFactor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WalkPreferences that = (WalkPreferences) o;
    return (
      doubleEquals(that.speed, speed) &&
      doubleEquals(that.reluctance, reluctance) &&
      boardCost == that.boardCost &&
      doubleEquals(that.stairsReluctance, stairsReluctance) &&
      doubleEquals(that.stairsTimeFactor, stairsTimeFactor) &&
      doubleEquals(that.safetyFactor, safetyFactor)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(
      speed,
      reluctance,
      boardCost,
      stairsReluctance,
      stairsTimeFactor,
      safetyFactor
    );
  }

  @Override
  public String toString() {
    return ToStringBuilder
      .of(WalkPreferences.class)
      .addNum("speed", speed, DEFAULT.speed)
      .addNum("reluctance", reluctance, DEFAULT.reluctance)
      .addNum("boardCost", boardCost, DEFAULT.boardCost)
      .addNum("stairsReluctance", stairsReluctance, DEFAULT.stairsReluctance)
      .addNum("stairsTimeFactor", stairsTimeFactor, DEFAULT.stairsTimeFactor)
      .addNum("safetyFactor", safetyFactor, DEFAULT.safetyFactor)
      .toString();
  }

  public static class Builder {

    private final WalkPreferences original;
    private double speed;
    private double reluctance;
    private int boardCost;
    private double stairsReluctance;
    private double stairsTimeFactor;
    private double safetyFactor;

    public Builder(WalkPreferences original) {
      this.original = original;
      this.speed = original.speed;
      this.reluctance = original.reluctance;
      this.boardCost = original.boardCost;
      this.stairsReluctance = original.stairsReluctance;
      this.stairsTimeFactor = original.stairsTimeFactor;
      this.safetyFactor = original.safetyFactor;
    }

    public double speed() {
      return speed;
    }

    public Builder setSpeed(double speed) {
      this.speed = speed;
      return this;
    }

    public double reluctance() {
      return reluctance;
    }

    public Builder setReluctance(double reluctance) {
      this.reluctance = reluctance;
      return this;
    }

    public int boardCost() {
      return boardCost;
    }

    public Builder setBoardCost(int boardCost) {
      this.boardCost = boardCost;
      return this;
    }

    public double stairsReluctance() {
      return stairsReluctance;
    }

    public Builder setStairsReluctance(double stairsReluctance) {
      this.stairsReluctance = stairsReluctance;
      return this;
    }

    public double stairsTimeFactor() {
      return stairsTimeFactor;
    }

    public Builder setStairsTimeFactor(double stairsTimeFactor) {
      this.stairsTimeFactor = stairsTimeFactor;
      return this;
    }

    public double safetyFactor() {
      return safetyFactor;
    }

    public Builder setSafetyFactor(double safetyFactor) {
      if (safetyFactor < 0) {
        this.safetyFactor = 0;
      } else if (safetyFactor > 1) {
        this.safetyFactor = 1;
      } else {
        this.safetyFactor = safetyFactor;
      }
      return this;
    }

    public WalkPreferences build() {
      var newObj = new WalkPreferences(this);
      return original.equals(newObj) ? original : newObj;
    }
  }
}
