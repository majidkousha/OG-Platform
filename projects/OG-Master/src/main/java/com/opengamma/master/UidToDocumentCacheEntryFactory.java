/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

public class UidToDocumentCacheEntryFactory<D extends AbstractDocument> implements CacheEntryFactory {

  /** The underlying master. */
  private final AbstractChangeProvidingMaster<D> _underlying;

  public UidToDocumentCacheEntryFactory(AbstractChangeProvidingMaster<D> underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  @Override
  public Object createEntry(Object key) throws Exception {
    return _underlying.get((UniqueId) key);
  }

}
