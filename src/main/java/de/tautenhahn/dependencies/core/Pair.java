package de.tautenhahn.dependencies.core;

/**
 * Own Pair class because various existing ones suffer access restrictions.
 * 
 * @author TT
 * @param <S>
 * @param <T>
 */
public class Pair<S, T>
{

  private final S first;

  private final T second;

  /**
   * Creates immutable instance.
   * 
   * @param first
   * @param second
   */
  public Pair(S first, T second)
  {
    this.first = first;
    this.second = second;
  }

  /**
   * Returns component.
   */
  public S getFirst()
  {
    return first;
  }

  /**
   * Returns component.
   */
  public T getSecond()
  {
    return second;
  }
}
