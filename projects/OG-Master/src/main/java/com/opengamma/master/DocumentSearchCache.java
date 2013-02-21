/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;

import org.joda.beans.JodaBeanUtils;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ExecutorServiceFactoryBean;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.ObjectsPair;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * A document cache for search results, providing common caching logic to caching masters with a document search facility.
 * <p>
 * The cache is implemented using {@code EHCache}.
 *
 * TODO investigate possibility of using SelfPopulatingCache for the search cache too
 * TODO OPTIMIZE finer grain range locking
 * TODO OPTIMIZE cache replacement policy/handling huge requests that would flush out entire content
 * TODO OPTIMIZE underlying search request coalescing
 * TODO OPTIMIZE add front cache maps to keep EHCache happy (is this still necessary?)
 *
 * @param <D> The document type to cache
 */
public class DocumentSearchCache<D extends AbstractDocument> {

  /** The number of units to prefetch on either side of the current paging request */
  private static final int PREFETCH_RADIUS = 2;
  /** The size of a prefetch unit */
  private static final int PREFETCH_GRANULARITY = 100;
  /** The maximum number of concurrent prefetch operations */
  private static final int MAX_PREFETCH_CONCURRENCY = 4;

  /**
   * The document cache indexed by search requests.
   * A cache entry contains unpaged (total) result size and a map from start indices to ranges of cached result documents
   */
  private final Cache _searchRequestCache;
  /** The document by search request cache's name. */
  private final String _searchRequestCacheName;

  /** The prefetch thread executor service */
  private final ExecutorService _executorService;

  /** The searcher provides access to master-specific operations */
  private CacheSearcher<D> _searcher;

  /** The document cache */
  private Ehcache _documentCache;

  /**
   * A cache searcher, used by the document search cache to pass search requests to an underlying master without
   * knowing its type.
   *
   * @param <D> the document type
   */
  public interface CacheSearcher<E extends AbstractDocument> {
    /** Searches an underlying master, casting search requests/results as required for a specific master
     * @param request   The search request (will be cast to a search request for a specific master)
     * @return          The search result
     */
    public AbstractSearchResult<E> search(AbstractSearchRequest request);
  }

  /**
   * Create a new document search cache.
   *
   * @param cacheManager  The cache manager to use
   * @param name          A unique name for this cache
   * @param searcher      The CacheSearcher to use for passing search requests to an underlying master
   * @param documentCache The UniqueId to Document cache where to put any loaded documents
   */
  public DocumentSearchCache(CacheManager cacheManager, String name, CacheSearcher<D> searcher, Ehcache documentCache) {
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(searcher, "searcher");
    ArgumentChecker.notNull(documentCache, "documentCache");

    _searcher = searcher;
    _searchRequestCacheName = name + "-searchRequestCache";
    _documentCache = documentCache;

    cacheManager.addCache(_searchRequestCacheName);
    _searchRequestCache = cacheManager.getCache(_searchRequestCacheName);

    // Async prefetch executor service
    ExecutorServiceFactoryBean execBean = new ExecutorServiceFactoryBean();
    execBean.setNumberOfThreads(MAX_PREFETCH_CONCURRENCY);
    execBean.setStyle(ExecutorServiceFactoryBean.Style.CACHED);
    _executorService = execBean.getObjectCreating();
  }

  /**
   * Return a clone of the supplied search request, but with its paging request nulled out
   *
   * @param request the search request
   * @return        a clone of the supplied search request, with its paging request nulled out
   */
  protected AbstractSearchRequest withoutPagingRequest(final AbstractSearchRequest request) {
    final AbstractSearchRequest newRequest = JodaBeanUtils.clone(request);
    newRequest.setPagingRequest(null);
    return newRequest;
  }

  /**
   * Calculate the range that should be prefetched for the supplied request and initiate the fetching of any uncached
   * ranges from the underlying master in the background, without blocking.
   *
   * @param request the search request
   */
  public void backgroundPrefetch(final AbstractSearchRequest originalRequest) {

    // Build larger range to prefetch
    final int start =
        (originalRequest.getPagingRequest().getFirstItem() / PREFETCH_GRANULARITY >= PREFETCH_RADIUS)
        ? ((originalRequest.getPagingRequest().getFirstItem() / PREFETCH_GRANULARITY) * PREFETCH_GRANULARITY)
              - (PREFETCH_RADIUS * PREFETCH_GRANULARITY)
        : 0;
    final int end =
        (originalRequest.getPagingRequest().getLastItem() < Integer.MAX_VALUE - (PREFETCH_RADIUS * PREFETCH_GRANULARITY))
        ? ((originalRequest.getPagingRequest().getLastItem() / PREFETCH_GRANULARITY) * PREFETCH_GRANULARITY)
              + (PREFETCH_RADIUS * PREFETCH_GRANULARITY)
        : Integer.MAX_VALUE;

    PagingRequest superPagingRequest = PagingRequest.ofIndex(start, end - start);

    // Build new search request with larger range
    final AbstractSearchRequest superRequest = withoutPagingRequest(originalRequest);
    superRequest.setPagingRequest(superPagingRequest);

    // Submit search task to background executor
    _executorService.submit(new Runnable() {
      @Override
      public void run() {
        doSearch(superRequest, true); // block until cached
      }
    });
  }

