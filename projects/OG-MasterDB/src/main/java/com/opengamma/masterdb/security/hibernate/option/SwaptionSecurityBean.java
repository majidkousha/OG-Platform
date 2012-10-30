/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.ExpiryBean;
import com.opengamma.masterdb.security.hibernate.ExternalIdBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * A Hibernate bean representation of {@link OptionSecurity}.
 */
@BeanDefinition
public class SwaptionSecurityBean extends SecurityBean {
  @PropertyDefinition
  private ExpiryBean _expiry;
  
  @PropertyDefinition
  private ExternalIdBean _underlying;
  
  @PropertyDefinition
  private Boolean _cashSettled;
  
  @PropertyDefinition
  private Boolean _longShort;
  
  @PropertyDefinition
  private Boolean _payer;
  
  @PropertyDefinition
  private CurrencyBean _currency;
  
  @PropertyDefinition
  private Double _notional;
  
  @PropertyDefinition
  private OptionExerciseType _optionExerciseType;
  
  @PropertyDefinition
  private ZonedDateTimeBean _settlementDate;
  
  public SwaptionSecurityBean() {
    super();
  }  

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof SwaptionSecurityBean)) {
      return false;
    }
    final SwaptionSecurityBean option = (SwaptionSecurityBean) other;
    //    if (getId() != -1 && option.getId() != -1) {
    //      return getId().longValue() == option.getId().longValue();
    //    }
    return new EqualsBuilder()
      .append(getId(), option.getId())
      .append(getExpiry(), option.getExpiry())
      .append(getUnderlying(), option.getUnderlying())
      .append(getCashSettled(), option.getCashSettled())
      .append(getLongShort(), option.getLongShort())
      .append(getPayer(), option.getPayer())
      .append(getCurrency(), getCurrency())
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getExpiry())
      .append(getUnderlying())
      .append(getCashSettled())
      .append(getLongShort())
      .append(getPayer())
      .append(getCurrency())
      .toHashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SwaptionSecurityBean}.
   * @return the meta-bean, not null
   */
  public static SwaptionSecurityBean.Meta meta() {
    return SwaptionSecurityBean.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(SwaptionSecurityBean.Meta.INSTANCE);
  }

  @Override
  public SwaptionSecurityBean.Meta metaBean() {
    return SwaptionSecurityBean.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -1289159373:  // expiry
        return getExpiry();
      case -1770633379:  // underlying
        return getUnderlying();
      case -871053882:  // cashSettled
        return getCashSettled();
      case 116685664:  // longShort
        return getLongShort();
      case 106443605:  // payer
        return getPayer();
      case 575402001:  // currency
        return getCurrency();
      case 1585636160:  // notional
        return getNotional();
      case -266326457:  // optionExerciseType
        return getOptionExerciseType();
      case -295948169:  // settlementDate
        return getSettlementDate();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -1289159373:  // expiry
        setExpiry((ExpiryBean) newValue);
        return;
      case -1770633379:  // underlying
        setUnderlying((ExternalIdBean) newValue);
        return;
      case -871053882:  // cashSettled
        setCashSettled((Boolean) newValue);
        return;
      case 116685664:  // longShort
        setLongShort((Boolean) newValue);
        return;
      case 106443605:  // payer
        setPayer((Boolean) newValue);
        return;
      case 575402001:  // currency
        setCurrency((CurrencyBean) newValue);
        return;
      case 1585636160:  // notional
        setNotional((Double) newValue);
        return;
      case -266326457:  // optionExerciseType
        setOptionExerciseType((OptionExerciseType) newValue);
        return;
      case -295948169:  // settlementDate
        setSettlementDate((ZonedDateTimeBean) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the expiry.
   * @return the value of the property
   */
  public ExpiryBean getExpiry() {
    return _expiry;
  }

  /**
   * Sets the expiry.
   * @param expiry  the new value of the property
   */
  public void setExpiry(ExpiryBean expiry) {
    this._expiry = expiry;
  }

  /**
   * Gets the the {@code expiry} property.
   * @return the property, not null
   */
  public final Property<ExpiryBean> expiry() {
    return metaBean().expiry().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying.
   * @return the value of the property
   */
  public ExternalIdBean getUnderlying() {
    return _underlying;
  }

  /**
   * Sets the underlying.
   * @param underlying  the new value of the property
   */
  public void setUnderlying(ExternalIdBean underlying) {
    this._underlying = underlying;
  }

  /**
   * Gets the the {@code underlying} property.
   * @return the property, not null
   */
  public final Property<ExternalIdBean> underlying() {
    return metaBean().underlying().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the cashSettled.
   * @return the value of the property
   */
  public Boolean getCashSettled() {
    return _cashSettled;
  }

  /**
   * Sets the cashSettled.
   * @param cashSettled  the new value of the property
   */
  public void setCashSettled(Boolean cashSettled) {
    this._cashSettled = cashSettled;
  }

  /**
   * Gets the the {@code cashSettled} property.
   * @return the property, not null
   */
  public final Property<Boolean> cashSettled() {
    return metaBean().cashSettled().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the longShort.
   * @return the value of the property
   */
  public Boolean getLongShort() {
    return _longShort;
  }

  /**
   * Sets the longShort.
   * @param longShort  the new value of the property
   */
  public void setLongShort(Boolean longShort) {
    this._longShort = longShort;
  }

  /**
   * Gets the the {@code longShort} property.
   * @return the property, not null
   */
  public final Property<Boolean> longShort() {
    return metaBean().longShort().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payer.
   * @return the value of the property
   */
  public Boolean getPayer() {
    return _payer;
  }

  /**
   * Sets the payer.
   * @param payer  the new value of the property
   */
  public void setPayer(Boolean payer) {
    this._payer = payer;
  }

  /**
   * Gets the the {@code payer} property.
   * @return the property, not null
   */
  public final Property<Boolean> payer() {
    return metaBean().payer().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency.
   * @return the value of the property
   */
  public CurrencyBean getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency.
   * @param currency  the new value of the property
   */
  public void setCurrency(CurrencyBean currency) {
    this._currency = currency;
  }

  /**
   * Gets the the {@code currency} property.
   * @return the property, not null
   */
  public final Property<CurrencyBean> currency() {
    return metaBean().currency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the notional.
   * @return the value of the property
   */
  public Double getNotional() {
    return _notional;
  }

  /**
   * Sets the notional.
   * @param notional  the new value of the property
   */
  public void setNotional(Double notional) {
    this._notional = notional;
  }

  /**
   * Gets the the {@code notional} property.
   * @return the property, not null
   */
  public final Property<Double> notional() {
    return metaBean().notional().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the optionExerciseType.
   * @return the value of the property
   */
  public OptionExerciseType getOptionExerciseType() {
    return _optionExerciseType;
  }

  /**
   * Sets the optionExerciseType.
   * @param optionExerciseType  the new value of the property
   */
  public void setOptionExerciseType(OptionExerciseType optionExerciseType) {
    this._optionExerciseType = optionExerciseType;
  }

  /**
   * Gets the the {@code optionExerciseType} property.
   * @return the property, not null
   */
  public final Property<OptionExerciseType> optionExerciseType() {
    return metaBean().optionExerciseType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the settlementDate.
   * @return the value of the property
   */
  public ZonedDateTimeBean getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Sets the settlementDate.
   * @param settlementDate  the new value of the property
   */
  public void setSettlementDate(ZonedDateTimeBean settlementDate) {
    this._settlementDate = settlementDate;
  }

  /**
   * Gets the the {@code settlementDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTimeBean> settlementDate() {
    return metaBean().settlementDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SwaptionSecurityBean}.
   */
  public static class Meta extends SecurityBean.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code expiry} property.
     */
    private final MetaProperty<ExpiryBean> _expiry = DirectMetaProperty.ofReadWrite(
        this, "expiry", SwaptionSecurityBean.class, ExpiryBean.class);
    /**
     * The meta-property for the {@code underlying} property.
     */
    private final MetaProperty<ExternalIdBean> _underlying = DirectMetaProperty.ofReadWrite(
        this, "underlying", SwaptionSecurityBean.class, ExternalIdBean.class);
    /**
     * The meta-property for the {@code cashSettled} property.
     */
    private final MetaProperty<Boolean> _cashSettled = DirectMetaProperty.ofReadWrite(
        this, "cashSettled", SwaptionSecurityBean.class, Boolean.class);
    /**
     * The meta-property for the {@code longShort} property.
     */
    private final MetaProperty<Boolean> _longShort = DirectMetaProperty.ofReadWrite(
        this, "longShort", SwaptionSecurityBean.class, Boolean.class);
    /**
     * The meta-property for the {@code payer} property.
     */
    private final MetaProperty<Boolean> _payer = DirectMetaProperty.ofReadWrite(
        this, "payer", SwaptionSecurityBean.class, Boolean.class);
    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<CurrencyBean> _currency = DirectMetaProperty.ofReadWrite(
        this, "currency", SwaptionSecurityBean.class, CurrencyBean.class);
    /**
     * The meta-property for the {@code notional} property.
     */
    private final MetaProperty<Double> _notional = DirectMetaProperty.ofReadWrite(
        this, "notional", SwaptionSecurityBean.class, Double.class);
    /**
     * The meta-property for the {@code optionExerciseType} property.
     */
    private final MetaProperty<OptionExerciseType> _optionExerciseType = DirectMetaProperty.ofReadWrite(
        this, "optionExerciseType", SwaptionSecurityBean.class, OptionExerciseType.class);
    /**
     * The meta-property for the {@code settlementDate} property.
     */
    private final MetaProperty<ZonedDateTimeBean> _settlementDate = DirectMetaProperty.ofReadWrite(
        this, "settlementDate", SwaptionSecurityBean.class, ZonedDateTimeBean.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "expiry",
        "underlying",
        "cashSettled",
        "longShort",
        "payer",
        "currency",
        "notional",
        "optionExerciseType",
        "settlementDate");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1289159373:  // expiry
          return _expiry;
        case -1770633379:  // underlying
          return _underlying;
        case -871053882:  // cashSettled
          return _cashSettled;
        case 116685664:  // longShort
          return _longShort;
        case 106443605:  // payer
          return _payer;
        case 575402001:  // currency
          return _currency;
        case 1585636160:  // notional
          return _notional;
        case -266326457:  // optionExerciseType
          return _optionExerciseType;
        case -295948169:  // settlementDate
          return _settlementDate;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SwaptionSecurityBean> builder() {
      return new DirectBeanBuilder<SwaptionSecurityBean>(new SwaptionSecurityBean());
    }

    @Override
    public Class<? extends SwaptionSecurityBean> beanType() {
      return SwaptionSecurityBean.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code expiry} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExpiryBean> expiry() {
      return _expiry;
    }

    /**
     * The meta-property for the {@code underlying} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdBean> underlying() {
      return _underlying;
    }

    /**
     * The meta-property for the {@code cashSettled} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> cashSettled() {
      return _cashSettled;
    }

    /**
     * The meta-property for the {@code longShort} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> longShort() {
      return _longShort;
    }

    /**
     * The meta-property for the {@code payer} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> payer() {
      return _payer;
    }

    /**
     * The meta-property for the {@code currency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurrencyBean> currency() {
      return _currency;
    }

    /**
     * The meta-property for the {@code notional} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> notional() {
      return _notional;
    }

    /**
     * The meta-property for the {@code optionExerciseType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<OptionExerciseType> optionExerciseType() {
      return _optionExerciseType;
    }

    /**
     * The meta-property for the {@code settlementDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTimeBean> settlementDate() {
      return _settlementDate;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}