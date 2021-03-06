/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.FunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class InterestRateInstrumentYieldCurveNodeSensitivitiesFunction extends InterestRateInstrumentCurveSpecificFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateInstrumentYieldCurveNodeSensitivitiesFunction.class);
  private static final PresentValueNodeSensitivityCalculator NSC = PresentValueNodeSensitivityCalculator.using(PresentValueCurveSensitivitySABRCalculator.getInstance());
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();

  public InterestRateInstrumentYieldCurveNodeSensitivitiesFunction() {
    super(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final LocalDate localNow = now.toLocalDate();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final InstrumentDefinition<?> definition = security.accept(getVisitor());
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ConfigSource configSource = OpenGammaExecutionContext.getConfigSource(executionContext);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();
    final InstrumentDerivative derivative = InterestRateInstrumentFunction.getDerivative(security, now, timeSeries, curveNames, definition, getConverter());
    final YieldCurveBundle curves = YieldCurveFunctionUtils.getYieldCurves(inputs, curveCalculationConfig);
    final YieldCurveBundle fixedCurves = YieldCurveFunctionUtils.getFixedCurves(inputs, curveCalculationConfig, curveCalculationConfigSource);
    final ValueProperties properties = createValueProperties(target, curveName, curveCalculationConfigName).get();
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirement(), target.toSpecification(), properties);
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final double[][] array = FunctionUtils.decodeJacobian(jacobianObject);
    final DoubleMatrix2D jacobian = new DoubleMatrix2D(array);
    DoubleMatrix1D sensitivities;
    if (curveCalculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING)) {
      final Object couponSensitivitiesObject = inputs.getValue(getCouponSensitivitiesRequirement(currency, curveCalculationConfigName));
      if (couponSensitivitiesObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY);
      }
      final DoubleMatrix1D couponSensitivity = new DoubleMatrix1D(FunctionUtils.decodeCouponSensitivities(couponSensitivitiesObject));
      sensitivities = CALCULATOR.calculateFromPresentValue(derivative, fixedCurves, curves, couponSensitivity, jacobian, NSC);
    } else {
      sensitivities = CALCULATOR.calculateFromParRate(derivative, fixedCurves, curves, jacobian, NSC);
    }
    if (curveCalculationMethod.equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      final Currency domesticCurrency = Currency.of(curveCalculationConfig.getUniqueId().getUniqueId().getValue());
      final Currency foreignCurrency =
          Currency.of(curveCalculationConfigSource.getConfig(curveCalculationConfig.getExogenousConfigData().keySet().iterator().next()).getUniqueId().getUniqueId().getValue());
      return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(sensitivities, domesticCurrency, foreignCurrency, curveNames,
          curves, configSource, localNow, resultSpec);
    }
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(currency, curveName);
    final Object curveSpecObject = inputs.getValue(curveSpecRequirement);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSpecRequirement);
    }
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    return YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(curveName, curves, sensitivities, curveSpec, resultSpec);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency currency = FinancialSecurityUtils.getCurrency(security);
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final boolean permissive = OpenGammaCompilationContext.isPermissive(context);
    Set<String> requestedCurveNames = constraints.getValues(ValuePropertyNames.CURVE);
    if (!permissive && (requestedCurveNames == null || requestedCurveNames.isEmpty())) {
      s_logger.error("Must specify a single curve name; have {}", requestedCurveNames);
      return null;
    }
    final String curveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurveCalculationConfigSource curveCalculationConfigSource = new ConfigDBCurveCalculationConfigSource(configSource);
    final MultiCurveCalculationConfig curveCalculationConfig = curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final UniqueIdentifiable uniqueId = curveCalculationConfig.getUniqueId();
    if (Currency.OBJECT_SCHEME.equals(uniqueId.getUniqueId().getScheme()) && !(uniqueId.getUniqueId().getValue().equals(currency.getCode()))) {
      return null;
    }
    final String[] availableCurveNames = curveCalculationConfig.getYieldCurveNames();
    if ((requestedCurveNames == null) || requestedCurveNames.isEmpty()) {
      requestedCurveNames = Sets.newHashSet(availableCurveNames);
    } else {
      final Set<String> intersection = YieldCurveFunctionUtils.intersection(requestedCurveNames, availableCurveNames);
      if (intersection.isEmpty()) {
        s_logger.error("None of the requested curves {} are available in curve calculation configuration called {}", requestedCurveNames, curveCalculationConfigName);
        return null;
      }
      requestedCurveNames = intersection;
    }
    if (!permissive && (requestedCurveNames.size() != 1)) {
      s_logger.error("Must specify single curve name constraint, got {}", requestedCurveNames);
      return null;
    }
    final String curve = requestedCurveNames.iterator().next();
    final String curveCalculationMethod = curveCalculationConfig.getCalculationMethod();

    final Set<ValueRequirement> curveRequirements = YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, curveCalculationConfigSource);
    final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(curveRequirements.size());
    for (final ValueRequirement curveRequirement : curveRequirements) {
      final ValueProperties.Builder properties = curveRequirement.getConstraints().copy();
      properties.with(PROPERTY_REQUESTED_CURVE, curve).withOptional(PROPERTY_REQUESTED_CURVE);
      requirements.add(new ValueRequirement(curveRequirement.getValueName(), curveRequirement.getTargetSpecification(), properties.get()));
    }
    if (!curveCalculationMethod.equals(FXImpliedYieldCurveFunction.FX_IMPLIED)) {
      requirements.add(getCurveSpecRequirement(currency, curve));
    }
    requirements.add(getJacobianRequirement(currency, curveCalculationConfigName, curveCalculationMethod));
    if (curveCalculationMethod.equals(MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING)) {
      requirements.add(getCouponSensitivitiesRequirement(currency, curveCalculationConfigName));
    }
    try {
      final Set<ValueRequirement> timeSeriesRequirements = InterestRateInstrumentFunction.getDerivativeTimeSeriesRequirements(security, security.accept(getVisitor()), getConverter());
      if (timeSeriesRequirements == null) {
        return null;
      }
      requirements.addAll(timeSeriesRequirements);
    } catch (final OpenGammaRuntimeException e) {
      s_logger.error("Could not get time series requirements; error was {}", e.getMessage());
      return null;
    }
    return requirements;
  }

  private ValueRequirement getCurveSpecRequirement(final Currency currency, final String curveName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, currency, properties);
  }

  private ValueRequirement getJacobianRequirement(final Currency currency, final String curveCalculationConfigName, final String curveCalculationMethod) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, curveCalculationMethod).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_JACOBIAN, currency, properties);
  }

  private ValueRequirement getCouponSensitivitiesRequirement(final Currency currency, final String curveCalculationConfigName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING).get();
    return new ValueRequirement(ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY, currency, properties);
  }

  @Override
  protected Set<ComputedValue> getResults(final InstrumentDerivative derivative, final String curveName, final YieldCurveBundle curves, final String curveCalculationConfigName,
      final String curveCalculationMethod, final FunctionInputs inputs, final ComputationTarget target, final ValueSpecification resultSpec) {
    throw new NotImplementedException();
  }
}
