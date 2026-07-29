package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * ============================================================================
 * TICKET-ADV023 — TradeFactory: build a TradeType by asset-class string
 *
 * WHAT:    Single entry point that takes an asset-class string + a map of
 *          field values and returns the right TradeType impl.
 * HOW:     Switch on the asset-class string, dispatch to the correct
 *          builder. Map values are cast/parsed per asset class.
 * WHY:     The Kafka consumer + REST POST endpoint both need to convert an
 *          untyped payload into a typed TradeType. Centralising the
 *          construction here means the parsing logic lives in one place.
 * OBSERVE: TradeFactoryTest.create_unknownAssetClass_throws fails when a
 *          new TradeType impl is added without updating the switch.
 * HINT:    Sealed hierarchy guarantees that every concrete TradeType MUST be
 *          listed in TradeType.permits — so this switch can be made
 *          exhaustive over assetClass enum.
 * ============================================================================
 */
public final class TradeFactory {

    private TradeFactory() { }

    public static TradeType create(String assetClass, Map<String, Object> p) {
        TradeType.AssetClass ac = TradeType.AssetClass.valueOf(assetClass.toUpperCase());
        return switch (ac) {
            case EQUITY     -> equity(p);
            case FX         -> fx(p);
            case BOND       -> bond(p);
            case DERIVATIVE -> derivative(p);
        };
    }

    private static EquityTrade equity(Map<String, Object> p) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of((String) p.get("tradeRef")))
                .instrumentSymbol((String) p.get("symbol"))
                .quantity(bigDecimal(p, "quantity"))
                .price(bigDecimal(p, "price"))
                .currency((String) p.get("currency"))
                .side(Side.valueOf((String) p.get("side")))
                .tradeDate(LocalDate.parse((String) p.get("tradeDate")))
                .counterpartyId(((Number) p.get("counterpartyId")).longValue())
                .build();
    }

    private static FXTrade fx(Map<String, Object> p) {
        return FXTrade.builder()
                .tradeRef(TradeRef.of((String) p.get("tradeRef")))
                .ccy1((String) p.get("ccy1"))
                .ccy2((String) p.get("ccy2"))
                .notionalCcy1(bigDecimal(p, "notionalCcy1"))
                .fxRate(bigDecimal(p, "fxRate"))
                .side(Side.valueOf((String) p.get("side")))
                .tradeDate(LocalDate.parse((String) p.get("tradeDate")))
                .counterpartyId(((Number) p.get("counterpartyId")).longValue())
                .build();
    }

    private static BondTrade bond(Map<String, Object> p) {
        return BondTrade.builder()
                .tradeRef(TradeRef.of((String) p.get("tradeRef")))
                .isin((String) p.get("isin"))
                .faceValue(bigDecimal(p, "faceValue"))
                .couponRate(bigDecimal(p, "couponRate"))
                .maturityDate(LocalDate.parse((String) p.get("maturityDate")))
                .currency((String) p.get("currency"))
                .side(Side.valueOf((String) p.get("side")))
                .tradeDate(LocalDate.parse((String) p.get("tradeDate")))
                .counterpartyId(((Number) p.get("counterpartyId")).longValue())
                .build();
    }

    private static DerivativeTrade derivative(Map<String, Object> p) {
        return DerivativeTrade.builder()
                .tradeRef(TradeRef.of((String) p.get("tradeRef")))
                .underlying((String) p.get("underlying"))
                .strike(bigDecimal(p, "strike"))
                .quantity(bigDecimal(p, "quantity"))
                .expiry(LocalDate.parse((String) p.get("expiry")))
                .optionType(DerivativeTrade.OptionType.valueOf((String) p.get("optionType")))
                .currency((String) p.get("currency"))
                .side(Side.valueOf((String) p.get("side")))
                .tradeDate(LocalDate.parse((String) p.get("tradeDate")))
                .counterpartyId(((Number) p.get("counterpartyId")).longValue())
                .build();
    }

    /**
     * Returns null (rather than throwing a field-less NPE from toString()) when
     * the key is absent, so a missing numeric field surfaces as the builder's
     * own named requireNonNull(field, "fieldName") instead of an anonymous NPE.
     */
    private static BigDecimal bigDecimal(Map<String, Object> p, String key) {
        Object v = p.get(key);
        return v == null ? null : new BigDecimal(v.toString());
    }
}
