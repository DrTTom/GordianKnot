package de.tautenhahn.dependencies.parser;

import java.util.Objects;


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

  @Override
  public String toString()
  {
    return "(" + first + ", " + second + ")";
  }

  @Override
  public int hashCode()
  {
    return (first == null ? 0 : first.hashCode()) + 3 * (second == null ? 0 : second.hashCode());
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null || Pair.class != obj.getClass())
    {
      return false;
    }
    @SuppressWarnings("rawtypes")
    Pair other = (Pair)obj;
    return Objects.equals(first, other.first) && Objects.equals(second, other.second);
  }


}
