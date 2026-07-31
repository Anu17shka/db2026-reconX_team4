package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeFactoryTest {

    @Test
    void create_equity_returnsEquityTrade() {

        TradeType trade = TradeFactory.create("EQUITY", equityMap());

        assertThat(trade)
                .isInstanceOf(EquityTrade.class);

        assertThat(trade.assetClass())
                .isEqualTo(TradeType.AssetClass.EQUITY);
    }


    @Test
    void create_fx_returnsFXTrade() {

        TradeType trade = TradeFactory.create("FX", fxMap());

        assertThat(trade)
                .isInstanceOf(FXTrade.class);
    }


    @Test
    void create_bond_returnsBondTrade() {

        TradeType trade = TradeFactory.create("BOND", bondMap());

        assertThat(trade)
                .isInstanceOf(BondTrade.class);
    }


    @Test
    void create_derivative_returnsDerivativeTrade() {

        TradeType trade = TradeFactory.create("DERIVATIVE", derivativeMap());

        assertThat(trade)
                .isInstanceOf(DerivativeTrade.class);
    }


    @Test
    void create_caseInsensitiveAssetClass() {

        TradeType trade = TradeFactory.create("equity", equityMap());

        assertThat(trade)
                .isInstanceOf(EquityTrade.class);
    }


    @Test
    void create_unknownAssetClass_throws() {

        Map<String, Object> map = equityMap();

        assertThatThrownBy(() -> TradeFactory.create("FOO", map))
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void create_missingRequiredKey_throwsNamingTheField() {

        Map<String, Object> map = equityMap();
        map.remove("price");

        assertThatThrownBy(() -> TradeFactory.create("EQUITY", map))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("price");
    }


    @Test
    void factory_hasPrivateNoArgConstructorAndNoInstanceState() throws Exception {

        Constructor<?>[] constructors = TradeFactory.class.getDeclaredConstructors();
        assertThat(constructors).hasSize(1);

        Constructor<?> ctor = constructors[0];
        assertThat(ctor.getParameterCount()).isZero();
        assertThat(Modifier.isPrivate(ctor.getModifiers())).isTrue();

        for (Field f : TradeFactory.class.getDeclaredFields()) {
            assertThat(Modifier.isStatic(f.getModifiers()))
                    .as("field %s should be static (no instance state)", f.getName())
                    .isTrue();
        }

        for (Method m : TradeFactory.class.getDeclaredMethods()) {
            assertThat(Modifier.isStatic(m.getModifiers()))
                    .as("method %s should be static", m.getName())
                    .isTrue();
        }
    }


    private Map<String, Object> equityMap() {
        Map<String, Object> p = new HashMap<>();
        p.put("tradeRef", "EQT-20260603-0001");
        p.put("symbol", "SAP.DE");
        p.put("quantity", "100");
        p.put("price", "100.50");
        p.put("currency", "EUR");
        p.put("side", "BUY");
        p.put("tradeDate", "2026-06-03");
        p.put("counterpartyId", 1L);
        return p;
    }

    private Map<String, Object> fxMap() {
        Map<String, Object> p = new HashMap<>();
        p.put("tradeRef", "FXX-20260603-0001");
        p.put("ccy1", "EUR");
        p.put("ccy2", "USD");
        p.put("notionalCcy1", "100000");
        p.put("fxRate", "1.1");
        p.put("side", "BUY");
        p.put("tradeDate", "2026-06-03");
        p.put("counterpartyId", 1L);
        return p;
    }

    private Map<String, Object> bondMap() {
        Map<String, Object> p = new HashMap<>();
        p.put("tradeRef", "BND-20260603-0001");
        p.put("isin", "US0378331005");
        p.put("faceValue", "1000000");
        p.put("couponRate", "0.05");
        p.put("maturityDate", "2030-06-03");
        p.put("currency", "USD");
        p.put("side", "BUY");
        p.put("tradeDate", "2026-06-03");
        p.put("counterpartyId", 1L);
        return p;
    }

    private Map<String, Object> derivativeMap() {
        Map<String, Object> p = new HashMap<>();
        p.put("tradeRef", "DRV-20260603-0001");
        p.put("underlying", "SAP.DE");
        p.put("strike", "150");
        p.put("quantity", "10");
        p.put("expiry", "2026-12-18");
        p.put("optionType", "CALL");
        p.put("currency", "EUR");
        p.put("side", "BUY");
        p.put("tradeDate", "2026-06-03");
        p.put("counterpartyId", 1L);
        return p;
    }
}
