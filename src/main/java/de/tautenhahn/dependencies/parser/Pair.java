package de.tautenhahn.dependencies.parser;

import java.util.Objects;


/**
 * Own Pair class because various existing ones suffer access restrictions.
 *
 * @author TT
 * @param <S> element type
 * @param <T> element type
 */
public class Pair<S, T>
{

  private final S first;

  private final T second;

  /**
   * Creates immutable instance.
   *
   * @param first wrapped value
   * @param second wrapped value
   */
  public Pair(S first, T second)
  {
    this.first = first;
    this.second = second;
  }

  /**
   * @return first value
   */
  public S getFirst()
  {
    return first;
  }

  /**
   * @return second value
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
    if (obj == null || getClass() != obj.getClass())
    {
      return false;
    }
    @SuppressWarnings("rawtypes")
    Pair other = (Pair)obj;
    return Objects.equals(first, other.first) && Objects.equals(second, other.second);
  }


}
