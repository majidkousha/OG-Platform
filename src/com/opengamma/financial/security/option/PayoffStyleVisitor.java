/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

/**
 * Visitor for the PayoffStyle subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface PayoffStyleVisitor<T> {

  T visitAssetOrNothingPayoffStyle(AssetOrNothingPayoffStyle payoffStyle);

  T visitAsymmetricPoweredPayoffStyle(AsymmetricPoweredPayoffStyle payoffStyle);

  T visitBarrierPayoffStyle(BarrierPayoffStyle payoffStyle);

  T visitCappedPoweredPayoffStyle(CappedPoweredPayoffStyle payoffStyle);

  T visitCashOrNothingPayoffStyle(CashOrNothingPayoffStyle payoffStyle);
  
  T visitFadeInPayoffStyle(FadeInPayoffStyle payoffStyle);
  
  T visitFixedStrikeLookbackPayoffStyle(FixedStrikeLookbackPayoffStyle payoffStyle);

  T visitFloatingStrikeLookbackPayoffStyle(FloatingStrikeLookbackPayoffStyle payoffStyle);

  T visitGapPayoffStyle(GapPayoffStyle payoffStyle);
  
  T visitPoweredPayoffStyle(PoweredPayoffStyle payoffStyle);

  T visitSupersharePayoffStyle(SupersharePayoffStyle payoffStyle);
  
  T visitVanillaPayoffStyle(VanillaPayoffStyle payoffStyle);
  
}
