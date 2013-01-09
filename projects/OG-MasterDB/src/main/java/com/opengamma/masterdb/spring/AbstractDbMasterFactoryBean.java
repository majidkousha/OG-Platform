/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.spring;

import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.db.DbConnector;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.spring.SpringFactoryBean;

/**
 * Spring factory bean to create the database master.
 * 
 * @param <T> the type of the master
 */
@BeanDefinition
public abstract class AbstractDbMasterFactoryBean<T> extends SpringFactoryBean<T> {

  /**
   * The database connector.
   */
  @PropertyDefinition
  private DbConnector _dbConnector;
  /**
   * The JMS connector, null for no JMS change manager.
   */
  @PropertyDefinition
  private JmsConnector _jmsConnector;
  /**
   * The config for the JMS change manager topic.
   */
  @PropertyDefinition
  private String _jmsChangeManagerTopic;
  /**
   * The config for the scheme used by the {@code UniqueId}.
   */
  @PropertyDefinition
  private String _uniqueIdScheme;
  /**
   * The config for the maximum number of retries when updating.
   */
  @PropertyDefinition
  private Integer _maxRetries;

  /**
   * Creates an instance.
   * 
   * @param type  the type, not null
   */
  public AbstractDbMasterFactoryBean(Class<T> type) {
    super(type);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code AbstractDbMasterFactoryBean}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static AbstractDbMasterFactoryBean.Meta meta() {
    return AbstractDbMasterFactoryBean.Meta.INSTANCE;
  }
  /**
   * The meta-bean for {@code AbstractDbMasterFactoryBean}.
   * @param <R>  the bean's generic type
   * @param cls  the bean's generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R> AbstractDbMasterFactoryBean.Meta<R> metaAbstractDbMasterFactoryBean(Class<R> cls) {
    return AbstractDbMasterFactoryBean.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(AbstractDbMasterFactoryBean.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public AbstractDbMasterFactoryBean.Meta<T> metaBean() {
    return AbstractDbMasterFactoryBean.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 39794031:  // dbConnector
        return getDbConnector();
      case -1495762275:  // jmsConnector
        return getJmsConnector();
      case -758086398:  // jmsChangeManagerTopic
        return getJmsChangeManagerTopic();
      case -1737146991:  // uniqueIdScheme
        return getUniqueIdScheme();
      case -2022653118:  // maxRetries
        return getMaxRetries();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 39794031:  // dbConnector
        setDbConnector((DbConnector) newValue);
        return;
      case -1495762275:  // jmsConnector
        setJmsConnector((JmsConnector) newValue);
        return;
      case -758086398:  // jmsChangeManagerTopic
        setJmsChangeManagerTopic((String) newValue);
        return;
      case -1737146991:  // uniqueIdScheme
        setUniqueIdScheme((String) newValue);
        return;
      case -2022653118:  // maxRetries
        setMaxRetries((Integer) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      AbstractDbMasterFactoryBean<?> other = (AbstractDbMasterFactoryBean<?>) obj;
      return JodaBeanUtils.equal(getDbConnector(), other.getDbConnector()) &&
          JodaBeanUtils.equal(getJmsConnector(), other.getJmsConnector()) &&
          JodaBeanUtils.equal(getJmsChangeManagerTopic(), other.getJmsChangeManagerTopic()) &&
          JodaBeanUtils.equal(getUniqueIdScheme(), other.getUniqueIdScheme()) &&
          JodaBeanUtils.equal(getMaxRetries(), other.getMaxRetries()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getDbConnector());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJmsConnector());
    hash += hash * 31 + JodaBeanUtils.hashCode(getJmsChangeManagerTopic());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueIdScheme());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMaxRetries());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the database connector.
   * @return the value of the property
   */
  public DbConnector getDbConnector() {
    return _dbConnector;
  }

  /**
   * Sets the database connector.
   * @param dbConnector  the new value of the property
   */
  public void setDbConnector(DbConnector dbConnector) {
    this._dbConnector = dbConnector;
  }

