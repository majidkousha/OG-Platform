/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.Currency;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author jim
 */
public abstract class EquityOptionSecurity extends FinancialSecurity implements Option {
  public static final String EQUITY_OPTION_TYPE="EQUITY_OPTION";
  private final OptionType _optionType;
  private final double _strike;
  private final Expiry _expiry;
  private final DomainSpecificIdentifier _underlyingIdentityKey;
  private final Currency _currency;
  private final String _exchange;
  // TODO: jim 23-Sep-2009 -- Add support for regions/countries

  public EquityOptionSecurity(OptionType optionType, double strike, Expiry expiry, DomainSpecificIdentifier underlyingIdentityKey, Currency currency, final String exchange) {
    _optionType = optionType;
    _strike = strike;
    _expiry = expiry;
    _underlyingIdentityKey = underlyingIdentityKey;
    _currency = currency;
    _exchange = exchange;
    setSecurityType(EQUITY_OPTION_TYPE);
  }

  /**
   * @return the optionType
   */
  public OptionType getOptionType() {
    return _optionType;
  }

  /**
   * @return the strike
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * @return the expiry
   */
  public Expiry getExpiry() {
    return _expiry;
  }
  
  public DomainSpecificIdentifier getUnderlyingIdentityKey() {
    return _underlyingIdentityKey;
  }
  
  public Currency getCurrency() {
    return _currency;
  }
  
  public String getExchange () {
    return _exchange;
  }
  
  public abstract <T> T accept(OptionVisitor<T> visitor);
  
  public abstract <T> T accept(EquityOptionSecurityVisitor<T> visitor);
  
  public <T> T accept (FinancialSecurityVisitor<T> visitor) {
    return accept ((EquityOptionSecurityVisitor<T>)visitor);
  }
  
}
