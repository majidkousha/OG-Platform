/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import static com.opengamma.master.InstantExtractor.MAX_INSTANT;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.SearchAttribute;
import net.sf.ehcache.config.Searchable;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;

/**
 * A cache decorating a master, mainly intended to reduce the frequency and repetition of queries to the underlying
 * master.
 * <p>
 * The cache is implemented using {@code EHCache}.
 *
 * TODO Check whether misses are cached by SelfPopulatingCache
 * TODO think about removing redundant cleanCache calls
 *
 *
 * @param <D> the document type returned by the master
 */
public abstract class AbstractEHCachingMaster<D extends AbstractDocument> implements AbstractChangeProvidingMaster<D> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractEHCachingMaster.class);
  /** The underlying master. */
  private final AbstractChangeProvidingMaster<D> _underlying;
  /** The cache manager. */
  private final CacheManager _cacheManager;
  /** Listens for changes in the underlying security source. */
  private final ChangeListener _changeListener;
  /** The local change manager. */
  private final ChangeManager _changeManager;
  /** The document cache indexed by UniqueId. */
  private final Ehcache _uidToDocumentCache;

  /**
   * Creates an instance over an underlying source specifying the cache manager.
   *
   * @param name          the cache name, not empty
   * @param underlying    the underlying source, not null
   * @param cacheManager  the cache manager, not null
   */
  public AbstractEHCachingMaster(final String name, final AbstractChangeProvidingMaster<D> underlying, final CacheManager cacheManager) {
    ArgumentChecker.notEmpty(name, "name");
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");

    _underlying = underlying;
    _cacheManager = cacheManager;

    CacheConfiguration cacheConfiguration = new CacheConfiguration(name + "-uidToDocumentCache", 1000).eternal(true);
    Searchable uidToDocumentCacheSearchable = new Searchable();
    uidToDocumentCacheSearchable.addSearchAttribute(new SearchAttribute().name("ObjectId")
        .expression("value.getObjectId().toString()"));
    uidToDocumentCacheSearchable.addSearchAttribute(new SearchAttribute().name("VersionFromInstant")
        .className("com.opengamma.master.InstantExtractor"));
    uidToDocumentCacheSearchable.addSearchAttribute(new SearchAttribute().name("VersionToInstant")
        .className("com.opengamma.master.InstantExtractor"));
    uidToDocumentCacheSearchable.addSearchAttribute(new SearchAttribute().name("CorrectionFromInstant")
        .className("com.opengamma.master.InstantExtractor"));
    uidToDocumentCacheSearchable.addSearchAttribute(new SearchAttribute().name("CorrectionToInstant")
        .className("com.opengamma.master.InstantExtractor"));
    cacheConfiguration.addSearchable(uidToDocumentCacheSearchable);

    _cacheManager.addCache(new Cache(cacheConfiguration));
    _uidToDocumentCache = new SelfPopulatingCache(_cacheManager.getCache(name + "-uidToDocumentCache"),
                                                  new UidToDocumentCacheEntryFactory<>(_underlying));
    _cacheManager.replaceCacheWithDecoratedCache(_cacheManager.getCache(name + "-uidToDocumentCache"), _uidToDocumentCache);
    //_uidToDocumentCache.registerCacheWriter(new UidToDocumentCacheWriterFactory().createCacheWriter(_uidToDocumentCache, null));

    // Listen to change events from underlying, clean this cache accordingly and relay events to our change listeners
    _changeManager = new BasicChangeManager();
    _changeListener = new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        final ObjectId oid = event.getObjectId();
        final Instant versionFrom = event.getVersionFrom();
        final Instant versionTo = event.getVersionTo();
        cleanCaches(oid, versionFrom, versionTo);
        _changeManager.entityChanged(event.getType(), event.getObjectId(),
            event.getVersionFrom(), event.getVersionTo(), event.getVersionInstant());
      }
    };
    underlying.changeManager().addChangeListener(_changeListener);
  }

  //-------------------------------------------------------------------------

  @Override
  public D get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");

    // Search through attributes for specified oid, versions/corrections
    Results results = _uidToDocumentCache.createQuery()
        .includeKeys().includeValues()
        .includeAttribute(_uidToDocumentCache.getSearchAttribute("ObjectId"))
        .includeAttribute(_uidToDocumentCache.getSearchAttribute("VersionFromInstant"))
        .includeAttribute(_uidToDocumentCache.getSearchAttribute("VersionToInstant"))
        .includeAttribute(_uidToDocumentCache.getSearchAttribute("CorrectionFromInstant"))
        .includeAttribute(_uidToDocumentCache.getSearchAttribute("CorrectionToInstant"))
        .addCriteria(_uidToDocumentCache.getSearchAttribute("ObjectId").eq(objectId.toString()))
        .addCriteria(_uidToDocumentCache.getSearchAttribute("VersionFromInstant")
                       .le(versionCorrection.withLatestFixed(InstantExtractor.MAX_INSTANT).getVersionAsOf().toString()))
        .addCriteria(_uidToDocumentCache.getSearchAttribute("VersionToInstant")
                       .ge(versionCorrection.withLatestFixed(InstantExtractor.MAX_INSTANT).getVersionAsOf().toString()))
        .addCriteria(_uidToDocumentCache.getSearchAttribute("CorrectionFromInstant")
                       .le(versionCorrection.withLatestFixed(InstantExtractor.MAX_INSTANT).getCorrectedTo().toString()))
        .addCriteria(_uidToDocumentCache.getSearchAttribute("CorrectionToInstant")
                       .ge(versionCorrection.withLatestFixed(InstantExtractor.MAX_INSTANT).getCorrectedTo().toString()))
        .execute();

    // Found a matching cached document
    if (results.size() == 1 && results.all().get(0).getValue() != null) {
      // Return cached value
      return (D) results.all().get(0).getValue();

    // No cached document found, fetch from underlying by oid/vc instead
    // Note: no self-populating by oid/vc, and no caching of misses by oid/vc
    } else if (results.size() == 0) {
      // Get from underlying by oid/vc, throwing exception if not there
      D result = _underlying.get(objectId, versionCorrection);

      // Explicitly insert in cache
      getUidToDocumentCache().put(new Element(result.getUniqueId().toString(), result));

      return result;

    // Invalid result
    } else {
      throw new DataNotFoundException("Unable to uniquely identify a document with the specified ObjectId/VersionCorrection");
    }
  }

  @Override
  public D get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    // Get from cache, which in turn self-populates from the underlying master
    Element element;
    try {
      element = _uidToDocumentCache.get(uniqueId);
    } catch (CacheException e) {
      throw new DataNotFoundException(e.getMessage());
    }

    if (element != null && element.getObjectValue() != null) {
      return (D) element.getObjectValue();
    } else {
      throw new DataNotFoundException("No document found with the specified UniqueId");
    }
  }

  @Override
  public Map<UniqueId, D> get(Collection<UniqueId> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "uniqueIds");

    Map<UniqueId, D> result = new HashMap<>();
    for (UniqueId uniqueId : uniqueIds) {
      try {
        D object = get(uniqueId);
        result.put(uniqueId, object);
      } catch (DataNotFoundException ex) {
        // do nothing
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------

  @Override
  public D add(D document) {
    ArgumentChecker.notNull(document, "document");

    // Add document to underlying master
    D result = getUnderlying().add(document);

    // Store document in UniqueId cache
    getUidToDocumentCache().put(new Element(result.getUniqueId(), result));

    return result;
  }

  @Override
  public D update(D document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getObjectId(), "document.objectId");

    // Flush previous latest version (and all its corrections) from cache
    cleanCaches(document.getObjectId(), Instant.now(), MAX_INSTANT);

    // Update document in underlying master
    D result = getUnderlying().update(document);

    // Store document in UniqueId cache
    getUidToDocumentCache().put(new Element(result.getUniqueId(), result));

    return result;
  }

  @Override
  public void remove(ObjectIdentifiable objectId) {
    ArgumentChecker.notNull(objectId, "objectId");

    // Remove document from underlying master
    getUnderlying().remove(objectId);

    // Adjust version/correction validity of latest version in Oid cache
    // Note: cleanCaches is already triggered by underlying master, so this is probably redundant
    cleanCaches(objectId.getObjectId(), Instant.now(), null);
  }

  @Override
  public D correct(D document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");

    // Flush the previous latest correction from cache
    getUidToDocumentCache().remove(document.getUniqueId());

    // Correct document in underlying master
    D result = getUnderlying().correct(document);

    // Store latest correction in UniqueId cache
    getUidToDocumentCache().put(new Element(result.getUniqueId(), result));

    return result;
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<D> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");

    // Flush the original version from cache
    getUidToDocumentCache().remove(uniqueId);

    // Replace version in underlying master
    List<UniqueId> results = getUnderlying().replaceVersion(uniqueId, replacementDocuments);

    // Don't cache replacementDocuments, whose version, correction instants may have been altered by underlying master

    return results;
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<D> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");

    // Flush all existing versions from cache
    cleanCaches(objectId.getObjectId(), null, null);

    // Replace all versions in underlying master
    List<UniqueId> results = getUnderlying().replaceAllVersions(objectId, replacementDocuments);

    // Don't cache replacementDocuments, whose version, correction instants may have been altered by underlying master

    return results;
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<D> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");

    // Flush all existing versions from cache
    cleanCaches(objectId.getObjectId(), null, null);

    // Replace versions in underlying master
    List<UniqueId> results = getUnderlying().replaceVersions(objectId, replacementDocuments);

    // Don't cache replacementDocuments, whose version, correction instants may have been altered by underlying master

    return results;
  }

  @Override
  public UniqueId replaceVersion(D replacementDocument) {
    ArgumentChecker.notNull(replacementDocument, "replacementDocument");

    final List<UniqueId> result =
        replaceVersion(replacementDocument.getUniqueId(), Collections.singletonList(replacementDocument));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  @Override
  public void removeVersion(UniqueId uniqueId) {
    replaceVersion(uniqueId, Collections.<D>emptyList());
  }

  @Override
  public UniqueId addVersion(ObjectIdentifiable objectId, D documentToAdd) {
    final List<UniqueId> result = replaceVersions(objectId, Collections.singletonList(documentToAdd));
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  //-------------------------------------------------------------------------

  private void cleanCaches(ObjectId objectId, Instant fromVersion, Instant toVersion) {

    Results results = _uidToDocumentCache.createQuery().includeKeys()
        .includeAttribute(_uidToDocumentCache.getSearchAttribute("ObjectId"))
        .includeAttribute(_uidToDocumentCache.getSearchAttribute("VersionFromInstant"))
        .includeAttribute(_uidToDocumentCache.getSearchAttribute("VersionToInstant"))
        .addCriteria(_uidToDocumentCache.getSearchAttribute("ObjectId")
                         .eq(objectId.toString()))
        .addCriteria(_uidToDocumentCache.getSearchAttribute("VersionFromInstant")
                         .le((fromVersion != null ? fromVersion : InstantExtractor.MIN_INSTANT).toString()))
        .addCriteria(_uidToDocumentCache.getSearchAttribute("VersionToInstant")
                         .ge((toVersion != null ? toVersion : InstantExtractor.MAX_INSTANT).toString()))
        .execute();

    for (Result result : results.all()) {
      getUidToDocumentCache().remove(result.getKey());
    }
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache.
   * It should not be part of a generic lifecycle method.
   */
  public void shutdown() {
    getUnderlying().changeManager().removeChangeListener(_changeListener);
    getCacheManager().removeCache(_uidToDocumentCache.getName());
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the underlying source of items.
   *
   * @return the underlying source of items, not null
   */
  protected AbstractChangeProvidingMaster<D> getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache manager.
   *
   * @return the cache manager, not null
   */
  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Gets the document by UniqueId cache.
   *
   * @return the cache, not null
   */
  protected Ehcache getUidToDocumentCache() {
    return _uidToDocumentCache;
  }

  /**
   * Gets the change manager.
   *
   * @return the change manager, not null
   */
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  //-------------------------------------------------------------------------

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUnderlying() + "]";
  }

}
