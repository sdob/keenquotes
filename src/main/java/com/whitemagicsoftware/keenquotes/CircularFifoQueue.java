/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.whitemagicsoftware.keenquotes;

import java.util.*;

import static java.lang.String.*;

/**
 * This is a first-in first-out queue with a fixed size that replaces its
 * oldest element if full.
 * <p>
 * The removal order of a {@link CircularFifoQueue} is based on the
 * insertion order; elements are removed in the same order in which they
 * were added. The iteration order is the same as the removal order.
 * </p>
 * <p>
 * The {@link #add(Object)}, {@link #remove()}, {@link #peek()},
 * {@link #poll()}, {@link #offer(Object)} operations all perform in constant
 * time. All other operations perform in linear time or worse.
 * </p>
 * <p>
 * This queue prevents {@code null} objects from being added.
 * </p>
 *
 * @param <E> the type of elements in this collection
 * @since 4.0
 */
public final class CircularFifoQueue<E> extends AbstractCollection<E>
  implements Queue<E> {
  /**
   * Underlying storage array.
   */
  private final transient E[] elements;

  /**
   * Array index of first (oldest) queue element.
   */
  private transient int start;

  /**
   * Index mod maxElements of the array position following the last queue
   * element.  Queue elements start at elements[start] and "wrap around"
   * elements[maxElements-1], ending at elements[decrement(end)].
   * For example, elements = {c,a,b}, start=1, end=1 corresponds to
   * the queue [a,b,c].
   */
  private transient int end;

  /**
   * Flag to indicate if the queue is currently full.
   */
  private transient boolean full;

  /**
   * Capacity of the queue.
   */
  private final int maxElements;

  /**
   * Constructor that creates a queue with the specified size.
   *
   * @param size Immutable queue size, must be greater than zero.
   */
  @SuppressWarnings( "unchecked" )
  public CircularFifoQueue( final int size ) {
    assert size > 0;

    elements = (E[]) new Object[ size ];
    maxElements = elements.length;
  }

  /**
   * Returns the number of elements stored in the queue.
   *
   * @return this queue's size
   */
  @Override
  public int size() {
    int size;

    if( end < start ) {
      size = maxElements - start + end;
    }
    else if( end == start ) {
      size = full ? maxElements : 0;
    }
    else {
      size = end - start;
    }

    return size;
  }

  /**
   * Returns true if this queue is empty; false otherwise.
   *
   * @return true if this queue is empty
   */
  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns {@code true} if the capacity limit of this queue has been reached,
   * i.e. the number of elements stored in the queue equals its maximum size.
   *
   * @return {@code true} if the capacity limit has been reached, {@code
   * false} otherwise
   * @since 4.1
   */
  public boolean isAtFullCapacity() {
    return size() == maxElements;
  }

  /**
   * Clears this queue.
   */
  @Override
  public void clear() {
    full = false;
    start = 0;
    end = 0;
    Arrays.fill( elements, null );
  }

  /**
   * Adds the given element to this queue. If the queue is full, the least
   * recently added element is discarded so that a new element can be inserted.
   *
   * @param element the element to add
   * @return true, always
   * @throws NullPointerException if the given element is null
   */
  @Override
  public boolean add( final E element ) {
    Objects.requireNonNull( element, "element" );

    if( isAtFullCapacity() ) {
      remove();
    }

    elements[ end++ ] = element;

    if( end >= maxElements ) {
      end = 0;
    }

    if( end == start ) {
      full = true;
    }

    return true;
  }

  /**
   * Returns the element at the specified position in this queue.
   *
   * @param index the position of the element in the queue, zero-based.
   * @return The element at position {@code index}.
   * @throws NoSuchElementException if the requested position is outside the
   *                                range [0, size).
   */
  public E get( final int index ) {
    invariant( index );

    return elements[ (start + index) % maxElements ];
  }

  public void set( final E element, final int index ) {
    Objects.requireNonNull( element, "element" );

    invariant( index );

    elements[ (start + index) % maxElements ] = element;
  }

  private void invariant( final int index ) {
    final int sz = size();

    if( index < 0 || index >= sz ) {
      throw new NoSuchElementException( format(
        "Index %1$d is outside the available range [0, %2$d)", index, sz
      ) );
    }
  }

  /**
   * Adds the given element to this queue. If the queue is full, the least
   * recently added element is discarded so that a new element can be inserted.
   *
   * @param element The element to add.
   * @return {@code true}, always
   * @throws NullPointerException if the given element is {@code null}.
   */
  @Override
  public boolean offer( final E element ) {
    return add( element );
  }

  @Override
  public E poll() {
    return isEmpty() ? null : remove();
  }

  @Override
  public E element() {
    if( isEmpty() ) {
      throw new NoSuchElementException( "empty queue" );
    }

    return peek();
  }

  @Override
  public E peek() {
    return isEmpty() ? null : elements[ start ];
  }

  @Override
  public E remove() {
    if( isEmpty() ) {
      throw new NoSuchElementException( "empty queue" );
    }

    final E element = elements[ start ];

    if( null != element ) {
      elements[ start++ ] = null;

      if( start >= maxElements ) {
        start = 0;
      }
      full = false;
    }

    return element;
  }

  /**
   * Increments the internal index.
   *
   * @param index the index to increment
   * @return the updated index
   */
  private int increment( int index ) {
    if( ++index >= maxElements ) {
      index = 0;
    }

    return index;
  }

  /**
   * Decrements the internal index.
   *
   * @param index the index to decrement
   * @return the updated index
   */
  private int decrement( int index ) {
    if( --index < 0 ) {
      index = maxElements - 1;
    }

    return index;
  }

  /**
   * Returns an iterator over this queue's elements.
   *
   * @return an iterator over this queue's elements
   */
  @Override
  public Iterator<E> iterator() {
    return new Iterator<>() {
      private int index = start;
      private int lastReturnedIndex = -1;
      private boolean isFirst = full;

      @Override
      public boolean hasNext() {
        return isFirst || index != end;
      }

      @Override
      public E next() {
        if( !hasNext() ) {
          throw new NoSuchElementException();
        }

        isFirst = false;
        lastReturnedIndex = index;
        index = increment( index );
        return elements[ lastReturnedIndex ];
      }

      @Override
      public void remove() {
        if( lastReturnedIndex == -1 ) {
          throw new IllegalStateException();
        }

        // First element can be removed quickly
        if( lastReturnedIndex == start ) {
          CircularFifoQueue.this.remove();
          lastReturnedIndex = -1;
          return;
        }

        int pos = lastReturnedIndex + 1;
        if( start < lastReturnedIndex && pos < end ) {
          // shift in one part
          System.arraycopy(
            elements, pos, elements, lastReturnedIndex, end - pos );
        }
        else {
          // Other elements require us to shift the subsequent elements
          while( pos != end ) {
            if( pos >= maxElements ) {
              elements[ pos - 1 ] = elements[ 0 ];
              pos = 0;
            }
            else {
              elements[ decrement( pos ) ] = elements[ pos ];
              pos = increment( pos );
            }
          }
        }

        lastReturnedIndex = -1;
        end = decrement( end );
        elements[ end ] = null;
        full = false;
        index = decrement( index );
      }
    };
  }
}
