/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * Loads security from bloomberg and populates the security master
 */
public class BloombergSecurityLoader implements SecurityLoader {

  /**
   * The security provider to load from.
   */
  private final SecurityProvider _securityProvider;
  /**
   * The security master to load into.
   */
  private final SecurityMaster _securityMaster;

  /**
   * Creates BloombergSecurityLoader
   * 
   * @param securityMaster  the security master, not null
   * @param securityProvider  the security provider, not null
   */
  public BloombergSecurityLoader(SecurityProvider securityProvider, SecurityMaster securityMaster) {
    ArgumentChecker.notNull(securityProvider, "securityProvider");
    ArgumentChecker.notNull(securityMaster, "HibernateSecMaster");
    _securityProvider = securityProvider;
    _securityMaster = securityMaster;
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<ExternalIdBundle, UniqueId> loadSecurity(final Collection<ExternalIdBundle> identifiers) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    
    Map<ExternalIdBundle, UniqueId> result = new HashMap<ExternalIdBundle, UniqueId>();
    
    Map<ExternalIdBundle, Security> securities = _securityProvider.getSecurities(identifiers);
    
    UnderlyingIdentifierCollector identifierCollector = new UnderlyingIdentifierCollector();
        
    for (Entry<ExternalIdBundle, Security> entry : securities.entrySet()) {
      ExternalIdBundle requestBundle = entry.getKey();
      Security security = entry.getValue();
      
      //getUnderlying identifiers
      if (security instanceof FinancialSecurity) {
        FinancialSecurity financialSecurity = (FinancialSecurity) security;
        financialSecurity.accept(identifierCollector.getFinancialSecurityVisitor());
      }
      
      UniqueId uid = null;
      if (security != null && security instanceof ManageableSecurity) {
        ExternalId buid = security.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_BUID);
        uid = addOrUpdate((ManageableSecurity) security, getUid(buid));
      }
      result.put(requestBundle, uid);
    }
    
    addOrUpdateUnderlying(identifierCollector.getUnderlyings());
    
    return result;
  }

  private void addOrUpdateUnderlying(Set<ExternalIdBundle> underlyingIdentifiers) {
    Map<ExternalIdBundle, Security> securities = _securityProvider.getSecurities(underlyingIdentifiers);
    for (Entry<ExternalIdBundle, Security> entry : securities.entrySet()) {
      Security security = entry.getValue();
      if (security != null && security instanceof ManageableSecurity) {
        ExternalId buid = security.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_BUID);
        UniqueId uid = getUid(buid);
        addOrUpdate((ManageableSecurity) security, uid);
      }
    }
  }

  private UniqueId addOrUpdate(ManageableSecurity security, UniqueId uid) {
    if (uid == null) {
      return addSecurity(security);
    } else {
      return updateSecurity(security, uid);
    }
 
  }

  private UniqueId updateSecurity(ManageableSecurity security, UniqueId uid) {
    final SecurityDocument document = new SecurityDocument();
    document.setSecurity(security);
    document.setUniqueId(uid);
    getSecurityMaster().update(document);
    return document.getUniqueId();
  }

  private UniqueId addSecurity(ManageableSecurity security) {
    final SecurityDocument document = new SecurityDocument();
    document.setSecurity(security);
    SecurityDocument addedSecurity = getSecurityMaster().add(document);
    return addedSecurity.getUniqueId();
  }

  private UniqueId getUid(ExternalId identifier) {
    SecuritySearchRequest request = new SecuritySearchRequest(ExternalIdBundle.of(identifier));
    SecuritySearchResult result = getSecurityMaster().search(request);
    UniqueId uid = null;
    if (!result.getDocuments().isEmpty()) {
      SecurityDocument document = result.getFirstDocument();
      uid = document.getUniqueId();
    }
    return uid;
  }

  @Override
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

}
