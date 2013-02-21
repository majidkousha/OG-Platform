/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractEHCachingMaster;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.master.DocumentSearchCache;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionHistoryRequest;
import com.opengamma.master.region.RegionHistoryResult;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.ObjectsPair;

import net.sf.ehcache.CacheManager;

/**
 * A cache decorating a {@code RegionMaster}, mainly intended to reduce the frequency and repetition of queries
 * from the management UI to a {@code DbRegionMaster}. In particular, prefetching is employed in paged queries,
 * which tend to scale poorly.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingRegionMaster extends AbstractEHCachingMaster<RegionDocument> implements RegionMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingRegionMaster.class);

  /** The search cache */
  private DocumentSearchCache _documentSearchCache;
  
  /**
   * Creates an instance over an underlying source specifying the cache manager.
   *
   * @param name          the cache name, not empty
   * @param underlying    the underlying region source, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingRegionMaster(final String name, final RegionMaster underlying, final CacheManager cacheManager) {
    super(name, underlying, cacheManager);
 
    // Create the doc search cache and register a region master searcher
    _documentSearchCache = new DocumentSearchCache(name, new DocumentSearchCache.CacheSearcher() {
      @Override
      public ObjectsPair<Integer, List<UniqueId>> search(AbstractSearchRequest request) {
        // Fetch search results from underlying master
        RegionSearchResult result = ((RegionMaster) getUnderlying()).search((RegionSearchRequest) request);

        // Cache the result documents
        DocumentSearchCache.cacheDocuments(result.getDocuments(), getUidToDocumentCache());

        // Return the list of result UniqueIds
        return new ObjectsPair<>(result.getPaging().getTotalItems(),
                                 DocumentSearchCache.extractUniqueIds(result.getDocuments()));
      }
    }, cacheManager);

    // Prime search cache
    RegionSearchRequest defaultSearch = new RegionSearchRequest();
    defaultSearch.setPagingRequest(PagingRequest.FIRST_PAGE);
    _documentSearchCache.backgroundPrefetch(defaultSearch);
  }

  @Override
  public RegionSearchResult search(RegionSearchRequest request) {
    // Ensure that the relevant prefetch range is cached, otherwise fetch and cache any missing sub-ranges in background
    _documentSearchCache.backgroundPrefetch(request);

    // Fetch the paged request range; if not entirely cached then fetch and cache it in foreground
    ObjectsPair<Integer, List<UniqueId>> pair = _documentSearchCache.doSearch(request, false); // don't block until cached

    List<RegionDocument> documents = new ArrayList<>();
    for (UniqueId uniqueId : pair.getSecond()) {
      documents.add(get(uniqueId));
    }

    RegionSearchResult result = new RegionSearchResult(documents);
    result.setPaging(Paging.of(request.getPagingRequest(), pair.getFirst()));
    return result;
  }

  @Override
  public RegionHistoryResult history(RegionHistoryRequest request) {
    return ((RegionMaster) getUnderlying()).history(request); //TODO
  }

}