  /**
   * If result is entirely cached return it immediately; otherwise, fetch any missing ranges from the underlying
   * master in the foreground, cache and return it.
   *
   * @param request the search request
   * @return        the search result
   */
  public ObjectsPair<Integer, List<UniqueId>> doSearch(final AbstractSearchRequest request, boolean blockUntilCached) {

    // Fetch the total #documents and cached document ranges for the search request (without paging)
    final ObjectsPair<Integer, ConcurrentNavigableMap<Integer, List<UniqueId>>> info =
        getCachedRequestInfo(getSearchRequestCache(), request);
    final int totalDocuments = info.getFirst();
    final ConcurrentNavigableMap<Integer, List<UniqueId>> rangeMap = info.getSecond();

    // Fix unpaged requests and end indexes larger than the total doc count
    if (request.getPagingRequest().getPagingSize() > totalDocuments) {
      request.setPagingRequest(PagingRequest.ofIndex(request.getPagingRequest().getFirstItem(), totalDocuments));
    }

    // Ensure that the required range is cached in its entirety
    ObjectsPair<Integer, List<UniqueId>> pair = cacheSuperRange(request, rangeMap, blockUntilCached);
    final int superIndex = pair.getFirst();
    final List<UniqueId> superRange = pair.getSecond();

    // Create and return the search result
    final List<UniqueId> resultDocuments = superRange.subList(
        request.getPagingRequest().getFirstItem() - superIndex,
        Math.min(request.getPagingRequest().getLastItem()  - superIndex, superRange.size()));

    return new ObjectsPair<>(totalDocuments, resultDocuments);
  }

  /**
   * Retrieve from cache the total #documents and the cached document ranges for the supplied search request (without
   * taking into account its paging request). If an cached entry is not found for the unpaged search request, then
   * create one, populate it with the results of the supplied paged search request, and return it.
   *
   * @param cache   the search request ehcache
   * @param request the search request
   * @return        the total document count and a range map of cached documents for the supplied search request without
   *                paging
   */
  protected ObjectsPair<Integer, ConcurrentNavigableMap<Integer, List<UniqueId>>>
                      getCachedRequestInfo(final Cache cache, final AbstractSearchRequest originalRequest) {

    AbstractSearchRequest request = withoutPagingRequest(originalRequest);

    // Get cache entry for current request (or create and get a primed cache entry if not found)
    final Element element = getSearchRequestCache().get(request);
    if (element != null) {
      return (ObjectsPair<Integer, ConcurrentNavigableMap<Integer, List<UniqueId>>>) element.getObjectValue();
    } else {
      // Build a new cached map entry and pre-fill it with the results of the supplied search request
      final AbstractSearchResult<D> resultToCache = getSearcher().search(originalRequest);
      final ConcurrentNavigableMap<Integer, List<UniqueId>> rangeMapToCache = new ConcurrentSkipListMap<>();

      // Cache UniqueIds in search cache and add documents to uidToDocumentCache
      rangeMapToCache.put(resultToCache.getPaging().getFirstItem(), extractUniqueIds(resultToCache.getDocuments()));
      cacheDocuments(resultToCache.getDocuments());

      final ObjectsPair<Integer, ConcurrentNavigableMap<Integer, List<UniqueId>>> newResult =
          new ObjectsPair<>(resultToCache.getPaging().getTotalItems(), rangeMapToCache);
      getSearchRequestCache().put(new Element(request, newResult));
      return newResult;
    }
  }