  /**
   * Gets the the {@code dbConnector} property.
   * @return the property, not null
   */
  public final Property<DbConnector> dbConnector() {
    return metaBean().dbConnector().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JMS connector, null for no JMS change manager.
   * @return the value of the property
   */
  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  /**
   * Sets the JMS connector, null for no JMS change manager.
   * @param jmsConnector  the new value of the property
   */
  public void setJmsConnector(JmsConnector jmsConnector) {
    this._jmsConnector = jmsConnector;
  }

  /**
   * Gets the the {@code jmsConnector} property.
   * @return the property, not null
   */
  public final Property<JmsConnector> jmsConnector() {
    return metaBean().jmsConnector().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config for the JMS change manager topic.
   * @return the value of the property
   */
  public String getJmsChangeManagerTopic() {
    return _jmsChangeManagerTopic;
  }

  /**
   * Sets the config for the JMS change manager topic.
   * @param jmsChangeManagerTopic  the new value of the property
   */
  public void setJmsChangeManagerTopic(String jmsChangeManagerTopic) {
    this._jmsChangeManagerTopic = jmsChangeManagerTopic;
  }

  /**
   * Gets the the {@code jmsChangeManagerTopic} property.
   * @return the property, not null
   */
  public final Property<String> jmsChangeManagerTopic() {
    return metaBean().jmsChangeManagerTopic().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config for the scheme used by the {@code UniqueId}.
   * @return the value of the property
   */
  public String getUniqueIdScheme() {
    return _uniqueIdScheme;
  }

  /**
   * Sets the config for the scheme used by the {@code UniqueId}.
   * @param uniqueIdScheme  the new value of the property
   */
  public void setUniqueIdScheme(String uniqueIdScheme) {
    this._uniqueIdScheme = uniqueIdScheme;
  }

  /**
   * Gets the the {@code uniqueIdScheme} property.
   * @return the property, not null
   */
  public final Property<String> uniqueIdScheme() {
    return metaBean().uniqueIdScheme().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config for the maximum number of retries when updating.
   * @return the value of the property
   */
  public Integer getMaxRetries() {
    return _maxRetries;
  }

  /**
   * Sets the config for the maximum number of retries when updating.
   * @param maxRetries  the new value of the property
   */
  public void setMaxRetries(Integer maxRetries) {
    this._maxRetries = maxRetries;
  }

  /**
   * Gets the the {@code maxRetries} property.
   * @return the property, not null
   */
  public final Property<Integer> maxRetries() {
    return metaBean().maxRetries().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code AbstractDbMasterFactoryBean}.
   */
  public static class Meta<T> extends SpringFactoryBean.Meta<T> {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code dbConnector} property.
     */
    private final MetaProperty<DbConnector> _dbConnector = DirectMetaProperty.ofReadWrite(
        this, "dbConnector", AbstractDbMasterFactoryBean.class, DbConnector.class);
    /**
     * The meta-property for the {@code jmsConnector} property.
     */
    private final MetaProperty<JmsConnector> _jmsConnector = DirectMetaProperty.ofReadWrite(
        this, "jmsConnector", AbstractDbMasterFactoryBean.class, JmsConnector.class);
    /**
     * The meta-property for the {@code jmsChangeManagerTopic} property.
     */
    private final MetaProperty<String> _jmsChangeManagerTopic = DirectMetaProperty.ofReadWrite(
        this, "jmsChangeManagerTopic", AbstractDbMasterFactoryBean.class, String.class);
    /**
     * The meta-property for the {@code uniqueIdScheme} property.
     */
    private final MetaProperty<String> _uniqueIdScheme = DirectMetaProperty.ofReadWrite(
        this, "uniqueIdScheme", AbstractDbMasterFactoryBean.class, String.class);
    /**
     * The meta-property for the {@code maxRetries} property.
     */
    private final MetaProperty<Integer> _maxRetries = DirectMetaProperty.ofReadWrite(
        this, "maxRetries", AbstractDbMasterFactoryBean.class, Integer.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "dbConnector",
        "jmsConnector",
        "jmsChangeManagerTopic",
        "uniqueIdScheme",
        "maxRetries");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 39794031:  // dbConnector
          return _dbConnector;
        case -1495762275:  // jmsConnector
          return _jmsConnector;
        case -758086398:  // jmsChangeManagerTopic
          return _jmsChangeManagerTopic;
        case -1737146991:  // uniqueIdScheme
          return _uniqueIdScheme;
        case -2022653118:  // maxRetries
          return _maxRetries;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends AbstractDbMasterFactoryBean<T>> builder() {
      throw new UnsupportedOperationException("AbstractDbMasterFactoryBean is an abstract class");
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends AbstractDbMasterFactoryBean<T>> beanType() {
      return (Class) AbstractDbMasterFactoryBean.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code dbConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DbConnector> dbConnector() {
      return _dbConnector;
    }

    /**
     * The meta-property for the {@code jmsConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<JmsConnector> jmsConnector() {
      return _jmsConnector;
    }

    /**
     * The meta-property for the {@code jmsChangeManagerTopic} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> jmsChangeManagerTopic() {
      return _jmsChangeManagerTopic;
    }

    /**
     * The meta-property for the {@code uniqueIdScheme} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> uniqueIdScheme() {
      return _uniqueIdScheme;
    }

    /**
     * The meta-property for the {@code maxRetries} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> maxRetries() {
      return _maxRetries;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
