package org.opentosca.container.core.next.repository;

import java.util.Collection;

public interface Repository<T> {

  void add(final T entity);

  void add(final Iterable<T> items);

  void update(final T entity);

  void remove(final T entity);

  T findById(final Long id);

  Collection<T> findAll();
}