  /**
   * Fill in any uncached gaps for the requested range from the underlying, creating a single cached 'super range' from
   * which the entire current search request can be satisfied. This method may be called concurrently in multiple
   * threads.
   *
   * @param originalRequest the search request
   * @param rangeMap        the range map of cached documents for the supplied search request without paging
   * @return                a super-range of cached documents that contains at least the requested documents
   */
  protected ObjectsPair<Integer, List<UniqueId>> cacheSuperRange(final AbstractSearchRequest originalRequest,
                            final ConcurrentNavigableMap<Integer, List<UniqueId>> rangeMap, boolean blockUntilCached) {

    final int startIndex = originalRequest.getPagingRequest().getFirstItem();
    final int endIndex = originalRequest.getPagingRequest().getLastItem();
    AbstractSearchRequest request = withoutPagingRequest(originalRequest);

    final List<UniqueId> superRange;
    final int superIndex;

    if (blockUntilCached) {
      synchronized (rangeMap) {

        // Determine the super range's start index
        if (rangeMap.floorKey(startIndex) != null &&
            rangeMap.floorKey(startIndex) +
                rangeMap.floorEntry(startIndex).getValue().size() >= startIndex) {
          superRange = rangeMap.floorEntry(startIndex).getValue();
          superIndex = rangeMap.floorKey(startIndex);
        } else {
          superRange = Collections.synchronizedList(new LinkedList<UniqueId>());
          superIndex = startIndex;
          rangeMap.put(superIndex, superRange);
        }

        // Get map subset from closest start index above requested start index, to closest start index below requested
        // end index
        final Integer start = rangeMap.ceilingKey(startIndex + 1);
        final Integer end = rangeMap.floorKey(endIndex);
        final ConcurrentNavigableMap<Integer, List<UniqueId>> subMap =
            (ConcurrentNavigableMap<Integer, List<UniqueId>>) (
              ((start != null) && (end != null) && (start <= end))
                  ? rangeMap.subMap(start, true, end, true)
                  : new ConcurrentSkipListMap<>()
            );

        // Iterate through ranges within the paging request range, concatenating them into the super range while
        // filling in any gaps from the underlying master
        int currentIndex = superIndex + superRange.size();
        final List<Integer> toRemove = new LinkedList<>();
        for (Map.Entry<Integer, List<UniqueId>> entry : subMap.entrySet()) {
          // If required, fill gap from underlying
          if (entry.getKey() > currentIndex) {
            request.setPagingRequest(PagingRequest.ofIndex(currentIndex, entry.getKey() - currentIndex));
            AbstractSearchResult<D> missingResult = getSearcher().search(request);

            // Cache UniqueIds in search cache and add documents to uidToDocumentCache
            superRange.addAll(extractUniqueIds(missingResult.getDocuments()));
            cacheDocuments(missingResult.getDocuments());

            currentIndex += missingResult.getDocuments().size();
          }

          // Add next cached range
          superRange.addAll(entry.getValue());
          currentIndex += entry.getValue().size();

          toRemove.add(entry.getKey());
        }


        // Remove original cached ranges (now part of the super range)
        for (Integer i : toRemove) {
          rangeMap.remove(i);
        }

        // If required, fill in the final range from underlying
        if (currentIndex < endIndex) {
          request.setPagingRequest(PagingRequest.ofIndex(
              currentIndex,
              endIndex - currentIndex
          ));
          final AbstractSearchResult<D> missingResult = getSearcher().search(request);

          // Cache UniqueIds in search cache and add documents to uidToDocumentCache
          superRange.addAll(extractUniqueIds(missingResult.getDocuments()));
          cacheDocuments(missingResult.getDocuments());

          currentIndex += missingResult.getDocuments().size();
        }

        // put expanded super range into range map
        rangeMap.put(superIndex, superRange);
      }

    } else {
      // If entirely cached then return cached values
      if (rangeMap.floorKey(startIndex) != null &&
          rangeMap.floorKey(startIndex) + rangeMap.floorEntry(startIndex).getValue().size() >= endIndex) {
        superRange = rangeMap.floorEntry(startIndex).getValue();
        superIndex = rangeMap.floorKey(startIndex);

      // If not entirely cached then just fetch from underlying without caching
      } else {
        final AbstractSearchResult<D> missingResult = getSearcher().search(originalRequest);
        superRange = Collections.synchronizedList(new LinkedList<UniqueId>());

        // Cache UniqueIds in search cache and add documents to uidToDocumentCache
        superRange.addAll(extractUniqueIds(missingResult.getDocuments()));
        cacheDocuments(missingResult.getDocuments());

        superIndex = startIndex;
      }
    }

    return new ObjectsPair<>(superIndex, superRange);
  }

  private List<UniqueId> extractUniqueIds(List<D> documents) {
    List <UniqueId> result = new LinkedList<>();
    for (D document : documents) {
      result.add(document.getUniqueId());
    }
    return result;
  }

  private void cacheDocuments(List<D> documents) {
    for (D document : documents) {
      _documentCache.put(new Element(document.getUniqueId(), document));
    }
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache.
   * It should not be part of a generic lifecycle method.
   */
  public void shutdown() {
    //getUnderlying().changeManager().removeChangeListener(_changeListener);
    //getCacheManager().clearAllStartingWith(_documentByOidCacheName);
    //getCacheManager().clearAllStartingWith(_documentByUidCacheName);
    //getCacheManager().removeCache(_documentByOidCacheName);
    //getCacheManager().removeCache(_documentByUidCacheName);
  }

  /**
   * Gets the documents by search request cache.
   *
   * @return the cache, not null
   */
  protected Cache getSearchRequestCache() {
    return _searchRequestCache;
  }

  public CacheSearcher<D> getSearcher() {
    return _searcher;
  }
}
